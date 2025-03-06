package rs.banka4.user_service.generator;

import rs.banka4.user_service.models.Account;

import java.util.UUID;

public class AccountObjectMother {

    public static Account generateBasicAccount() {
        Account account = new Account();
        account.setId(UUID.fromString("155de92c-4a16-41bf-89c4-5997a53a0009"));
        account.setAccountNumber("444000000000123456");
        return account;
    }

}
