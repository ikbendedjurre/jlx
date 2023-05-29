package models.testing2;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import models.point.types.*;

/**
 * Eu.Doc.36
 * Requirements specification for subsystem Point; 2.7 (0.A)
 * Page 31
 */
public class A_SR {
	public static class Block extends Type1IBD {
		public final Initialization cOp1_Init = new Initialization(
			"T1_Cmd := FALSE;",
			"DT1_Pos := \"PointPos.NONE\";",
			"T2_Cmd := FALSE;",
			"DT2_Pos := \"PointPos.NONE\";"
		);
		
		public final InPort<JPulse> T1_Cmd = new InPort<>();
		public final InPort<PointPos> DT1_Pos = new InPort<>();
		public final OutPort<JPulse> T2_Cmd = new OutPort<>();
		public final OutPort<PointPos> DT2_Pos = new OutPort<>();
	}
}
