from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import os

app = FastAPI(title="Formation ML Recommendation Service")

# Modèles Pydantic pour l'API
class FormationData(BaseModel):
    id: int
    titre: str
    description: Optional[str] = ""
    competences: List[str] = []
    categorie: Optional[str] = ""
    niveau: Optional[str] = ""

class RecommendationRequest(BaseModel):
    candidat_competences: List[str]
    candidat_niveau: Optional[str] = ""
    formations_terminees_ids: List[int] = []
    formations_disponibles: List[FormationData]

class RecommendationResult(BaseModel):
    formation_id: int
    score_match: float
    raisons: List[str]

# --- NOUVEAUX MODÈLES POUR SKILL GAP ---
class SkillGapRequest(BaseModel):
    candidat_competences: List[str]
    target_job: str 
    formations_terminees_ids: List[int] = []
    formations_disponibles: List[FormationData]

class SkillGapResponse(BaseModel):
    job_title: str
    matching_skills: List[str]
    missing_skills: List[str]
    match_percentage: float
    recommended_formations: List[RecommendationResult]

# Dictionnaire des métiers cibles (La connaissance métier)
CAREER_PATHS = {
    "Full Stack Developer": ["HTML", "CSS", "JavaScript", "React", "Node.js", "SQL", "Angular", "Spring Boot", "Git"],
    "Backend Developer": ["Java", "Spring Boot", "Node.js", "Python", "SQL", "NoSQL", "REST API", "Microservices", "Docker"],
    "Frontend Developer": ["HTML", "CSS", "JavaScript", "TypeScript", "Angular", "React", "Vue.js", "SASS", "Tailwind"],
    "Data Scientist": ["Python", "SQL", "Machine Learning", "Statistics", "Pandas", "Scikit-Learn", "Deep Learning", "Data Visualization"],
    "Data Engineer": ["Python", "SQL", "Big Data", "Spark", "Hadoop", "ETL", "Kafka", "NoSQL", "Cloud Storage"],
    "DevOps Engineer": ["Docker", "Kubernetes", "Ansible", "Terraform", "AWS", "Linux", "CI/CD", "Jenkins", "Prometheus"],
    "Cloud Architect": ["AWS", "Azure", "GCP", "Cloud Security", "Infrastructure as Code", "Networking", "Serverless", "Kubernetes"],
    "Mobile Developer": ["Flutter", "Dart", "React Native", "Android", "iOS", "Firebase", "Swift", "Kotlin"],
    "Cybersecurity Analyst": ["Network Security", "Cryptography", "Ethical Hacking", "Linux", "OWASP", "SIEM", "Incident Response"],
    "AI Engineer": ["Python", "Deep Learning", "NLP", "Computer Vision", "TensorFlow", "PyTorch", "OpenAI", "Neural Networks"],
    "UI/UX Designer": ["Figma", "Adobe XD", "User Research", "Wireframing", "Prototyping", "Design Systems", "Typography", "Color Theory"],
    "Product Manager": ["Agile", "Scrum", "Product Roadmap", "User Stories", "Market Research", "Analytics", "Communication", "Leadership"],
    "QA Automation Engineer": ["Selenium", "JUnit", "Cypress", "Python", "Testing Strategies", "Automation", "Git", "Jenkins"]
}

# Initialisation du modèle NLP
vectorizer = TfidfVectorizer(stop_words='english', max_features=5000)
dataset_path = "coursea_data.csv"
model_is_trained = False

def train_model_if_dataset_exists():
    global model_is_trained, vectorizer
    if os.path.exists(dataset_path):
        try:
            print(f"Chargement du dataset {dataset_path}...")
            df = pd.read_csv(dataset_path)
            
            # Colonnes observées dans coursea_data.csv
            text_corpus = []
            possible_cols = ['course_title', 'course_difficulty', 'course_organization', 'course_Certificate_type']
            for col in possible_cols:
                if col in df.columns:
                    text_corpus.extend(df[col].dropna().astype(str).tolist())
            
            if text_corpus:
                print(f"Entraînement du modèle sur {len(text_corpus)} textes du dataset...")
                vectorizer.fit(text_corpus)
                model_is_trained = True
                print("Modèle entraîné avec succès !")
        except Exception as e:
            print(f"Erreur lors du chargement du dataset: {e}")

# Charger le dataset au démarrage
train_model_if_dataset_exists()

