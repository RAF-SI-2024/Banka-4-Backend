package rs.banka4.stock_service.domain.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.forex.mapper.ForexPairMapper;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.domain.security.future.mapper.FutureMapper;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.domain.security.stock.mapper.StockMapper;

@Component
@RequiredArgsConstructor
public class SecurityMapperImpl {
    public SecurityDto toDto(Security security) {
        if (security instanceof Stock) {
            return new SecurityDto(
                security.getId(),
                security.getName(),
                security.getPrice(),
                StockMapper.INSTANCE.toDto((Stock) security),
                null,
                null
            );
        } else if (security instanceof Future) {
            return new SecurityDto(
                security.getId(),
                security.getName(),
                security.getPrice(),
                null,
                FutureMapper.INSTANCE.toDto((Future) security),
                null
            );
        } else { // security instanceof ForexPair
            return new SecurityDto(
                security.getId(),
                security.getName(),
                security.getPrice(),
                null,
                null,
                ForexPairMapper.INSTANCE.toDto((ForexPair) security)
            );
        }
    }

    public Security toEntity(SecurityDto securityDto) {
        if (securityDto.stock() != null) {
            return StockMapper.INSTANCE.toEntity(securityDto.stock());
        } else if (securityDto.future() != null) {
            return FutureMapper.INSTANCE.toEntity(securityDto.future());
        } else { // if (securityDto.forexPair() != null){
            ForexPair fp = ForexPairMapper.INSTANCE.toEntity(securityDto.forexPair());
            fp.setName(fp.getBaseCurrency() + "/" + fp.getQuoteCurrency());
            return fp;
        }
    }
}
