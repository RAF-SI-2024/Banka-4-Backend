package rs.banka4.user_service.service.impl;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.loan.db.*;
import rs.banka4.user_service.domain.loan.db.InterestRate;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanRequest;
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationResponseDto;
import rs.banka4.user_service.domain.loan.dtos.LoanFilterDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;
import rs.banka4.user_service.domain.loan.mapper.LoanMapper;
import rs.banka4.user_service.domain.loan.specification.LoanSpecification;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.exceptions.NullPageRequest;
import rs.banka4.user_service.exceptions.account.AccountNotActive;
import rs.banka4.user_service.exceptions.account.NotAccountOwner;
import rs.banka4.user_service.exceptions.jwt.Unauthorized;
import rs.banka4.user_service.exceptions.loan.InterestRateAmountNotSupported;
import rs.banka4.user_service.exceptions.loan.InvalidLoanStatus;
import rs.banka4.user_service.exceptions.loan.LoanNotFound;
import rs.banka4.user_service.exceptions.loan.NoLoansOnAccount;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.repositories.InterestRateRepository;
import rs.banka4.user_service.repositories.LoanInstallmentRepository;
import rs.banka4.user_service.repositories.LoanRepository;
import rs.banka4.user_service.repositories.LoanRequestRepository;
import rs.banka4.user_service.service.abstraction.AccountService;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.abstraction.LoanService;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.loans.LoanRateScheduler;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;


