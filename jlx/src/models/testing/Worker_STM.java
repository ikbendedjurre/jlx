package models.testing;

import jlx.behave.*;

public class Worker_STM extends Worker.Block implements StateMachine {
	public static class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(STANDBY.class,
					"/counter := 0; start := FALSE;"
				)
			};
		}
	}
	
	public static class STANDBY extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(COMPUTING.class,
					"when(start)/"
				)
			};
		}
	}
	
	public static class COMPUTING extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(STANDBY.class,
					"/result := getResult(); notify := TRUE;"
				)
			};
		}
	}
}

