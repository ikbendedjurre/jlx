package jlx.asal.j;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JTypeName {
	public String s();
	
	//We use this annotation to define a custom name for a JType.
	//This is the same as JTypeTextify, but more simple (without the special format, for example).
}
