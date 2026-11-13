package com.parag.ecom.tests;

import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

// Retries a test only when it failed on a network error (timeout, dropped connection).
// A failed assertion is a real result and is never retried.
// Applied to every test via META-INF/services, so it also works with -Dtest=...
public class NetworkRetry implements IRetryAnalyzer, IAnnotationTransformer {
    private static final int MAX_RETRIES = 2;

    private int retries = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retries < MAX_RETRIES && isNetworkError(result.getThrowable())) {
            retries++;
            System.out.println(">>> RETRY " + result.getMethod().getMethodName()
                    + " (" + retries + "/" + MAX_RETRIES + ") after network error: " + result.getThrowable());
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(NetworkRetry.class);
    }

    public static boolean isNetworkError(Throwable t) {
        while (t != null) {
            if (t instanceof IOException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}
