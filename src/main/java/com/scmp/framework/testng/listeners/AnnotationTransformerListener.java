package com.scmp.framework.testng.listeners;

import com.scmp.framework.annotations.testrail.TestRailTestCase;
import com.scmp.framework.context.RunTimeContext;
import com.scmp.framework.testrail.models.TestRunTest;
import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static com.scmp.framework.utils.Constants.FILTERED_TEST_OBJECT;

public class AnnotationTransformerListener implements IAnnotationTransformer {
	@Override
	public void transform(
			ITestAnnotation iTestAnnotation, Class aClass, Constructor constructor, Method method) {

		// Handle Retry
		IRetryAnalyzer retry = iTestAnnotation.getRetryAnalyzer();
		if (retry==null) {
			iTestAnnotation.setRetryAnalyzer(RetryAnalyzer.class);
		}
	}
}
