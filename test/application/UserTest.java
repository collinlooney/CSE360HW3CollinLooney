package application;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;

public class UserTest {

    // Make sure the constructor wires fields and saves the roles we pass in
    @Test
    public void constructorWiresFields_andStoresRoles() {
        User u = new User(
            "skyler", "Skyler", "carwash@aol.com", "pw",
            List.of(Role.ADMIN, Role.BASIC_USER, Role.STUDENT)
        );

        assertEquals("skyler", u.getUserName());
        assertEquals("Skyler", u.getName());
        assertEquals("carwash@aol.com", u.getEmail());
        assertEquals("pw", u.getPassword());
        assertEquals(3, u.getRoles().size());
        assertTrue(u.getRoles().contains(Role.ADMIN));
        assertTrue(u.getRoles().contains(Role.BASIC_USER));
        assertTrue(u.getRoles().contains(Role.STUDENT));
    }

    // rolesToString should spit out "1,2,3" in the same order as the roles list. Will add more roles to test as we add them
    @Test
    public void rolesToString_formatsCsvOfRoleCodes_inOrder() {
        User u = new User(
            "skyler", "Skyler", "carwash@aol.com", "pw",
            List.of(Role.ADMIN, Role.BASIC_USER, Role.STUDENT)
        );
        assertEquals("1,2,3", u.rolesToString());
    }

    // rolesToString should return an empty string if there are no roles
    @Test
    public void rolesToString_handlesEmptyList() {
        User u = new User("nobody", "Nobody", "n@ex.com", "pw", List.of());
        assertEquals("", u.rolesToString());
    }

    // rolesFromString should turn "1,2,3" back into the matching roles, same order
    @Test
    public void rolesFromString_parsesCsvToRoles_inOrder() {
        var roles = User.rolesFromString("1,2,3");
        assertEquals(List.of(Role.ADMIN, Role.BASIC_USER, Role.STUDENT), roles);
    }

  
}
