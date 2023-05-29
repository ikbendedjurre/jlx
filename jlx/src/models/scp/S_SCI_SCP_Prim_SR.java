package models.scp;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

/**
 * gitlab.git/formasig/Models/Model PDI protocol/EULYNX SysML models
 * Page 5
 */
public class S_SCI_SCP_Prim_SR {
	public static class Block extends Type1IBD {
		public final JBool Can_Retry = new JBool();
		
		public final InPort<JPulse> T1_Establish_SCP_Connection = new InPort<>();
		public final InPort<JPulse> T2_Terminate_SCP_Connection = new InPort<>();
		public final InPort<JPulse> T3_HB_inacceptable_delayed = new InPort<>();
		public final InPort<JPulse> T7_Conn_Resp = new InPort<>();
		public final OutPort<JPulse> T4_SCP_Connection_Established = new OutPort<>();
		public final OutPort<JPulse> T5_SCP_Connection_Terminated = new OutPort<>();
		public final OutPort<JPulse> T6_Conn_Req = new OutPort<>();
		public final OutPort<JPulse> T8_HB = new OutPort<>();
		public final OutPort<JPulse> T9_Disc_Req = new OutPort<>();
		public final InPort<JBool> D10_Error = new InPort<>();
		public final InPort<JPulse> T11_Disc_Req = new InPort<>();
		public final InPort<JInt> D100_Mem_Tmax = new InPort<>();
	}
}
