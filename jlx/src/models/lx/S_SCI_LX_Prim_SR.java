package models.lx;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

/**
 * Requirements specification SCI-LX Eu.Doc.111 v1.0
 * Page 17/23
 */
public class S_SCI_LX_Prim_SR {
	public static class Block extends Type1IBD {
		public final Initialization cOp1_Init = new Initialization(
			"DT100_Type := \"ExternalInputType.Empty\";",
			"DT21_Type := \"ExternalInputType.Empty\";"
		);
		
		public final Operation<JVoid> cOp2_SCI_LX_GenerateCommand = new Operation<JVoid>(
			"if ExternalInputType = \"Cd_Track_related_Activation\" then", 
			"DT21_Type := ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"elseif ExternalInputType = \"Cd_Track_related_Deactivation\" then", 
			"DT21_Type := ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"elseif ExternalInputType = \"Cd_Control_Activation_Point\" then", 
			"if (D1001_Con_IM_007900 or D1002_Con_IM_008000) then", 
			"DT21_Type := ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"end if", 
			"elseif ExternalInputType = \"Cd_LX_Activation\" then", 
			"if (D1001_Con_IM_007900 or D1003_Con_IM_008200) then", 
			"DT21_Type := ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"end if", 
			"elseif ExternalInputType = \"Cd_LX_Deactivation\" then", 
			"if (D1001_Con_IM_007900 or D1003_Con_IM_008200) then", 
			"DT21_Type := ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"end if", 
			"elseif ExternalInputType = \"Cd_Block_LX\" then", 
			"if (D1002_Con_IM_008000) then", 
			"DT21_Type := ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"end if", 
			"elseif ExternalInputType = \"Cd_Track_Related_Isolation\" then", 
			"if (D1001_Con_IM_007900) then", 
			"DT21_Type := ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"end if", 
			"elseif ExternalInputType = \"Cd_Track_related_Prolong_Activation\" then", 
			"if (D1001_Con_IM_007900 or D1002_Con_IM_008000) then", 
			"DT21_Type := ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"end if", 
			"elseif ExternalInputType = \"Cd_Crossing_Clear\" then", 
			"if (D1002_Con_IM_008000) then", 
			"DT21_Type := ExternalInputType;", 
			"T21_Internal_Output := TRUE;", 
			"end if",
			"end if"
		).addParam("ExternalInputType", new ExternalInputType());
		
		public final Operation<JVoid> cOp3_Internal_GenerateMessage = new Operation<JVoid>(
			"if ExternalInputType= \"Msg_LX_Failure_Status\" then", 
			"DT100_Type:= ExternalInputType;", 
			"T100_Msg_XX := TRUE;", 
			"elseif ExternalInputType = \"Msg_LX_Functional_Status\" then", 
			"DT100_Type:= ExternalInputType;", 
			"T100_Msg_XX := TRUE;", 
			"elseif ExternalInputType = \"Msg_LX_Monitoring_Status\" then", 
			"DT100_Type:= ExternalInputType;", 
			"T100_Msg_XX := TRUE;", 
			"elseif ExternalInputType = \"Msg_Track_related_Failure_Status\" then", 
			"DT100_Type:= ExternalInputType;", 
			"T100_Msg_XX := TRUE;", 
			"elseif ExternalInputType = \"Msg_Track_related_Functional_Status\" then", 
			"DT100_Type:= ExternalInputType;", 
			"T100_Msg_XX := TRUE;", 
			"elseif ExternalInputType = \"Msg_Track_related_Monitoring_Status\" then", 
			"DT100_Type:= ExternalInputType;", 
			"T100_Msg_XX := TRUE;", 
			"elseif ExternalInputType = \"Msg_Detection_Element_Status\" then", 
			"if (D1001_Con_IM_007900) then", 
			"DT100_Type:= ExternalInputType;", 
			"T100_Msg_XX := TRUE;", 
			"end if", 
			"elseif ExternalInputType = \"Msg_LX_Command_Admissibility\" then", 
			"if (D1002_Con_IM_008000 or D1003_Con_IM_008200) then", 
			"DT100_Type:= ExternalInputType;",
			"T100_Msg_XX := TRUE;", 
			"end if", 
			"elseif ExternalInputType = \"Msg_Obstacle_Detection_Status\" then", 
			"if (D1002_Con_IM_008000) then", 
			"DT100_Type:= ExternalInputType;", 
			"T100_Msg_XX := TRUE;", 
			"end if", 
			"elseif ExternalInputType = \"Msg_Status_Of_Activation_Point\" then", 
			"if (D1002_Con_IM_008000) then", 
			"DT100_Type:= ExternalInputType;", 
			"T100_Msg_XX := TRUE;", 
			"end if", 
			"elseif ExternalInputType = \"Msg_Track_related_Command_Admissibility\" then", 
			"if (D1002_Con_IM_008000 or D1003_Con_IM_008200) then", 
			"DT100_Type:= ExternalInputType;", 
			"T100_Msg_XX := TRUE;", 
			"end if", 
			"end if"
		).addParam("ExternalInputType", new ExternalInputType());
		
		//First column:
		public final InPort<JBool> D1001_Con_IM_007900 = new InPort<>();
		public final InPort<JBool> D1002_Con_IM_008000 = new InPort<>();
		public final InPort<JBool> D1003_Con_IM_008200 = new InPort<>();
		public final InPort<JPulse> T1_PDI_Connection_Established = new InPort<>();
		public final InPort<JPulse> T2_PDI_Connection_Closed = new InPort<>();
		public final InPort<JPulse> T3_Establishing_PDI_Connection = new InPort<>();
		public final InPort<JPulse> T18_Check_Sec_Status = new InPort<>();
		public final InPort<JPulse> T20_Internal_Input = new InPort<>();
		public final InPort<ExternalInputType> DT20_Type = new InPort<>();
		public final InPort<JPulse> T22_Adj_Status_Report_Complete = new InPort<>();
		public final InPort<JPulse> T24_Start_Prim_Status_Report = new InPort<>();
		public final InPort<JPulse> T25_Own_Status_Report_Completed = new InPort<>();
		public final InPort<JPulse> T101_Msg_XX = new InPort<>(); //Used to be T101_Cd_XX.
		public final InPort<ExternalInputType> DT101_Type = new InPort<>();
		
		//Second column:
		public final OutPort<JPulse> T16_Start_Status_Report = new OutPort<>();
		public final OutPort<JPulse> T19_Check_Received_Status = new OutPort<>();
		public final OutPort<JPulse> T21_Internal_Output = new OutPort<>();
		public final OutPort<ExternalInputType> DT21_Type = new OutPort<>();
		public final OutPort<JPulse> T23_Sec_Status_Report_Complete = new OutPort<>();
		public final OutPort<JPulse> T26_Prim_Status_Report_Completed = new OutPort<>();
		public final OutPort<JPulse> T100_Msg_XX = new OutPort<>();
		public final OutPort<ExternalInputType> DT100_Type = new OutPort<>();
	}
}
