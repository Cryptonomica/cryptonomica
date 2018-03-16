package net.cryptonomica.forms;

import java.io.Serializable;

/**
 *
 */
public class GetUsersKeysByUserIdForm implements Serializable {

    String userId; // google user id = cryptonomica user id

    public GetUsersKeysByUserIdForm() {
    }

    public GetUsersKeysByUserIdForm(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
