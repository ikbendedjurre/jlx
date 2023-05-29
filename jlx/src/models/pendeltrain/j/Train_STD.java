package models.pendeltrain.j;

import jlx.asal.j.*;
import jlx.behave.*;

public class Train_STD extends Train_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(AT_S1.class, none())
			};
		}
	}
	
	public class AT_S1 extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(entry(assign(D2_moving, JBool.FALSE), assign(D3_pos, TrainPos.S1)));
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(MOVING_1.class, when(eq(D4_signal1, SignalColor.GREEN)))
			};
		}
	}
	
	public class AT_S2 extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(entry(assign(D2_moving, JBool.FALSE), assign(D3_pos, TrainPos.S2)));
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(MOVING_2.class, when(eq(D5_signal2, SignalColor.GREEN)))
			};
		}
	}
	
	public class MOVING_1 extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(entry(assign(D2_moving, JBool.TRUE)));
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(AT_S2.class, when(T1_arrival))
			};
		}
	}
	
	public class MOVING_2 extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(entry(assign(D2_moving, JBool.TRUE)));
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(AT_S1.class, when(T1_arrival))
			};
		}
	}
}
