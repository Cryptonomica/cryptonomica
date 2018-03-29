package net.cryptonomica.forms;

import java.io.Serializable;

/**
 * general search for user profiles
 */
public class GeneralSearchUserProfilesForm implements Serializable {
    String firstName;
    String lastName;
    String email;

    public GeneralSearchUserProfilesForm() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
