package models.generic.unclearversion;

import jlx.blocks.ibd2.*;
import models.scp.SCP;

/**
 * Generic interface and subsystem requirements (v3.1/2A)
 * Page 17
 */
public class EfeS_SCI_XX_Protocol_Stack_SR {
	public static class Block extends Type2IBD {
		//public final Subsystem_Electronic_Interlocking_SR eil = new Subsystem_Electronic_Interlocking_SR();
		//public final EULYNX_Field_Element_Subsystem_SR fe = new EULYNX_Field_Element_Subsystem_SR();
		public final EfeS_SCI_XX_PDI_SR.Block pdi = new EfeS_SCI_XX_PDI_SR.Block();
		public final SCP.Block scp = new SCP.Block();
		
		@Override
		public void connectFlows() {
			pdi.SAP_SubS_EIL_SCP.connect(scp.SAP_SCP_SubS_EIL);
			pdi.SAP_SubS_XX_SCP.connect(scp.SAP_SCP_SubS_XX);
		}
	}
}
