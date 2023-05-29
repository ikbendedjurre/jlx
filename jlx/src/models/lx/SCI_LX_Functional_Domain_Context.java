package models.lx;

import jlx.blocks.ibd2.*;
import models.generic.unclearversion.Subsystem_Electronic_Interlocking_SR;

public class SCI_LX_Functional_Domain_Context {
	public static class Block extends Type2IBD {
		//Diagram does not use name that ends with _SR:
		public final Subsystem_Electronic_Interlocking_SR.Block eil = new Subsystem_Electronic_Interlocking_SR.Block();
		
		//Diagram does not use name that ends with _SR:
		public final External_Level_Crossing_System_SR.Block fe = new External_Level_Crossing_System_SR.Block();
		
		@Override
		public void connectFlows() {
			eil.SCI_XX.connect(fe.SCI_XX);
		}
	}
}
