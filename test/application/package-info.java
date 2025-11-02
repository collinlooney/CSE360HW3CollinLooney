/**
 * <h2>Application Test Package</h2>
 *
 * <p>Automated tests and mainlines for authorization logic. This package
 * includes both a JUnit suite and a standalone, human-readable mainline:</p>
 *
 * <ul>
 *   <li>{@link application.AuthorizationTest} — JUnit 4 unit tests</li>
 *   <li>{@link application.AuthorizationTestingAutomation} — runnable mainline</li>
 * </ul>
 *
 * <h3>How to Run</h3>
 * <pre>{@code
 * // JUnit:
 * Right-click AuthorizationTest.java -> Run As -> JUnit Test
 *
 * // Mainline:
 * Right-click AuthorizationTestingAutomation.java -> Run As -> Java Application
 * }</pre>
 *
 * <h4>Scope</h4>
 * <p>Exercises {@link application.Authorization}’s ability to discern administrative
 * authorization from both in-memory and database-provided roles, including defensive
 * behavior on null users and database errors.</p>
 *
 * @since HW03
 */
package application;
