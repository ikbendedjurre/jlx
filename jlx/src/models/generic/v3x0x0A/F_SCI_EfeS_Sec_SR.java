package models.generic.v3x0x0A;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import models.generic.types.*;

/**
 * Generic interface and subsystem requirements (v3.1/2A)
 * Page 23
 */
public class F_SCI_EfeS_Sec_SR {
	public static class Block extends Type1IBD {
	//public static class Block extends F_EST_EfeS.Block { //(according to pdf, but I do not believe it)
		public final Initialization cOp1_init = new Initialization(
			"D50_PDI_Connection_State := \"PDI_Connection_State.NONE\";", //Changed enum.
			"T11_PDI_Connection_Established := FALSE;",
			"T13_Msg_PDI_Version_Check := FALSE;",
			"DT13_Result := \"PDI_Checksum_Result.Empty\";", //Changed enum.
			"DT13_Checksum_Data := \"PDI_Checksum_Data.NotApplicable\";", //Changed enum.
			"T14_Msg_Start_Initialisation := FALSE;",
			"T15_Msg_Initialisation_Completed := FALSE;",
			"T12_Terminate_SCP_Connection := FALSE;",
			"T17_PDI_Connection_Closed := FALSE;"
		);
		
		//First column:
		public final InPort<PDI_Version> D3_Con_PDI_Version = new InPort<>();
		public final InPort<PDI_Checksum_Data> D4_Con_Checksum_Data = new InPort<>();
		public final InPort<JBool> D23_Con_Checksum_Data_Used = new InPort<>();
		
		public final InPort<JPulse> T20_Protocol_Error = new InPort<>();
		public final InPort<JPulse> T21_Formal_Telegram_Error = new InPort<>();
		public final InPort<JPulse> T22_Content_Telegram_Error = new InPort<>();
		
		public final OutPort<PDI_Connection_State> D50_PDI_Connection_State = new OutPort<>();
		public final OutPort<JPulse> T6_Start_Status_Report = new OutPort<>();
		
		public final InPort<JPulse> T1_Ready_For_PDI_Connection = new InPort<>();
		public final InPort<JPulse> T18_Not_Ready_For_PDI_Connection = new InPort<>();
		public final OutPort<JPulse> T11_PDI_Connection_Established = new OutPort<>();
		public final OutPort<JPulse> T17_PDI_Connection_Closed = new OutPort<>();
		
		//Second column:
		public final InPort<JPulse> T7_Cd_PDI_Version_Check = new InPort<>();
		public final InPort<PDI_Version> DT7_PDI_Version = new InPort<>();
		public final OutPort<JPulse> T13_Msg_PDI_Version_Check = new OutPort<>();
		public final OutPort<PDI_Checksum_Result> DT13_Result = new OutPort<>();
		public final OutPort<PDI_Checksum_Data> DT13_Checksum_Data = new OutPort<>();
		public final InPort<JPulse> T8_Cd_Initialisation_Request = new InPort<>();
		public final OutPort<JPulse> T14_Msg_Start_Initialisation = new OutPort<>();
		public final OutPort<JPulse> T15_Msg_Initialisation_Completed = new OutPort<>();
		
		public final InPort<JPulse> T10_SCP_Connection_Terminated = new InPort<>();
		public final InPort<JPulse> T5_SCP_Connection_Established = new InPort<>();
		public final InPort<JPulse> T9_Status_Report_Completed = new InPort<>();
		public final OutPort<JPulse> T12_Terminate_SCP_Connection = new OutPort<>();
	}
}
