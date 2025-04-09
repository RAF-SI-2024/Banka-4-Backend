package rs.banka4.stock_service.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import rs.banka4.stock_service.domain.assets.db.AssetOwnership;
import rs.banka4.stock_service.domain.assets.db.AssetOwnershipId;
import rs.banka4.stock_service.repositories.AssetOwnershipRepository;
import rs.banka4.stock_service.repositories.AssetRepository;
import rs.banka4.stock_service.repositories.ExchangeRepository;
import rs.banka4.stock_service.utils.AssetGenerator;
import rs.banka4.stock_service.utils.ExchangeGenerator;
import rs.banka4.testlib.integration.DbEnabledTest;
import rs.banka4.testlib.utils.JwtPlaceholders;

@SpringBootTest
@DbEnabledTest
@AutoConfigureMockMvc
public class StockTests {
    @Autowired
    private MockMvcTester mvc;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private ExchangeRepository exchangeRepo;
    @Autowired
    private AssetOwnershipRepository assetOwnershipRepo;

    @Test
    public void transfer_test_public() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 =
            assetRepository.findById(AssetGenerator.STOCK_EX1_UUID)
                .get();

        assetOwnershipRepo.save(
            new AssetOwnership(new AssetOwnershipId(JwtPlaceholders.CLIENT_ID, ex1), 100, 0, 0)
        );

        mvc.put()
            .uri("/stocks/transfer")
            .header("Authorization", "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .contentType("application/json")
            .content("""
                {
                "stockId": "%s",
                "amount": "50",
                "transferTo" : "PUBLIC"
                }
                """.formatted(ex1.getId()))
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                "publicAmount": 50,
                "privateAmount": 50
                }""");

    }

    @Test
    public void transfer_test_private() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 =
            assetRepository.findById(AssetGenerator.STOCK_EX1_UUID)
                .get();

        assetOwnershipRepo.save(
            new AssetOwnership(new AssetOwnershipId(JwtPlaceholders.CLIENT_ID, ex1), 100, 50, 0)
        );

        mvc.put()
            .uri("/stocks/transfer")
            .header("Authorization", "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .contentType("application/json")
            .content("""
                {
                "stockId": "%s",
                "amount": "10",
                "transferTo" : "PRIVATE"
                }
                """.formatted(ex1.getId()))
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                "publicAmount": 40,
                "privateAmount": 110
                }""");

    }

    @Test
    public void transfer_test_not_enough_stocks() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 =
            assetRepository.findById(AssetGenerator.STOCK_EX1_UUID)
                .get();

        assetOwnershipRepo.save(
            new AssetOwnership(new AssetOwnershipId(JwtPlaceholders.CLIENT_ID, ex1), 100, 50, 0)
        );

        mvc.put()
            .uri("/stocks/transfer")
            .header("Authorization", "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .contentType("application/json")
            .content("""
                {
                "stockId": "%s",
                "amount": "60",
                "transferTo" : "PRIVATE"
                }
                """.formatted(ex1.getId()))
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                "code":"NotEnoughStock"
                }
                """);

    }
}
