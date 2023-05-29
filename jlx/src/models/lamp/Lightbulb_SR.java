package models.lamp;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

public class Lightbulb_SR {
	public static class Block extends Type1IBD {
		public final InPort<JBool> D1_power = new InPort<>();
		public final OutPort<JBool> D2_givesLight = new OutPort<>();
		
	}
}

