package models.generic.v3x0x0A;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

/**
 * Generic interface and subsystem requirements (v3.1/2A)
 * Page 30
 */
public class F_SMI_EfeS_SR {
	public static class Block extends Type1IBD {
	//public static class Block extends F_EST_EfeS.Block { //(the superclass seems superfluous)
		public final JInt Mem_t_Ini_Delay = new JInt();
		
		public final Initialization cOp1_init = new Initialization(
			"T16_Data_Installation_Complete := FALSE;",
			"T19_Validate_Data := FALSE;",
			"T20_Ready_For_Update_Of_Data := FALSE;",
			"T21_Data_Update_Finished := FALSE;"
		);
		
		//Left:
		public final InPort<JInt> D1_Con_t_Ini_Def_Delay = new InPort<>();
		public final InPort<JInt> D2_Con_t_Ini_Step = new InPort<>();
		public final InPort<JInt> D3_Con_t_Ini_Max = new InPort<>();
		public final InPort<JInt> D4_Con_tmax_Response_MDM = new InPort<>();
		public final InPort<JInt> D5_Con_tmax_DataTransmission = new InPort<>();
		public final InPort<JPulse> T6_Data_Up_To_Date = new InPort<>();
		public final InPort<JPulse> T7_Data_Not_Up_To_Date = new InPort<>();
		public final InPort<JPulse> T8_Data = new InPort<>();
		public final InPort<JPulse> T9_Transmission_Complete = new InPort<>();
		public final InPort<JPulse> T10_Data_Valid = new InPort<>();
		public final InPort<JPulse> T11_Data_Invalid = new InPort<>();
		public final InPort<JPulse> T12_Data_Installation_Successfully = new InPort<>();
		public final InPort<JPulse> T13_Data_Update_After_Booting = new InPort<>();
		public final InPort<JPulse> T14_Data_Update_After_Operational = new InPort<>();
		public final InPort<JPulse> T15_Data_Update_In_Initialising = new InPort<>();
		public final InPort<JPulse> T22_Data_Update_Stop = new InPort<>();
		
		//Second column:
		public final OutPort<JPulse> T21_Data_Update_Finished = new OutPort<>();
		public final OutPort<JPulse> T19_Validate_Data = new OutPort<>();
		public final OutPort<JPulse> T20_Ready_For_Update_Of_Data = new OutPort<>();
		public final OutPort<JPulse> T16_Data_Installation_Complete = new OutPort<>();
	}
}

