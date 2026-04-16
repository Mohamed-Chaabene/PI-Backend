package t.esprit.arctic.jobmatch.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class JobCreateDTO {
    private String title;
    private String description;
    private String type; // "FIXED" or "HOURLY"
    private BigDecimal budget;
    private Integer estimatedDays;
}