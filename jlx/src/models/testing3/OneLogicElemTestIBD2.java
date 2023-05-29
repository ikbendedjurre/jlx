package models.testing3;

import jlx.blocks.ibd2.*;

public class OneLogicElemTestIBD2 {
	public static class Block extends Type2IBD {
		public final OrIBD1.Block or = new OrIBD1.Block();
		
		@Override
		public void connectFlows() {
			//Empty.
		}
	}
}
