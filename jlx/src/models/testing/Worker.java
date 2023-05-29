package models.testing;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

public class Worker {
	public static class Block extends Type1IBD {
		public final static JInt counter = new JInt();
		
		public final static Operation<JBool> getResult = new Operation<JBool>(
			"if counter%2 = 0 then",
			"	counter := counter + 1;",
			"	return TRUE;",
			"else",
			"	counter := counter + 1;",
			"	return TRUE;",
			"end if"
		);
		
		public final InPort<JPulse> start = new InPort<>();
		public final OutPort<JPulse> notify = new OutPort<>();
		public final OutPort<JBool> result = new OutPort<>();
	}
}
