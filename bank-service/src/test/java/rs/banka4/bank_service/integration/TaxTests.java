package rs.banka4.bank_service.integration;

import static org.junit.jupiter.api.Assertions.*;
import static rs.banka4.bank_service.utils.AssetGenerator.STOCK_EX1_UUID;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.options.db.Option;
import rs.banka4.bank_service.domain.security.stock.db.Stock;
import rs.banka4.bank_service.domain.taxes.db.UserTaxDebts;
import rs.banka4.bank_service.domain.user.client.db.Client;
import rs.banka4.bank_service.generator.AccountObjectMother;
import rs.banka4.bank_service.integration.generator.PortfolioGenerator;
import rs.banka4.bank_service.integration.generator.UserGenerator;
import rs.banka4.bank_service.integration.generator.UserTaxGenerator;
import rs.banka4.bank_service.repositories.*;
import rs.banka4.bank_service.runners.TestDataRunner;
import rs.banka4.bank_service.service.abstraction.ExchangeRateService;
import rs.banka4.bank_service.service.impl.TaxServiceImp;
import rs.banka4.bank_service.utils.AssetGenerator;
import rs.banka4.bank_service.utils.ExchangeGenerator;
import rs.banka4.bank_service.utils.ListingGenerator;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.testlib.integration.DbEnabledTest;
import rs.banka4.testlib.utils.JwtPlaceholders;

@SpringBootTest
@DbEnabledTest
@AutoConfigureMockMvc
public class TaxTests {
    @Autowired
    private MockMvcTester mvc;
    @Autowired
    private UserGenerator userGen;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TaxServiceImp taxService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    ExchangeRateService exchangeRateService;
    @Autowired
    private UserTaxDebtsRepository userTaxDebtsRepository;
    @Autowired
    private PortfolioGenerator portfolioGenerator;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private SecurityRepository securityRepository;
    @Autowired
    private ListingRepository listingRepo;
    @Autowired
    private ExchangeRepository exchangeRepo;
    @Autowired
    private ListingDailyPriceInfoRepository listingHistoryRepo;

    private Client createTestClient() {
        final var assetOwner =
            userGen.createClient(
                x -> x.id(JwtPlaceholders.CLIENT_ID)
                    .email("johndqoe@example.com")
            );
        return userRepository.save(assetOwner);
    }

    private Client createTestClient2() {
        final var assetOwner =
            userGen.createClient(
                x -> x.id(UUID.randomUUID())
                    .firstName("Michael")
                    .lastName("Smith")
                    .email("johndqoeads@example.com")
            );
        return userRepository.save(assetOwner);
    }

    private Account createStateAccount(Client client) {
        var account = AccountObjectMother.generateBasicToAccount();
        account.setClient(client);
        account.setAccountNumber(TestDataRunner.STATE_ACCOUNT_NUMBER);
        userRepository.save(account.getEmployee());
        return accountRepository.save(account);
    }

    private Client createTestClient3() {
        final var assetOwner =
            userGen.createClient(
                x -> x.id(UUID.randomUUID())
                    .email("johndqoeads@addasd.com")
            );
        return userRepository.save(assetOwner);
    }

