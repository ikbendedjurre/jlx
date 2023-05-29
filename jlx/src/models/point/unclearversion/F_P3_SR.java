package models.point.unclearversion;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import models.generic.types.EST_EfeS_State;
import models.point.types.*;

/**
 * Requirements specification for subsystem Point (v3.0/3A)
 * Page 36
 */
public class F_P3_SR extends Type1IBD {
	public static class Block {
		public final static PointPos Mem_last_Target_Requested = new PointPos();
		public final static PointPos Mem_Current_Point_Position = new PointPos();
		
		public final static Initialization cOp1_Init = new Initialization(
			"T4_Information_No_End_Position := FALSE;",
			"T5_Info_End_Position_Arrived := FALSE;",
			"D5_Drive_State := \"STOPPED\";",
			"D6_Detection_State := \"\";",
			"T6_Information_Trailed_Point := FALSE;",
			"T7_Information_Out_Of_Sequence := FALSE;",
			"D10_Move_Left := FALSE;",
			"D11_Move_Right := FALSE;",
			"T20_Point_Position := FALSE;",
			"DT20_Point_Position := \"UNKNOWN\";",
			"D25_Redrive := FALSE;",
			"T30_Report_Timeout := FALSE;",
			"Mem_last_Target_Requested := \"\";",
			"Mem_Current_Point_Position := \"\";"
		);
		
		public final static Operation<JBool> cOp2_All_Left = new Operation<>(
			"if cOp8_Supports_Multiple_PMs() then",
			"  return (",
			"    D21_PM1_Position = \"LEFT\" and",
			"    (D22_PM2_Position = \"LEFT\" or D13_PM2_Activation = \"INACTIVE\")",
			"  );",
			"else",
			"  return D21_PM1_Position = \"LEFT\";",
			"end if"
		);
		
		public final static Operation<JBool> cOp3_No_End_Position = new Operation<>(
			"return not cOp6_Error() and not cOp5_Trailed() and not cOp2_All_Left() and not cOp4_All_Right();"
		);
		
		public final static Operation<JBool> cOp4_All_Right = new Operation<>(
			"if cOp8_Supports_Multiple_PMs() then",
			"  return (",
			"    D21_PM1_Position = \"RIGHT\" and",
			"    (D22_PM2_Position = \"RIGHT\" or D13_PM2_Activation = \"INACTIVE\")",
			"  );",
			"else",
			"  return D21_PM1_Position = \"RIGHT\";",
			"end if"
		);
		
		public final static Operation<JBool> cOp5_Trailed = new Operation<>(
			"if not cOp6_Error() then",
			"  if cOp8_Supports_Multiple_PMs() then",
			"    return D21_PM1_Position = \"TRAILED\" or D22_PM2_Position = \"TRAILED\";",
			"  else",
			"    return D21_PM1_Position = \"TRAILED\";",
			"  end if",
			"else",
			"  return FALSE;",
			"end if"
		);
		
		public final static Operation<JBool> cOp6_Error = new Operation<>(
			"if cOp8_Supports_Multiple_PMs() then",
			"  return D21_PM1_Position = \"ERROR\" or D22_PM2_Position = \"ERROR\";",
			"else",
			"  return D21_PM1_Position = \"ERROR\";",
			"end if"
		);
		
		public final static Operation<JBool> cOp7_Is_Trailable = new Operation<>(
			"return D32_Con_007600 or D33_Con_007900 or D34_Con_008000 or D35_Con_008200;"
		);
		
		public final static Operation<JBool> cOp8_Supports_Multiple_PMs = new Operation<>(
			"return D30_Con_007000 or D32_Con_007600 or D33_Con_007900 or D34_Con_008000 or D35_Con_008200 or D37_Con_008400;"
		);
		
		public final static Operation<JBool> cOp9_Redrive_Enabled = new Operation<>(
			"return D30_Con_007000;"
		);
		
		public final static Operation<JBool> cOp10_Redrive_Right = new Operation<>(
			"return cOp3_No_End_Position() and Mem_last_Target_Requested = \"RIGHT\" and Mem_Current_Point_Position = \"RIGHT\";"
		);
		
		public final static Operation<JBool> cOp11_Redrive_Left = new Operation<>(
			"return cOp3_No_End_Position() and Mem_last_Target_Requested = \"LEFT\" and Mem_Current_Point_Position = \"LEFT\";"
		);
		
		public final static Operation<JVoid> cOp12_Timeout = new Operation<>(
			"D5_Drive_State := \"STOPPED\";",
			"if D32_Con_007600 or D33_Con_007900 or D34_Con_008000 or D35_Con_008200 or D37_Con_008400 then",
			"  T30_Report_Timeout := TRUE;",
			"end if"
		);
		
		public final static Operation<JBool> cOp13_Not_Initialised = new Operation<>(
			"return D20_F_EST_EfeS_Gen_SR_State = \"NO_OPERATING_VOLTAGE\"",
			"  or D20_F_EST_EfeS_Gen_SR_State = \"BOOTING\"",
			"  or D20_F_EST_EfeS_Gen_SR_State = \"FALLBACK_MODE\";"
		);
		
		//First column:
		public final InPort<JPulse> T1_Move = new InPort<>();
		public final InPort<PointPos> DT1_Move_Target = new InPort<>();
		public final InPort<JPulse> T2_Stop_Operation = new InPort<>();
		public final InPort<EST_EfeS_State> D20_F_EST_EfeS_Gen_SR_State = new InPort<>();
		public final InPort<PointPos> D21_PM1_Position = new InPort<>();
		public final InPort<PointPos> D22_PM2_Position = new InPort<>();
		public final InPort<JInt> D4_Con_tmax_Point_Operation = new InPort<>();
		public final InPort<JPulse> T40_Report_Status = new InPort<>();
		public final InPort<PM2_Activation> D13_PM2_Activation = new InPort<>();
		public final InPort<JBool> D30_Con_007000 = new InPort<>();
		public final InPort<JBool> D32_Con_007600 = new InPort<>();
		public final InPort<JBool> D33_Con_007900 = new InPort<>();
		public final InPort<JBool> D34_Con_008000 = new InPort<>();
		public final InPort<JBool> D35_Con_008200 = new InPort<>();
		public final InPort<JBool> D36_Con_008300 = new InPort<>();
		public final InPort<JBool> D37_Con_008400 = new InPort<>();
		public final InPort<JBool> D38_Con_008500 = new InPort<>();
		
		//Second column:
		public final OutPort<JBool> D10_Move_Left = new OutPort<>();
		public final OutPort<JBool> D11_Move_Right = new OutPort<>();
		public final OutPort<JPulse> T4_Information_No_End_Position = new OutPort<>();
		public final OutPort<JPulse> T5_Info_End_Position_Arrived = new OutPort<>();
		public final OutPort<PointPos> DT20_Point_Position = new OutPort<>();
		public final OutPort<DriveState> D5_Drive_State = new OutPort<>();
		public final OutPort<JPulse> T6_Information_Trailed_Point = new OutPort<>();
		public final OutPort<JPulse> T7_Information_Out_Of_Sequence = new OutPort<>();
		public final OutPort<JPulse> T20_Point_Position = new OutPort<>();
		public final OutPort<JPulse> T30_Report_Timeout = new OutPort<>();
		public final OutPort<Detection_State> D6_Detection_State = new OutPort<>();
		public final OutPort<JBool> D25_Redrive = new OutPort<>();
	}
}
