package application;
import application.Role;
import java.util.*;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, 
 * name, email, password, and roles.
 */
public class User {
    // userName is used when signing in and as a key in storage
    private String userName;
    private String name;
    private String email;
    private String password;
    private List<Role> roles;

    // Constructor to initialize a new User object with 
    // userName, name, email, password, and roles.
    public User(
        String userName,
        String name,
        String email,
        String password,
        List<Role> roles
    ) {
        this.userName = userName;
        this.name = name;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }
    
    // Sets the roles of the user.
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public List<Role> getRoles() { return roles; }

    public void ViewHomeScreen(Role role) {
        // todo
    }
}
