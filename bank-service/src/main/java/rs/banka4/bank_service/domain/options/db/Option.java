package rs.banka4.bank_service.domain.options.db;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.security.stock.db.Stock;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;

@Entity(name = "options")
@Getter
@Setter
@RequiredArgsConstructor
@SuperBuilder
public class Option extends Asset {

    public Option(
        UUID id,
        String name,
        String ticker,
        Stock stock,
        OptionType optionType,
        MonetaryAmount strikePrice,
        MonetaryAmount premium,
        double impliedVolatility,
        int openInterest,
        OffsetDateTime settlementDate,
        boolean active,
        ForeignBankId foreignId
    ) {
        super(id, name, ticker);
        this.stock = stock;
        this.optionType = optionType;
        this.strikePrice = strikePrice;
        this.premium = premium;
        this.impliedVolatility = impliedVolatility;
        this.openInterest = openInterest;
        this.settlementDate = settlementDate;
        this.active = active;
        this.foreignId = foreignId;
    }

    @ManyToOne(optional = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OptionType optionType;

    @Column(nullable = false)
    private MonetaryAmount strikePrice;

    @Column(nullable = false)
    private MonetaryAmount premium;

    /**
     * Implied volatility, measured in percent (so, 40% means {@code
     * impliedVolatility = 40})
     */
    @Column(nullable = false)
    private double impliedVolatility;

    @Column(nullable = false)
    private int openInterest;

    @Column(nullable = false)
    private OffsetDateTime settlementDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    private ForeignBankId foreignId;
}
