package com.github.framework.report;

import org.testng.IInvokedMethod;

import com.github.framework.annotations.Author;

public class ExtentTestDescription 
{
    private IInvokedMethod methodName;
    private String description;
    private boolean authorNamePresent;
    private String descriptionMethodName;

    public ExtentTestDescription(IInvokedMethod methodName, String description) 
    {
        this.methodName = methodName;
        this.description = description;
        
        init();
    }

    public boolean isAuthorNamePresent() 
    {
        return authorNamePresent;
    }
    
    public String getDescriptionMethodName() 
    {
        return descriptionMethodName;
    }

    private void init() 
    {
        if (description.isEmpty())
        {
            descriptionMethodName = methodName.getTestMethod().getMethodName();
        } 
        else 
        {
            descriptionMethodName = methodName.getTestMethod().getMethodName() + "[" + description + "]";
        }
        
        if (methodName.getTestMethod().getConstructorOrMethod().getMethod().getAnnotation(Author.class) != null) 
        {
            authorNamePresent = true;
        } 
        else 
        {
            authorNamePresent = false;
        }
    }
}
