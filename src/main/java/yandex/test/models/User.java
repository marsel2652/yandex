package yandex.test.models;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class User {
    private long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private int userStatus;
}
