package t.esprit.arctic.jobmatch.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

        String token = Jwts.builder()
                .setSubject(email)
                .claim("id", user.getId())
                .claim("role", user.getRole().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getKey())
                .compact();

        System.out.println("✅ [JwtService] Token généré pour: " + email);
        return token;
    }

    public String extractEmail(String token) {
        try {
            System.out.println("📧 [JwtService] Extraction email du token: " + token.substring(0, Math.min(50, token.length())) + "...");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.getSubject();
            System.out.println("📧 [JwtService] Email extrait: " + email);
            return email;
        } catch (Exception e) {
            System.err.println("❌ [JwtService] Erreur extraction email: " + e.getMessage());
            return null;
        }
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