package models.pendeltrain;

import jlx.behave.*;

public class Train_STD extends Train_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(AT_S1.class, "/")
			};
		}
	}
	
	public class AT_S1 extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition("Entry / D2_moving := FALSE; D3_pos := \"S1\";");
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(MOVING_1.class, "when(D4_signal1 = \"GREEN\") /")
			};
		}
	}
	
	public class AT_S2 extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition("Entry / D2_moving := FALSE; D3_pos := \"S2\";");
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(MOVING_2.class, "when(D5_signal2 = \"GREEN\") /")
			};
		}
	}
	
	public class MOVING_1 extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition("Entry / D2_moving := TRUE;");
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(AT_S2.class, "when(T1_arrival) /")
			};
		}
	}
	
	public class MOVING_2 extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition("Entry / D2_moving := TRUE;");
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(AT_S1.class, "when(T1_arrival) /")
			};
		}
	}
}
