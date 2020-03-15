package com.rayzhou.framework.testng.model;

import java.lang.reflect.Method;

import com.rayzhou.framework.annotations.Authors;
import com.rayzhou.framework.annotations.ClassDescription;
import org.testng.IInvokedMethod;
import org.testng.annotations.Test;

public class TestInfo {
    private IInvokedMethod invokedMethod;
    private Method declaredMethod;
    private String description;

    public TestInfo(IInvokedMethod methodName) {
        this.invokedMethod = methodName;
        this.declaredMethod = this.invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
        this.description = this.declaredMethod.getAnnotation(Test.class).description();
    }

    public IInvokedMethod getInvokedMethod(){
        return this.invokedMethod;
    }

    public Method getDeclaredMethod(){
        return this.declaredMethod;
    }

    public String getClassName() {
        return this.declaredMethod.getDeclaringClass().getSimpleName();
    }

    public String getClassDescription() {
        ClassDescription description = this.declaredMethod.getDeclaringClass().getAnnotation(ClassDescription.class);

        return description == null ? "" : description.value();
    }

    public String getMethodName() {
        return this.declaredMethod.getName();
    }

    public boolean isTestMethod() {
        return this.declaredMethod.getAnnotation(Test.class) != null;
    }

    public String[] getAuthorName() {
        return declaredMethod.getAnnotation(Authors.class) == null ? null : declaredMethod.getAnnotation(Authors.class).name();
    }

    public String getTestName() {
        String testName = "";

        if (this.description == null || this.description.isEmpty()) {
            testName = this.declaredMethod.getName();
        } else {
            testName = this.declaredMethod.getName() + "[" + this.description + "]";
        }

        return testName;
    }
}
