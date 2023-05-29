package models.adjacent;

import jlx.blocks.ibd2.Type2IBD;

public class AdjS_SCI_XX_Functional_Connection_Domain_Context {
	public static class Block extends Type2IBD {
		//Diagram does not use name that ends with _SR:
		public final Subsystem_Electronic_Interlocking_SR.Block eil = new Subsystem_Electronic_Interlocking_SR.Block();
		
		//Diagram does not use name that ends with _SR:
		public final Adjacent_System_SR.Block adjSys = new Adjacent_System_SR.Block();
		
		@Override
		public void connectFlows() {
			eil.SCI_XX.connect(adjSys.SCI_XX);
		}
	}
}
