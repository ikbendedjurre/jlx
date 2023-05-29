package jlx.asal.ops;

import java.lang.reflect.Field;
import java.util.List;

import jlx.asal.j.*;
import jlx.asal.parsing.api.ASALStatement;
import jlx.asal.vars.ASALParam;
import jlx.asal.vars.ASALProperty;
import jlx.blocks.ibd1.Type1IBD;
import jlx.common.reflection.ModelException;

public interface ASALOp {
	public String getName();
	public Field getField();
	public Type1IBD getOwner();
	public List<ASALParam> getParams();
	
	/**
	 * Cannot be NULL; when the return type is void, it returns "JVoid.class".
	 */
	public Class<? extends JType> getReturnType();
	public JScope createScope(JScope surroundingScope);
	public void initBody(JScope surroundingScope) throws ModelException;
	public ASALStatement getBody();
	public void attachHelperPulsePort(ASALProperty helperPulsePort);
}
