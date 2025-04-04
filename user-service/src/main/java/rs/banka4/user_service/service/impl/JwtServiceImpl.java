package rs.banka4.user_service.service.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rs.banka4.rafeisen.common.exceptions.jwt.ExpiredJwt;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserPrincipal;
import rs.banka4.rafeisen.common.utils.jwt.JwtParseFailed;
import rs.banka4.rafeisen.common.utils.jwt.JwtUtil;
import rs.banka4.rafeisen.common.utils.jwt.UnverifiedToken;
import rs.banka4.rafeisen.common.utils.jwt.VerifiedAccessToken;
import rs.banka4.rafeisen.common.utils.jwt.VerifiedRefreshToken;
import rs.banka4.rafeisen.common.utils.jwt.VerifiedToken;
import rs.banka4.user_service.domain.user.User;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.service.abstraction.TokenService;

@Service
public class JwtServiceImpl implements JwtService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtServiceImpl.class);
    private final TokenService tokenService;
    private final JwtUtil jwtParser;
    private final SecretKey jwtKey;
    private final int jwtRefreshExpirationMs;
    private final int jwtExpirationMs;

    public JwtServiceImpl(
        @Value("${jwt.secret.key}") String secretKey,
        @Value("${jwt.expiration}") int jwtExpiration,
        @Value("${jwt.refresh.token.expiration}") int refreshExpiration,
        TokenService tokenService
    ) {
        this.jwtExpirationMs = jwtExpiration;
        this.jwtRefreshExpirationMs = refreshExpiration;
        this.tokenService = tokenService;
        this.jwtKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.jwtParser =
            new JwtUtil(
                Jwts.parser()
                    .verifyWith(jwtKey)
                    .build()
            );
    }

    @Override
    public String generateAccessToken(User user) {
        final var accessToken =
            new VerifiedAccessToken(user.getUserType(), user.getId(), user.getPrivileges());
        return Jwts.builder()
            .claims(accessToken.getClaims())
            .expiration(new Date(System.currentTimeMillis() + this.jwtExpirationMs))
            .signWith(this.jwtKey)
            .compact();
    }

    @Override
    public String generateRefreshToken(AuthenticatedBankUserPrincipal principal) {
        final var refreshToken = new VerifiedRefreshToken(principal.userType(), principal.userId());
        return Jwts.builder()
            .claims(refreshToken.getClaims())
            .expiration(new Date(System.currentTimeMillis() + this.jwtRefreshExpirationMs))
            .signWith(this.jwtKey)
            .compact();

    }

    @Override
    public boolean isTokenExpired(String token) {
        /* Throws if expired. */
        parseToken(new UnverifiedToken(token));
        return false;
    }

    @Override
    public boolean isTokenInvalidated(String token) {
        return tokenService.findByToken(token)
            .isPresent();
    }

    @Override
    public void invalidateToken(String token) {
        tokenService.invalidateToken(token);
    }

    @Override
    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    @Override
    public String extractRole(String token) {
        final var tokenObj = parseToken(new UnverifiedToken(token));
        return tokenObj.getRole()
            .name();
    }

    @Override
    public UUID extractUserId(String token) {
        final var tokenObj = parseToken(new UnverifiedToken(token));
        return tokenObj.getSub();
    }

    @Override
    public VerifiedToken parseToken(UnverifiedToken token) {
        try {
            return jwtParser.parseToken(token);
        } catch (JwtParseFailed e) {
            /* TODO(arsen): handle more specific inner error */
            throw new ExpiredJwt();
        }
    }
}
