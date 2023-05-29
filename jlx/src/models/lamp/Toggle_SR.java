package models.lamp;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

public class Toggle_SR {
	public static class Block extends Type1IBD {
		public final InPort<JPulse> T1_toggle = new InPort<>();
		public final OutPort<JBool> D2_power = new OutPort<>();
	}
}

