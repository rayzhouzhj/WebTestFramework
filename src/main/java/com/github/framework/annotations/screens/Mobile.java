package com.github.framework.annotations.screens;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD}) 
@Retention(RetentionPolicy.RUNTIME)
public @interface Mobile {
	DeviceName name() default DeviceName.iPhoneX;
	int width() default 375;
	int height() default 812;
}
