package rs.banka4.bank_service.integration;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import rs.banka4.bank_service.tx.data.TransactionVote;
import rs.banka4.testlib.integration.DbEnabledTest;

@SpringBootTest
@DbEnabledTest
public class TxDataTests {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void test_that_the_decoded_vote_format_is_ok() throws JsonMappingException,
        JsonProcessingException {
        final var json = """
            {
                "vote": "YES"
            }
            """;

        assertThat(objectMapper.readValue(json, TransactionVote.class)).isEqualTo(
            new TransactionVote.Yes()
        );
    }
}
