package models.point.machines;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import models.point.types.*;

public class F_PM_Gen {
	public static class Block extends Type1IBD {
		public final Operation<JVoid> cOp1_End_Position = new Operation<JVoid>(
			"return (D12_Position_In = \"LEFT\") or (D12_Position_In = \"RIGHT\");"
		);
		
		public final InPort<JBool> D11_Active = new InPort<>();
		public final InPort<PointPos> D12_Position_In = new InPort<>();
		public final InPort<JPulse> T2_Reset = new InPort<>();
		public final OutPort<PointPos> D1_Position_Out = new OutPort<>();
	}
}
