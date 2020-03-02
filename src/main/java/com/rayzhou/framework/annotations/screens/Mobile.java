package com.rayzhou.framework.annotations.screens;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD}) 
@Retention(RetentionPolicy.RUNTIME)
public @interface Mobile {
	DeviceName device() default DeviceName.OtherDevice;
	int width() default 375; 	// DeviceName.iPhoneX.width
	int height() default 812;	// DeviceName.iPhoneX.height
}
