package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
<<<<<<< HEAD
//  Objet retourné à Angular avec toutes les statistiques
public class EvenementStatsResponse {

    // Nombre total d'événements ce mois
    private int totalEvenements;

    // Nombre total de participations ce mois
    private int totalParticipations;

    // Nombre de participations confirmées
    private int totalConfirmees;

    // Nombre de participations en attente
    private int totalEnAttente;

    // Taux de remplissage moyen (participations / capacité)
    private double tauxRemplissage;

    // Événement le plus populaire (plus de participations)
    private String evenementLePlusPopulaire;

    // Nombre de participations de l'événement populaire
=======

public class EvenementStatsResponse {

    private int totalEvenements;
    private int totalParticipations;
    private int totalConfirmees;
    private int totalEnAttente;
    private double tauxRemplissage;
    private String evenementLePlusPopulaire;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    private int maxParticipations;
}