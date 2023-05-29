package models.testing2;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import models.point.types.*;

/**
 * Eu.Doc.36
 * Requirements specification for subsystem Point; 2.7 (0.A)
 * Page 31
 */
public class B_SR {
	public static class Block extends Type1IBD {
		public final Initialization cOp1_Init = new Initialization(
			"T3_Cmd := FALSE;",
			"DT3_Pos := \"PointPos.NONE\";",
			"T4_Cmd := FALSE;",
			"DT4_Pos := \"PointPos.NONE\";"
		);
		
		public final InPort<JPulse> T3_Cmd = new InPort<>();
		public final InPort<PointPos> DT3_Pos = new InPort<>();
		public final OutPort<JPulse> T4_Cmd = new OutPort<>();
		public final OutPort<PointPos> DT4_Pos = new OutPort<>();
	}
}
