package models.testing4;

import jlx.blocks.ibd2.*;

public class LonelyIBD2 {
	public static class Block extends LonelyBaseIBD2.Block {
		public final RightLonelyIBD1 right = new RightLonelyIBD1();
		
		public final InterfacePort LONELY_2 = new InterfacePort();
		
		@Override
		public void connectFlows() {
			right.output2.connect(LONELY);
			right.input2.connect(LONELY);
		}
	}
}
