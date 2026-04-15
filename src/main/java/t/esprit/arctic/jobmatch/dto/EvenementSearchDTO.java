package t.esprit.arctic.jobmatch.dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EvenementSearchDTO {

    // Les événements qui correspondent à la requête de recherche
    private List<EvenementResponse> resultats;

    // Événements suggérés basés sur les préférences du candidat
    // (types favoris issus de ses participations et feedbacks)
    private List<EvenementResponse> suggestions;

    // Les 5 derniers termes recherchés par ce candidat
    private List<String> historiqueRecherches;

    // Nombre total de résultats trouvés (utile pour la pagination future)
    private int totalResultats;
}