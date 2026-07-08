package org.ginsim.core.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a service with an alias to define a short name (to be used in script mode for example)
 * 
 * @author Aurelien Naldi
 */
@Documented
@Target(ElementType.TYPE)
@Retention( RetentionPolicy.RUNTIME)
public @interface Alias {
	/**
	 * String NOALIAS definition
	 */
	public static final String NOALIAS = "";

	/**
	 * getter default value
	 * @return the default value NOALIAS
	 */
	String value() default NOALIAS;
}
