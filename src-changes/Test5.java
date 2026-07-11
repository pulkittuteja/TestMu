// TASK 3 - a test that fails on purpose (deterministic hard assertion failure,
// NOT flaky/random). Add this file to src/test/java/Test5.java in the sample repo
// (default package, matching Test1..Test4), then register it in xml/testng_win.xml
// (see testng_win.xml.snippet) so autosplit discovery picks it up as "Test_5".
//
// No Selenium/WebDriver is used here on purpose: the failure is guaranteed and
// isolated to the assertion, so the retry behaviour is unambiguous in the logs.

import org.testng.Assert;
import org.testng.annotations.Test;

public class Test5 {

    @Test
    public void intentionalFailure() {
        System.out.println("Test5.intentionalFailure - this assertion will fail on purpose");

        // Also demonstrates the Task 2 env var during execution of the failing test.
        System.out.println("ENVIRONMENT (during Test5) => " + System.getenv("ENVIRONMENT"));

        Assert.fail("Intentional hard assertion failure to demonstrate HyperExecute retryOnFailure");
    }
}
