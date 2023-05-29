package jlx.asal.j;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JTypeExpr {
	//Empty.
	
	//We use this annotation to indicate that a type constructor is NOT used for "regular values" (i.e. closed).
}
