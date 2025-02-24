package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.models.Token;
import rs.banka4.user_service.repositories.TokenRepository;
import rs.banka4.user_service.service.abstraction.TokenService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;

    @Override
    public void invalidateToken(String token) {
        Token invalidatedToken = new Token().withToken(token).withValid(false);
        tokenRepository.save(invalidatedToken);
    }

    @Override
    public Optional<Token> findByToken(String token) {
        return tokenRepository.findByToken(token);
    }

}
