package rs.banka4.user_service.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.*;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.banka4.user_service.domain.auth.db.SecuredUser;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.exceptions.jwt.ExpiredJwt;
import rs.banka4.user_service.exceptions.jwt.IllegalArgumentJwt;
import rs.banka4.user_service.exceptions.jwt.MalformedJwt;
import rs.banka4.user_service.exceptions.jwt.UnsupportedJwt;
import rs.banka4.user_service.service.abstraction.TokenService;

@Service
@Transactional
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh.token.expiration}")
    private long refreshExpiration;


    private final TokenService tokenService;

    public JwtUtil(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public String extractUsername(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
    }

    public String extractRole(String jwt) {
        return extractClaim(jwt, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwt();
        } catch (MalformedJwtException e) {
            throw new MalformedJwt();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentJwt();
        } catch (Exception e) {
            throw new UnsupportedJwt();
        }
    }

    public String generateToken(Client client) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", client.getId());
        claims.put("role", "client");
        return generateToken(claims, new SecuredUser(client), jwtExpiration);
    }

    public String generateToken(Employee employee) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", employee.getId());
        claims.put("role", "employee");
        return generateToken(claims, new SecuredUser(employee), jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return generateToken(claims, userDetails, refreshExpiration);
    }

    public String generateToken(
        Map<String, Object> extraClaims,
        UserDetails userDetails,
        long expiration
    ) {
        return Jwts.builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean validateToken(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    public void invalidateToken(String token) {
        tokenService.invalidateToken(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenInvalidated(String token) {
        return tokenService.findByToken(token)
            .isPresent();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
