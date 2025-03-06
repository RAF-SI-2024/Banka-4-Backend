package rs.banka4.user_service.generator;

import rs.banka4.user_service.dto.ClientContactDto;
import rs.banka4.user_service.models.Client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClientObjectMother {

    public static Client generateClientWithAllAttributes() {
        Client client = new Client();
        client.setId("6b105ac4-10fb-4bcf-abfb-96f4916be227");
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setEmail("user@example.com");
        client.setSavedContacts(new HashSet<>());
        client.setAccounts(new HashSet<>());
        return client;
    }

    public static Client generateClientWithEmptyContacts() {
        Client client = new Client();
        client.setId("6b105ac4-10fb-4bcf-abfb-96f4916be227");
        client.setSavedContacts(Collections.emptySet());
        return client;
    }

    public static ClientContactDto createClientContactDto() {
        return new ClientContactDto("Mehmedalija", "Doe", "444000000000123456");
    }

}
