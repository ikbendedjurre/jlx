package jlx.asal.j;

public class JBool extends JUserType<JBool> {
	@JTypeDefaultValue
	@JTypeTextify(format = "FALSE") //We define this because "JBool.FALSE" is the automatic format.
	public final static class FALSE extends JBool {}
	
	@JTypeTextify(format = "TRUE") //We define this because otherwise "JBool.TRUE" is the automatic format.
	public final static class TRUE extends JBool {}
	
	public final static FALSE FALSE = new FALSE();
	public final static TRUE TRUE = new TRUE();
	
	@JTypeExpr
	@JTypeTextify(format = "#op:before #expr #op:after")
	public final static class UNARY extends JBool {
		public JOperation op;
		public JBool expr;
		
		public UNARY() {
			//Empty.
		}
		
		public UNARY(JOperation op, JBool expr) {
			this.op = op;
			this.expr = expr;
		}
	}
	
	@JTypeExpr
	@JTypeTextify(format = "( #lhs #op #rhs )")
	public final static class BINARY extends JBool {
		public JOperation op;
		public JBool lhs;
		public JBool rhs;
		
		public BINARY() {
			//Empty.
		}
		
		public BINARY(JOperation op, JBool lhs, JBool rhs) {
			this.op = op;
			this.lhs = lhs;
			this.rhs = rhs;
		}
	}
	
	@JTypeExpr
	@JTypeTextify(format = "( #lhs = #rhs )")
	public final static class EQ extends JBool {
		public JType lhs;
		public JType rhs;
		
		public EQ() {
			//Empty.
		}
		
		public EQ(JType lhs, JType rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
	}
	
	@JTypeExpr
	@JTypeTextify(format = "( #lhs <> #rhs )")
	public final static class NEQ extends JBool {
		public JType lhs;
		public JType rhs;
		
		public NEQ() {
			//Empty.
		}
		
		public NEQ(JType lhs, JType rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
	}
}
