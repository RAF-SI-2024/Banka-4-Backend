package rs.banka4.user_service.generator;

import rs.banka4.user_service.models.Client;

import java.util.Set;

public class ClientObjectMother {

    public static Client generateClientWithAllAttributes() {
        Client client = new Client();
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setEmail("user@example.com");
        client.setSavedContacts(Set.of());
        return client;
    }

}
