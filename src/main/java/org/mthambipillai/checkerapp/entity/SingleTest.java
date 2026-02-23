package org.mthambipillai.checkerapp.entity;

import java.util.function.Supplier;

public class SingleTest extends TestCase {
    private final Supplier<Boolean> tester;

    public SingleTest(String name, Supplier<Boolean> tester) {
        this.name = name;
        this.tester = tester;
    }

    @Override
    public TestResult isSuccessful() {
        System.out.printf("Starting single test '%s'.%n", name);
        boolean success = tester.get();
        System.out.printf("single test '%s' %s.%n", name, success ? "passed" : "failed");
        return new TestResult(success, success ? 1 : 0);
    }

    @Override
    public int getTotalExpectedScore() {
        return 1;
    }
}
