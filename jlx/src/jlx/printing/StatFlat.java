package jlx.printing;

public class StatFlat extends AbstractFlat {
	public final String hasReturnedCondition;
	public final String returnedJExpr;
	
	public StatFlat() {
		this(null, null, null);
	}
	
	public StatFlat(AbstractFlat source, String hasReturnedCondition, String returnedJExpr) {
		super(source);
		
		this.hasReturnedCondition = hasReturnedCondition;
		this.returnedJExpr = returnedJExpr;
	}
}

