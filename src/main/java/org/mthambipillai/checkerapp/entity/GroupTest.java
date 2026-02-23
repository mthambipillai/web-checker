package org.mthambipillai.checkerapp.entity;

import java.util.List;

public class GroupTest extends TestCase {
    private final List<TestCase> testCases;
    private final Runnable failurePostAction;

    public GroupTest(String name, List<TestCase> testCases, Runnable failurePostAction) {
        this.name = name;
        this.testCases = testCases;
        this.failurePostAction = failurePostAction;
    }

    public GroupTest(String name, List<TestCase> testCases) {
        this(name,  testCases, null);
    }

    @Override
    public TestResult isSuccessful() {
        int total = 0;
        System.out.printf("Starting group test '%s'.%n", name);
        for (TestCase testCase : testCases) {
            TestResult result = testCase.isSuccessful();
            total = total + result.getScore();
            if (!result.isSuccess()) {
                if (failurePostAction != null) {
                    failurePostAction.run();
                }
                return new TestResult(false, total);
            }
        }
        return new TestResult(true, total);
    }

    @Override
    public int getTotalExpectedScore() {
        return testCases.stream().mapToInt(TestCase::getTotalExpectedScore).sum();
    }
}
