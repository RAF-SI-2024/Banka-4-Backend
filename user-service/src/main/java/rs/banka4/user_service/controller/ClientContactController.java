package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactDto;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactRequest;
import rs.banka4.user_service.service.abstraction.ClientContactService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/client-contact")
@RequiredArgsConstructor
public class ClientContactController {

    private final ClientContactService clientContactService;

    @GetMapping
    public ResponseEntity<Page<ClientContactDto>> getAllClientContacts(Authentication auth, Pageable pageable) {
        return ResponseEntity.ok(clientContactService.getAllClientContacts(auth.getCredentials().toString(), pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ClientContactDto>> getListContact(Authentication auth) {
        return ResponseEntity.ok(clientContactService.getClientContacts(auth.getCredentials().toString()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientContactDto> getSpecificClientContact(Authentication auth, @PathVariable UUID id) {
        return ResponseEntity.ok(clientContactService.getSpecificClientContact(auth.getCredentials().toString(), id));
    }

    @PostMapping
    public ResponseEntity<Void> createClientContact(Authentication auth, @RequestBody @Valid ClientContactRequest request) {
        clientContactService.createClientContact(auth.getCredentials().toString(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateClientContact(Authentication auth, @PathVariable UUID id, @RequestBody ClientContactRequest request) {
        clientContactService.updateClientContact(auth.getCredentials().toString(), id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClientContact(Authentication auth, @PathVariable UUID id) {
        clientContactService.deleteClientContact(auth.getCredentials().toString(), id);
        return ResponseEntity.ok().build();
    }
}
