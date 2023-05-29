package models.point.machines;

import jlx.behave.*;

public class F_PM_Gen_STD_1 extends F_PM_Gen implements StateMachine {
	public static class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(INACTIVE.class,
					""
				)
			};
		}
	}
	
	public static class INACTIVE extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ACTIVE.class, "when(D11_Active) /"),
				new Outgoing(ACTIVE.class, "[D11_Active] /")
			};
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(ACTIVE.class, "when(Not D11_Active) /"),
				new Incoming(ACTIVE.class, "when(T2_Reset) /")
			};
		}
	}
	
	public static class ACTIVE extends CompositeState {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition("Entry / D1_Position_Out := \"ACTIVE\";");
		}
		
		public static class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Junction0.class, "/")
				};
			}
		}
		
		public static class Junction0 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(END_POSITION.class, "[cOp1_End_Position] /"),
					new Outgoing(TRAILED.class, "[D12_Position_In = \"TRAILED\"] /"),
					new Outgoing(NO_END_POSITION.class, "[D12_Position_In = \"NO_END_POSITION\"] /")
				};
			}
		}
		
		public static class END_POSITION extends CompositeState {
			public static class Initial2 extends InitialState {
				@Override
				public Outgoing[] getOutgoing() {
					return new Outgoing[] {
						new Outgoing(Junction1.class, "/")
					};
				}
			}
			
			public static class Junction1 extends JunctionVertex {
				@Override
				public Outgoing[] getOutgoing() {
					return new Outgoing[] {
						new Outgoing(LEFT.class, "[D12_Position_In = \"LEFT\"] /"),
						new Outgoing(RIGHT.class, "[D12_Position_In = \"RIGHT\"] /")
					};
				}
			}
			
			public static class LEFT extends State {
				@Override
				public LocalTransition onEntry() {
					return new LocalTransition("Entry / D1_Position_Out := \"LEFT\";");
				}
			}
			
			public static class RIGHT extends State {
				@Override
				public LocalTransition onEntry() {
					return new LocalTransition("Entry / D1_Position_Out := \"RIGHT\";");
				}
			}
		}
		
		public static class TRAILED extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition("Entry / D1_Position_Out := \"TRAILED\";");
			}
			
			@Override
			public Incoming[] getIncoming() {
				return new Incoming[] {
					new Incoming(END_POSITION.class, "[D12_Position_In = \"TRAILED\"] /")
				};
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(NO_END_POSITION.class, "[D12_Position_In = \"NO_END_POSITION\"] /")
				};
			}
		}
		
		public static class NO_END_POSITION extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition("Entry / D1_Position_Out := \"NO_END_POSITION\";");
			}
			
			@Override
			public Incoming[] getIncoming() {
				return new Incoming[] {
					new Incoming(END_POSITION.class, "[D12_Position_In = \"NO_END_POSITION\"] /")
				};
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(END_POSITION.class, "when(cOp1_End_Position()) /")
				};
			}
		}
		
		public static class ERROR extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition("Entry / D1_Position_Out := \"ERROR\";");
			}
			
			@Override
			public Incoming[] getIncoming() {
				return new Incoming[] {
					new Incoming(END_POSITION.RIGHT.class, "[D12_Position_In = \"LEFT\"] /"),
					new Incoming(END_POSITION.LEFT.class, "[D12_Position_In = \"RIGHT\"] /"),
					new Incoming(TRAILED.class, "when(cOp1_End_Position()) /"),
					new Incoming(NO_END_POSITION.class, "[D12_Position_In = \"TRAILED\"] /")
				};
			}
		}
	}
}
