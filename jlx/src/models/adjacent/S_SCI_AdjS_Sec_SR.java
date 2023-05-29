package models.adjacent;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import models.generic.types.*;

/**
 * Generic interface and subsystem requirements (v3.2/2A)
 * Page 40/44
 */
public class S_SCI_AdjS_Sec_SR {
	public static class Block extends Type1IBD {
		public final Initialization cOp1_init = new Initialization(
			"DT13_Result := \"PDI_Checksum_Result.Empty\";",
			"DT13_Checksum_Data := \"PDI_Checksum_Data.D1\";",
			"T32_PDI_Connection_Closed := TRUE;"
		);
		
		//First column:
		public final InPort<PDI_Version> D3_Con_PDI_Version = new InPort<>();
		public final InPort<PDI_Checksum_Data> D4_Con_Checksum_Data = new InPort<>();
		public final InPort<JBool> D23_Con_Checksum_Data_Used = new InPort<>();
		
		public final InPort<JPulse> T20_Protocol_Error = new InPort<>();
		public final InPort<JPulse> T21_Formal_Telegram_Error = new InPort<>();
		public final InPort<JPulse> T22_Content_Telegram_Error = new InPort<>();
		public final InPort<JPulse> T30_Reset_Connection = new InPort<>();
		
		public final OutPort<JPulse> T6_Start_Sec_Status_Report = new OutPort<>();
		public final InPort<JPulse> T17_Prim_Status_Report_Complete = new InPort<>();
		public final InPort<JPulse> T24_Sec_Status_Report_Completed = new InPort<>();
		public final OutPort<JPulse> T27_Check_Prim_Status = new OutPort<>();
		
		public final OutPort<JPulse> T31_PDI_Connection_Established = new OutPort<>();
		public final OutPort<JPulse> T32_PDI_Connection_Closed = new OutPort<>();
		public final OutPort<JPulse> T33_Establishing_PDI_Connection = new OutPort<>();
		public final OutPort<JPulse> T34_PDI_Connection_Impermissible = new OutPort<>();
		
		//Second column:
		public final InPort<JPulse> T7_Cd_PDI_Version_Check = new InPort<>();
		public final InPort<PDI_Version> DT7_PDI_Version = new InPort<>();
		public final OutPort<JPulse> T13_Msg_PDI_Version_Check = new OutPort<>();
		public final OutPort<PDI_Checksum_Result> DT13_Result = new OutPort<>();
		public final OutPort<PDI_Checksum_Data> DT13_Checksum_Data = new OutPort<>();
		public final InPort<JPulse> T8_Cd_Initialisation_Request = new InPort<>();
		public final OutPort<JPulse> T14_Msg_Start_Initialisation = new OutPort<>();
		public final OutPort<JPulse> T26_Msg_Status_Report_Completed = new OutPort<>();
		public final InPort<JPulse> T25_Msg_Status_Report_Completed = new InPort<>();
		public final OutPort<JPulse> T15_Msg_Initialisation_Completed = new OutPort<>();
		
		public final InPort<JPulse> T5_SCP_Connection_Established = new InPort<>();
		public final InPort<JPulse> T10_SCP_Connection_Terminated = new InPort<>();
		public final OutPort<JPulse> T12_Terminate_SCP_Connection = new OutPort<>();
		//public final OutPort<JPulse> T1_Establish_SCP_Connection = new OutPort<>();
	}
}
