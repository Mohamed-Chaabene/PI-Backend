package t.esprit.arctic.jobmatch.freelance.service;

import com.pusher.rest.Pusher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.esprit.arctic.jobmatch.freelance.entity.Mission;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.freelance.dto.*;
import t.esprit.arctic.jobmatch.freelance.entity.*;
import t.esprit.arctic.jobmatch.freelance.repository.*;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FreelanceWorkspaceService {

    private final FreelanceContractRepository contractRepository;
    private final FreelancePaymentRepository paymentRepository;
    private final FreelanceChatRoomRepository roomRepository;
    private final FreelanceChatMessageRepository messageRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MissionRepository missionRepository;
    private final Pusher pusher;

    private Utilisateur findUser(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    // ==========================================
    // CHAT SYSTEM
    // ==========================================

    @Transactional
    public FreelanceChatRoomDTO getOrCreateRoom(String email, Long missionId, Long freelancerId) {
        Utilisateur client = findUser(email);
        
        // Ensure the room exists
        FreelanceChatRoom room = roomRepository.findByParticipantsAndMission(missionId, client.getId(), freelancerId)
                .orElseGet(() -> {
                    Mission mission = missionRepository.findById(missionId).orElse(null);
                    Utilisateur fl = utilisateurRepository.findById(freelancerId)
                            .orElseThrow(() -> new RuntimeException("Freelancer not found"));
                    FreelanceChatRoom newRoom = new FreelanceChatRoom();
                    newRoom.setClient(client);
                    newRoom.setFreelancer(fl);
                    newRoom.setMission(mission);
                    return roomRepository.save(newRoom);
                });
                
        return FreelanceChatRoomDTO.fromEntity(room);
    }

    @Transactional(readOnly = true)
    public List<FreelanceChatRoomDTO> getMyRooms(String email) {
        Utilisateur user = findUser(email);
        return roomRepository.findByUserId(user.getId()).stream()
                .map(FreelanceChatRoomDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FreelanceChatMessageDTO> getRoomMessages(Long roomId, String email) {
        // verify access (skipped for brevity)
        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId).stream()
                .map(FreelanceChatMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public FreelanceChatMessageDTO sendMessage(String email, Long roomId, String content) {
        Utilisateur sender = findUser(email);
        FreelanceChatRoom room = roomRepository.findById(roomId).orElseThrow();
        
        FreelanceChatMessage msg = new FreelanceChatMessage();
        msg.setRoom(room);
        msg.setSender(sender);
        msg.setContent(content);
        msg = messageRepository.save(msg);
        
        FreelanceChatMessageDTO dto = FreelanceChatMessageDTO.fromEntity(msg);
        
        // Push notification via Websockets (Pusher)
        try {
            pusher.trigger("chat-room-" + roomId, "new-message", dto);
        } catch (Exception e) {
            System.err.println("Pusher failed: " + e.getMessage());
        }

        return dto;
    }

    // ==========================================
    // CONTRACT & PAYMENT SYSTEM
    // ==========================================

    @Transactional(readOnly = true)
    public List<FreelanceContractDTO> getMyContracts(String email) {
        Utilisateur user = findUser(email);
        List<FreelanceContract> contracts;
        
        // If they have client role, check client contracts, else freelancer
        boolean isClient = user.getRole() != null && user.getRole().name().equals("CLIENT_FREELANCE");
        
        if (isClient) {
            contracts = contractRepository.findByClientId(user.getId());
        } else {
            contracts = contractRepository.findByFreelancerId(user.getId());
        }
        
        return contracts.stream().map(c -> {
            FreelanceContractDTO dto = FreelanceContractDTO.fromEntity(c);
            
            // Calculate escrow and released totals
            List<FreelancePayment> payments = paymentRepository.findByContractId(c.getId());
            double escrow = payments.stream().filter(p -> p.getStatus() == PaymentStatus.ESCROW).mapToDouble(FreelancePayment::getAmount).sum();
            double released = payments.stream().filter(p -> p.getStatus() == PaymentStatus.RELEASED).mapToDouble(FreelancePayment::getAmount).sum();
            
            dto.setInEscrow(escrow);
            dto.setTotalPaid(released);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public FreelanceContractDTO proposeContract(String email, Long missionId, Long freelancerId, Double amount, String terms) {
        Utilisateur client = findUser(email);
        Mission mission = missionRepository.findById(missionId).orElseThrow();
        Utilisateur fl = utilisateurRepository.findById(freelancerId).orElseThrow();

        FreelanceContract contract = new FreelanceContract();
        contract.setMission(mission);
        contract.setClient(client);
        contract.setFreelancer(fl);
        contract.setAmount(amount);
        contract.setTerms(terms);
        contract.setStatus(ContractStatus.PROPOSED);

        return FreelanceContractDTO.fromEntity(contractRepository.save(contract));
    }

    @Transactional
    public FreelanceContractDTO acceptContract(String email, Long contractId) {
        // Assume freelancer is accepting
        FreelanceContract contract = contractRepository.findById(contractId).orElseThrow();
        contract.setStatus(ContractStatus.ACTIVE);
        return FreelanceContractDTO.fromEntity(contractRepository.save(contract));
    }

    @Transactional
    public FreelanceContractDTO fundEscrow(String email, Long contractId, Double amount) {
        Utilisateur client = findUser(email);
        FreelanceContract contract = contractRepository.findById(contractId).orElseThrow();
        
        // Create payment in Escrow
        FreelancePayment payment = new FreelancePayment();
        payment.setContract(contract);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.ESCROW);
        paymentRepository.save(payment);
        
        return getContractDetails(contractId);
    }

    @Transactional
    public FreelanceContractDTO releasePayment(String email, Long contractId) {
        Utilisateur client = findUser(email);
        FreelanceContract contract = contractRepository.findById(contractId).orElseThrow();
        
        // Release all Escrow funds
        List<FreelancePayment> payments = paymentRepository.findByContractId(contractId);
        for (FreelancePayment p : payments) {
            if (p.getStatus() == PaymentStatus.ESCROW) {
                p.setStatus(PaymentStatus.RELEASED);
                // In a real app, send money to Freelancer here
                paymentRepository.save(p);
            }
        }
        
        // Mark contract as completed
        contract.setStatus(ContractStatus.COMPLETED);
        contractRepository.save(contract);
        
        return getContractDetails(contractId);
    }

    private FreelanceContractDTO getContractDetails(Long contractId) {
        FreelanceContract c = contractRepository.findById(contractId).orElseThrow();
        FreelanceContractDTO dto = FreelanceContractDTO.fromEntity(c);
        
        List<FreelancePayment> payments = paymentRepository.findByContractId(c.getId());
        double escrow = payments.stream().filter(p -> p.getStatus() == PaymentStatus.ESCROW).mapToDouble(FreelancePayment::getAmount).sum();
        double released = payments.stream().filter(p -> p.getStatus() == PaymentStatus.RELEASED).mapToDouble(FreelancePayment::getAmount).sum();
        
        dto.setInEscrow(escrow);
        dto.setTotalPaid(released);
        return dto;
    }
}
