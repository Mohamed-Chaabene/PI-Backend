package t.esprit.arctic.jobmatch.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class JobResponseDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal budget;
    private String clientName;
    private LocalDateTime createdAt;
}