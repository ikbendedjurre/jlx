package models.lx.simplified;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import models.lx.ExternalInputType;
import models.generic.types.*;

public class IL_IBD1 {
	public static class Block extends Type1IBD {
		public final JBool SCP_Connection_Established = new JBool();
		public final JBool SCP_Connection_Allowed = new JBool();
		
		public final Initialization cOp1_Init = new Initialization(
				"DT100_Type := \"ExternalInputType.Cd_Track_related_Activation\";", //Changed from ""
				"DT21_Type := \"ExternalInputType.Cd_Track_related_Activation\";", //Changed from ""
				"DT13_Result := \"PDI_Checksum_Result.Empty\";", //Changed from ""
				"DT13_Checksum_Data := \"PDI_Checksum_Data.D1\";", //Changed from ""
				"SCP_Connection_Established := FALSE;",
				"SCP_Connection_Allowed := FALSE;"
			);
			
		public final Operation<JVoid> cOp2_SCI_LX_GenerateCommand = new Operation<JVoid>(
			"if ExternalInputType = \"Cd_Track_related_Activation\" then",
			"DT100_Type := ExternalInputType;",
			"T100_Cd_XX := TRUE;", 
			"elseif ExternalInputType = \"Cd_Track_related_Deactivation\" then", 
			"DT100_Type := ExternalInputType;", 
			"T100_Cd_XX := TRUE;", 
			"elseif ExternalInputType = \"Cd_Control_Activation_Point\" then", 
			"DT100_Type := ExternalInputType;", 
			"T100_Cd_XX := TRUE;", 
			"elseif ExternalInputType = \"Cd_LX_Activation\" then", 
			"DT100_Type := ExternalInputType;", 
			"T100_Cd_XX := TRUE;", 
			"elseif ExternalInputType = \"Cd_LX_Deactivation\" then", 
			"DT100_Type := ExternalInputType;", 
			"T100_Cd_XX := TRUE;", 
			"elseif ExternalInputType = \"Cd_Block_LX\" then", 
			"DT100_Type := ExternalInputType;", 
			"T100_Cd_XX := TRUE;", 
			"elseif ExternalInputType = \"Cd_Track_Related_Isolation\" then", 
			"DT100_Type := ExternalInputType;", 
			"T100_Cd_XX := TRUE;", 
			"elseif ExternalInputType = \"Cd_Track_related_Prolong_Activation\" then", 
			"DT100_Type := ExternalInputType;", 
			"T100_Cd_XX := TRUE;", 
			"elseif ExternalInputType = \"Cd_Crossing_Clear\" then", 
			"DT100_Type := ExternalInputType;", 
			"T100_Cd_XX := TRUE;", 
			"end if"
		).addParam("ExternalInputType", new ExternalInputType());
		
		public final Operation<JVoid> cOp3_Internal_GenerateMessage = new Operation<JVoid>(
			"if ExternalInputType = \"Msg_LX_Failure_Status\" then", 
			"DT21_Type:= ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"elseif ExternalInputType = \"Msg_LX_Functional_Status\" then", 
			"DT21_Type:= ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"elseif ExternalInputType = \"Msg_LX_Monitoring_Status\" then", 
			"DT21_Type:= ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"elseif ExternalInputType = \"Msg_Track_related_Failure_Status\" then", 
			"DT21_Type:= ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"elseif ExternalInputType = \"Msg_Track_related_Functional_Status\" then", 
			"DT21_Type:= ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"elseif ExternalInputType = \"Msg_Track_related_Monitoring_Status\" then", 
			"DT21_Type:= ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"elseif ExternalInputType = \"Msg_Detection_Element_Status\" then", 
			"DT21_Type:= ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"elseif ExternalInputType = \"Msg_LX_Command_Admissibility\" then", 
			"DT21_Type:= ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"elseif ExternalInputType = \"Msg_Obstacle_Detection_Status\" then", 
			"DT21_Type:= ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"elseif ExternalInputType = \"Msg_Status_Of_Activation_Point\" then", 
			"DT21_Type:= ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"elseif ExternalInputType = \"Msg_Track_related_Command_Admissibility\" then", 
			"DT21_Type:= ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"end if"
		).addParam("ExternalInputType", new ExternalInputType());
		
		//First column:
		public final InPort<JPulse> T14_Own_Status_Report_Completed = new InPort<>();
		public final InPort<JPulse> T20_Internal_Input = new InPort<>();
		public final InPort<JPulse> T18_Adj_Status_Report_Complete = new InPort<>();
		public final InPort<JPulse> T101_Msg_XX = new InPort<>();
		public final InPort<ExternalInputType> DT20_Type = new InPort<>();
		public final InPort<ExternalInputType> DT101_Type = new InPort<>();
		
		//Second column:
		public final OutPort<JPulse> T13_Start_Status_Report = new OutPort<>();
		public final OutPort<JPulse> T17_Check_Received_Status = new OutPort<>();
		public final OutPort<JPulse> T21_Internal_Output = new OutPort<>();
		public final OutPort<ExternalInputType> DT21_Type = new OutPort<>();
		public final OutPort<JPulse> T100_Cd_XX = new OutPort<>();
		public final OutPort<ExternalInputType> DT100_Type = new OutPort<>();
		
		//First column:
		public final InPort<PDI_Version> D3_Con_PDI_Version = new InPort<>();
		public final InPort<PDI_Checksum_Data> D4_Con_Checksum_Data = new InPort<>();
		public final InPort<JBool> D23_Con_Checksum_Data_Used = new InPort<>();
		
		public final InPort<JPulse> T20_Protocol_Error = new InPort<>();
		public final InPort<JPulse> T21_Formal_Telegram_Error = new InPort<>();
		public final InPort<JPulse> T22_Content_Telegram_Error = new InPort<>();
		public final InPort<JPulse> T30_Reset_Connection = new InPort<>();
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
		
		public final InPort<JPulse> T3_HB_inacceptable_delayed = new InPort<>();
		public final InPort<JPulse> T5_Conn_Req = new InPort<>();
		public final OutPort<JPulse> T6_Conn_Resp = new OutPort<>();
		public final InPort<JPulse> T7_HB = new InPort<>();
		public final InPort<JPulse> T8_Disc_Req = new InPort<>();
		public final OutPort<JPulse> T9_Disc_Req = new OutPort<>();
		public final InPort<JInt> D100_Mem_Tmax = new InPort<>();
	}
}
