package models.lamp;

import jlx.behave.*;

public class Toggle_STD extends Toggle_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OFF.class, "/")
			};
		}
	}
	
	public class OFF extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition("Entry / D2_power := FALSE;");
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ON.class, "when(T1_toggle) /")
			};
		}
	}
	
	public class ON extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition("Entry / D2_power := TRUE;");
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OFF.class, "when(T1_toggle) /")
			};
		}
	}
}
