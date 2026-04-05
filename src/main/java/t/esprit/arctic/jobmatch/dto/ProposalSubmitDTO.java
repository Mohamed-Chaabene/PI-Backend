package t.esprit.arctic.jobmatch.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProposalSubmitDTO {
    private String coverLetter;
    private BigDecimal proposedPrice;
    private Integer estimatedDays;
}