package rs.banka4.stock_service.integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.stock_service.domain.actuaries.db.ActuaryInfo;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.stock_service.repositories.ActuaryRepository;
import rs.banka4.stock_service.utils.ActuaryGenerator;
import rs.banka4.testlib.integration.DbEnabledTest;
import rs.banka4.testlib.utils.JwtPlaceholders;

@SpringBootTest
@DbEnabledTest
@AutoConfigureMockMvc
public class ActuaryTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActuaryRepository actuaryRepository;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        actuaryRepository.deleteAll();
        List<ActuaryInfo> list = ActuaryGenerator.makeExampleActuaries();
        actuaryRepository.saveAll(list);
    }

    @Test
    void shouldRegisterActuarySuccessfully() throws Exception {
        jwtToken = "Bearer " + JwtPlaceholders.V3_VALID_ADMIN_EMPLOYEE_TOKEN;
        String payload = """
            {
                "needsApproval": false,
                "limitAmount": 10000,
                "limitCurrencyCode": "RSD",
                "actuaryId": "%s"
            }
            """.formatted(ActuaryGenerator.FOR_NEWLY_CREATED_ACTUARY_3_UUID);

        mockMvc.perform(
            MockMvcRequestBuilders.post("/actuaries/register")
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
            .andExpect(status().isCreated());

        Assertions.assertEquals(3, actuaryRepository.count());
    }

    @Test
    void shouldUpdateActuarySuccessfully() throws Exception {
        jwtToken = "Bearer " + JwtPlaceholders.V3_VALID_ADMIN_EMPLOYEE_TOKEN;
        UUID actuaryId = ActuaryGenerator.ACTUARY_2_UUID;
        String payload = """
            {
                "needsApproval": false,
                "limitAmount": 20000,
                "limitCurrencyCode": "RSD",
                "actuaryId": "%s"
            }
            """.formatted(ActuaryGenerator.ACTUARY_2_UUID);

        mockMvc.perform(
            MockMvcRequestBuilders.put("/actuaries/update/" + actuaryId)
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
            .andExpect(status().isCreated());

        ActuaryInfo actuary =
            actuaryRepository.findById(actuaryId)
                .get();
        Assertions.assertEquals(
            0,
            actuary.getLimit()
                .getAmount()
                .compareTo(BigDecimal.valueOf(20000))
        );
    }


    @Test
    void shouldFailRegisterActuaryBecauseOfJwt() throws Exception {
        jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        String payload = """
            {
                "needsApproval": false,
                "limitAmount": 10000,
                "limitCurrencyCode": "RSD",
                "actuaryId": "%s"
            }
            """.formatted(ActuaryGenerator.FOR_NEWLY_CREATED_ACTUARY_3_UUID);

        mockMvc.perform(
            MockMvcRequestBuilders.post("/actuaries/register")
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
            .andExpect(status().isForbidden());

        Assertions.assertEquals(2, actuaryRepository.count());
    }

    @Test
    void shouldFailUpdateActuaryBecauseOfJwt() throws Exception {
        jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        UUID actuaryId = ActuaryGenerator.ACTUARY_2_UUID;
        String payload = """
            {
                "needsApproval": false,
                "limitAmount": 20000,
                "limitCurrencyCode": "RSD",
                "actuaryId": "%s"
            }
            """.formatted(ActuaryGenerator.ACTUARY_2_UUID);

        mockMvc.perform(
            MockMvcRequestBuilders.put("/actuaries/update/" + actuaryId)
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
            .andExpect(status().isForbidden());

        ActuaryInfo actuary =
            actuaryRepository.findById(actuaryId)
                .get();
        Assertions.assertEquals(
            0,
            actuary.getLimit()
                .getAmount()
                .compareTo(BigDecimal.valueOf(50000))
        );
    }

    @Test
    void shouldResetUsedLimitSuccessfully() throws Exception {
        jwtToken = "Bearer " + JwtPlaceholders.V3_VALID_ADMIN_EMPLOYEE_TOKEN;

        UUID actuaryId = createTestActuary();
        // Simulate used limit by modifying the actuary manually
        ActuaryInfo actuary =
            actuaryRepository.findById(actuaryId)
                .get();
        actuary.getUsedLimit()
            .setAmount(BigDecimal.valueOf(5000));
        actuaryRepository.save(actuary);

        // Send the reset request
        mockMvc.perform(
            MockMvcRequestBuilders.put("/actuaries/limit/reset/" + actuaryId)
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
        )
            .andExpect(status().isAccepted());

        // Check if used limit was reset to 0
        ActuaryInfo updated =
            actuaryRepository.findById(actuaryId)
                .get();
        Assertions.assertEquals(
            0,
            updated.getUsedLimit()
                .getAmount()
                .compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void shouldFailToResetUsedLimitSuccessfullyBecauseOfJwt() throws Exception {
        jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;

        UUID actuaryId = createTestActuary();
        // Simulate used limit by modifying the actuary manually
        ActuaryInfo actuary =
            actuaryRepository.findById(actuaryId)
                .get();
        actuary.getUsedLimit()
            .setAmount(BigDecimal.valueOf(5000));
        actuaryRepository.save(actuary);

        // Send the reset request
        mockMvc.perform(
            MockMvcRequestBuilders.put("/actuaries/limit/reset/" + actuaryId)
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
        )
            .andExpect(status().isForbidden());

        // Check if used limit was reset to 0
        ActuaryInfo updated =
            actuaryRepository.findById(actuaryId)
                .get();
        Assertions.assertEquals(
            0,
            updated.getUsedLimit()
                .getAmount()
                .compareTo(BigDecimal.valueOf(5000))
        );
    }

    @Test
    void shouldUpdateLimitSuccessfully() throws Exception {
        jwtToken = "Bearer " + JwtPlaceholders.V3_VALID_ADMIN_EMPLOYEE_TOKEN;

        UUID actuaryId = createTestActuary();
        String requestBody = """
            {
                "limitAmount": 1000,
                "limitCurrencyCode" : "RSD"
            }
            """;

        mockMvc.perform(
            MockMvcRequestBuilders.put("/actuaries/limit/" + actuaryId)
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isAccepted());

        ActuaryInfo updated =
            actuaryRepository.findById(actuaryId)
                .get();
        Assertions.assertEquals(
            0,
            updated.getLimit()
                .getAmount()
                .compareTo(BigDecimal.valueOf(1000))
        );
    }

    @Test
    void shouldFailToUpdateLimitSuccessfullyBecauseOfJwt() throws Exception {
        jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;

        UUID actuaryId = createTestActuary();
        String requestBody = """
            {
                "limitAmount": 1000,
                "limitCurrencyCode" : "RSD"
            }
            """;

        mockMvc.perform(
            MockMvcRequestBuilders.put("/actuaries/limit/" + actuaryId)
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isForbidden());

        ActuaryInfo updated =
            actuaryRepository.findById(actuaryId)
                .get();
        Assertions.assertEquals(
            0,
            updated.getLimit()
                .getAmount()
                .compareTo(BigDecimal.valueOf(9999))
        );
    }

    private UUID createTestActuary() {
        UUID id = UUID.randomUUID();
        ActuaryInfo actuary = new ActuaryInfo();
        actuary.setUserId(id);
        actuary.setLimit(new MonetaryAmount(BigDecimal.valueOf(9999), CurrencyCode.RSD));
        actuary.setUsedLimit(new MonetaryAmount(BigDecimal.valueOf(9999), CurrencyCode.RSD));
        actuary.setNeedApproval(true);
        actuaryRepository.save(actuary);
        return id;
    }

}
