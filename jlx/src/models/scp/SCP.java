package models.scp;

import jlx.blocks.ibd2.*;

/**
 * gitlab.git/formasig/Models/Model PDI protocol/EULYNX SysML models
 * Page 5
 */
public class SCP {
	public static class Block extends Type2IBD {
		public final S_SCI_SCP_Prim_SR.Block scpPrim = new S_SCI_SCP_Prim_SR.Block();
		public final F_SCI_SCP_Sec_SR.Block scpSec = new F_SCI_SCP_Sec_SR.Block();
		
		public final InterfacePort SAP_SCP_SubS_EIL = new InterfacePort();
		public final InterfacePort SAP_SCP_SubS_XX = new InterfacePort();
		
		@Override
		public void connectFlows() {
			scpPrim.T6_Conn_Req.connect(scpSec.T5_Conn_Req);
			scpPrim.T7_Conn_Resp.connect(scpSec.T6_Conn_Resp);
			scpPrim.T8_HB.connect(scpSec.T7_HB);
			scpPrim.T11_Disc_Req.connect(scpSec.T9_Disc_Req);
			scpPrim.T9_Disc_Req.connect(scpSec.T8_Disc_Req);
			
			scpPrim.T2_Terminate_SCP_Connection.connect(SAP_SCP_SubS_EIL);
			scpPrim.T5_SCP_Connection_Terminated.connect(SAP_SCP_SubS_EIL);
			scpPrim.T1_Establish_SCP_Connection.connect(SAP_SCP_SubS_EIL);
			scpPrim.T4_SCP_Connection_Established.connect(SAP_SCP_SubS_EIL);
			
			scpSec.T4_Terminate_SCP_Connection.connect(SAP_SCP_SubS_XX);
			scpSec.T2_SCP_Connection_Terminated.connect(SAP_SCP_SubS_XX);
			scpSec.T1_SCP_Connection_Established.connect(SAP_SCP_SubS_XX);
			//scpSec.T10_Establish_SCP_Connection.connect(SAP_SCP_SubS_EIL);
		}
	}
}
