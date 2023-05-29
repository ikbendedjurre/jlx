package models.scp;

import jlx.behave.*;

public class SCI_SCP_Prim_STD_1 extends S_SCI_SCP_Prim_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(CLOSED.class, "/")
			};
		}
	}
	
	public class CLOSED extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(START.class, "when(T1_Establish_SCP_Connection) / T6_Conn_Req := TRUE;"),
			};
		}
	}
	
	public class RETRY extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(START.class, "when(Can_Retry)/ T6_Conn_Req := TRUE;"),
			};
		}
	}
	
	public class START extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(Junction2.class, "after(D100_Mem_Tmax) / "),
				new Outgoing(UP.class, "when(T7_Conn_Resp) / T8_HB := TRUE; T4_SCP_Connection_Established := TRUE; "),
				new Outgoing(Junction2.class, "when(T1_Establish_SCP_Connection) /"),
				new Outgoing(Junction1.class, "when(T2_Terminate_SCP_Connection) / "),
				new Outgoing(RETRY.class, "when(T11_Disc_Req) / Can_Retry := TRUE;Can_Retry := FALSE;"),
			};
		}
	}

	
	public class UP extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(Junction1.class, "when(T2_Terminate_SCP_Connection) / "),
				new Outgoing(Junction1.class, "when(T1_Establish_SCP_Connection) / "),
				new Outgoing(Junction1.class, "after(D100_Mem_Tmax) / "),
				new Outgoing(Junction1.class, "when(T7_Conn_Resp) / "),
				new Outgoing(CLOSED.class, "when(T11_Disc_Req) / T5_SCP_Connection_Terminated := TRUE;"),
			};
		}
	}
	
	public class Junction1 extends JunctionVertex {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(CLOSED.class, "/ T9_Disc_Req := TRUE; T5_SCP_Connection_Terminated := TRUE;")
			};
		}
	}
	
	public class Junction2 extends JunctionVertex {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(RETRY.class, "/ T9_Disc_Req := TRUE; Can_Retry := TRUE;Can_Retry := FALSE;")
			};
		}
	}
}

