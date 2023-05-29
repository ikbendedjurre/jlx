package models.testing;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

public class Main {
	public static class Block extends Type1IBD {
		public final static JInt debug = new JInt();
		public final static JBool sleep = new JBool();
		
		public final InPort<JPulse> request = new InPort<>();
		public final OutPort<JPulse> t_result = new OutPort<>();
		public final OutPort<JInt> dt_result1 = new OutPort<>();
		public final OutPort<Result> dt_result2 = new OutPort<>();
		
		public final OutPort<JPulse> start = new OutPort<>();
		
		public final InPort<JBool> value1 = new InPort<>();
		public final InPort<JPulse> finished1 = new InPort<>();
		public final InPort<JBool> value2 = new InPort<>();
		public final InPort<JPulse> finished2 = new InPort<>();
	}
}
