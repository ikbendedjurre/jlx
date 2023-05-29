package jlx.asal.parsing;

import jlx.asal.j.*;

public abstract class ASALSyntaxTreeAPI {
	private ASALSyntaxTreeAPI parent;
	private ASALSyntaxTree tree;
	
	public ASALSyntaxTreeAPI(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		this.parent = parent;
		this.tree = tree;
	}
	
	public ASALSyntaxTreeAPI getParent() {
		return parent;
	}
	
	public ASALSyntaxTree getTree() {
		return tree;
	}
	
	public <T extends ASALSyntaxTreeAPI> T createAPI(String subtreeName, Class<T> clz, boolean canBeNull) {
		ASALSyntaxTree subtree = tree.get(subtreeName);
		
		if (subtree == null) {
			if (canBeNull) {
				return null;
			}
			
			throw new Error("Unknown subtree (\"" + subtreeName + "\")!");
		}
		
		return subtree.createAPI(this, clz);
	}
	
	public abstract void validateAndCrossRef(JScope context, Class<? extends JType> expectedType) throws ASALException;
	
//	protected String abbreviateVarName(String name, LOD lod) {
//		lod.abbreviate(text)
//		switch (lod) {
//			case ABBREVIATED:
//				return abbreviate(name);
//			case COMPREHENSIVE:
//				return name;
//			case IDENTIFIABLE:
//				return name;
//			default:
//				throw new Error("Unknown LOD: " + lod);
//		}
//	}
}
