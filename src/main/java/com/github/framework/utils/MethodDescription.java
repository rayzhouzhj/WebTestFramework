package com.github.framework.utils;

import org.testng.IInvokedMethod;

import com.github.framework.annotation.values.Author;

/**
 *
 */
public class MethodDescription 
{
    private IInvokedMethod methodName;
    private String description;
    private boolean authorNamePresent;
    private String descriptionMethodName;

    public MethodDescription(IInvokedMethod methodName, String description) 
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