@app.post("/recommend", response_model=List[RecommendationResult])
async def recommend_formations(req: RecommendationRequest):
    if not req.formations_disponibles:
        return []

    # 1. Construire le profil du candidat
    #    On combine : compétences déclarées + catégories/titres des formations TERMINÉES
    profil_parts = list(req.candidat_competences)
    if req.candidat_niveau:
        profil_parts.append(req.candidat_niveau)

    # Enrichir avec les infos des formations déjà terminées (cold-start fix)
    terminees_set = set(req.formations_terminees_ids)
    for f in req.formations_disponibles:
        if f.id in terminees_set:
            # Ajouter le contexte de la formation terminée au profil
            profil_parts.append(f.titre)
            profil_parts.append(f.categorie or "")
            profil_parts.extend(f.competences)

    profil_text = " ".join(p for p in profil_parts if p).strip()

    # 2. Préparer le corpus des formations disponibles (non terminées)
    formations_text = []
    formation_ids = []

    for f in req.formations_disponibles:
        if f.id in terminees_set:
            continue
        f_text = f"{f.titre} {f.titre} {f.description} {' '.join(f.competences)} {f.categorie} {f.niveau}"
        formations_text.append(f_text)
        formation_ids.append(f.id)

    if not formations_text:
        return []

    # 3. Vectorisation TF-IDF
    global vectorizer, model_is_trained
    all_text = [profil_text] + formations_text
    if not model_is_trained:
        vectorizer.fit(all_text)
    else:
        # Même avec dataset, on re-fit sur les données locales si profil vide
        try:
            profil_vec_test = vectorizer.transform([profil_text])
        except Exception:
            vectorizer.fit(all_text)

    try:
        profil_vec = vectorizer.transform([profil_text])
        formations_vec = vectorizer.transform(formations_text)
    except Exception:
        vectorizer.fit(all_text)
        profil_vec = vectorizer.transform([profil_text])
        formations_vec = vectorizer.transform(formations_text)

    # 4. Similarité Cosinus
    print(f"[ML] Profil calculé : '{profil_text}'")
    similarities = cosine_similarity(profil_vec, formations_vec)[0]
    print(f"[ML] Top score trouvé : {max(similarities) if len(similarities)>0 else 0}")

    # 5. Score final avec boost par catégorie
    results = []
    for i, raw_score in enumerate(similarities):
        f = next((x for x in req.formations_disponibles if x.id == formation_ids[i]), None)
        if f is None:
            continue

        score_pct = float(raw_score) * 100

        # Boost si la catégorie de la formation correspond à une formation terminée
        terminees_categories = {
            x.categorie for x in req.formations_disponibles if x.id in terminees_set
        }
        if f.categorie and f.categorie in terminees_categories:
            score_pct = max(score_pct, 35.0)  # boost "même catégorie"

        # Boost si la formation correspond aux compétences textuellement
        for comp in req.candidat_competences:
            if comp.lower() in formations_text[i].lower():
                score_pct = max(score_pct, 25.0)

        # Score plancher : toujours au moins 5% pour montrer que l'IA a analysé
        score_pct = max(score_pct, 5.0)
        score_pct = round(min(score_pct, 100.0), 1)

        raisons = []
        if score_pct >= 35:
            raisons.append("Correspond à votre parcours de formation.")
        elif score_pct >= 25:
            raisons.append("Correspond à vos compétences déclarées.")
        else:
            raisons.append("Recommandé selon votre profil.")

        results.append(RecommendationResult(
            formation_id=formation_ids[i],
            score_match=score_pct,
            raisons=raisons
        ))

    # Trier par score décroissant et retourner le Top 10
    results.sort(key=lambda x: x.score_match, reverse=True)
    return results[:10]

@app.post("/analyze-gap", response_model=SkillGapResponse)
async def analyze_skill_gap(req: SkillGapRequest):
    if req.target_job not in CAREER_PATHS:
        raise HTTPException(status_code=404, detail="Career path not found")
    
    required_skills = CAREER_PATHS[req.target_job]
    candidat_skills = [s.lower() for s in req.candidat_competences]
    
    matching_skills = []
    missing_skills = []
    
    for skill in required_skills:
        # Recherche simple + sémantique légère
        found = False
        for c_skill in candidat_skills:
            if skill.lower() in c_skill or c_skill in skill.lower():
                found = True
                break
        
        if found:
            matching_skills.append(skill)
        else:
            missing_skills.append(skill)
            
    match_percentage = (len(matching_skills) / len(required_skills)) * 100 if required_skills else 0
    
    # Recommander des formations qui couvrent les compétences manquantes
    recommended = []
    if missing_skills:
        missing_text = " ".join(missing_skills)
        f_texts = [f"{f.titre} {f.description} {' '.join(f.competences)}" for f in req.formations_disponibles]
        
        if f_texts:
            try:
                m_vec = vectorizer.transform([missing_text])
                f_vecs = vectorizer.transform(f_texts)
                sims = cosine_similarity(m_vec, f_vecs)[0]
                
                for i, score in enumerate(sims):
                    if score > 0.02: # Seuil bas pour s'assurer d'avoir des résultats
                        # Trouver quelle compétence manquante est couverte
                        f_comp = [c.lower() for c in req.formations_disponibles[i].competences]
                        covered = [m for m in missing_skills if m.lower() in f_comp or any(m.lower() in t.lower() for t in f_comp)]
                        
                        raisons = []
                        if covered:
                            raisons.append(f"Comble votre lacune en : {covered[0]}")
                        else:
                            raisons.append("Correspond à votre objectif de carrière")
                            
                        recommended.append(RecommendationResult(
                            formation_id=req.formations_disponibles[i].id,
                            score_match=min(98.0, float(score) * 150), # Boost pour l'affichage
                            raisons=raisons
                        ))
                
                print(f"[GAP] {len(recommended)} formations recommandées trouvées")
            except Exception as e:
                print(f"[GAP] Erreur matching: {str(e)}")
    
    # Filtrer les formations déjà terminées
    terminees_ids = set(req.formations_terminees_ids)
    final_recommended = [r for r in recommended if r.formation_id not in terminees_ids]
    
    final_recommended.sort(key=lambda x: x.score_match, reverse=True)
    
    return SkillGapResponse(
        job_title=req.target_job,
        matching_skills=matching_skills,
        missing_skills=missing_skills,
        match_percentage=round(match_percentage, 1),
        recommended_formations=final_recommended[:3]
    )


@app.get("/health")
def health_check():
    return {"status": "ok", "model_trained": model_is_trained}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
