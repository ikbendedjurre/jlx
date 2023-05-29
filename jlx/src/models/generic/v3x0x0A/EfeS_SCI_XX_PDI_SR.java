package models.generic.v3x0x0A;

import jlx.blocks.ibd2.*;

/**
 * Generic interface and subsystem requirements (v3.1/2A)
 * Page 18
 */
public class EfeS_SCI_XX_PDI_SR {
	public static class Block extends Type2IBD {
		public final S_SCI_EfeS_Prim_SR.Block prim = new S_SCI_EfeS_Prim_SR.Block();
		public final F_SCI_EfeS_Sec_SR.Block sec = new F_SCI_EfeS_Sec_SR.Block();
		public final SCP_Input_SR.Block scpInput = new SCP_Input_SR.Block(); //(Added to synchronize certain inputs.)
		
		public final InterfacePort SAP_SubS_EIL_SCP = new InterfacePort();
		public final InterfacePort SAP_SubS_EIL = new InterfacePort();
		public final InterfacePort SAP_SubS_XX_SCP = new InterfacePort();
		public final InterfacePort SAP_SubS_XX = new InterfacePort();
		
		@Override
		public void connectFlows() {
			//Added to synchronize certain inputs:
			scpInput.T5_SCP_Connection.connect(prim.T5_SCP_Connection_Established);
			scpInput.T5_SCP_Connection.connect(sec.T5_SCP_Connection_Established);
			scpInput.T10_SCP_Connection_Terminated.connect(prim.T10_SCP_Connection_Terminated);
			scpInput.T10_SCP_Connection_Terminated.connect(sec.T10_SCP_Connection_Terminated);
			
			//Middle:
			prim.T7_Cd_PDI_Version_Check.connect(sec.T7_Cd_PDI_Version_Check);
			prim.DT7_PDI_Version.connect(sec.DT7_PDI_Version);
			prim.T8_Cd_Initialisation_Request.connect(sec.T8_Cd_Initialisation_Request);
			prim.T13_Msg_PDI_Version_Check.connect(sec.T13_Msg_PDI_Version_Check);
			prim.DT13_Result.connect(sec.DT13_Result);
			prim.DT13_Checksum_Data.connect(sec.DT13_Checksum_Data);
			prim.T14_Msg_Start_Initialisation.connect(sec.T14_Msg_Start_Initialisation);
			prim.T15_Msg_Initialisation_Completed.connect(sec.T15_Msg_Initialisation_Completed);
			
			//Left, top:
			prim.T5_SCP_Connection_Established.connect(SAP_SubS_EIL_SCP);
			prim.T6_Establish_SCP_Connection.connect(SAP_SubS_EIL_SCP);
			prim.T10_SCP_Connection_Terminated.connect(SAP_SubS_EIL_SCP);
			prim.T12_Terminate_SCP_Connection.connect(SAP_SubS_EIL_SCP);
			
			//Left, bottom:
			prim.D2_Con_tmax_PDI_Connection.connect(SAP_SubS_EIL);
			prim.D3_Con_PDI_Version.connect(SAP_SubS_EIL);
			prim.D4_Con_Checksum_Data.connect(SAP_SubS_EIL);
			prim.D23_Con_Checksum_Data_Used.connect(SAP_SubS_EIL);
			prim.T20_Protocol_Error.connect(SAP_SubS_EIL);
			prim.T21_Formal_Telegram_Error.connect(SAP_SubS_EIL);
			prim.T22_Content_Telegram_Error.connect(SAP_SubS_EIL);
			
			//Right, top:
			sec.T5_SCP_Connection_Established.connect(SAP_SubS_XX_SCP);
			sec.T10_SCP_Connection_Terminated.connect(SAP_SubS_XX_SCP);
			sec.T12_Terminate_SCP_Connection.connect(SAP_SubS_XX_SCP);
			
			//Right, bottom:
			sec.T1_Ready_For_PDI_Connection.connect(SAP_SubS_XX);
			sec.D3_Con_PDI_Version.connect(SAP_SubS_XX);
			sec.D4_Con_Checksum_Data.connect(SAP_SubS_XX);
			sec.D23_Con_Checksum_Data_Used.connect(SAP_SubS_XX);
			sec.T20_Protocol_Error.connect(SAP_SubS_XX);
			sec.T21_Formal_Telegram_Error.connect(SAP_SubS_XX);
			sec.T22_Content_Telegram_Error.connect(SAP_SubS_XX);
			sec.T11_PDI_Connection_Established.connect(SAP_SubS_XX);
			sec.T17_PDI_Connection_Closed.connect(SAP_SubS_XX);
			sec.T18_Not_Ready_For_PDI_Connection.connect(SAP_SubS_XX);
		}
	}
}
