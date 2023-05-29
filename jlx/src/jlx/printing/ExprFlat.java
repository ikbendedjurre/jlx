package jlx.printing;

public class ExprFlat extends AbstractFlat {
	public final String constructedJExpr;
	
	public ExprFlat() {
		this(null, null);
	}
	
	public ExprFlat(AbstractFlat source, String constructedJExpr) {
		super(source);
		
		this.constructedJExpr = constructedJExpr;
	}
}

