package models.point.v2x7x0A;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import models.generic.types.PDI_Connection_State;
import models.point.types.*;

/**
 * Eu.Doc.36
 * Requirements specification for subsystem Point; 2.7 (0.A)
 * Page 33
 */
public class F_SCI_P_SR {
	public static class Block extends Type1IBD {
		public final PointPos Mem_Move_Point = new PointPos(); //Missing from diagram!
		public final PointPos Mem_Point_Position = new PointPos(); //Missing from diagram!
//		public final PointPos Mem_Last_DT20 = new PointPos(); //Added b/c DT20 can only be read when T20 fires.
		
		public final Initialization cOp1_init = new Initialization(
			"T2_Msg_Point_Position := FALSE;",
			//"DT2_Point_Position := \"PointPos.NONE\";", //Changed enum.
			"D2_Point_Position := \"PointPos.NONE\";", //Changed enum. + Replaces line above.
			"T3_Msg_Timeout := FALSE;",
			"T10_Move := FALSE;",
			"DT10_Move_Target := \"PointPos.NONE\";", //Changed enum.
			"T11_Stop_Operation := FALSE;",
			"T23_Sending_Status_Report_Completed := FALSE;",
			"T40_Send_Status_Report := FALSE;",
			"Mem_Move_Point := \"PointPos.NONE\";", //Changed enum.
			"Mem_Point_Position := \"NO_END_POSITION\";"
			//, "Mem_Last_DT20 := \"NO_END_POSITION\";"
		);
		
		//First column:
		public final InPort<JPulse> T1_Cd_Move_Point = new InPort<>();
		public final InPort<PointPos> DT1_Move_Point_Target = new InPort<>();
		public final InPort<JPulse> T20_Point_Position = new InPort<>();
		//public final InPort<PointPos> DT20_Point_Position = new InPort<>();
		public final InPort<PointPos> D20_Point_Position = new InPort<>();
		public final InPort<JPulse> T18_Start_Status_Report = new InPort<>();
		public final InPort<JPulse> T30_Report_Timeout = new InPort<>();
		public final InPort<PDI_Connection_State> D21_F_SCI_EfeS_Gen_SR_State = new InPort<>();
		
		//Second column:
		public final OutPort<JPulse> T2_Msg_Point_Position = new OutPort<>();
		//public final OutPort<PointPos> DT2_Point_Position = new OutPort<>();
		public final OutPort<PointPos> D2_Point_Position = new OutPort<>(); //Replaces line above.
		public final OutPort<JPulse> T3_Msg_Timeout = new OutPort<>();
		public final OutPort<JPulse> T10_Move = new OutPort<>();
		public final OutPort<PointPos> DT10_Move_Target = new OutPort<>();
		public final OutPort<JPulse> T11_Stop_Operation = new OutPort<>();
		public final OutPort<JPulse> T40_Send_Status_Report = new OutPort<>();
		public final OutPort<JPulse> T23_Sending_Status_Report_Completed = new OutPort<>();
	}
}
