package models.testing3;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

public class AndIBD1 {
	public static class Block extends Type1IBD {
		public final JBool conjunct = new JBool.TRUE();
		public final InPort<JPulse> T1_Input = new InPort<>();
		public final InPort<JBool> DT1_Input = new InPort<>();
		public final OutPort<JPulse> T2_Output = new OutPort<>();
		public final OutPort<JBool> DT2_Output = new OutPort<>();
	}
}
