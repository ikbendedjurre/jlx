package models.pendeltrain.j;

import jlx.behave.*;

public class Infrastructure_STD extends Infrastructure_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(TRAIN_AT_S1.class, none())
			};
		}
	}
	
	public class TRAIN_AT_S1 extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				entry(
					assign(oldTrainPos, TrainPos.S1),
					assign(D2_signal1, SignalColor.GREEN),
					assign(D3_signal2, SignalColor.RED)
				)
			);
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OPERATIONAL.class, guard(neq(D1_trainPos, oldTrainPos))),
				new Outgoing(BREAKDOWN.class, when(T4_breakdown))
			};
		}
	}
	
	public class OPERATIONAL extends CompositeState {
		public class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Junction0.class, none())
				};
			}
		}
		
		public class Junction0 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(TRAIN_AT_S1.class, guard(eq(D1_trainPos, TrainPos.S1))),
					new Outgoing(TRAIN_AT_S2.class, guard(eq(D1_trainPos, TrainPos.S2)))
				};
			}
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OPERATIONAL.class, guard(neq(D1_trainPos, oldTrainPos)))
			};
		}
		
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				entry(assign(oldTrainPos, D1_trainPos))
			);
		}
		
		public class TRAIN_AT_S1 extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					entry(assign(D2_signal1, SignalColor.GREEN), assign(D3_signal2, SignalColor.RED))
				);
			}
		}
		
		public class TRAIN_AT_S2 extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					entry(assign(D2_signal1, SignalColor.RED), assign(D3_signal2, SignalColor.GREEN))
				);
			}
		}
	}
	
	public class BREAKDOWN extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				entry(assign(D2_signal1, SignalColor.RED), assign(D3_signal2, SignalColor.RED))
			);
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(OPERATIONAL.class, when(T4_breakdown))
			};
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OPERATIONAL.class, when(T5_repair))
			};
		}
	}
}
