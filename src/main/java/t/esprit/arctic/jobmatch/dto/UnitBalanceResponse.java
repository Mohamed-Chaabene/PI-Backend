package t.esprit.arctic.jobmatch.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UnitBalanceResponse {
    private int balance;
    private int totalEarned;
    private int totalSpent;
}