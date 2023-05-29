package models.lx;

import jlx.blocks.ibd2.*;

public class External_Level_Crossing_System_SR {
	public static class Block extends Type2IBD {
		public final InterfacePort SCI_XX = new InterfacePort("Subsystem_Electronic_Interlocking");
	
		@Override
		public void connectFlows() {
			//Do nothing.
		}
	}
}
