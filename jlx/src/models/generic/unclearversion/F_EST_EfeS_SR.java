package models.generic.unclearversion;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import models.generic.types.EST_EfeS_State;

/**
 * Generic interface and subsystem requirements (v3.1/2A)
 * Page 11
 */
public class F_EST_EfeS_SR {
	public static class Block extends F_EST_EfeS.Block {
		public final Initialization cOp1_Init = new Initialization(
			"T18_Not_Ready_For_PDI_Connection := FALSE;",
			"T13_Data_Update_After_Booting := FALSE;",
			"T14_Data_Update_After_Operational := FALSE;",
			"T15_Data_Update_In_Initialising := FALSE;",
			"D51_EST_EfeS_State := \"\";",
			"T21_Ready_For_PDI_Connection := FALSE;",
			"T22_Data_Update_Stop := FALSE;"
		);
		
		//First column:
		public final InPort<JPulse> T1_Power_On_Detected = new InPort<>();
		public final InPort<JPulse> T2_Power_Off_Detected = new InPort<>();
		public final InPort<JPulse> T3_Reset = new InPort<>();
		public final InPort<JPulse> T4_Booted = new InPort<>();
		public final InPort<JPulse> T5_SIL_Not_Fulfilled = new InPort<>();
		public final InPort<JPulse> T7_Invalid_Or_Missing_Basic_Data = new InPort<>();
		public final InPort<JPulse> T9_PDI_Connection_Established = new InPort<>();
		public final InPort<JPulse> T10_PDI_Connection_Closed = new InPort<>();
		
		//Second column:
		public final OutPort<JPulse> T18_Not_Ready_For_PDI_Connection = new OutPort<>();
		public final OutPort<JPulse> T13_Data_Update_After_Booting = new OutPort<>();
		public final OutPort<JPulse> T14_Data_Update_After_Operational = new OutPort<>();
		public final OutPort<JPulse> T15_Data_Update_In_Initialising = new OutPort<>();
		public final InPort<JPulse> T16_Data_Installation_Complete = new InPort<>();
		public final InPort<JPulse> T17_Data_Update_Finished = new InPort<>();
		public final OutPort<JPulse> T22_Data_Update_Stop = new OutPort<>();
		public final InPort<JBool> D20_Con_MDM_Used = new InPort<>();
		public final OutPort<JPulse> T21_Ready_For_PDI_Connection = new OutPort<>();
		public final OutPort<EST_EfeS_State> D51_EST_EfeS_State = new OutPort<>();
	}
}
