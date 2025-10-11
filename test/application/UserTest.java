package application;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;

public class UserTest {

    @Test
    public void constructorWiresFields_andStoresRoles() {
        
    	User u = new User(
            "skyler", "Skyler", "carwashlady@aol.com", "pw",
            List.of(Role.ADMIN, Role.BASIC_USER, Role.STUDENT)
        );

        assertEquals("skyler", u.getUserName());
        assertEquals("Skyler", u.getName());
        assertEquals("carwashlady@aol.com", u.getEmail());
        assertEquals("pw", u.getPassword());
        assertEquals(3, u.getRoles().size());
        assertTrue(u.getRoles().contains(Role.ADMIN));
        assertTrue(u.getRoles().contains(Role.BASIC_USER));
        assertTrue(u.getRoles().contains(Role.STUDENT));
    }
    
    
}