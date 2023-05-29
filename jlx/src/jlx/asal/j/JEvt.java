package jlx.asal.j;

public class JEvt extends JUserType<JEvt> {
	@JTypeExpr
	@JTypeTextify(format = "Entry _ / _ #cmd")
	public final static class ENTRY extends JEvt {
		public JCmd cmd;
		
		public ENTRY(JCmd cmd) {
			this.cmd = cmd;
		}
	}
	
	@JTypeExpr
	@JTypeTextify(format = "Exit _ / _ #cmd")
	public final static class EXIT extends JEvt {
		public JCmd cmd;
		
		public EXIT(JCmd cmd) {
			this.cmd = cmd;
		}
	}
	
	@JTypeExpr
	@JTypeTextify(format = "[ #guard ] _ / _ #cmd")
	public final static class NONE extends JEvt {
		public JBool guard;
		public JCmd cmd;
		
//		public NONE() {
//			//Empty.
//		}
		
		public NONE(JBool guard, JCmd cmd) {
			this.guard = guard;
			this.cmd = cmd;
		}
	}
	
	@JTypeExpr
	@JTypeTextify(format = "after( #timeout ) _ [ #guard ] _ / _ #cmd")
	public final static class AFTER extends JEvt {
		public JType timeout;
		public JBool guard;
		public JCmd cmd;
		
		public AFTER() {
			//Empty.
		}
		
		public AFTER(JType timeout, JBool guard, JCmd cmd) {
			this.timeout = timeout;
			this.guard = guard;
			this.cmd = cmd;
		}
	}
	
	@JTypeExpr
	@JTypeTextify(format = "when( #predicate ) _ [ #guard ] _ / _ #cmd")
	public final static class WHEN extends JEvt {
		public JType predicate;
		public JBool guard;
		public JCmd cmd;
		
		public WHEN() {
			//Empty.
		}
		
		public WHEN(JType predicate, JBool guard, JCmd cmd) {
			this.predicate = predicate;
			this.guard = guard;
			this.cmd = cmd;
		}
	}
}
