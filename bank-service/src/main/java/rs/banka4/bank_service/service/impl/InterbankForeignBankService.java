package rs.banka4.bank_service.service.impl;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.user.User;
import rs.banka4.bank_service.repositories.UserRepository;
import rs.banka4.bank_service.service.abstraction.ForeignBankService;
import rs.banka4.bank_service.tx.data.UserInformation;
import rs.banka4.bank_service.tx.otc.config.InterbankRetrofitProvider;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterbankForeignBankService implements ForeignBankService {
    private final InterbankRetrofitProvider interbanker;
    private final UserRepository userRepository;

    @Override
    public Optional<UserInformation> getUserInfoFor(ForeignBankId foreignBankId)
        throws IOException {
        if (foreignBankId.routingNumber() == ForeignBankId.OUR_ROUTING_NUMBER) {
            return userRepository.findById(UUID.fromString(foreignBankId.id()))
                .map(User::getEmail)
                .map(email -> new UserInformation("Raffeisen", email));
        }

        final var ib = interbanker.get(foreignBankId.routingNumber());
        final var infoCall = ib.getUserInfo(foreignBankId.routingNumber(), foreignBankId.id());
        final var infoResp = infoCall.execute();
        if (!infoResp.isSuccessful()) {
            log.debug("request to resolve {} failed: {}", foreignBankId, infoResp);
            return Optional.empty();
        }
        return Optional.of(infoResp.body());
    }
}
