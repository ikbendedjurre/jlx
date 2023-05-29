package jlx.asal.j;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JTypeTextify {
	public String format();
	
	//We use this annotation to define a custom to-string conversion for a JType.
}
