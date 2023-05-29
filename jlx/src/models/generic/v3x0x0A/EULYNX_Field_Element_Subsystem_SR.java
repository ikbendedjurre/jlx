package models.generic.v3x0x0A;

import jlx.blocks.ibd2.*;

/**
 * Generic interface and subsystem requirements (v3.1/2A)
 * Page 10
 */
public class EULYNX_Field_Element_Subsystem_SR {
	public static class Block extends Type2IBD {
		public final F_SCI_EfeS_Sec_SR.Block sec = new F_SCI_EfeS_Sec_SR.Block();
		public final F_EST_EfeS_SR.Block est = new F_EST_EfeS_SR.Block();
		public final F_SMI_EfeS_SR.Block smi = new F_SMI_EfeS_SR.Block();
		
		public final InterfacePort SMI_XX = new InterfacePort("Subsystem_MDM_M");
		public final InterfacePort SCI_XX = new InterfacePort("Subsystem_Electronic_Interlocking");
		
		public final InterfacePort SAP_SubS_EIL = new InterfacePort();
		
		@Override
		public void connectFlows() {
			//Middle, bottom:
			est.T21_Ready_For_PDI_Connection.connect(sec.T1_Ready_For_PDI_Connection);
			est.T9_PDI_Connection_Established.connect(sec.T11_PDI_Connection_Established);
			est.T10_PDI_Connection_Closed.connect(sec.T17_PDI_Connection_Closed);
			est.T18_Not_Ready_For_PDI_Connection.connect(sec.T18_Not_Ready_For_PDI_Connection);
			
			//Middle:
			smi.T22_Data_Update_Stop.connect(est.T22_Data_Update_Stop);
			smi.T21_Data_Update_Finished.connect(est.T17_Data_Update_Finished);
			smi.T13_Data_Update_After_Booting.connect(est.T13_Data_Update_After_Booting);
			smi.T14_Data_Update_After_Operational.connect(est.T14_Data_Update_After_Operational);
			smi.T15_Data_Update_In_Initialising.connect(est.T15_Data_Update_In_Initialising);
			smi.T16_Data_Installation_Complete.connect(est.T16_Data_Installation_Complete);
			
			//Left:
			smi.T6_Data_Up_To_Date.connect(SMI_XX);
			smi.T7_Data_Not_Up_To_Date.connect(SMI_XX);
			smi.T8_Data.connect(SMI_XX);
			smi.T9_Transmission_Complete.connect(SMI_XX);
			smi.T10_Data_Valid.connect(SMI_XX);
			smi.T11_Data_Invalid.connect(SMI_XX);
			smi.T12_Data_Installation_Successfully.connect(SMI_XX);
			smi.T20_Ready_For_Update_Of_Data.connect(SMI_XX);
			smi.T19_Validate_Data.connect(SMI_XX);
			
			//Right:
			sec.T7_Cd_PDI_Version_Check.connect(SCI_XX);
			sec.DT7_PDI_Version.connect(SCI_XX);
			sec.T13_Msg_PDI_Version_Check.connect(SCI_XX);
			sec.DT13_Result.connect(SCI_XX);
			sec.DT13_Checksum_Data.connect(SCI_XX);
			sec.T8_Cd_Initialisation_Request.connect(SCI_XX);
			sec.T14_Msg_Start_Initialisation.connect(SCI_XX);
			sec.T15_Msg_Initialisation_Completed.connect(SCI_XX);
		}
	}
}
