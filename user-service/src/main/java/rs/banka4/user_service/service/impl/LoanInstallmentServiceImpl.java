package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import rs.banka4.rafeisen.common.exceptions.jwt.Unauthorized;
import rs.banka4.user_service.domain.loan.db.LoanInstallment;
import rs.banka4.user_service.domain.loan.dtos.LoanInstallmentDto;
import rs.banka4.user_service.domain.loan.mapper.LoanMapper;
import rs.banka4.user_service.domain.loan.specification.LoanSpecification;
import rs.banka4.user_service.exceptions.loan.LoanNotFound;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.LoanInstallmentRepository;
import rs.banka4.user_service.repositories.LoanRepository;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.service.abstraction.LoanInstallmentService;

@RequiredArgsConstructor
@Service
public class LoanInstallmentServiceImpl implements LoanInstallmentService {
    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final JwtService jwtService;
    private final ClientRepository clientRepository;

    @Override
    public Page<LoanInstallmentDto> getInstallmentsForLoan(
        Long loanNumber,
        int page,
        int size,
        String auth
    ) {
        ensureClientRole(auth);
        var clientId = jwtService.extractUserId(auth);
        var client =
            clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFound(clientId.toString()));
        var loan =
            loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(LoanNotFound::new);
        if (
            !loan.getAccount()
                .getClient()
                .equals(client)
        ) {
            throw new Unauthorized(auth);
        }

        Page<LoanInstallment> pages =
            loanInstallmentRepository.findAll(
                LoanSpecification.findPaidAndNextUpcomingInstallment(loan.getId()),
                PageRequest.of(page, size)
            );

        return pages.map(LoanMapper.INSTANCE::toDto);
    }

    private void ensureClientRole(String auth) {
        var role = jwtService.extractRole(auth);
        if (!role.equalsIgnoreCase("client")) throw new Unauthorized(auth);
    }
}
