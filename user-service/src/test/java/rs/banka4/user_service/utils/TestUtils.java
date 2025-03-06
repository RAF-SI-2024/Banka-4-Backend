package rs.banka4.user_service.utils;

import org.mockito.MockedStatic;

import java.util.function.Consumer;

import static org.mockito.Mockito.mockStatic;

public class TestUtils {

    public static void runWithMockedAuth(Consumer<Void> testLogic) {
        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getLoggedUserId).thenReturn("6b105ac4-10fb-4bcf-abfb-96f4916be227");
            testLogic.accept(null);
        }
    }

}
