package t.esprit.arctic.jobmatch.controller;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;

import t.esprit.arctic.jobmatch.entity.Partenaire;

import t.esprit.arctic.jobmatch.entity.TypePartenaire;
import t.esprit.arctic.jobmatch.service.PartenaireService;
import t.esprit.arctic.jobmatch.entity.Partenaire;
import t.esprit.arctic.jobmatch.entity.OffrePartenaire;




@RestController
@RequestMapping("/api/partenaires")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PartenaireController {

    private final PartenaireService service;

    @GetMapping
    public List<Partenaire> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Partenaire getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Partenaire create(@RequestBody Partenaire p) {
        return service.create(p);
    }

    @PutMapping("/{id}")
    public Partenaire update(@PathVariable Long id,
                             @RequestBody Partenaire p) {
        return service.update(id, p);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }



    @GetMapping("/type/{type}")
    public List<Partenaire> getByType(@PathVariable TypePartenaire type) {
        return service.getByType(type);
    }

    @GetMapping("/{id}/offres")
    public List<OffrePartenaire> getOffresByPartenaire(@PathVariable Long id) {
        Partenaire p = service.getById(id);
        return p.getOffres();
    }
}