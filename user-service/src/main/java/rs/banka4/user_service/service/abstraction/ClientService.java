package rs.banka4.user_service.service.abstraction;

import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.ClientDto;

public interface ClientService {
    ResponseEntity<ClientDto> getMe(String authorization);
}
