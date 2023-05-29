package jlx.asal.j;

public class JInt extends JUserType<JInt> {
	@JTypeName(s = "1000")
	public final static class _1000 extends JInt {}
	@JTypeName(s = "10000")
	public final static class _10000 extends JInt {}
	@JTypeName(s = "12000")
	public final static class _12000 extends JInt {}
	@JTypeName(s = "20000")
	public final static class _20000 extends JInt {}
	@JTypeName(s = "600000")
	public final static class _600000 extends JInt {}
	
	@JTypeExpr
	@JTypeTextify(format = "#value")
	public final static class LITERAL extends JInt {
		public int value;
		
		public LITERAL() {
			//Empty.
		}
		
		public LITERAL(int value) {
			this.value = value;
		}
	}
	
	@JTypeExpr
	@JTypeTextify(format = "#op:before #expr #op:after")
	public final static class UNARY extends JInt {
		public JOperation op;
		public JInt expr;
		
		public UNARY() {
			//Empty.
		}
		
		public UNARY(JOperation op, JInt expr) {
			this.op = op;
			this.expr = expr;
		}
	}
	
	@JTypeExpr
	@JTypeTextify(format = "( #lhs #op #rhs )")
	public final static class BINARY extends JInt {
		public JOperation op;
		public JInt lhs;
		public JInt rhs;
		
		public BINARY() {
			//Empty.
		}
		
		public BINARY(JOperation op, JInt lhs, JInt rhs) {
			this.op = op;
			this.lhs = lhs;
			this.rhs = rhs;
		}
	}
}
