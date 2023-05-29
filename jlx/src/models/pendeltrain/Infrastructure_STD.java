package models.pendeltrain;

import jlx.behave.*;

public class Infrastructure_STD extends Infrastructure_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(TRAIN_AT_S1.class, "/")
			};
		}
	}
	
	public class TRAIN_AT_S1 extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry / oldTrainPos := \"S1\";",
				"D2_signal1 := \"GREEN\";",
				"D3_signal2 := \"RED\";");
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OPERATIONAL.class, "when(D1_trainPos <> oldTrainPos) /"),
				new Outgoing(BREAKDOWN.class, "when(T4_breakdown) /")
			};
		}
	}
	
	public class OPERATIONAL extends CompositeState {
		public class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Junction0.class, "/")
				};
			}
		}
		
		public class Junction0 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(TRAIN_AT_S1.class, "[D1_trainPos = \"S1\"] /"),
					new Outgoing(TRAIN_AT_S2.class, "[D1_trainPos = \"S2\"] /")
				};
			}
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OPERATIONAL.class, "[D1_trainPos <> oldTrainPos] /")
			};
		}
		
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition("Entry / oldTrainPos := D1_trainPos;");
		}
		
		public class TRAIN_AT_S1 extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition("Entry / D2_signal1 := \"GREEN\"; D3_signal2 := \"RED\";");
			}
		}
		
		public class TRAIN_AT_S2 extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition("Entry / D2_signal1 := \"RED\"; D3_signal2 := \"GREEN\";");
			}
		}
	}
	
	public class BREAKDOWN extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition("Entry / D2_signal1 := \"RED\"; D3_signal2 := \"RED\";");
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(OPERATIONAL.class, "when(T4_breakdown) /")
			};
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OPERATIONAL.class, "when(T5_repair) /")
			};
		}
	}
}
