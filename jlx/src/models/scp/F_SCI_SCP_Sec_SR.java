package models.scp;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

/**
 * gitlab.git/formasig/Models/Model PDI protocol/EULYNX SysML models
 * Page 5
 */
public class F_SCI_SCP_Sec_SR {
	public static class Block extends Type1IBD {
		public final JBool Can_Try = new JBool();
		
		public final OutPort<JPulse> T1_SCP_Connection_Established = new OutPort<>();
		public final OutPort<JPulse> T2_SCP_Connection_Terminated = new OutPort<>();
		public final InPort<JPulse> T3_HB_inacceptable_delayed = new InPort<>();
		public final InPort<JPulse> T4_Terminate_SCP_Connection = new InPort<>();
		public final InPort<JPulse> T5_Conn_Req = new InPort<>();
		public final OutPort<JPulse> T6_Conn_Resp = new OutPort<>();
		public final InPort<JPulse> T7_HB = new InPort<>();
		public final InPort<JPulse> T8_Disc_Req = new InPort<>();
		public final OutPort<JPulse> T9_Disc_Req = new OutPort<>();
		//public final InPort<JPulse> T10_Establish_SCP_Connection = new InPort<>();
		public final InPort<JInt> D100_Mem_Tmax = new InPort<>();
	}
}
