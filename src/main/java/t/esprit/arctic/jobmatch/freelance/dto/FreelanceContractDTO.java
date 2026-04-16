package t.esprit.arctic.jobmatch.freelance.dto;

import lombok.Data;
import t.esprit.arctic.jobmatch.freelance.entity.ContractStatus;
import t.esprit.arctic.jobmatch.freelance.entity.FreelanceContract;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FreelanceContractDTO {
    private Long id;
    private Long missionId;
    private String missionTitre;
    private Long clientId;
    private String clientNom;
    private Long freelancerId;
    private String freelancerNom;
    private Double amount;
    private String terms;
    private ContractStatus status;
    private String createdAt;

    // Payment details (simulated)
    private Double totalPaid;
    private Double inEscrow;

    public static FreelanceContractDTO fromEntity(FreelanceContract contract) {
        FreelanceContractDTO dto = new FreelanceContractDTO();
        dto.setId(contract.getId());
        dto.setMissionId(contract.getMission().getId());
        dto.setMissionTitre(contract.getMission().getTitre());
        dto.setClientId(contract.getClient().getId());
        dto.setClientNom(contract.getClient().getNom());
        dto.setFreelancerId(contract.getFreelancer().getId());
        dto.setFreelancerNom(contract.getFreelancer().getNom());
        dto.setAmount(contract.getAmount());
        dto.setTerms(contract.getTerms());
        dto.setStatus(contract.getStatus());
        dto.setCreatedAt(contract.getCreatedAt().toString());
        return dto;
    }
}
