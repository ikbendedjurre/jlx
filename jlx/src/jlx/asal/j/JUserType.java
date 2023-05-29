package jlx.asal.j;

import java.lang.reflect.InvocationTargetException;

public class JUserType<T extends JType> extends JType {
	public JBool eq(JUserType<? extends T> other) {
		return new JBool.EQ(this, other);
	}
	
	public JBool eq(Class<? extends T> value) {
		T other;
		
		try {
			other = value.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new Error("Should not happen!", e);
		}
		
		return new JBool.EQ(this, other);
	}
	
//	public final JBool neq(JUserType<? extends T> other) {
//		return XModel.not(eq(other));
//	}
//	
//	public final JBool neq(Class<? extends T> value) {
//		return XModel.not(eq(value));
//	}
}
