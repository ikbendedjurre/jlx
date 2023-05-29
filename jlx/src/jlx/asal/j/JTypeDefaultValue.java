package jlx.asal.j;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JTypeDefaultValue {
	//We use this annotation to indicate that a constructor is the constructor of the default value of its JType.
}
