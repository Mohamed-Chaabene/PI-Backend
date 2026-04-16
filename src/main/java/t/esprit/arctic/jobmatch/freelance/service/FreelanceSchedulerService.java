package t.esprit.arctic.jobmatch.freelance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.freelance.dto.CreateEventRequest;
import t.esprit.arctic.jobmatch.freelance.dto.FreelanceEventDTO;
import t.esprit.arctic.jobmatch.freelance.entity.*;
import t.esprit.arctic.jobmatch.freelance.repository.FreelanceEventRepository;
import t.esprit.arctic.jobmatch.freelance.repository.MissionRepository;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FreelanceSchedulerService {

    private final FreelanceEventRepository eventRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MissionRepository missionRepository;

    // ── Fetch events ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FreelanceEventDTO> getMyEvents(String email) {
        Utilisateur user = findUser(email);
        return eventRepository.findAllByUserId(user.getId()).stream()
                .map(FreelanceEventDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FreelanceEventDTO> getMyEventsByRange(String email, LocalDateTime start, LocalDateTime end) {
        Utilisateur user = findUser(email);
        return eventRepository.findByUserIdAndDateRange(user.getId(), start, end).stream()
                .map(FreelanceEventDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Create event ─────────────────────────────────────────────────

    @Transactional
    public FreelanceEventDTO createEvent(String email, CreateEventRequest req) {
        Utilisateur organizer = findUser(email);

        FreelanceEvent event = new FreelanceEvent();
        event.setTitle(req.getTitle());
        event.setDescription(req.getDescription());
        event.setType(EventType.valueOf(req.getType()));
        event.setStartDate(LocalDateTime.parse(req.getStartDate()));
        event.setEndDate(LocalDateTime.parse(req.getEndDate()));
        event.setStatus(EventStatus.SCHEDULED);
        event.setOrganizer(organizer);

        if (req.getParticipantId() != null) {
            Utilisateur participant = utilisateurRepository.findById(req.getParticipantId())
                    .orElseThrow(() -> new RuntimeException("Participant introuvable"));
            event.setParticipant(participant);
        }

        if (req.getMissionId() != null) {
            Mission mission = missionRepository.findById(req.getMissionId())
                    .orElseThrow(() -> new RuntimeException("Mission introuvable"));
            event.setMission(mission);
        }

        return FreelanceEventDTO.fromEntity(eventRepository.save(event));
    }

    // ── Update event ─────────────────────────────────────────────────

    @Transactional
    public FreelanceEventDTO updateEvent(Long eventId, String email, CreateEventRequest req) {
        FreelanceEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));

        Utilisateur user = findUser(email);
        if (!event.getOrganizer().getId().equals(user.getId())) {
            throw new RuntimeException("Seul l'organisateur peut modifier cet événement");
        }

        event.setTitle(req.getTitle());
        event.setDescription(req.getDescription());
        event.setType(EventType.valueOf(req.getType()));
        event.setStartDate(LocalDateTime.parse(req.getStartDate()));
        event.setEndDate(LocalDateTime.parse(req.getEndDate()));

        if (req.getParticipantId() != null) {
            event.setParticipant(utilisateurRepository.findById(req.getParticipantId()).orElse(null));
        }
        if (req.getMissionId() != null) {
            event.setMission(missionRepository.findById(req.getMissionId()).orElse(null));
        }

        return FreelanceEventDTO.fromEntity(eventRepository.save(event));
    }

    // ── Change status ────────────────────────────────────────────────

    @Transactional
    public FreelanceEventDTO updateStatus(Long eventId, String email, String newStatus) {
        FreelanceEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));

        Utilisateur user = findUser(email);
        boolean isOrganizer = event.getOrganizer().getId().equals(user.getId());
        boolean isParticipant = event.getParticipant() != null && event.getParticipant().getId().equals(user.getId());

        if (!isOrganizer && !isParticipant) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cet événement");
        }

        event.setStatus(EventStatus.valueOf(newStatus));
        return FreelanceEventDTO.fromEntity(eventRepository.save(event));
    }

    // ── Delete event ─────────────────────────────────────────────────

    @Transactional
    public void deleteEvent(Long eventId, String email) {
        FreelanceEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));

        Utilisateur user = findUser(email);
        if (!event.getOrganizer().getId().equals(user.getId())) {
            throw new RuntimeException("Seul l'organisateur peut supprimer cet événement");
        }

        eventRepository.delete(event);
    }

    // ── Helper ───────────────────────────────────────────────────────

    private Utilisateur findUser(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));
    }
}
