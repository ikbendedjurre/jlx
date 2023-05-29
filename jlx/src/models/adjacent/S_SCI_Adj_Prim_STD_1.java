package models.adjacent;

import jlx.behave.*;

/**
 * Generic interface and subsystem requirements Eu.Doc.20 v3.2
 * Page 38/44
 */
public class S_SCI_Adj_Prim_STD_1 extends S_SCI_AdjS_Prim_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(PDI_CONNECTION_CLOSED.class, "/ cOp1_init();")
			};
		}
	}
	
	public class PDI_CONNECTION_CLOSED extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"T6_Establish_SCP_Connection := TRUE;"
			);
		}
		
		/*@Override
		public LocalTransition[] onDo() {
			return new LocalTransition[] {
				new LocalTransition(
					"when(T10_SCP_Connection_Terminated) /",
					"T6_Establish_SCP_Connection := TRUE;"
				)
			};
		}*/
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ESTABLISHING_PDI_CONNECTION.class, "when(T5_SCP_Connection_Established) /")
			};
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(ESTABLISHING_PDI_CONNECTION.class, "when(T10_SCP_Connection_Terminated) /T32_PDI_Connection_Closed := TRUE;"),
				new Incoming(PDI_CONNECTION_ESTABLISHED.class, "when(T10_SCP_Connection_Terminated) /"),
				new Incoming(PDI_INIT_TIMEOUT.class, "when(T10_SCP_Connection_Terminated) /"),
				new Incoming(PDI_PROTOCOL_ERROR.class, "when(T10_SCP_Connection_Terminated) /")
			};
		}
	}
	
	public class ESTABLISHING_PDI_CONNECTION extends CompositeState {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"T33_Establishing_PDI_Connection := TRUE;"
			);
		}
		
		public class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(WAITING_FOR_VERSION_CHECK.class, "/ DT7_PDI_Version := D3_Con_PDI_Version;", "T7_Cd_PDI_Version_Check := TRUE;")
				};
			}
		}
		
		public class WAITING_FOR_VERSION_CHECK extends State {
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
					new Outgoing(PDI_VERSION_UNEQUAL.class, "[DT13_Result = \"not match\"] /T32_PDI_Connection_Closed := TRUE;"), 
					new Outgoing(Junction1.class, "[DT13_Result = \"match\"] /")
				};
			}
		}
		
		public class Junction1 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(WAITING_FOR_INIT_START.class,
						"[NOT D23_Con_Checksum_Data_Used] /",
						"T8_Cd_Initialisation_Request := TRUE;"
					),
					new Outgoing(Junction2.class,
						"[D23_Con_Checksum_Data_Used] /"
					)
				};
			}
		}
		
		public class Junction2 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(WAITING_FOR_INIT_START.class,
						"[DT13_Checksum_Data = D4_Con_Checksum_Data] /",
						"T8_Cd_Initialisation_Request := TRUE;"
					),
					new Outgoing(PDI_CHECKSUM_UNEQUAL.class,
						"[DT13_Checksum_Data <> D4_Con_Checksum_Data] /",
						"T32_PDI_Connection_Closed := TRUE;"
					)
				};
			}
		}
		
		public class WAITING_FOR_INIT_START extends State {			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(RECEIVING_SEC_STATUS.class,
						"when(T14_Msg_Start_Initialisation) /"
					)
				};
			}
		}
		
		public class RECEIVING_SEC_STATUS extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(CHECKING_SEC_STATUS.class,
						"when(T24_Msg_Status_Report_Completed) /"
					)
				};
			}
		}
		
		public class CHECKING_SEC_STATUS extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					"T27_Check_Sec_Status := TRUE;"
				);
			}
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(SENDING_PRIM_STATUS.class,
						"when(T25_Sec_Status_Report_Complete) /"
					)
				};
			}
		}
		
		public class SENDING_PRIM_STATUS extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					"T28_Start_Prim_Status_Report := TRUE;"
				);
			}
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(WAITING_FOR_INIT_COMPLETION.class,
						"when(T26_Prim_Status_Report_Completed) /"
					)
				};
			}
		}
		
		public class WAITING_FOR_INIT_COMPLETION extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					"T29_Msg_Status_Report_Completed := TRUE;"
				);
			}
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(PDI_CONNECTION_ESTABLISHED.class,
						"when(T15_Msg_Initialisation_Completed) /"
					)
				};
			}
		}
	}
	
	public class PDI_CONNECTION_ESTABLISHED extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"T31_PDI_Connection_Established := TRUE;"
			);
		}
		@Override
		public LocalTransition onExit() {
			return new LocalTransition(
				"Entry /",
				"T32_PDI_Connection_Closed := TRUE;"
			);
		}
	}
	
	public class PDI_CONNECTION_IMPERMISSIBLE extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"T34_PDI_Connection_Impermissible := TRUE;"
			);
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(PDI_VERSION_UNEQUAL.class, "when(T10_SCP_Connection_Terminated) /"),
				new Incoming(PDI_CHECKSUM_UNEQUAL.class, "when(T10_SCP_Connection_Terminated) /"),
				new Incoming(PDI_TELEGRAM_ERROR.class, "when(T10_SCP_Connection_Terminated) /")
			};
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(PDI_CONNECTION_CLOSED.class, "when( T30_Reset_Connection )/",
					"T32_PDI_Connection_Closed := TRUE;"
				)
			};
		}
	}
	
	public class PDI_INIT_TIMEOUT extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(ESTABLISHING_PDI_CONNECTION.class, "after(D2_Con_tmax_PDI_Connection) /")
			};
		}
	}
	
	public class PDI_VERSION_UNEQUAL extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
	}
	
	public class PDI_CHECKSUM_UNEQUAL extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
	}
	
	public class PDI_PROTOCOL_ERROR extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(ESTABLISHING_PDI_CONNECTION.class, "when( T20_Protocol_Error )/",
					"T32_PDI_Connection_Closed := TRUE;"
				),
				new Incoming(PDI_CONNECTION_ESTABLISHED.class, "when( T20_Protocol_Error )/")
			};
		}
	}
	
	public class PDI_TELEGRAM_ERROR extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(ESTABLISHING_PDI_CONNECTION.class, "when(T21_Formal_Telegram_Error) /",
						"T32_PDI_Connection_Closed := TRUE;"),
				new Incoming(PDI_CONNECTION_ESTABLISHED.class, "when(T21_Formal_Telegram_Error) /"),
				new Incoming(ESTABLISHING_PDI_CONNECTION.class, "when(T22_Content_Telegram_Error) /",
						"T32_PDI_Connection_Closed := TRUE;"),
				new Incoming(PDI_CONNECTION_ESTABLISHED.class, "when(T22_Content_Telegram_Error) /")
			};
		}
	}
}
