package jlx.asal.vars;

import java.util.Set;

import jlx.asal.j.JType;
import jlx.blocks.ibd1.VerificationAction;
import jlx.utils.Dir;

public class ASALPortFields {
	public Dir dir;
	public JType initialValue;
	public Integer priority;
	public Set<VerificationAction> verificationActions;
	public int executionTime;
}
