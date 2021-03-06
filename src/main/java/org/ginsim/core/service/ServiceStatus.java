package org.ginsim.core.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention( RetentionPolicy.RUNTIME)
public @interface ServiceStatus {

//	public static final int UNDER_DEVELOPMENT = 0;
//	public static final int RELEASED = 1;
//	public static final int DEPRECATED = -1;
//	public static final int TOOLKIT = 2;
//    public static final int UNKNOWN = -2;

	EStatus value();

}

