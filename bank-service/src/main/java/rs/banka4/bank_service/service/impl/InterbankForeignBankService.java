package rs.banka4.bank_service.service.impl;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.user.User;
import rs.banka4.bank_service.repositories.UserRepository;
import rs.banka4.bank_service.service.abstraction.ForeignBankService;
import rs.banka4.bank_service.tx.data.UserInformation;
import rs.banka4.bank_service.tx.otc.config.InterbankRetrofitProvider;

@Service
@Slf4j
public class InterbankForeignBankService implements ForeignBankService {
    private final InterbankRetrofitProvider interbanker;
    private final UserRepository userRepository;
    private final RedisTemplate<String, UserInformation> redisTemplate;

    public InterbankForeignBankService(
        @Autowired(required = false) RedisConnectionFactory redisConn,
        UserRepository userRepository,
        InterbankRetrofitProvider interbanker
    ) {
        this.interbanker = interbanker;
        this.userRepository = userRepository;
        if (redisConn != null) {
            this.redisTemplate = new RedisTemplate<>();
            this.redisTemplate.setConnectionFactory(redisConn);
            this.redisTemplate.setDefaultSerializer(RedisSerializer.json());
            this.redisTemplate.afterPropertiesSet();
        } else {
            this.redisTemplate = null;
        }
    }

    private String cacheKeyForFbid(ForeignBankId foreignBankId) {
        return "FBID_USER_INFO:%d/%s".formatted(foreignBankId.routingNumber(), foreignBankId.id());
    }

    @Override
    public Optional<UserInformation> getUserInfoFor(ForeignBankId foreignBankId)
        throws IOException {
        if (foreignBankId.routingNumber() == ForeignBankId.OUR_ROUTING_NUMBER) {
            return userRepository.findById(UUID.fromString(foreignBankId.id()))
                .map(User::getEmail)
                .map(email -> new UserInformation("Raffeisen", email));
        }

        /* Check cache. */
        if (redisTemplate != null) {
            final var cachedInfo =
                redisTemplate.opsForValue()
                    .get(cacheKeyForFbid(foreignBankId));
            if (cachedInfo != null) {
                log.trace("found cached user information {} for {}", cachedInfo, foreignBankId);
                return Optional.of(cachedInfo);
            }
        }

        final var ib = interbanker.get(foreignBankId.routingNumber());
        final var infoCall = ib.getUserInfo(foreignBankId.routingNumber(), foreignBankId.id());
        final var infoResp = infoCall.execute();
        if (infoResp.code() == 404) return Optional.empty();
        if (!infoResp.isSuccessful()) {
            log.debug("request to resolve {} failed: {}", foreignBankId, infoResp);
            throw new IOException("Failed to resolve user info");
        }

        final var userInfo = infoResp.body();

        /* Update cache. */
        if (redisTemplate != null) {
            log.trace("caching user information {} for {}", userInfo, foreignBankId);
            redisTemplate.opsForValue()
                .set(cacheKeyForFbid(foreignBankId), userInfo, 5, TimeUnit.MINUTES);
        }

        return Optional.of(userInfo);
    }
}
