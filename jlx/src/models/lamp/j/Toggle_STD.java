package models.lamp.j;

import jlx.asal.j.*;
import jlx.behave.*;

public class Toggle_STD extends Toggle_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OFF.class, none())
			};
		}
	}
	
	public class OFF extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				entry(assign(D2_power, JBool.FALSE))
			);
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ON.class, when(T1_toggle))
			};
		}
	}
	
	public class ON extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				entry(assign(D2_power, JBool.TRUE))
			);
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OFF.class, when(T1_toggle))
			};
		}
	}
}
