package rs.banka4.testlib.utils;

import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

/**
 * A variety of tokens that do not expire to use in tests in services where logging in is more
 * annoying. All tokens in this class are signed with {@link #TERRIBLE_SECRET}, and are valid
 * forever.
 */
public class JwtPlaceholders {
    /**
     * Version 3 valid access token for an admin employee.
     */
    public static final String V3_VALID_ADMIN_EMPLOYEE_TOKEN =
        "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhNGJmMzcwZS0yMTI5LTQxMTYtOTI0My0wYzRlYWQwZmU0M2UiLCJwcml2aWxlZ2VzIjpbIkFETUlOIl0sInR5cCI6IkFDQ0VTUyIsInZlciI6Mywicm9sZSI6IkVNUExPWUVFIn0.E0PwDfFI1vemqjmr6jPDvCN-N288iySu2ao1B90MBIhgpEAVfTP_adnCVH0PfEBqgdxyYfZeP7V9FAtPfVg0eA";
    /**
     * Latest-version valid access token for an admin employee.
     */
    public static final String ADMIN_EMPLOYEE_TOKEN = V3_VALID_ADMIN_EMPLOYEE_TOKEN;

    /**
     * Version 3 valid access token for a client.
     */
    public static final String V3_VALID_CLIENT_TOKEN =
        "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhNGJmMzcwZS0yMTI5LTQxMTYtOTI0My0wYzRlYWQwZmU0M2UiLCJwcml2aWxlZ2VzIjpbXSwidHlwIjoiQUNDRVNTIiwidmVyIjozLCJyb2xlIjoiQ0xJRU5UIn0.Kf8IGsn0PdR64UORHCPtOvH-aSiHZeOwvrVCYwnT7uLPuV0Gwbm9kcwGmAxFyxHB8SAUTWS2j8SqXMhgTMuslw";
    /**
     * Latest-version valid access token for a client.
     */
    public static final String CLIENT_TOKEN = V3_VALID_CLIENT_TOKEN;
    /**
     * Id for user with CLIENT_TOKEN
     */
    public static final UUID CLIENT_ID = UUID.fromString("a4bf370e-2129-4116-9243-0c4ead0fe43e");

    public static final byte[] TERRIBLE_SECRET = new byte[64];
    public static final String TERRIBLE_SECRET_B64 =
        "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";

    static {
        assert Arrays.equals(
            Base64.getDecoder()
                .decode(TERRIBLE_SECRET_B64),
            TERRIBLE_SECRET
        );
    }
}
