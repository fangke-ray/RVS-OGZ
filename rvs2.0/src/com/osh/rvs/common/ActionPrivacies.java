package com.osh.rvs.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface ActionPrivacies {

	/**
	 * 容许的权限编号表
	 * String permit(Exception e);
	 */
	RvsPrivacy[] permit();

}
