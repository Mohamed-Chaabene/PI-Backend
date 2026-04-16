package t.esprit.arctic.jobmatch.freelance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.freelance.dto.FreelanceChatMessageDTO;
import t.esprit.arctic.jobmatch.freelance.dto.FreelanceChatRoomDTO;
import t.esprit.arctic.jobmatch.freelance.dto.FreelanceContractDTO;
import t.esprit.arctic.jobmatch.freelance.service.FreelanceWorkspaceService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/freelance/workspace")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FreelanceWorkspaceController {

    private final FreelanceWorkspaceService workspaceService;

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    // ==========================================
    // CHAT
    // ==========================================

    @GetMapping("/rooms")
    public ResponseEntity<List<FreelanceChatRoomDTO>> getMyRooms() {
        return ResponseEntity.ok(workspaceService.getMyRooms(getCurrentUserEmail()));
    }

    @PostMapping("/rooms/get-or-create")
    public ResponseEntity<FreelanceChatRoomDTO> getOrCreateRoom(@RequestBody Map<String, Long> payload) {
        Long missionId = payload.get("missionId");
        Long freelancerId = payload.get("freelancerId");
        return ResponseEntity.ok(workspaceService.getOrCreateRoom(getCurrentUserEmail(), missionId, freelancerId));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<FreelanceChatMessageDTO>> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(workspaceService.getRoomMessages(roomId, getCurrentUserEmail()));
    }

    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<FreelanceChatMessageDTO> sendMessage(@PathVariable Long roomId, @RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        return ResponseEntity.ok(workspaceService.sendMessage(getCurrentUserEmail(), roomId, content));
    }

    // ==========================================
    // CONTRACTS & PAYMENTS
    // ==========================================

    @GetMapping("/contracts")
    public ResponseEntity<List<FreelanceContractDTO>> getMyContracts() {
        return ResponseEntity.ok(workspaceService.getMyContracts(getCurrentUserEmail()));
    }

    @PostMapping("/contracts/propose")
    public ResponseEntity<FreelanceContractDTO> proposeContract(@RequestBody Map<String, Object> payload) {
        Long missionId = Long.valueOf(payload.get("missionId").toString());
        Long freelancerId = Long.valueOf(payload.get("freelancerId").toString());
        Double amount = Double.valueOf(payload.get("amount").toString());
        String terms = payload.get("terms").toString();
        
        return ResponseEntity.ok(workspaceService.proposeContract(getCurrentUserEmail(), missionId, freelancerId, amount, terms));
    }

    @PostMapping("/contracts/{id}/accept")
    public ResponseEntity<FreelanceContractDTO> acceptContract(@PathVariable Long id) {
        return ResponseEntity.ok(workspaceService.acceptContract(getCurrentUserEmail(), id));
    }

    @PostMapping("/contracts/{id}/fund")
    public ResponseEntity<FreelanceContractDTO> fundEscrow(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Double amount = Double.valueOf(payload.get("amount").toString());
        return ResponseEntity.ok(workspaceService.fundEscrow(getCurrentUserEmail(), id, amount));
    }

    @PostMapping("/contracts/{id}/release")
    public ResponseEntity<FreelanceContractDTO> releasePayment(@PathVariable Long id) {
        return ResponseEntity.ok(workspaceService.releasePayment(getCurrentUserEmail(), id));
    }
}
