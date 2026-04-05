//package t.esprit.arctic.jobmatch.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import t.esprit.arctic.jobmatch.dto.JobCreateDTO;
//import t.esprit.arctic.jobmatch.entity.Job;
//import t.esprit.arctic.jobmatch.service.FreelanceService;   // ← Keep FreelanceService
//import t.esprit.arctic.jobmatch.security.JwtService;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/freelance")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
//public class JobController {
//
//    private final FreelanceService freelanceService;     // ← Keep as FreelanceService
//    private final JwtService jwtService;
//
//    private Long getUserId(String authHeader) {
//        String token = authHeader.substring(7);
//        return jwtService.extractId(token);
//    }
//
//    @PostMapping("/jobs")
//    public ResponseEntity<Job> postJob(
//            @RequestBody JobCreateDTO dto,
//            @RequestHeader("Authorization") String token) {
//
//        Long clientId = getUserId(token);
//        Job job = freelanceService.createJob(dto, clientId);
//        return ResponseEntity.ok(job);
//    }
//
//    @GetMapping("/jobs")
//    public ResponseEntity<List<Job>> getOpenJobs() {
//        return ResponseEntity.ok(freelanceService.getAllOpenJobs());
//    }
//}