@RequiredArgsConstructor
@Service
public class LoanServiceImpl implements LoanService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoanServiceImpl.class);

    private final ClientService clientService;
    private final AccountService accountService;
    private final LoanRepository loanRepository;
    private final LoanRequestRepository loanRequestRepository;
    private final InterestRateRepository interestRateRepository;
    private final JwtUtil jwtUtil;
    private final LoanInstallmentRepository loanInstallmentRepository;

    /**
     * Creates a new loan request for the given loan application.
     *
     * @param loanApplicationDto The loan application to be processed.
     * @param auth The JWT token provided by the client.
     *
     * @throws ClientNotFound If the JWT token does not contain a valid client email.
     * @throws NotAccountOwner If the account number provided in the loan application does not
     *         belong to the client.
     * @throws AccountNotActive If the account is not active.
     * @throws InterestRateAmountNotSupported If the interest rate for the given amount is not
     *         supported.
     */
    @Transactional
    @Override
    public void createLoanApplication(LoanApplicationDto loanApplicationDto, String auth) {
        String email = jwtUtil.extractUsername(auth);
        Optional<Client> client = clientService.getClientByEmail(email);

        if (client.isEmpty()) throw new ClientNotFound(email);

        Loan newLoan = LoanMapper.INSTANCE.toEntity(loanApplicationDto);
        newLoan.setStatus(LoanStatus.PROCESSING);
        newLoan.setRemainingDebt(loanApplicationDto.amount());

        connectAccountToLoan(loanApplicationDto, newLoan, email);
        setLoanInterestRate(newLoan, loanApplicationDto);
        generateLoanNumber(newLoan);
        makeLoanRequest(newLoan, loanApplicationDto);
    }

    /**
     * Sets the interest rate on a loan based on the current date and loan amount. If the interest
     * type is not fixed, the interest rate is modified to include the current interest rate
     * variant.
     *
     * @param newLoan the loan to set the interest rate to
     * @param loanApplicationDto application from which we get the interest type
     *        {@link rs.banka4.user_service.domain.loan.db.Loan.InterestType#FIXED} or
     *        {@link rs.banka4.user_service.domain.loan.db.Loan.InterestType#VARIABLE}
     *
     * @throws InterestRateAmountNotSupported if the interest rate for the given amount is not
     *         supported
     */
    private void setLoanInterestRate(Loan newLoan, LoanApplicationDto loanApplicationDto) {
        InterestRate interestRate =
            interestRateRepository.findByAmountAndDate(newLoan.getAmount(), LocalDate.now())
                .orElseThrow(InterestRateAmountNotSupported::new);

        BigDecimal baseRate = interestRate.getFixedRate();

        if (loanApplicationDto.interestType() != Loan.InterestType.FIXED) {
            baseRate = baseRate.add(LoanRateScheduler.getInterestRateVariant());
        }

        newLoan.setBaseInterestRate(baseRate);
        newLoan.setInterestRate(interestRate);
    }

    @Override
    public ResponseEntity<Page<LoanInformationDto>> getAllLoans(
        String token,
        PageRequest pageRequest,
        LoanFilterDto filterDto
    ) {
        ensureEmployeeRole(token);

        Page<Loan> loanPage =
            loanRepository.findAll(
                LoanSpecification.searchLoans(filterDto),
                pageRequest.withSort(Sort.by("account.accountNumber"))
            );
        return ResponseEntity.ok(loanPage.map(LoanMapper.INSTANCE::toDto));
    }

    @Override
    public ResponseEntity<Page<LoanApplicationResponseDto>> getAllLoansProcessing(
        String token,
        PageRequest pageRequest,
        LoanFilterDto filterDto
    ) {
        ensureEmployeeRole(token);

        Page<LoanRequest> loanPage =
            loanRequestRepository.findAll(
                LoanSpecification.searchLoanRequests(filterDto),
                pageRequest.withSort(
                    Sort.by("account.accountNumber")
                        .descending()
                )
            );

        return ResponseEntity.ok(loanPage.map(LoanMapper.INSTANCE::toDtoApplicationResponse));
    }

    /**
     * Gets all loans for the given client.
     *
     * @param token the JWT token containing the client email
     * @param pageRequest the page request for pagination
     * @return a page of loans for the given client
     *
     * @throws ClientNotFound if the JWT token does not contain a valid client email
     * @throws NoLoansOnAccount if the client has no active accounts
     * @throws NullPageRequest if the page request is null
     */
    @Override
    public ResponseEntity<Page<LoanInformationDto>> getMyLoans(
        String token,
        PageRequest pageRequest
    ) {

        if (pageRequest == null) {
            throw new NullPageRequest();
        }

        String username = jwtUtil.extractUsername(token);
        Optional<Client> client = clientService.getClientByEmail(username);
        if (client.isEmpty()) throw new ClientNotFound(username);

        Set<AccountDto> accounts = accountService.getAccountsForClient(token);
        if (accounts.isEmpty()) throw new NoLoansOnAccount(username);

        Set<String> accountNumbers =
            accounts.stream()
                .map(AccountDto::accountNumber)
                .collect(Collectors.toSet());
        SpecificationCombinator<Loan> combinator = new SpecificationCombinator<>();

        for (String accountNumber : accountNumbers) {
            combinator.or(LoanSpecification.hasAccountNumber(accountNumber));
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "amount");
        PageRequest pageRequestWithSort =
            PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), sort);

        Page<Loan> loansPage = loanRepository.findAll(combinator.build(), pageRequestWithSort);
        List<Loan> listOfLoans = loansPage.toList();
        Stream<Loan> streamOfLoans =
            listOfLoans.stream()
                .filter(loan -> loan.getStatus() != LoanStatus.PROCESSING);
        listOfLoans = streamOfLoans.toList();
        loansPage = new PageImpl<>(listOfLoans);

        Page<LoanInformationDto> loanDtoPage = loansPage.map(LoanMapper.INSTANCE::toDto);
        return ResponseEntity.ok(loanDtoPage);
    }

    @Transactional
    @Override
    public void approveLoan(Long loanNumber, String auth) {
        ensureEmployeeRole(auth);

        var loan = loanRepository.findByLoanNumber(loanNumber);

        if (loan.isEmpty()) throw new LoanNotFound();
        if (
            !loan.get()
                .getStatus()
                .equals(LoanStatus.PROCESSING)
        )
            throw new InvalidLoanStatus(
                loan.get()
                    .getStatus()
                    .name()
            );

        loan.get()
            .setNextInstallmentDate(
                LocalDate.now()
                    .plusMonths(1)
            );
        loan.get()
            .setDueDate(
                LocalDate.now()
                    .plusMonths(
                        loan.get()
                            .getRepaymentPeriod()
                    )
            );
        loan.get()
            .setStatus(LoanStatus.APPROVED);
        loan.get()
            .setAgreementDate(LocalDate.now());

        loanRepository.save(loan.get());

        makeLoanInstallmentFromLoan(loan.get());
    }

    private void makeLoanInstallmentFromLoan(Loan loan) {
        var loneInstallment = new LoanInstallment();
        var interest =
            loan.getInterestRate() == null
                ? null
                : loan.getInterestRate()
                    .getFixedRate();

        loneInstallment.setLoan(loan);
        loneInstallment.setInstallmentAmount(loan.getMonthlyInstallment());
        loneInstallment.setExpectedDueDate(
            LocalDate.now()
                .plusMonths(1)
        );
        loneInstallment.setInterestRateAmount(interest);
        loneInstallment.setPaymentStatus(PaymentStatus.UNPAID);

        loanInstallmentRepository.save(loneInstallment);
    }

    private void ensureEmployeeRole(String auth) {
        var role = jwtUtil.extractRole(auth);
        if (!role.equals("employee")) throw new Unauthorized(auth);
    }

    @Transactional
    @Override
    public void rejectLoan(Long loanNumber, String auth) {
        ensureEmployeeRole(auth);

        var loan = loanRepository.findByLoanNumber(loanNumber);

        if (loan.isEmpty()) throw new LoanNotFound();

        if (
            !loan.get()
                .getStatus()
                .equals(LoanStatus.PROCESSING)
        )
            throw new InvalidLoanStatus(
                loan.get()
                    .getStatus()
                    .name()
            );

        loan.get()
            .setStatus(LoanStatus.REJECTED);

        loanRepository.save(loan.get());

    }

    /**
     * Connects the given account to the given loan.
     *
     * @param loanApplicationDto The loan application from which to get the account number.
     * @param newLoan The loan to connect the account to.
     * @param clientEmail The email of the client to check against the account's client.
     *
     * @throws NotAccountOwner If the account number provided in the loan application does not
     *         belong to the client.
     * @throws AccountNotActive If the account is not active.
     */
    private void connectAccountToLoan(
        LoanApplicationDto loanApplicationDto,
        Loan newLoan,
        String clientEmail
    ) {
        Account account =
            accountService.getAccountByAccountNumber(loanApplicationDto.accountNumber());

        if (
            !account.getClient()
                .getEmail()
                .equals(clientEmail)
        ) throw new NotAccountOwner();

        if (!account.isActive()) throw new AccountNotActive();

        newLoan.setAccount(account);
    }

    /**
     * Generates a unique loan number for the given loan. The generated number is in the format
     * <code>YYYYMMXXXX</code>, where <code>YYYY</code> is the year, <code>MM</code> is the month,
     * and <code>XXXX</code> is a unique sequence number.
     *
     * @param newLoan The loan to generate the loan number for.
     */
