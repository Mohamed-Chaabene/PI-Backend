package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.LoginRequest;
import t.esprit.arctic.jobmatch.dto.RegisterRequest;
import t.esprit.arctic.jobmatch.dto.LoginResponse;
import t.esprit.arctic.jobmatch.dto.RegisterResponse;
import t.esprit.arctic.jobmatch.entity.*;
import t.esprit.arctic.jobmatch.security.JwtService;
import t.esprit.arctic.jobmatch.service.UtilisateurService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AuthController {

    private final UtilisateurService service;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    //  REGISTER
    @PostMapping("/register")
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        Role resolvedRole = request.role;
        if (resolvedRole == null && request.roleString != null) {
            String normalized = request.roleString.trim().toUpperCase().replace("ROLE_", "");
            try {
                resolvedRole = Role.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Role invalide: " + request.roleString);
            }
        }

        if (resolvedRole == null) {
            throw new IllegalArgumentException("Role requis pour l'inscription");
        }

        Utilisateur user;
        switch (resolvedRole) {
            case CANDIDAT:
                Candidat candidat = new Candidat();
                candidat.setPrenom(request.prenom != null ? request.prenom : "");
                candidat.setTelephone(request.telephone != null ? request.telephone : "");
                candidat.setNiveauEtude(request.niveauEtude != null ? request.niveauEtude : "");
                candidat.setCv(request.cv);
                candidat.setLienPortfolio(request.lienPortfolio);
                candidat.setDescription(request.description);
                user = candidat;
                break;
            case RECRUTEUR:
                Recruteur recruteur = new Recruteur();
                recruteur.setEntreprise(request.entreprise);
                recruteur.setPoste(request.poste);
                recruteur.setSecteur(request.secteur);
                user = recruteur;
                break;
            case CLIENT_FREELANCE:
                ClientFreelance client = new ClientFreelance();
                client.setEntreprise(request.entreprise);
                client.setBudget(request.budget != null ? request.budget : 0.0);
                user = client;
                break;
            case ORGANISATEUR:
                OrganisateurEvenement organisateur = new OrganisateurEvenement();
                organisateur.setOrganisation(request.organisation);
                organisateur.setAdresse(request.adresse);
                organisateur.setDescriptionProjet(request.descriptionProjet);
                user = organisateur;
                break;
            default:
                user = new Utilisateur();
                break;
        }
        user.setNom(request.nom);
        user.setEmail(request.email);
        user.setMotDePasse(request.motDePasse);
        user.setRole(resolvedRole);
        user.setActif(true);
        user.setDateCreation(java.time.LocalDateTime.now());

        Utilisateur savedUser = service.register(user);
        
        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getNom(),
                savedUser.getEmail(),
                savedUser.getRole().toString(),
                "Inscription réussie"
        );
    }

    // LOGIN
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        try {
            if (request.email == null || request.email.trim().isEmpty()) {
                throw new BadCredentialsException("Email requis");
            }
            if (request.motDePasse == null || request.motDePasse.isEmpty()) {
                throw new BadCredentialsException("Mot de passe requis");
            }

            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email,
                            request.motDePasse
                    )
            );

            String token = jwtService.generateToken(request.email);
            if (token == null || token.isEmpty()) {
                throw new RuntimeException("Token JWT non généré");
            }
            
            // Extraire le rôle du token
            String role = jwtService.extractRole(token);
            System.out.println("[AUTH DEBUG] login email=" + request.email + " roleFromToken=" + role);
            
            return new LoginResponse(token, request.email, role, "Connexion réussie");
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        } catch (Exception ex) {
            System.err.println("Erreur login: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Erreur lors de la connexion: " + ex.getMessage());
        }
    }

    //  TEST ROLE (DEBUG)
    @GetMapping("/test-role")
    public String testRole(Authentication auth) {

        if (auth == null) {
            return "No authentication";
        }

        return auth.getAuthorities().toString();
    }

    //  ENDPOINT DE TEST JWT
    @PostMapping("/test-jwt")
    public java.util.Map<String, Object> testJwt(@RequestBody LoginRequest request) {
        try {
            String token = jwtService.generateToken(request.email);
            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);
            
            return java.util.Map.of(
                    "token", token,
                    "email", email,
                    "role", role,
                    "message", "JWT généré avec succès"
            );
        } catch (Exception ex) {
            return java.util.Map.of(
                    "error", ex.getMessage()
            );
        }
    }

    //  ENDPOINT DE TEST AUTHENTIFICATION
    @GetMapping("/test-auth")
    public java.util.Map<String, Object> testAuth(Authentication auth) {
        if (auth == null) {
            return java.util.Map.of(
                    "authenticated", false,
                    "message", "Aucune authentification"
            );
        }

        return java.util.Map.of(
                "authenticated", true,
                "principal", auth.getPrincipal().toString(),
                "authorities", auth.getAuthorities().toString(),
                "name", auth.getName(),
                "message", "Authentification valide"
        );
    }
}