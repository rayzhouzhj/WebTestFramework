package com.rayzhou.framework.testng.model;

import java.lang.reflect.Method;

import com.rayzhou.framework.annotations.Authors;
import com.rayzhou.framework.annotations.ClassDescription;
import com.rayzhou.framework.annotations.ClassGroups;
import org.testng.IInvokedMethod;
import org.testng.annotations.Test;

public class TestInfo {
    private IInvokedMethod invokedMethod;
    private Method declaredMethod;

    public TestInfo(IInvokedMethod methodName) {
        this.invokedMethod = methodName;
        this.declaredMethod = this.invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
    }

    public IInvokedMethod getInvokedMethod() {
        return this.invokedMethod;
    }

    public Method getDeclaredMethod() {
        return this.declaredMethod;
    }

    public String getClassName() {
        return this.declaredMethod.getDeclaringClass().getSimpleName();
    }

    public String[] getClassGroups() {
        ClassGroups groups = this.declaredMethod.getDeclaringClass().getAnnotation(ClassGroups.class);
        return groups == null ? null : groups.groups();
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

    public String[] getAuthorNames() {
        return declaredMethod.getAnnotation(Authors.class) == null ? null : declaredMethod.getAnnotation(Authors.class).name();
    }

    public String getTestName() {
        String dataProvider = null;
        Object dataParameter = this.invokedMethod.getTestResult().getParameters();
        if (((Object[]) dataParameter).length > 0) {
            dataProvider = (String) ((Object[]) dataParameter)[0];
        }

        return dataProvider == null ? this.declaredMethod.getName() : this.declaredMethod.getName() + " [" + dataProvider + "]";
    }

    public String getTestMethodDescription() {
        return this.declaredMethod.getAnnotation(Test.class).description();
    }

    public String[] getTestGroups() {
        return this.invokedMethod.getTestMethod().getGroups();
    }
}