//     TODO: This needs refactoring.
    private void generateLoanNumber(Loan newLoan) {
        String comb;

        LocalDate dayOfLoanRequest = LocalDate.now();
        if (dayOfLoanRequest.getMonthValue() < 10) {
            comb = dayOfLoanRequest.getYear() + "0" + dayOfLoanRequest.getMonthValue();
        } else {
            comb = dayOfLoanRequest.getYear() + "" + dayOfLoanRequest.getMonthValue();
        }
        while (true) {
            try {
                long random =
                    ThreadLocalRandom.current()
                        .nextLong(0, 100000);
                String loanNumber = String.format("%06d", Integer.parseInt(comb) + random);

                newLoan.setLoanNumber(Long.valueOf(loanNumber));
                loanRepository.save(newLoan);

                break;
            } catch (DataIntegrityViolationException e) {
                LOGGER.warn("Loan with this loan number already exists!");
            }
        }
    }

    /**
     * Creates a new loan request for the given loan and loan application.
     *
     * @param loan The loan to create a loan request for.
     * @param loanApplicationDto The loan application from which to create the loan request.
     */
    private void makeLoanRequest(Loan loan, LoanApplicationDto loanApplicationDto) {
        LoanRequest loanRequest = LoanMapper.INSTANCE.toLoanRequest(loanApplicationDto);
        loanRequest.setCurrency(
            loan.getAccount()
                .getCurrency()
        );
        loanRequest.setLoan(loan);

        loanRequest.setAccount(loan.getAccount());
        loanRequestRepository.save(loanRequest);
    }
}
