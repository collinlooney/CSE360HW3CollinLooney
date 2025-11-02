package application;

import static org.junit.Assert.*;
import org.junit.Test;

import databasePart1.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <h2>Authorization JUnit Tests</h2>
 *
 * <p>Deterministic JUnit 4 tests for
 * {@link Authorization#isAdmin(User, databasePart1.DatabaseHelper)}.
 * Tiny in-memory fakes subclass
 * {@link databasePart1.DatabaseHelper} to supply user roles.</p>
 *
 * <h3>Covered Behaviors</h3>
 * <ol>
 *   <li>Admin via in-memory roles</li>
 *   <li>Admin via DB fallback</li>
 *   <li>Null user returns false</li>
 *   <li>No admin in memory nor DB returns false</li>
 *   <li>DB exception is caught → false (fail-closed)</li>
 * </ol>
 *
 * @author Collin Looney
 * @version 1.0
 * @since HW03
 * @see Authorization
 * @see AuthorizationTestingAutomation
 */
public class AuthorizationTest {

    /**
     * Builds a mutable list of roles from varargs.
     *
     * @param rs zero or more roles
     * @return a new {@link ArrayList} of roles (never {@code null})
     */
    private static ArrayList<Role> roles(Role... rs) {
        ArrayList<Role> list = new ArrayList<>();
        if (rs != null) for (Role r : rs) list.add(r);
        return list;
    }

    /**
     * Constructs a {@link User} using the project’s 5-argument constructor.
     * Dummy values are used for name/email/password; only username/roles matter.
     *
     * @param username the username
     * @param roles    in-memory roles (or {@code null})
     * @return a user instance suitable for testing
     */
    private static User makeUser(String username, ArrayList<Role> roles) {
        String name = "Name-" + username;
        String email = username + "@example.com";
        String password = "pw";
        return new User(username, name, email, password, roles);
    }

    /**
     * Minimal in-memory fake DB that maps usernames to role lists.
     * Only {@link #getUserRoles(String)} is overridden because it’s the
     * only dependency used by {@link Authorization#isAdmin(User, databasePart1.DatabaseHelper)}.
     */
    private static final class RolesDb extends DatabaseHelper {
        private final Map<String, ArrayList<Role>> byUser = new HashMap<>();
        /** Registers roles for a username and returns this instance (fluent). */
        public RolesDb put(String user, ArrayList<Role> r) { byUser.put(user, r); return this; }
        /** Returns registered roles for the user, or {@code null} if none. */
        @Override public ArrayList<Role> getUserRoles(String userName) { return byUser.get(userName); }
    }

    /**
     * Fake DB that always throws; verifies that {@code isAdmin} fails closed (returns {@code false}).
     */
    private static final class ThrowDb extends DatabaseHelper {
        @Override public ArrayList<Role> getUserRoles(String userName) {
            throw new RuntimeException("DB down");
        }
    }

    /** Test 1: in-memory roles contain {@link Role#ADMIN} → {@code true} without DB access. */
    @Test
    public void test01_IsAdmin_true_viaUserRoles() {
        User collin = makeUser("collin", roles(Role.ADMIN));
        DatabaseHelper db = new RolesDb(); // DB irrelevant for this path
        boolean isAdmin = Authorization.isAdmin(collin, db);
        assertTrue("User with ADMIN in in-memory roles should be admin", isAdmin);
    }

    /** Test 2: DB provides {@link Role#ADMIN} when in-memory roles are null/empty → {@code true}. */
    @Test
    public void test02_IsAdmin_true_viaDatabase() {
        User billy = makeUser("billy", null); // no in-memory roles
        DatabaseHelper db = new RolesDb().put("billy", roles(Role.ADMIN));
        boolean isAdmin = Authorization.isAdmin(billy, db);
        assertTrue("DB returning ADMIN should make user admin", isAdmin);
    }

    /** Test 3: {@code null} user must evaluate to {@code false}. */
    @Test
    public void test03_IsAdmin_false_nullUser() {
        DatabaseHelper db = new RolesDb().put("anyone", roles(Role.ADMIN));
        boolean isAdmin = Authorization.isAdmin(null, db);
        assertFalse("Null user should not be admin", isAdmin);
    }

    /** Test 4: Neither in-memory nor DB grant admin → {@code false}. */
    @Test
    public void test04_IsAdmin_false_noAdminAnywhere() {
        User tina = makeUser("tina", roles());               // empty = non-admin
        DatabaseHelper db = new RolesDb().put("tina", roles()); // empty = non-admin
        boolean isAdmin = Authorization.isAdmin(tina, db);
        assertFalse("Neither user nor DB provide ADMIN", isAdmin);
    }

    /** Test 5: DB throws an exception → method catches and returns {@code false} (fail-closed). */
    @Test
    public void test05_IsAdmin_false_dbThrows() {
        User jimmy = makeUser("jimmy", null);
        DatabaseHelper db = new ThrowDb();
        boolean isAdmin = Authorization.isAdmin(jimmy, db);
        assertFalse("On DB error, isAdmin should fail closed (false)", isAdmin);
    }
}
