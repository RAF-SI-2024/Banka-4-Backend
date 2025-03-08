package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactDto;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactRequest;
import rs.banka4.user_service.service.abstraction.ClientContactService;

import java.util.UUID;

@RestController
@RequestMapping("/client-contact")
@RequiredArgsConstructor
public class ClientContactController {

    private final ClientContactService clientContactService;

    @GetMapping
    public ResponseEntity<Page<ClientContactDto>> getAllClientContacts(Pageable pageable) {
        return ResponseEntity.ok(clientContactService.getAllClientContacts(pageable));
    }

    @PostMapping
    public ResponseEntity<Void> createClientContact(@RequestBody @Valid ClientContactRequest request) {
        clientContactService.createClientContact(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateClientContact(@PathVariable UUID id, @RequestBody ClientContactRequest request) {
        clientContactService.updateClientContact(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClientContact(@PathVariable UUID id) {
        clientContactService.deleteClientContact(id);
        return ResponseEntity.ok().build();
    }
}
