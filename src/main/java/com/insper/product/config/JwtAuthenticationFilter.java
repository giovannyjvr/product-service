package com.insper.product.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Filtro que intercepta TODAS as requisições, extrai o token JWT do header "Authorization"
 * e, se válido, insere uma Authentication no SecurityContextHolder para autenticar o usuário.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // Pode injetar via @Value("${jwt.secret}") se preferir, mas para simplificar:
    private final String jwtSecret = "MINHA_CHAVE_SECRETA";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = parseJwt(request);
            if (jwt != null && validateJwtToken(jwt)) {
                Claims claims = Jwts.parser()
                                   .setSigningKey(jwtSecret.getBytes())
                                   .parseClaimsJws(jwt)
                                   .getBody();

                String username = claims.getSubject();

                // Se seu token contiver roles, você extraíria aqui e populava as authorities.
                // Para simplificar, cria um usuário “dummy” sem roles:
                User principal = new User(username, "", new ArrayList<>());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Não foi possível validar o token JWT: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o JWT do header "Authorization" se começar com "Bearer ".
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    /**
     * Valida o JWT (assinatura, formato, expiração), retornando true se for válido.
     */
    private boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret.getBytes()).parseClaimsJws(authToken);
            return true;
        } catch (io.jsonwebtoken.SignatureException e) {
            logger.error("Assinatura JWT inválida: {}", e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.error("Token JWT expirado: {}", e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.error("Token JWT malformado: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro ao validar o token JWT: {}", e.getMessage());
        }
        return false;
    }
}
