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
    
    // Setters
    // Sets the roles of the user.
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
    // Sets name of user
    public void setName(String name) {
        this.name = name;
    }
    // Sets email of user
    public void setEmail(String email) {
        this.email = email;
    }

    // Getters
    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public List<Role> getRoles() { return roles; }

    // Helper to check if a user has Admin role
    public boolean hasAdmin() {
        return roles.contains(Role.ADMIN);
    }

    // Helper methods for converting Roles to/from storage format
    // Converts roles to storage format "1,2,3"
    public String rolesToString() {
        if (roles == null || roles.isEmpty()) {
            return "";
        }
        String res = "";
        for (int i = 0; i < roles.size(); i++) {
            result += roles.get(i).toInt();
            if (i < roles.size() - 1) {
                result += ",";
            }
        }
        return res;
    }

    // Convert from DB storage form of roles ("1,2,3") to List<Role>
    public static ArrayList<Role> rolesFromString(String s) {
        ArrayList<Role> roles = new ArrayList<>();

        String[] ps = s.split(",");
        for (int i = 0; i < ps.length; i++) {
            int code = Integer.parseInt(ps[i].trim());
            roles.add(Role.fromInt(code));
        }

        return roles;
    }


    public void ViewHomeScreen(Role role) {
        // todo
    }
}
