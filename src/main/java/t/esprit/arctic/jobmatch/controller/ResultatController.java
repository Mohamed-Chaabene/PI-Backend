package t.esprit.arctic.jobmatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.ResultatDTO;
import t.esprit.arctic.jobmatch.service.ResultatService;

@RestController
@RequestMapping("/api/resultats")
@CrossOrigin(origins = "*")
public class ResultatController {

    @Autowired
    private ResultatService resultatService;

    @PostMapping("/entretien/{entretienId}")
    public ResponseEntity<ResultatDTO> calculerResultat(@PathVariable Long entretienId) {
        return ResponseEntity.ok(resultatService.calculerResultat(entretienId));
    }

    @GetMapping("/entretien/{entretienId}")
    public ResponseEntity<ResultatDTO> getResultat(@PathVariable Long entretienId) {
        ResultatDTO resultat = resultatService.getResultat(entretienId);
        return resultat != null ? ResponseEntity.ok(resultat) : ResponseEntity.notFound().build();
    }
}

