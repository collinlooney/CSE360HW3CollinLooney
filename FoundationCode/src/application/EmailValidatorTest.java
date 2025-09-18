package application;

import static org.junit.Assert.*;
import org.junit.Test;

public class EmailValidatorTest {

    @Test
    public void testValidSimpleEmail() {
        assertEquals("", EmailValidator.checkForValidEmail("user@example.com"));
    }

    @Test
    public void testValidWithHyphenAndTld() {
        assertEquals("", EmailValidator.checkForValidEmail("john-doe@mail.co"));
    }

    @Test
    public void testEmptyInput() {
        assertEquals("\nEMAIL ERROR: The input is empty",
            EmailValidator.checkForValidEmail(""));
    }

    @Test
    public void testMultipleAtSymbols() {
        assertEquals("\nEMAIL ERROR: Multiple '@' symbols are not allowed.",
            EmailValidator.checkForValidEmail("a@b@c.com"));
    }

    @Test
    public void testMissingAt() {
        assertEquals("\nEMAIL ERROR: Missing '@' symbol.",
            EmailValidator.checkForValidEmail("user.domain.com"));
    }

    @Test
    public void testMissingDotInDomain() {
        assertEquals("\nEMAIL ERROR: Missing '.' in domain.",
            EmailValidator.checkForValidEmail("user@domain"));
    }

    @Test
    public void testInvalidCharInLocal() {
        assertEquals("\nEMAIL ERROR: Invalid character in local part of email.",
            EmailValidator.checkForValidEmail("us,er@domain.com"));
    }

    @Test
    public void testStartsWithAt() {
        assertEquals("\nEMAIL ERROR: Email can't start with '@'.",
            EmailValidator.checkForValidEmail("@domain.com"));
    }

    @Test
    public void testStartsWithDot() {
        assertEquals("\nEMAIL ERROR: Email can't start with '.'.",
            EmailValidator.checkForValidEmail(".user@domain.com"));
    }

    @Test
    public void testOnlyAlphanumBetweenDotsInLocal() {
        assertEquals("\nEMAIL ERROR: Only alphanumeric or \"~`!#$%^&*_-+{}|'?/\" are allowed between '.' in the local part of email.",
            EmailValidator.checkForValidEmail("us..er@domain.com"));
    }

    @Test
    public void testDomainStartsWithHyphen() {
        assertEquals("\nEMAIL ERROR: Domain can't start with a hyphen.",
            EmailValidator.checkForValidEmail("user@-domain.com"));
    }
    @Test
    public void testDomainMustStartWithLetterOrDigit() {
        assertEquals("\nEMAIL ERROR: Domain must start with a letter or digit.",
            EmailValidator.checkForValidEmail("user@.com"));
    }

    @Test
    public void testHyphenAfterDotInDomain() {
        assertEquals("\nEMAIL ERROR: A hyphen can not follow a '.' in the domain.",
            EmailValidator.checkForValidEmail("user@do.-main.com"));
    }

    @Test
    public void testMissingDotBeforeTld() {
        assertEquals("\nEMAIL ERROR: The character immediately before '.' must be alphanumeric.",
            EmailValidator.checkForValidEmail("user@domain-.com"));
    }
    @Test
    public void testInvalidCharinDomain() {
        assertEquals("\nEMAIL ERROR: Invalid char in the domain.",
            EmailValidator.checkForValidEmail("user@domai%n.com"));
    }
    @Test
    public void testTldMustStartWithAlpha() {
        assertEquals("\nEMAIL ERROR: The character immediately after the last '.' must be alphabetic (A-Z or a-z).",
            EmailValidator.checkForValidEmail("user@domain.1com"));
    }

    @Test
    public void testEmailCannotEndWithHyphen() {
        assertEquals("\nEMAIL ERROR: Email can not end with hyphen.",
            EmailValidator.checkForValidEmail("user@domain.co-"));
    }

    @Test
    public void testOnlyAlphabeticOrHyphensAllowedInTld() {
        assertEquals("\nEMAIL ERROR: Only alphabetic characters or hyphens are allowed in TLD.",
            EmailValidator.checkForValidEmail("user@domain.c_m"));
    }
    
}
