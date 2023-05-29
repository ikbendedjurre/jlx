package models.testing3;

import jlx.blocks.ibd2.*;

public class SimpleLogicTestIBD2 {
	public static class Block extends Type2IBD {
		public final OrIBD1.Block or = new OrIBD1.Block();
		public final AndIBD1.Block and = new AndIBD1.Block();
		
		@Override
		public void connectFlows() {
			or.T2_Output.connect(and.T1_Input);
			or.DT2_Output.connect(and.DT1_Input);
		}
	}
}
