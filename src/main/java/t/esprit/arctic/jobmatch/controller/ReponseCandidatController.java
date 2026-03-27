package t.esprit.arctic.jobmatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.ReponseDTO;
import t.esprit.arctic.jobmatch.service.ReponseCandidatService;

import java.util.List;

@RestController
@RequestMapping("/api/reponses")
@CrossOrigin(origins = "*")
public class ReponseCandidatController {

    @Autowired
    private ReponseCandidatService reponseCandidatService;

    @PostMapping
    public ResponseEntity<ReponseDTO> submitReponse(@RequestBody ReponseDTO reponseDTO) {
        return ResponseEntity.ok(reponseCandidatService.submitReponse(reponseDTO));
    }

    @GetMapping("/entretien/{candidatId}/{entretienId}")
    public ResponseEntity<List<ReponseDTO>> getReponsesByEntretien(
            @PathVariable Long candidatId,
            @PathVariable Long entretienId) {
        return ResponseEntity.ok(reponseCandidatService.getReponsesByEntretien(candidatId, entretienId));
    }
}

