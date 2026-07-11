// TASK 2 - reading the custom env variable from inside a test case.
//
// Place this at the top of an existing @Test method in src/test/java/Test1.java
// (e.g. inside test1_element_addition_1()), OR in a @BeforeMethod/@BeforeTest.
// The sample already reads LT_USERNAME / LT_ACCESS_KEY via System.getenv(...),
// so the same mechanism is used here.

String environment = System.getenv("ENVIRONMENT");
System.out.println("ENVIRONMENT (during test execution) => " + environment);

// Example of using it:
if (environment == null) {
    System.out.println("ENVIRONMENT not set - did the YAML 'env:' block get applied?");
} else {
    System.out.println("Running test suite against the '" + environment + "' environment");
}