    /**
     * Verifies that the tax summary endpoint returns the correct unpaid tax total (converted to
     * RSD) for a single client who has debts in RSD and EUR.
     */
    @Test
    public void testTaxSummary() {
        Client client = createTestClient();

        UserTaxGenerator.createDummyTax(
            client,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );
        UserTaxGenerator.createDummyTaxEur(
            client,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );

        String expectedJson = """
            {
               "content":[
                  {
                     "userId":"a4bf370e-2129-4116-9243-0c4ead0fe43e",
                     "firstName":"John",
                     "email":"johndqoe@example.com",
                     "lastName":"Doe",
                     "unpaidTax":6020.2813599062140000,
                     "currency":"RSD"
                  }
               ],
               "page":{
                  "size":10,
                  "number":0,
                  "totalElements":1,
                  "totalPages":1
               }
            }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.V3_VALID_ADMIN_EMPLOYEE_TOKEN;
        mvc.get()
            .uri("/stock/tax/summary")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    /**
     * Ensures that when multiple clients each have multiple accounts with debts, the summary
     * endpoint returns an entry for each client, with correct aggregation of unpaid tax across all
     * their accounts.
     */
    @Test
    public void testTaxSummaryMultipleAccountsMultipleUsers() {
        Client client1 = createTestClient();
        Client client2 = createTestClient2();
        UserTaxGenerator.createMultipleDebts(
            client1,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );
        UserTaxGenerator.createMultipleDebts(
            client2,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );

        String expectedJson = """
            {
               "content":[
                  {
                     "firstName":"John",
                     "email":"johndqoe@example.com",
                     "lastName":"Doe",
                     "unpaidTax":11940.5627198124280000,
                     "currency":"RSD"
                  },
                  {
                     "firstName":"Michael",
                     "email":"johndqoeads@example.com",
                     "lastName":"Smith",
                     "unpaidTax":11940.5627198124280000,
                     "currency":"RSD"
                  }
               ],
               "page":{
                  "size":10,
                  "number":0,
                  "totalElements":2,
                  "totalPages":1
               }
            }`
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.V3_VALID_ADMIN_EMPLOYEE_TOKEN;
        mvc.get()
            .uri("/stock/tax/summary")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    /**
     * Verifies that the summary endpoint filters by first name (partial match) and only returns
     * clients whose first name contains the given substring.
     */
    @Test
    public void testTaxSummaryMultipleAccountsMultipleUsersSearchByName() {
        Client client1 = createTestClient();
        Client client2 = createTestClient2();
        UserTaxGenerator.createMultipleDebts(
            client1,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );
        UserTaxGenerator.createMultipleDebts(
            client2,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );

        String expectedJson = """
            {
               "content":[
                  {
                     "firstName":"John",
                     "email":"johndqoe@example.com",
                     "lastName":"Doe",
                     "unpaidTax":11940.5627198124280000,
                     "currency":"RSD"
                  }
               ],
               "page":{
                  "size":10,
                  "number":0,
                  "totalElements":1,
                  "totalPages":1
               }
            }`
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.V3_VALID_ADMIN_EMPLOYEE_TOKEN;
        mvc.get()
            .uri("/stock/tax/summary")
            .param("firstName", "Joh")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    /**
     * Verifies that the summary endpoint filters by last name (partial match) and only returns
     * clients whose last name contains the given substring.
     */
    @Test
    public void testTaxSummaryMultipleAccountsMultipleUsersSearchByLastName() {
        Client client1 = createTestClient();
        Client client2 = createTestClient2();
        UserTaxGenerator.createMultipleDebts(
            client1,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );
        UserTaxGenerator.createMultipleDebts(
            client2,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );

        String expectedJson = """
            {
               "content":[
                  {
                     "firstName":"Michael",
                     "email":"johndqoeads@example.com",
                     "lastName":"Smith",
                     "unpaidTax":11940.5627198124280000,
                     "currency":"RSD"
                  }
               ],
               "page":{
                  "size":10,
                  "number":0,
                  "totalElements":1,
                  "totalPages":1
               }
            }`
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.V3_VALID_ADMIN_EMPLOYEE_TOKEN;
        mvc.get()
            .uri("/stock/tax/summary")
            .param("lastName", "Smit")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    /**
     * Verifies that applying both firstName and lastName filters together returns only the clients
     * matching both criteria.
     */
    @Test
    public void testTaxSummaryMultipleAccountsMultipleUsersSearchByFullName() {
        Client client1 = createTestClient();
        Client client2 = createTestClient2();
        UserTaxGenerator.createMultipleDebts(
            client1,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );
        UserTaxGenerator.createMultipleDebts(
            client2,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );

        String expectedJson = """
            {
               "content":[
                  {
                     "firstName":"Michael",
                     "email":"johndqoeads@example.com",
                     "lastName":"Smith",
                     "unpaidTax":11940.5627198124280000,
                     "currency":"RSD"
                  }
               ],
               "page":{
                  "size":10,
                  "number":0,
                  "totalElements":1,
                  "totalPages":1
               }
            }`
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.V3_VALID_ADMIN_EMPLOYEE_TOKEN;
        mvc.get()
            .uri("/stock/tax/summary")
            .param("lastName", "Smi")
            .param("firstName", "Micha")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    /**
     * Tests that collecting tax for a specific client zeros out all their debts and credits exactly
     * the same amount (in the correct currency) to the state account.
     */
    @Test
    public void testTaxSpecificClient() {
        Client client1 = createTestClient();
        Client client2 = createTestClient2();
        Client client3 = createTestClient3();
        var stateAcc = createStateAccount(client3);
        var stateBalance = stateAcc.getAvailableBalance();
        var debt =
            UserTaxGenerator.createMultipleDebtsRSD(
                client1,
                2,
                userRepository,
                accountRepository,
                userTaxDebtsRepository
            );
        UserTaxGenerator.createMultipleDebts(
            client2,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );

        String jwtToken = "Bearer " + JwtPlaceholders.V3_VALID_ADMIN_EMPLOYEE_TOKEN;
        mvc.post()
            .uri("/stock/tax/collect/" + client1.getId())
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk();
        var userDebts = userTaxDebtsRepository.findByAccount_Client_Id(client1.getId());
        userDebts.forEach(userDebt -> {
            assertEquals(
                BigDecimal.ZERO,
                userDebt.getDebtAmount()
                    .stripTrailingZeros()
            );
        });
        var stateBalanceAfter =
            accountRepository.findAccountByAccountNumber(TestDataRunner.STATE_ACCOUNT_NUMBER)
                .orElseThrow()
                .getAvailableBalance();
        assertEquals(
            stateBalance.add(debt)
                .stripTrailingZeros(),
            stateBalanceAfter.stripTrailingZeros()
        );
    }

    /**
     * Tests tax collection for a client whose debts are denominated in EUR. Verifies that the EUR
     * amount is converted to RSD before crediting the state account.
     */
    @Test
    public void testTaxSpecificClientNotRSD() {
        Client client1 = createTestClient();
        Client client3 = createTestClient3();
        var stateAcc = createStateAccount(client3);
        var stateBalance = stateAcc.getAvailableBalance();
        var debt =
            UserTaxGenerator.createDummyTaxEur(
                client1,
                userRepository,
                accountRepository,
                userTaxDebtsRepository
            );

        String jwtToken = "Bearer " + JwtPlaceholders.V3_VALID_ADMIN_EMPLOYEE_TOKEN;
        mvc.post()
            .uri("/stock/tax/collect/" + client1.getId())
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk();
        var userDebts = userTaxDebtsRepository.findByAccount_Client_Id(client1.getId());
        userDebts.forEach(userDebt -> {
            assertEquals(
                BigDecimal.ZERO,
                userDebt.getDebtAmount()
                    .stripTrailingZeros()
            );
        });
        var stateBalanceAfter =
            accountRepository.findAccountByAccountNumber(TestDataRunner.STATE_ACCOUNT_NUMBER)
                .orElseThrow()
                .getAvailableBalance();
        debt = exchangeRateService.convertCurrency(debt, CurrencyCode.EUR, CurrencyCode.RSD);
        assertEquals(
            stateBalance.add(debt)
                .stripTrailingZeros()
                .round(MathContext.DECIMAL32),
            stateBalanceAfter.stripTrailingZeros()
                .round(MathContext.DECIMAL32)
        );
    }

    /**
     * Ensures that attempting to collect tax for a client with insufficient funds results in a
     * FORBIDDEN (HTTP 403) response and leaves their debts unchanged.
     */
    @Test
    public void testTaxSpecificClientInsufficientFunds() {
        Client client1 = createTestClient();
        Client client2 = createTestClient2();
        Client client3 = createTestClient3();
        var stateAcc = createStateAccount(client3);
        UserTaxGenerator.createMultipleDebtsInsufficientFunds(
            client1,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );
        UserTaxGenerator.createMultipleDebts(
            client2,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.post()
            .uri("/stock/tax/collect/" + client1.getId())
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatus(HttpStatus.FORBIDDEN);
    }

    /**
     * Ensures that attempting to collect tax for a client using clients jwt results in a FORBIDDEN
     * (HTTP 403) response and leaves their debts unchanged.
     */
    @Test
    public void testTaxSpecificClientUnauthorized() {
        Client client1 = createTestClient();
        Client client2 = createTestClient2();
        Client client3 = createTestClient3();
        var stateAcc = createStateAccount(client3);
        UserTaxGenerator.createMultipleDebts(
            client1,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );
        UserTaxGenerator.createMultipleDebts(
            client2,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.post()
            .uri("/stock/tax/collect/" + client1.getId())
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatus(HttpStatus.FORBIDDEN);
    }

    /**
     * Tests the "trigger-monthly" endpoint: it should collect and clear debts for all clients,
     * crediting the sum of all their debts to the state account, including proper conversion for
     * non‑RSD debts.
     */
    @Test
    public void testTaxAllClients() {
        Client client1 = createTestClient();
        Client client2 = createTestClient2();
        Client client3 = createTestClient3();
        var stateAcc = createStateAccount(client3);
        var stateBalance = stateAcc.getAvailableBalance();
        var debt =
            UserTaxGenerator.createMultipleDebtsRSD(
                client1,
                2,
                userRepository,
                accountRepository,
                userTaxDebtsRepository
            );
        var debt2 =
            UserTaxGenerator.createMultipleDebtsEUR(
                client2,
                2,
                userRepository,
                accountRepository,
                userTaxDebtsRepository
            );

        String jwtToken = "Bearer " + JwtPlaceholders.V3_VALID_ADMIN_EMPLOYEE_TOKEN;
        mvc.post()
            .uri("/stock/tax/trigger-monthly")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk();
        var allDebts = userTaxDebtsRepository.findAll();
        allDebts.forEach(userDebt -> {
            assertEquals(
                BigDecimal.ZERO,
                userDebt.getDebtAmount()
                    .stripTrailingZeros()
            );
        });
        debt2 = exchangeRateService.convertCurrency(debt2, CurrencyCode.EUR, CurrencyCode.RSD);
        var stateBalanceAfter =
            accountRepository.findAccountByAccountNumber(TestDataRunner.STATE_ACCOUNT_NUMBER)
                .orElseThrow()
                .getAvailableBalance();
        assertEquals(
            stateBalance.add(debt)
                .add(debt2)
                .stripTrailingZeros()
                .round(MathContext.DECIMAL32),
            stateBalanceAfter.stripTrailingZeros()
                .round(MathContext.DECIMAL32)
        );
    }

    /**
     * Verifies that the "trigger-monthly" job will still collect from clients who have sufficient
     * funds, even if one of the clients has insufficient funds. Only the solvable debts are
     * collected.
     */
    @Test
    public void testTaxAllClientsOneInsufficientFunds() {
        Client client1 = createTestClient();
        Client client2 = createTestClient2();
        Client client3 = createTestClient3();
        var stateAcc = createStateAccount(client3);
        var stateBalance = stateAcc.getAvailableBalance();
        var debt =
            UserTaxGenerator.createMultipleDebtsRSD(
                client1,
                2,
                userRepository,
                accountRepository,
                userTaxDebtsRepository
            );
        UserTaxGenerator.createMultipleDebtsInsufficientFunds(
            client2,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );


        String jwtToken = "Bearer " + JwtPlaceholders.V3_VALID_ADMIN_EMPLOYEE_TOKEN;
        mvc.post()
            .uri("/stock/tax/trigger-monthly")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk();
        var stateBalanceAfter =
            accountRepository.findAccountByAccountNumber(TestDataRunner.STATE_ACCOUNT_NUMBER)
                .orElseThrow()
                .getAvailableBalance();
        assertEquals(
            stateBalance.add(debt)
                .stripTrailingZeros()
                .round(MathContext.DECIMAL32),
            stateBalanceAfter.stripTrailingZeros()
                .round(MathContext.DECIMAL32)
        );
    }

    /**
     * Tests the yearly cleanup operation, which should reset all clients' yearlyDebtAmount back to
     * zero without touching the monthly debt amounts.
     */
    @Test
    public void testTaxYearlyClean() {
        Client client1 = createTestClient();
        Client client2 = createTestClient2();
        Client client3 = createTestClient3();
        createStateAccount(client3);
        UserTaxGenerator.createMultipleDebtsRSD(
            client1,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );
        UserTaxGenerator.createMultipleDebtsEUR(
            client2,
            2,
            userRepository,
            accountRepository,
            userTaxDebtsRepository
        );

        taxService.cleanYearlyDebt();

        var allDebts = userTaxDebtsRepository.findAll();
        allDebts.forEach(userDebt -> {
            assertEquals(
                BigDecimal.ZERO,
                userDebt.getYearlyDebtAmount()
                    .stripTrailingZeros()
            );
        });
    }

    @Transactional
    @Test
    void addTaxAmountToDB_createsNewDebtAndAccumulates() {
        var user = portfolioGenerator.createTestClient();
        var account = AccountObjectMother.generateBasicEURFromAccount();
        account.setClient(user);
        account.setAccountNumber(
            UUID.randomUUID()
                .toString()
        );
        userRepository.save(account.getEmployee());
        accountRepository.save(account);

        assertTrue(
            userTaxDebtsRepository.findByAccount_AccountNumber(account.getAccountNumber())
                .isEmpty()
        );

        // add 50 USD
        taxService.addTaxAmountToDB(
            new MonetaryAmount(BigDecimal.valueOf(50), CurrencyCode.EUR),
            account
        );
        Optional<UserTaxDebts> first =
            userTaxDebtsRepository.findByAccount_AccountNumber(account.getAccountNumber());
        assertTrue(first.isPresent());
        assertEquals(
            0,
            first.get()
                .getDebtAmount()
                .compareTo(BigDecimal.valueOf(7.5))
        );

        // add another 20 USD
        taxService.addTaxAmountToDB(
            new MonetaryAmount(BigDecimal.valueOf(20), CurrencyCode.EUR),
            account
        );
        var updated =
            userTaxDebtsRepository.findByAccount_AccountNumber(account.getAccountNumber())
                .get();
        assertEquals(
            0,
            updated.getDebtAmount()
                .compareTo(BigDecimal.valueOf(10.50))
        );

        // adding zero or negative should be no‐op
        taxService.addTaxAmountToDB(new MonetaryAmount(BigDecimal.ZERO, CurrencyCode.EUR), account);
        taxService.addTaxAmountToDB(
            new MonetaryAmount(BigDecimal.valueOf(-10), CurrencyCode.EUR),
            account
        );
        // unchanged
        assertEquals(
            0,
            userTaxDebtsRepository.findByAccount_AccountNumber(account.getAccountNumber())
                .get()
                .getDebtAmount()
                .compareTo(BigDecimal.valueOf(10.50))
        );
    }

    @Test
    void addTaxAmountToDB_currencyMismatch_throws() {
        var user = portfolioGenerator.createTestClient();
        var account = AccountObjectMother.generateBasicEURFromAccount();
        account.setClient(user);
        account.setAccountNumber(
            UUID.randomUUID()
                .toString()
        );
        userRepository.save(account.getEmployee());
        accountRepository.save(account);
        assertThrows(
            IllegalArgumentException.class,
            () -> taxService.addTaxAmountToDB(
                new MonetaryAmount(BigDecimal.TEN, CurrencyCode.USD),
                account
            )
        );
    }

    @Test
    @Transactional
    void addTaxForOrderToDB_stubsProfitAndPersistsDebt() {
        Client client = portfolioGenerator.createTestClient();
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var stock = securityRepository.findById(STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            stock.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        portfolioGenerator.createDummyBuyOrder(
            client,
            stock.get(),
            100,
            BigDecimal.valueOf(40),
            CurrencyCode.EUR
        );
        var order =
            portfolioGenerator.createDummySellOrder(
                client,
                stock.get(),
                200,
                BigDecimal.valueOf(50),
                CurrencyCode.EUR
            );

        taxService.addTaxForOrderToDB(order);

        var debt =
            userTaxDebtsRepository.findByAccount_AccountNumber(
                order.getAccount()
                    .getAccountNumber()
            )
                .get();
        assertEquals(
            0,
            debt.getDebtAmount()
                .compareTo(BigDecimal.valueOf(147.90))
        );
    }

    @Test
    @Transactional
    void addTaxForOrderToDB_nonSellOrNonStock_noop() {
        Client client = portfolioGenerator.createTestClient();
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var stock = securityRepository.findById(STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            stock.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        var forex = securityRepository.findById(AssetGenerator.FOREX_EUR_USD_UUID);
        ListingGenerator.makeExampleListings(
            forex.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        var orderBuy =
            portfolioGenerator.createDummyBuyOrder(
                client,
                stock.get(),
                100,
                BigDecimal.valueOf(40),
                CurrencyCode.EUR
            );

        var order =
            portfolioGenerator.createDummySellOrder(
                client,
                forex.get(),
                200,
                BigDecimal.valueOf(50),
                CurrencyCode.EUR
            );


        taxService.addTaxForOrderToDB(orderBuy);
        assertTrue(
            userTaxDebtsRepository.findByAccount_AccountNumber(
                orderBuy.getAccount()
                    .getAccountNumber()
            )
                .isEmpty()
        );

        taxService.addTaxForOrderToDB(order);
        assertTrue(
            userTaxDebtsRepository.findByAccount_AccountNumber(
                order.getAccount()
                    .getAccountNumber()
            )
                .isEmpty()
        );
    }

    @Test
    @Transactional
    void addTaxForOtcToDB_stubsOptionProfitAndPersistsDebt() {
        Client client = portfolioGenerator.createTestClient();
        var account = AccountObjectMother.generateBasicEURFromAccount();
        final var stock1 =
            Stock.builder()
                .id(STOCK_EX1_UUID)
                .name("Example One™")
                .ticker("EX1")
                .dividendYield(new BigDecimal("0.052"))
                .outstandingShares(325_000)
                .build();
        final var ber1 = ExchangeGenerator.makeBer1();

        account.setCurrency(CurrencyCode.USD);
        account.setClient(client);
        account.setAccountNumber(
            UUID.randomUUID()
                .toString()
        );
        userRepository.save(account.getEmployee());
        accountRepository.save(account);
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        assetRepository.save(stock1);
        ListingGenerator.makeExampleListings(stock1, ber1, listingRepo, listingHistoryRepo);
        var option = assetRepository.findById(AssetGenerator.OPTION_EX1_PUT_UUID);

        taxService.addTaxForOtcToDB((Option) option.get(), account);

        var debt =
            userTaxDebtsRepository.findByAccount_AccountNumber(account.getAccountNumber())
                .get();
        assertEquals(
            0,
            debt.getDebtAmount()
                .compareTo(BigDecimal.valueOf(1400.4000))
        );
    }
}
