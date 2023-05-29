package models.generic.v3x0x0A;

import jlx.behave.*;

public class SCP_Input_STD extends SCP_Input_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(MAIN.class, "/ cOp1_init();")
			};
		}
	}
	
	public class MAIN extends State {
		@Override
		public LocalTransition[] onDo() {
			return new LocalTransition[] {
				new LocalTransition("when(T1_SCP_Connection) / T5_SCP_Connection := TRUE;"),
				new LocalTransition("when(T2_SCP_Connection_Terminated) / T10_SCP_Connection_Terminated := TRUE;")
			};
		}
	}
}
