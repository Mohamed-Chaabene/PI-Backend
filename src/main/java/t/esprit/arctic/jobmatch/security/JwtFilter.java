package t.esprit.arctic.jobmatch.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        System.out.println("========================================");
        System.out.println("🔑 [JwtFilter] URL: " + request.getRequestURI());
        System.out.println("🔑 [JwtFilter] Header: " + authHeader);
        System.out.println("========================================");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String email = jwtService.extractEmail(token);
                System.out.println("📧 Email extrait: '" + email + "'");

                if (email != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    System.out.println("✅ UserDetails chargé: " + userDetails.getUsername());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ AUTHENTIFICATION SET pour: " + email);
                    System.out.println("✅ Vérification: " + SecurityContextHolder.getContext().getAuthentication().getName());
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur JWT: " + e.getMessage());
                e.printStackTrace();
            }
        }

        chain.doFilter(request, response);
    }
}