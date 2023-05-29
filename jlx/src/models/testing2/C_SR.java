package models.testing2;

import jlx.blocks.ibd2.Type2IBD;

/**
 * Eu.Doc.36
 * Requirements specification for subsystem Point; 2.7 (0.A)
 * Page 31
 */
public class C_SR {
	public static class Block extends Type2IBD {
		public final A_SR.Block a = new A_SR.Block();
		public final B_SR.Block b = new B_SR.Block();
		
		@Override
		public void connectFlows() {
			a.T2_Cmd.connect(b.T3_Cmd);
			a.DT2_Pos.connect(b.DT3_Pos);
		}
	}
}
