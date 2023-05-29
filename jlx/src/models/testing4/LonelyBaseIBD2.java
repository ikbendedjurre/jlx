package models.testing4;

import jlx.blocks.ibd2.*;

public class LonelyBaseIBD2 {
	public static class Block extends Type2IBD {
		public final LeftLonelyIBD1 left = new LeftLonelyIBD1();
		
		public final InterfacePort LONELY = new InterfacePort();
		
		@Override
		public void connectFlows() {
			left.output1.connect(LONELY);
			left.input1.connect(LONELY);
		}
	}
}
