package application;

import databasePart1.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <h2>Authorization Testing Automation</h2>
 *
 * <p>A standalone test harness that verifies the behavior of
 * {@link Authorization#isAdmin(User, databasePart1.DatabaseHelper)} using
 * simulated users and a fake in-memory database. The program prints PASS/FAIL
 * results and a final summary to the console.</p>
 *
 * <h3>Test Goals</h3>
 * <ul>
 *   <li>Validate that users with {@link Role#ADMIN} in memory are admins.</li>
 *   <li>Validate that users can gain admin via database roles.</li>
 *   <li>Validate that {@code null} users are rejected.</li>
 *   <li>Validate that users without admin in either memory or DB are rejected.</li>
 *   <li>Validate that a DB failure is handled gracefully (fail-closed = {@code false}).</li>
 * </ul>
 *
 * <p>This class is intentionally <strong>not</strong> a JUnit test—it serves as a
 * mainline automation script for environments without JUnit integration.</p>
 *
 * @author Collin Looney
 * @version 1.0
 * @since HW03
 * @see Authorization
 * @see AuthorizationTest
 */
public final class AuthorizationTestingAutomation {

    /**
     * Builds a mutable list of roles from varargs.
     *
     * @param rs zero or more {@link Role}s
     * @return a list containing the given roles (never {@code null})
     */
    private static ArrayList<Role> roles(Role... rs) {
        ArrayList<Role> list = new ArrayList<>();
        if (rs != null) for (Role r : rs) list.add(r);
        return list;
    }

    /**
     * Constructs a {@link User} using the project’s 5-argument constructor.
     * Dummy values are used for name, email, and password.
     *
     * @param username the username
     * @param roles    in-memory roles, or {@code null} for “no roles”
     * @return a {@link User} object suitable for testing
     */
    private static User makeUser(String username, ArrayList<Role> roles) {
        String name = "Name-" + username;
        String email = username + "@example.com";
        String password = "pw";
        return new User(username, name, email, password, roles);
    }

    /**
     * Minimal in-memory fake database that maps usernames to role lists.
     * Only {@link #getUserRoles(String)} is overridden, as it’s the only
     * dependency used by {@link Authorization#isAdmin(User, databasePart1.DatabaseHelper)}.
     */
    private static final class RolesDb extends DatabaseHelper {
        private final Map<String, ArrayList<Role>> byUser = new HashMap<>();

        /**
         * Registers roles for a given username.
         *
         * @param user the username
         * @param r    the list of roles (may be empty)
         * @return this instance, to allow chaining
         */
        public RolesDb put(String user, ArrayList<Role> r) {
            byUser.put(user, r);
            return this;
        }

        /** Returns the registered roles for a username, or {@code null} if none exist. */
        @Override
        public ArrayList<Role> getUserRoles(String userName) {
            return byUser.get(userName);
        }
    }

    /**
     * A DB fake that always throws from {@link #getUserRoles(String)} to simulate outages.
     * Used to verify that {@code isAdmin} fails closed (returns {@code false}).
     */
    private static final class ThrowDb extends DatabaseHelper {
        @Override
        public ArrayList<Role> getUserRoles(String userName) {
            throw new RuntimeException("DB down");
        }
    }

    /**
     * Prints a single formatted result line for a test case.
     *
     * @param name short name of the test case
     * @param ok   {@code true} for pass; {@code false} for fail
     */
    private static void check(String name, boolean ok) {
        System.out.println((ok ? "[PASS] " : "[FAIL] ") + name);
    }

    /**
     * Program entry point. Builds test users, configures the fake database,
     * runs all authorization checks (including a DB-failure simulation),
     * prints results, and exits with code 0 on success.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        System.out.println("==== Authorization Testing Automation ====");

        // Arrange test users
        User collin = makeUser("collin", roles(Role.ADMIN)); // In-memory admin
        User billy  = makeUser("billy",  roles());           // Empty roles
        User tina   = makeUser("tina",   null);              // Null roles

        // Arrange fake database: grants Billy admin, denies Tina
        RolesDb db = new RolesDb()
                .put("billy", roles(Role.ADMIN))
                .put("tina",  roles());

        int total = 0;
        int passed = 0;

        // 1) Admin via in-memory roles
        total++;
        boolean ok1 = Authorization.isAdmin(collin, db);
        check("isAdmin via in-memory roles", ok1);
        if (ok1) passed++;

        // 2) Admin via database roles
        total++;
        boolean ok2 = Authorization.isAdmin(billy, db);
        check("isAdmin via DB roles", ok2);
        if (ok2) passed++;

        // 3) Null user should not be admin
        total++;
        boolean ok3 = !Authorization.isAdmin(null, db);
        check("isAdmin false for null user", ok3);
        if (ok3) passed++;

        // 4) Null in-memory roles + non-admin DB → false
        total++;
        boolean ok4 = !Authorization.isAdmin(tina, db);
        check("isAdmin false fallback", ok4);
        if (ok4) passed++;

        // 5) DB throws → isAdmin must fail closed (return false)
        total++;
        DatabaseHelper throwingDb = new ThrowDb();
        boolean ok5 = !Authorization.isAdmin(billy, throwingDb); 
        check("isAdmin false when DB throws", ok5);
        if (ok5) passed++;

        // Summary
        System.out.printf("%nSummary: %d/%d passed%n", passed, total);
        System.exit(passed == total ? 0 : 1);
    }
}
