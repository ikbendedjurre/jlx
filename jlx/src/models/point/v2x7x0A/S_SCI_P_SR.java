package models.point.v2x7x0A;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import models.generic.types.PDI_Connection_State;
import models.point.types.*;

/**
 * Eu.Doc.36
 * Requirements specification for subsystem Point; 2.7 (0.A)
 * Page 31
 */
public class S_SCI_P_SR {
	public static class Block extends Type1IBD {
		public final PointPos Mem_Move_Point = new PointPos(); //Missing in diagram!
		public final PointPos Mem_Point_Position = new PointPos(); //Missing in diagram!
		//public final PointPos Mem_Last_DT2 = new PointPos(); //Added b/c DT2 can only be read when T2 fires.
		
		public final Initialization cOp1_Init = new Initialization(
			"T1_Cd_Move_Point := FALSE;",
			"DT1_Move_Point_Target := \"PointPos.NONE\";",
			"T20_Point_Position := FALSE;",
			"DT20_Point_Position := \"PointPos.NONE\";",
			"T30_Timeout := FALSE;",
			"Mem_Move_Point := \"PointPos.NONE\";",
			"Mem_Point_Position := \"NO_END_POSITION\";"
			//, "Mem_Last_DT2 := \"NO_END_POSITION\";" //Added.
		);
		
		//First column:
		public final InPort<JPulse> T2_Msg_Point_Position = new InPort<>();
		//public final InPort<PointPos> DT2_Point_Position = new InPort<>();
		public final InPort<PointPos> D2_Point_Position = new InPort<>(); //Replaces line above.
		public final InPort<JPulse> T3_Msg_Timeout = new InPort<>();
		public final InPort<JPulse> T10_Move_Point = new InPort<>();
		public final InPort<PointPos> DT10_Move_Point = new InPort<>();
		public final InPort<PDI_Connection_State> D21_S_SCI_EfeS_Gen_SR_State = new InPort<>();
		
		//Second column:
		public final OutPort<JPulse> T1_Cd_Move_Point = new OutPort<>();
		public final OutPort<PointPos> DT1_Move_Point_Target = new OutPort<>();
		public final OutPort<JPulse> T20_Point_Position = new OutPort<>();
		public final OutPort<PointPos> DT20_Point_Position = new OutPort<>();
		public final OutPort<JPulse> T30_Timeout = new OutPort<>();
	}
}
