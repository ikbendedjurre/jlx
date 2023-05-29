package models.scp;

import jlx.behave.*;
//Server in RaSTA
public class SCI_SCP_Sec_STD_1 extends F_SCI_SCP_Sec_SR.Block implements StateMachine {
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
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"Can_Try := TRUE;Can_Try := FALSE;"
			);
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(DOWN.class, "when(Can_Try)/")
			};
		}
	}
	
	public class DOWN extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(START.class, "when(T5_Conn_Req) / T6_Conn_Resp := TRUE;"),
				new Outgoing(CLOSED.class, "when(T4_Terminate_SCP_Connection) / T2_SCP_Connection_Terminated := TRUE;"),
			};
		}
	}
	
	public class START extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(CLOSED.class, "when(T8_Disc_Req) /"),
				new Outgoing(Junction2.class, "after(D100_Mem_Tmax) /"),
				new Outgoing(Junction2.class, "when(T5_Conn_Req) /"),
				new Outgoing(Junction1.class, "when(T4_Terminate_SCP_Connection)/"),
				new Outgoing(UP.class, "when(T7_HB) / T1_SCP_Connection_Established := TRUE;")
			};
		}
	}
	
	public class UP extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
					new Outgoing(CLOSED.class, "when(T8_Disc_Req) / T2_SCP_Connection_Terminated := TRUE;"),
					new Outgoing(Junction1.class, "when(T3_HB_inacceptable_delayed)/"),
					new Outgoing(Junction1.class, "when(T5_Conn_Req)/"),
					new Outgoing(Junction1.class, "when(T4_Terminate_SCP_Connection)/"),
			};
		}
	}
	
	public class Junction1 extends JunctionVertex {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(CLOSED.class, "/ T9_Disc_Req := TRUE; T2_SCP_Connection_Terminated := TRUE;")
			};
		}
	}
	
	public class Junction2 extends JunctionVertex {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(CLOSED.class, "/ T9_Disc_Req := TRUE;")
			};
		}
	}
}

