package jlx.asal.j;

public class JCmd extends JUserType<JCmd> {
	@JTypeTextify(format = "")
	public final static class EMPTY extends JCmd {}
	
	@JTypeExpr
	@JTypeTextify(format = "#target _ := _ #expr ;")
	public final static class ASSIGNMENT extends JCmd {
		public JType target;
		public JType expr;
		
		public ASSIGNMENT() {
			//Empty.
		}
		
		public ASSIGNMENT(JType target, JType expr) {
			this.target = target;
			this.expr = expr;
		}
	}
	
	@JTypeExpr
	@JTypeTextify(format = "#target _ := _ #expr ;")
	public final static class CALL extends JCmd {
		public JType target;
		public JType expr;
		
		public CALL() {
			//Empty.
		}
		
		public CALL(JType target, JType expr) {
			this.target = target;
			this.expr = expr;
		}
	}
	
	@JTypeExpr
	@JTypeTextify(format = "if _ ( #condition ) _ #thenBranch _ else _ #elseBranch _ end _ if")
	public final static class IF extends JCmd {
		public JBool condition;
		public JCmd thenBranch;
		public JCmd elseBranch;
		
		public IF() {
			//Empty.
		}
		
		public IF(JBool condition, JCmd thenBranch, JCmd elseBranch) {
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}
	}
	
	@JTypeExpr
	@JTypeTextify(format = "#statement _ #successor")
	public final static class SEQ extends JCmd {
		public JCmd statement;
		public JCmd successor;
		
		public SEQ() {
			//Empty.
		}
		
		public SEQ(JCmd statement, JCmd successor) {
			this.statement = statement;
			this.successor = successor;
		}
	}
	
	@JTypeExpr
	@JTypeTextify(format = "while _ ( #condition ) _ #body _ end _ while")
	public final static class WHILE extends JCmd {
		public JBool condition;
		public JCmd body;
		
		public WHILE() {
			//Empty.
		}
		
		public WHILE(JBool condition, JCmd body) {
			this.condition = condition;
			this.body = body;
		}
	}
}
