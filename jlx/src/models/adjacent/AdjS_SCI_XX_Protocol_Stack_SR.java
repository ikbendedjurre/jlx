package models.adjacent;

import jlx.blocks.ibd2.*;
import models.scp.SCP;

/**
 * Generic interface and subsystem requirements Eu.Doc.20 (v3.2)
 * Page 34/44
 */
public class AdjS_SCI_XX_Protocol_Stack_SR {
	public static class Block extends Type2IBD {
		public final AdjS_SCI_XX_PDI_SR.Block pdi = new AdjS_SCI_XX_PDI_SR.Block();
		public final SCP.Block scp = new SCP.Block();
		
		@Override
		public void connectFlows() {
			pdi.SAP_SubS_EIL_SCP.connect(scp.SAP_SCP_SubS_EIL);
			pdi.SAP_Sys_XX_SCP.connect(scp.SAP_SCP_SubS_XX);
		}
	}
}
