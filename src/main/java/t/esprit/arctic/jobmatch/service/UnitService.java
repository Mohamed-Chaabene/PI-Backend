package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.entity.freelance.UnitTransaction;
import t.esprit.arctic.jobmatch.entity.UnitTransactionType;
import t.esprit.arctic.jobmatch.repository.UnitTransactionRepository;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitTransactionRepository unitRepo;
    private final UtilisateurRepository userRepo;

    public int getBalance(Long userId) {
        Utilisateur user = userRepo.findById(userId).orElseThrow();
        return unitRepo.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .mapToInt(UnitTransaction::getAmount)
                .sum();
    }

    public void addUnits(Long userId, int amount, UnitTransactionType type, String description) {
        Utilisateur user = userRepo.findById(userId).orElseThrow();
        UnitTransaction tx = new UnitTransaction();
        tx.setUser(user);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setDescription(description);
        unitRepo.save(tx);
    }

    public boolean spendUnits(Long userId, int amount, String reason) {
        int balance = getBalance(userId);
        if (balance < amount) return false;

        addUnits(userId, -amount, UnitTransactionType.SPENT_APPLY, reason);
        return true;
    }
}