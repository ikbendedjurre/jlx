package models.adjacent;

import jlx.blocks.ibd2.*;

public class Adjacent_System_SR {
	public static class Block extends Type2IBD {
		public final InterfacePort SCI_XX = new InterfacePort("~Adjacent_System");
		
		@Override
		public void connectFlows() {
			//
		}
	}
}
