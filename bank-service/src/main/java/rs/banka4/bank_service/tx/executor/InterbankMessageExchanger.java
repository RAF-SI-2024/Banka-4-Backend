package rs.banka4.bank_service.tx.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.banka4.bank_service.tx.data.Message;

@RestController
@RequestMapping("/interbank")
@RequiredArgsConstructor
public class InterbankMessageExchanger {
    private final InterbankTxExecutor interbankExecutor;

    @PostMapping("/interbank")
    public ResponseEntity<?> exchangeMessage(@RequestBody Message receivedMsg) {
        switch (receivedMsg) {
        case Message.CommitTx commit -> {
            interbankExecutor.processCommitOrRollbackMessage(commit);
            return ResponseEntity.noContent()
                .build();
        }
        case Message.RollbackTx rollback -> {
            interbankExecutor.processCommitOrRollbackMessage(rollback);
            return ResponseEntity.noContent()
                .build();
        }

        case Message.NewTx newTx -> {
            return ResponseEntity.ok(interbankExecutor.processNewTxMessage(newTx));
        }
        }
    }
}
