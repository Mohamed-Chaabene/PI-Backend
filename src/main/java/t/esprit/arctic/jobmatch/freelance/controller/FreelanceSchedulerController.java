package t.esprit.arctic.jobmatch.freelance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.freelance.dto.CreateEventRequest;
import t.esprit.arctic.jobmatch.freelance.dto.FreelanceEventDTO;
import t.esprit.arctic.jobmatch.freelance.service.FreelanceSchedulerService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/freelance/scheduler")
@RequiredArgsConstructor
public class FreelanceSchedulerController {

    private final FreelanceSchedulerService schedulerService;

    /** Get all events for current user */
    @GetMapping("/events")
    public ResponseEntity<List<FreelanceEventDTO>> getMyEvents(Principal principal) {
        return ResponseEntity.ok(schedulerService.getMyEvents(principal.getName()));
    }

    /** Get events in a date range */
    @GetMapping("/events/range")
    public ResponseEntity<List<FreelanceEventDTO>> getEventsByRange(
            Principal principal,
            @RequestParam String start,
            @RequestParam String end) {
        return ResponseEntity.ok(schedulerService.getMyEventsByRange(
                principal.getName(),
                LocalDateTime.parse(start),
                LocalDateTime.parse(end)
        ));
    }

    /** Create a new event */
    @PostMapping("/events")
    public ResponseEntity<FreelanceEventDTO> createEvent(
            Principal principal,
            @RequestBody CreateEventRequest req) {
        return ResponseEntity.ok(schedulerService.createEvent(principal.getName(), req));
    }

    /** Update an event */
    @PutMapping("/events/{id}")
    public ResponseEntity<FreelanceEventDTO> updateEvent(
            @PathVariable Long id,
            Principal principal,
            @RequestBody CreateEventRequest req) {
        return ResponseEntity.ok(schedulerService.updateEvent(id, principal.getName(), req));
    }

    /** Change event status (CONFIRMED, CANCELLED, COMPLETED) */
    @PatchMapping("/events/{id}/status")
    public ResponseEntity<FreelanceEventDTO> updateStatus(
            @PathVariable Long id,
            Principal principal,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(schedulerService.updateStatus(id, principal.getName(), body.get("status")));
    }

    /** Delete event */
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, Principal principal) {
        schedulerService.deleteEvent(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
