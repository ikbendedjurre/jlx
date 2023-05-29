package models.generic.v3x0x0A;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

/**
 * Eu.Doc.20
 * Generic interface and subsystem requirements; 3.0 (0.A)
 * Page 6
 */
public class F_EST_EfeS {
	public static class Block extends Type1IBD {
		//All of these properties are missing from diagram:
		public final JInt Con_t_Ini_Def_Delay = new JInt();
		public final JInt Con_t_Ini_Max = new JInt();
		public final JInt Con_t_Ini_Step = new JInt();
		public final JInt Con_t_Max_Booting = new JInt();
		public final JInt Mem_t_Ini_Delay = new JInt();
		public final JInt Con_tmax_DataInstallation = new JInt();
		public final JInt Con_tmax_DataTransmission = new JInt();
		public final JInt Con_tmax_PDI_Connection = new JInt();
		public final JInt Con_tmax_Response_MDM = new JInt();
		
		//Left side:
		public final InPort<JPulse> T1_Power_On_Detected = new InPort<>();
		public final InPort<JPulse> T2_Power_Off_Detected = new InPort<>();
		public final InPort<JPulse> T3_Reset = new InPort<>();
		public final InPort<JPulse> T4_Booted = new InPort<>();
		public final InPort<JPulse> T5_SIL_Not_Fulfilled = new InPort<>();
		public final InPort<JPulse> T6_Data_Invalid = new InPort<>();
		public final InPort<JPulse> T7_Invalid_Or_Missing_Basic_Data = new InPort<>();
		public final InPort<JPulse> T8_Data_Installation_Complete = new InPort<>();
		public final InPort<JPulse> T9_PDI_Connection_Established = new InPort<>();
		public final InPort<JPulse> T10_SCP_Connection_Terminated = new InPort<>();
		//Commented because simulator does not use it:
		//public final InPort<Pulse> T11_Data_Transmission_Timeout = new InPort<>();
		
		//Right side:
		public final OutPort<JPulse> T12_Terminate_SCP_Connection = new OutPort<>();
	}
}

