package models.lamp.j;

import jlx.asal.j.*;
import jlx.behave.*;

public class Lightbulb_STD extends Lightbulb_SR.Block implements StateMachine {
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
				entry(assign(D2_givesLight, JBool.FALSE))
			);
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ON.class, guard(eq(D1_power, JBool.TRUE)))
			};
		}
	}
	
	public class ON extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				entry(assign(D2_givesLight, JBool.TRUE))
			);
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OFF.class, guard(eq(D1_power, JBool.FALSE)))
			};
		}
	}
}
