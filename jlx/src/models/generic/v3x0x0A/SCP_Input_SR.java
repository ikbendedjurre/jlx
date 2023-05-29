package models.generic.v3x0x0A;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

public class SCP_Input_SR {
	public static class Block extends Type1IBD {
		public final Initialization cOp1_init = new Initialization(
			"T1_SCP_Connection := FALSE;",
			"T2_SCP_Connection_Terminated := FALSE;",
			"T5_SCP_Connection := FALSE;",
			"T10_SCP_Connection_Terminated := FALSE;"
		);
		
		public final InPort<JPulse> T1_SCP_Connection = new InPort<>();
		public final InPort<JPulse> T2_SCP_Connection_Terminated = new InPort<>();
		public final OutPort<JPulse> T5_SCP_Connection = new OutPort<>();
		public final OutPort<JPulse> T10_SCP_Connection_Terminated = new OutPort<>();
	}
}
