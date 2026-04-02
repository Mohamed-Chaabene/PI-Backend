package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import t.esprit.arctic.jobmatch.entity.OffreEmploi;
import t.esprit.arctic.jobmatch.repository.OffreEmploiRepository;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/offres-emploi")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class OffreEmploiController {

    private final OffreEmploiRepository offreEmploiRepository;

    @GetMapping
    public ResponseEntity<List<OffreEmploi>> getAllOffres() {
        List<OffreEmploi> offres = offreEmploiRepository.findAll();
        offres.sort(Comparator.comparing(OffreEmploi::getDatePublication,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return ResponseEntity.ok(offres);
    }
}