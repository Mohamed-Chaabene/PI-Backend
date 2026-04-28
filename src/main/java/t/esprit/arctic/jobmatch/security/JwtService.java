package t.esprit.arctic.jobmatch.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.entity.Candidat;

import t.esprit.arctic.jobmatch.entity.OrganisateurEvenement;
import t.esprit.arctic.jobmatch.entity.Recruteur;
import t.esprit.arctic.jobmatch.entity.Role;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.security.Key;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final String SECRET = "mysecretkeymysecretkeymysecretkey123456"; // >= 32 chars
    private final UtilisateurRepository utilisateurRepository;

    private Key getKey() {
        byte[] keyBytes = SECRET.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email) {
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));

        Role effectiveRole = resolveEffectiveRole(user);
        
        return Jwts.builder()
                .setSubject(email)
                .claim("id", user.getId())  // ✅ Ajouter l'ID de l'utilisateur
            .claim("role", effectiveRole.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getKey())
                .compact();
    }

        private Role resolveEffectiveRole(Utilisateur user) {
        if (user instanceof OrganisateurEvenement) return Role.ORGANISATEUR;
        if (user instanceof Recruteur) return Role.RECRUTEUR;
        if (user instanceof Candidat) return Role.CANDIDAT;
        return user.getRole() != null ? user.getRole() : Role.CANDIDAT;
        }

    public String extractEmail(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public String extractRole(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("role", String.class);
    }

    public Long extractId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Object idObj = claims.get("id");
        if (idObj instanceof Integer) {
            return Long.valueOf((Integer) idObj);
        } else if (idObj instanceof Long) {
            return (Long) idObj;
        }
        return null;
    }
}