package models.generic.v3x0x0A;

import jlx.blocks.ibd2.*;

/**
 * Generic interface and subsystem requirements (v3.1/2A)
 * Page 14
 */
public class EfeS_SCI_XX_Functional_Connection_Domain_Context {
	public static class Block extends Type2IBD {
		//Diagram does not use name that ends with _SR:
		public final Subsystem_Electronic_Interlocking_SR.Block eil = new Subsystem_Electronic_Interlocking_SR.Block();
		
		//Diagram does not use name that ends with _SR:
		public final EULYNX_Field_Element_Subsystem_SR.Block fe = new EULYNX_Field_Element_Subsystem_SR.Block();
		
		@Override
		public void connectFlows() {
			eil.SCI_XX.connect(fe.SCI_XX);
		}
	}
}
