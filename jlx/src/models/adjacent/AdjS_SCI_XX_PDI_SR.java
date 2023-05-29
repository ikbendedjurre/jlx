package models.adjacent;

import jlx.blocks.ibd2.*;

/**
 * Generic interface and subsystem requirements Eu.Doc.20 (v3.2)
 * Page 35/44
 */
public class AdjS_SCI_XX_PDI_SR {
	public static class Block extends Type2IBD {
		public final S_SCI_AdjS_Prim_SR.Block prim = new S_SCI_AdjS_Prim_SR.Block();
		public final S_SCI_AdjS_Sec_SR.Block sec = new S_SCI_AdjS_Sec_SR.Block();
		
		public final InterfacePort SAP_SubS_EIL_SCP = new InterfacePort();
		public final InterfacePort SAP_SubS_EIL = new InterfacePort();
		public final InterfacePort SAP_Sys_XX_SCP = new InterfacePort();
		public final InterfacePort SAP_Sys_XX = new InterfacePort();
		
		@Override
		public void connectFlows() {
			//Middle:
			prim.T7_Cd_PDI_Version_Check.connect(sec.T7_Cd_PDI_Version_Check);
			prim.DT7_PDI_Version.connect(sec.DT7_PDI_Version);
			prim.T8_Cd_Initialisation_Request.connect(sec.T8_Cd_Initialisation_Request);
			prim.T13_Msg_PDI_Version_Check.connect(sec.T13_Msg_PDI_Version_Check);
			prim.DT13_Result.connect(sec.DT13_Result);
			prim.DT13_Checksum_Data.connect(sec.DT13_Checksum_Data);
			prim.T14_Msg_Start_Initialisation.connect(sec.T14_Msg_Start_Initialisation);
			prim.T29_Msg_Status_Report_Completed.connect(sec.T25_Msg_Status_Report_Completed);
			prim.T24_Msg_Status_Report_Completed.connect(sec.T26_Msg_Status_Report_Completed);
			prim.T15_Msg_Initialisation_Completed.connect(sec.T15_Msg_Initialisation_Completed);
			
			//Left side:
			prim.T5_SCP_Connection_Established.connect(SAP_Sys_XX_SCP);
			prim.T6_Establish_SCP_Connection.connect(SAP_Sys_XX_SCP);
			prim.T10_SCP_Connection_Terminated.connect(SAP_Sys_XX_SCP);
			prim.T12_Terminate_SCP_Connection.connect(SAP_Sys_XX_SCP);
			
			prim.D2_Con_tmax_PDI_Connection.connect(SAP_Sys_XX);
			prim.D3_Con_PDI_Version.connect(SAP_Sys_XX);
			prim.D4_Con_Checksum_Data.connect(SAP_Sys_XX);
			prim.D23_Con_Checksum_Data_Used.connect(SAP_Sys_XX);
			prim.T20_Protocol_Error.connect(SAP_Sys_XX);
			prim.T21_Formal_Telegram_Error.connect(SAP_Sys_XX);
			prim.T22_Content_Telegram_Error.connect(SAP_Sys_XX);
			prim.T28_Start_Prim_Status_Report.connect(SAP_Sys_XX);
			prim.T25_Sec_Status_Report_Complete.connect(SAP_Sys_XX);
			prim.T26_Prim_Status_Report_Completed.connect(SAP_Sys_XX);
			prim.T27_Check_Sec_Status.connect(SAP_Sys_XX);
			prim.T30_Reset_Connection.connect(SAP_Sys_XX);
			prim.T31_PDI_Connection_Established.connect(SAP_Sys_XX);
			prim.T32_PDI_Connection_Closed.connect(SAP_Sys_XX);
			prim.T33_Establishing_PDI_Connection.connect(SAP_Sys_XX);
			prim.T34_PDI_Connection_Impermissible.connect(SAP_Sys_XX);
			
			//Right side:
			sec.T5_SCP_Connection_Established.connect(SAP_SubS_EIL_SCP);
			sec.T10_SCP_Connection_Terminated.connect(SAP_SubS_EIL_SCP);
			sec.T12_Terminate_SCP_Connection.connect(SAP_SubS_EIL_SCP);
			//sec.T1_Establish_SCP_Connection.connect(SAP_SubS_EIL_SCP);
			
			sec.D3_Con_PDI_Version.connect(SAP_SubS_EIL);
			sec.D4_Con_Checksum_Data.connect(SAP_SubS_EIL);
			sec.D23_Con_Checksum_Data_Used.connect(SAP_SubS_EIL);
			sec.T20_Protocol_Error.connect(SAP_SubS_EIL);
			sec.T21_Formal_Telegram_Error.connect(SAP_SubS_EIL);
			sec.T22_Content_Telegram_Error.connect(SAP_SubS_EIL);
			sec.T6_Start_Sec_Status_Report.connect(SAP_SubS_EIL);
			sec.T17_Prim_Status_Report_Complete.connect(SAP_SubS_EIL);
			sec.T24_Sec_Status_Report_Completed.connect(SAP_SubS_EIL);
			sec.T27_Check_Prim_Status.connect(SAP_SubS_EIL);
			sec.T30_Reset_Connection.connect(SAP_SubS_EIL);
			sec.T31_PDI_Connection_Established.connect(SAP_SubS_EIL);
			sec.T32_PDI_Connection_Closed.connect(SAP_SubS_EIL);
			sec.T33_Establishing_PDI_Connection.connect(SAP_SubS_EIL);
			sec.T34_PDI_Connection_Impermissible.connect(SAP_SubS_EIL);
		}
	}
}
