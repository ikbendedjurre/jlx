package jlx.asal.j;

import jlx.blocks.ibd1.*;

public abstract class JBuilder {
	protected JEvt entry(JCmd... cmds) {
		return new JEvt.ENTRY(seq(cmds));
	}
	
	protected JEvt exit(JCmd... cmds) {
		return new JEvt.EXIT(seq(cmds));
	}
	
	protected JEvt none() {
		return new JEvt.NONE(JBool.TRUE, new JCmd.EMPTY());
	}
	
	protected JEvt guard(JBool guard, JCmd... cmds) {
		return new JEvt.NONE(guard, seq(cmds));
	}
	
	protected JEvt when(JBool predicate, JBool guard, JCmd... cmds) {
		return new JEvt.WHEN(predicate, guard, seq(cmds));
	}
	
	protected JEvt when(JBool predicate, JCmd... cmds) {
		return new JEvt.WHEN(predicate, JBool.TRUE, seq(cmds));
	}
	
	protected JEvt when(InPort<JPulse> predicate, JBool guard, JCmd... cmds) {
		return new JEvt.WHEN(predicate, guard, seq(cmds));
	}
	
	protected JEvt when(InPort<JPulse> predicate, JCmd... cmds) {
		return new JEvt.WHEN(predicate, JBool.TRUE, seq(cmds));
	}
	
	protected JEvt after(InPort<JInt> timeout, JBool guard, JCmd... cmds) {
		return new JEvt.AFTER(timeout, guard, seq(cmds));
	}
	
	protected JEvt after(InPort<JInt> timeout, JCmd... cmds) {
		return new JEvt.AFTER(timeout, JBool.TRUE, seq(cmds));
	}
	
	protected JCmd empty() {
		return new JCmd.EMPTY();
	}
	
	protected <T extends JType> JCmd assign(InPort<T> var, T expr) {
		return new JCmd.ASSIGNMENT(var, expr);
	}
	
	protected <T extends JType> JCmd assign(OutPort<T> var, T expr) {
		return new JCmd.ASSIGNMENT(var, expr);
	}
	
	protected <T extends JType> JCmd assign(T var, T expr) {
		return new JCmd.ASSIGNMENT(var, expr);
	}
	
	protected <T extends JType> JCmd assign(JBool var, JPulse expr) {
		return new JCmd.ASSIGNMENT(var, expr);
	}
	
	protected <T extends JType> JCmd assign(JPulse var, JBool expr) {
		return new JCmd.ASSIGNMENT(var, expr);
	}
	
	protected <T extends JType> JCmd $if(JBool condition, JCmd ifBranch) {
		return new JCmd.IF(condition, ifBranch, new JCmd.EMPTY());
	}
	
	protected <T extends JType> JCmd $if(JBool condition, JCmd ifBranch, JCmd elseBranch) {
		return new JCmd.IF(condition, ifBranch, elseBranch);
	}
	
	protected JCmd seq(JCmd... cmds) {
		if (cmds.length == 0) {
			return new JCmd.EMPTY();
		}
		
		JCmd result = cmds[0];
		
		for (int index = 1; index < cmds.length; index++) {
			result = new JCmd.SEQ(result, cmds[index]);
		}
		
		return result;
	}
	
	protected <T extends JType> JBool eq(T x, T y) {
		return new JBool.EQ(x, y);
	}
	
	protected <T extends JType> JBool neq(T x, T y) {
		return new JBool.NEQ(x, y);
	}
}
