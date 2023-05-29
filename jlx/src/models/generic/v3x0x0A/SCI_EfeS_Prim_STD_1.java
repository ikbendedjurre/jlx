package models.generic.v3x0x0A;

import jlx.behave.*;

/**
 * Page 21.
 */
public class SCI_EfeS_Prim_STD_1 extends S_SCI_EfeS_Prim_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(PDI_CONNECTION_CLOSED.class,
					"/ cOp1_init();"
				)
			};
		}
	}
	
	public class PDI_CONNECTION_CLOSED extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry / D50_PDI_Connection_State := \"CLOSED_REQUESTED\";",
				"T6_Establish_SCP_Connection := TRUE;"
			);
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ESTABLISHING_PDI_CONNECTION.class, "when(T5_SCP_Connection_Established) /")
			};
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(ESTABLISHING_PDI_CONNECTION.class, "when(T10_SCP_Connection_Terminated) /"),
				new Incoming(PDI_CONNECTION_ESTABLISHED.class, "when(T10_SCP_Connection_Terminated) /"),
				new Incoming(PDI_INIT_TIMEOUT.class, "when(T10_SCP_Connection_Terminated) /"),
				new Incoming(PDI_VERSION_UNEQUAL.class, "when(T10_SCP_Connection_Terminated) /"),
				new Incoming(PDI_CHECKSUM_UNEQUAL.class, "when(T10_SCP_Connection_Terminated) /"),
				new Incoming(PDI_PROTOCOL_ERROR.class, "when(T10_SCP_Connection_Terminated) /")
			};
		}
	}
	
	public class ESTABLISHING_PDI_CONNECTION extends CompositeState {
		public class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(WAITING_FOR_VERSION_CHECK.class,
						"/",
						"DT7_PDI_Version := D3_Con_PDI_Version;",
						"T7_Cd_PDI_Version_Check := TRUE;"
					)
				};
			}
		}
		
		public class WAITING_FOR_VERSION_CHECK extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry / D50_PDI_Connection_State := \"WAITING_FOR_VERSION_CHECK\";"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Junction0.class, "when(T13_Msg_PDI_Version_Check) /")
				};
			}
		}
		
		public class Junction0 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Junction1.class, "[DT13_Result = \"PDI_Checksum_Result.Match\"] /"), //Changed enum.
					new Outgoing(PDI_VERSION_UNEQUAL.class, "[DT13_Result <> \"PDI_Checksum_Result.Match\"] /") //Changed enum.
				};
			}
		}
		
		public class Junction1 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Junction2.class, "[D23_Con_Checksum_Data_Used] /"),
					new Outgoing(WAITING_FOR_INITIALISATION.class, "[not D23_Con_Checksum_Data_Used] / T8_Cd_Initialisation_Request := TRUE;")
				};
			}
		}
		
		public class Junction2 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(PDI_CHECKSUM_UNEQUAL.class, "[DT13_Checksum_Data <> D4_Con_Checksum_Data] /"),
					new Outgoing(WAITING_FOR_INITIALISATION.class, "[DT13_Checksum_Data = D4_Con_Checksum_Data] / T8_Cd_Initialisation_Request := TRUE;")
				};
			}
		}
		
		public class WAITING_FOR_INITIALISATION extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry / D50_PDI_Connection_State := \"WAITING_FOR_INITIALISATION\";"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(RECEIVING_STATUS.class, "when(T14_Msg_Start_Initialisation) /")
				};
			}
		}
		
		public class RECEIVING_STATUS extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry / D50_PDI_Connection_State := \"RECEIVING_STATUS\";"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(PDI_CONNECTION_ESTABLISHED.class, "when(T15_Msg_Initialisation_Completed) /")
				};
			}
		}
	}
	
	public class PDI_CONNECTION_ESTABLISHED extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry / D50_PDI_Connection_State := \"ESTABLISHED\";"
			);
		}
	}
	
	public class PDI_INIT_TIMEOUT extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry / D50_PDI_Connection_State := \"INIT_TIMEOUT\";",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(ESTABLISHING_PDI_CONNECTION.class,
					"after(D2_Con_tmax_PDI_Connection) /"
				)
			};
		}
	}
	
	public class PDI_VERSION_UNEQUAL extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry / D50_PDI_Connection_State := \"VERSION_UNEQUAL\";",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
	}
	
	public class PDI_CHECKSUM_UNEQUAL extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry / D50_PDI_Connection_State := \"CHECKSUM_UNEQUAL\";",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
	}
	
	public class PDI_PROTOCOL_ERROR extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry / D50_PDI_Connection_State := \"PROTOCOL_ERROR\";",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(ESTABLISHING_PDI_CONNECTION.class,
					"when(T20_Protocol_Error) /"
				),
				new Incoming(PDI_CONNECTION_ESTABLISHED.class,
					"when(T20_Protocol_Error) /"
				)
			};
		}
	}
	
	public class PDI_TELEGRAM_ERROR extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry / D50_PDI_Connection_State := \"TELEGRAM_ERROR\";",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(ESTABLISHING_PDI_CONNECTION.class,
					"when(T21_Formal_Telegram_Error) /"
				),
				new Incoming(ESTABLISHING_PDI_CONNECTION.class,
					"when(T22_Content_Telegram_Error) /"
				),
				new Incoming(PDI_CONNECTION_ESTABLISHED.class,
					"when(T21_Formal_Telegram_Error) /"
				),
				new Incoming(PDI_CONNECTION_ESTABLISHED.class,
					"when(T22_Content_Telegram_Error) /"
				)
			};
		}
	}
	
	public class PDI_CONNECTION_IMPERMISSIBLE extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry / D50_PDI_Connection_State := \"IMPERMISSIBLE\";"
			);
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(PDI_CONNECTION_IMPERMISSIBLE.class, "/") //Self-loop is required for liveliness
			};
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(PDI_TELEGRAM_ERROR.class, "when(T10_SCP_Connection_Terminated) /")
			};
		}
	}
	
//	public class Final0 extends FinalState {
//		//Do nothing.
//	}
}
