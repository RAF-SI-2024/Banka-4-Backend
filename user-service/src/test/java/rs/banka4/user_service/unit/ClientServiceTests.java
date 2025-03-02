package rs.banka4.user_service.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.ClientDto;
import rs.banka4.user_service.exceptions.NotAuthenticated;
import rs.banka4.user_service.exceptions.NotFound;
import rs.banka4.user_service.generator.ClientObjectMother;
import rs.banka4.user_service.mapper.BasicClientMapper;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.models.Privilege;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.impl.ClientServiceImpl;
import rs.banka4.user_service.utils.JwtUtil;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class ClientServiceTests {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private BasicClientMapper basicClientMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ClientServiceImpl clientService;

    private static final String AUTH_HEADER = "Bearer validToken";
    private static final String TOKEN = "validToken";
    private static final String CLIENT_EMAIL = "test@example.com";
    private static final Client CLIENT = new Client();  // Assuming a default constructor
    private static final ClientDto CLIENT_DTO = ClientObjectMother.generateExampleClientDto();

    @BeforeEach
    void setUp() {
        CLIENT.setEmail(CLIENT_EMAIL);
    }

    @Test
    void testGetMe_ValidToken_ReturnsClientDto() {
        when(jwtUtil.extractUsername(TOKEN)).thenReturn(CLIENT_EMAIL);
        when(jwtUtil.isTokenExpired(TOKEN)).thenReturn(false);
        when(jwtUtil.isTokenInvalidated(TOKEN)).thenReturn(false);

        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(CLIENT));
        when(basicClientMapper.entityToDto(CLIENT)).thenReturn(CLIENT_DTO);

        ResponseEntity<ClientDto> response = clientService.getMe(AUTH_HEADER);

        assertNotNull(response);
        assertEquals(CLIENT_DTO, response.getBody());
    }

    @Test
    void testGetMe_ExpiredToken_ThrowsNotAuthenticated() {
        when(jwtUtil.extractUsername(TOKEN)).thenReturn(CLIENT_EMAIL);
        when(jwtUtil.isTokenExpired(TOKEN)).thenReturn(true);

        assertThrows(NotAuthenticated.class, () -> clientService.getMe(AUTH_HEADER));
    }

    @Test
    void testGetMe_InvalidatedToken_ThrowsNotAuthenticated() {
        when(jwtUtil.extractUsername(TOKEN)).thenReturn(CLIENT_EMAIL);
        when(jwtUtil.isTokenExpired(TOKEN)).thenReturn(false);
        when(jwtUtil.isTokenInvalidated(TOKEN)).thenReturn(true);

        assertThrows(NotAuthenticated.class, () -> clientService.getMe(AUTH_HEADER));
    }

    @Test
    void testGetMe_ClientNotFound_ThrowsNotFound() {
        when(jwtUtil.extractUsername(TOKEN)).thenReturn(CLIENT_EMAIL);
        when(jwtUtil.isTokenExpired(TOKEN)).thenReturn(false);
        when(jwtUtil.isTokenInvalidated(TOKEN)).thenReturn(false);
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFound.class, () -> clientService.getMe(AUTH_HEADER));
    }
}
