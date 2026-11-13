package com.parag.ecom.tests;

import io.qameta.allure.Description;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;

// Prints a clear start/result line for every test, plus a summary at the end
public class TestLogListener implements ITestListener {
    private static final String LINE = "=".repeat(80);

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("START  " + name(result));
        String description = description(result);
        if (description != null) {
            System.out.println("       " + description);
        }
        System.out.println(LINE);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println(">>> PASS  " + name(result) + " (" + duration(result) + " ms)");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println(">>> FAIL  " + name(result) + " (" + duration(result) + " ms)");
        System.out.println("    Reason: " + result.getThrowable().getMessage());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // TestNG marks a failed attempt that is going to be retried as skipped
        if (result.wasRetried()) {
            System.out.println(">>> FAILED ATTEMPT (will retry)  " + name(result) + " (" + duration(result) + " ms)");
            return;
        }
        System.out.println(">>> SKIP  " + name(result));
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println();
        System.out.println(LINE);
        // Runs once per <test> in testng.xml
        System.out.printf("SUMMARY  %s - passed: %d, failed: %d, skipped: %d%n",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
        System.out.println("Report   run:  .\\report   (or: mvn allure:report, then open target\\site\\allure-maven-plugin\\index.html)");
        System.out.println(LINE);
    }

    private static String name(ITestResult result) {
        return result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
    }

    private static String description(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        Description d = method.getAnnotation(Description.class);
        return d == null ? null : d.value();
    }

    private static long duration(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }
}
