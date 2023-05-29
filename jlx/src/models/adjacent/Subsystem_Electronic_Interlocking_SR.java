package models.adjacent;

import jlx.blocks.ibd2.*;

public class Subsystem_Electronic_Interlocking_SR {
	public static class Block extends Type2IBD {
		public final InterfacePort SAP_SubS_XX = new InterfacePort();
		public final InterfacePort SCI_XX = new InterfacePort("Adjacent_System");
		
		@Override
		public void connectFlows() {
			//Do nothing.
		}
	}
}
