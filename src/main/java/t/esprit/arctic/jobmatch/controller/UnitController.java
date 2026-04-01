package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.service.UnitService;
import t.esprit.arctic.jobmatch.security.JwtService;

@RestController
@RequestMapping("/api/freelance/units")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UnitController {

    private final UnitService unitService;
    private final JwtService jwtService;

    // Helper method to extract userId from token
    private Long extractUserId(String authHeader) {
        String token = authHeader.substring(7);   // remove "Bearer "
        return jwtService.extractId(token);
    }

    @GetMapping("/balance")
    public int getBalance(@RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        return unitService.getBalance(userId);
    }

    @PostMapping("/spend")
    public boolean spend(@RequestParam int amount,
                         @RequestParam String reason,
                         @RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        return unitService.spendUnits(userId, amount, reason);
    }
}