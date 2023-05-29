package models.lamp;

import jlx.behave.*;

public class Lightbulb_STD extends Lightbulb_SR.Block implements StateMachine {
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
			return new LocalTransition("Entry / D2_givesLight := FALSE;");
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ON.class, "[D1_power] /")
			};
		}
	}
	
	public class ON extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition("Entry / D2_givesLight := TRUE;");
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OFF.class, "[not D1_power] /")
			};
		}
	}
}
