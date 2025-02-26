package rs.banka4.user_service.service.impl;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.SecuredUser;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;

import java.util.Optional;

@Service
@Getter
@Setter
public class CustomUserDetailsService implements UserDetailsService {

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    public static String role;

    public CustomUserDetailsService(ClientRepository clientRepository,
                                    EmployeeRepository employeeRepository) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if ("employee".equals(role)) {
            Optional<Employee> employee = employeeRepository.findByEmail(username);
            if (employee.isPresent()) {
                return new SecuredUser(employee.get());
            }
            throw new UsernameNotFoundException("Employee not found with email: " + username);
        }

        Optional<Client> client = clientRepository.findByEmail(username);
        if (client.isPresent()) {
            return new SecuredUser(client.get());
        }

        throw new UsernameNotFoundException("Client not found with email: " + username);
    }
}