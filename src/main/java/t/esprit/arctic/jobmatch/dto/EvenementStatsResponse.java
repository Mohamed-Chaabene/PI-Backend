package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
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
    private int maxParticipations;
}