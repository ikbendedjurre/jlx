package models.generic.v3x0x0A;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import models.generic.types.*;

/**
 * Generic interface and subsystem requirements (v3.1/2A)
 * Page 19
 */
public class S_SCI_EfeS_Prim_SR {
	public static class Block extends Type1IBD {
		public final Initialization cOp1_init = new Initialization(
			"D50_PDI_Connection_State := \"PDI_Connection_State.NONE\";", //Changed enum.
			"T12_Terminate_SCP_Connection := FALSE;",
			"T7_Cd_PDI_Version_Check := FALSE;",
			"DT7_PDI_Version := \"V1\";",
			"T8_Cd_Initialisation_Request := FALSE;",
			"T6_Establish_SCP_Connection := FALSE;"
		);
		
		//Left:
		public final InPort<JInt> D2_Con_tmax_PDI_Connection = new InPort<>();
		public final InPort<PDI_Version> D3_Con_PDI_Version = new InPort<>();
		public final InPort<PDI_Checksum_Data> D4_Con_Checksum_Data = new InPort<>();
		public final InPort<JBool> D23_Con_Checksum_Data_Used = new InPort<>();
		
		public final InPort<JPulse> T20_Protocol_Error = new InPort<>();
		public final InPort<JPulse> T21_Formal_Telegram_Error = new InPort<>();
		public final InPort<JPulse> T22_Content_Telegram_Error = new InPort<>();
		
		public final OutPort<PDI_Connection_State> D50_PDI_Connection_State = new OutPort<>();
		
		//Right:
		public final OutPort<JPulse> T7_Cd_PDI_Version_Check = new OutPort<>();
		public final OutPort<PDI_Version> DT7_PDI_Version = new OutPort<>();
		public final InPort<JPulse> T13_Msg_PDI_Version_Check = new InPort<>();
		public final InPort<PDI_Checksum_Result> DT13_Result = new InPort<>();
		public final InPort<PDI_Checksum_Data> DT13_Checksum_Data = new InPort<>();
		public final OutPort<JPulse> T8_Cd_Initialisation_Request = new OutPort<>();
		public final InPort<JPulse> T14_Msg_Start_Initialisation = new InPort<>();
		public final InPort<JPulse> T15_Msg_Initialisation_Completed = new InPort<>();
		
		public final InPort<JPulse> T5_SCP_Connection_Established = new InPort<>();
		public final InPort<JPulse> T10_SCP_Connection_Terminated = new InPort<>();
		public final OutPort<JPulse> T6_Establish_SCP_Connection = new OutPort<>();
		public final OutPort<JPulse> T12_Terminate_SCP_Connection = new OutPort<>();
	}
}
