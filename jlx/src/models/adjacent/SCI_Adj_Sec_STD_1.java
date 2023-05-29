package models.adjacent;

import jlx.behave.*;

/**
 * Generic interface and subsystem requirements Eu.Doc.20 v3.2
 * Page 42/44
 */
public class SCI_Adj_Sec_STD_1 extends S_SCI_AdjS_Sec_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(PDI_CONNECTION_CLOSED.class, "/ cOp1_init();")
			};
		}
	}
	
	public class PDI_CONNECTION_CLOSED extends State {	
		/*@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"T1_Establish_SCP_Connection := TRUE;"
			);
		}*/
		
		/*@Override
		public LocalTransition[] onDo() {
			return new LocalTransition[] {
				new LocalTransition(
					"when(T10_SCP_Connection_Terminated) /",
					"T1_Establish_SCP_Connection := TRUE;"
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
				new Incoming(ESTABLISHING_PDI_CONNECTION.class, "when(T10_SCP_Connection_Terminated) /",
						"T32_PDI_Connection_Closed := TRUE;"),
				new Incoming(PDI_CONNECTION_ESTABLISHED.class, "when(T10_SCP_Connection_Terminated) /"),
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
					new Outgoing(WAITING_FOR_VERSION_CHECK.class, "/")
				};
			}
		}
		
		public class WAITING_FOR_VERSION_CHECK extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Junction0.class, "when(T7_Cd_PDI_Version_Check)/")
				};
			}
		}
		
		public class Junction0 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Junction1.class,
						"[DT7_PDI_Version <> D3_Con_PDI_Version] /"
					),
					new Outgoing(Junction2.class,
						"[DT7_PDI_Version = D3_Con_PDI_Version] /"
					)
				};
			}
		}
		
		public class Junction1 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(PDI_VERSION_UNEQUAL.class,
						"[D23_Con_Checksum_Data_Used] /",
						"DT13_Result := \"not match\";",
						"DT13_Checksum_Data := D4_Con_Checksum_Data;",
						"T13_Msg_PDI_Version_Check := TRUE;",
						"T32_PDI_Connection_Closed := TRUE;"
					),
					new Outgoing(PDI_VERSION_UNEQUAL.class,
						"[NOT D23_Con_Checksum_Data_Used] /",
						"DT13_Result := \"not match\";",
						"DT13_Checksum_Data := \"not applicable\";",
						"T13_Msg_PDI_Version_Check := TRUE;",
						"T32_PDI_Connection_Closed := TRUE;"
					)
				};
			}
		}
		
		public class Junction2 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(WAITING_FOR_INIT_REQUEST.class,
						"[NOT D23_Con_Checksum_Data_Used] /",
						"DT13_Result := \"match\";",
						"DT13_Checksum_Data := \"not applicable\";",
						"T13_Msg_PDI_Version_Check := TRUE;"
					),
					new Outgoing(WAITING_FOR_INIT_REQUEST.class,
						"[D23_Con_Checksum_Data_Used] /",
						"DT13_Result := \"match\";",
						"DT13_Checksum_Data := D4_Con_Checksum_Data;",
						"T13_Msg_PDI_Version_Check := TRUE;"
					)
				};
			}
		}
		
		public class WAITING_FOR_INIT_REQUEST extends State {			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(SENDING_SEC_STATUS.class,
						"when(T8_Cd_Initialisation_Request) /",
						"T14_Msg_Start_Initialisation := TRUE;"
					)
				};
			}
		}
		
		public class SENDING_SEC_STATUS extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					"T6_Start_Sec_Status_Report := TRUE;"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(RECEIVING_PRIM_STATUS.class,
						"when(T24_Sec_Status_Report_Completed) /"
					)
				};
			}
		}
		
		public class RECEIVING_PRIM_STATUS extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					"T26_Msg_Status_Report_Completed := TRUE;"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(CHECKING_PRIM_STATUS.class,
						"when(T25_Msg_Status_Report_Completed) /"
					)
				};
			}
		}
		
		public class CHECKING_PRIM_STATUS extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					"T27_Check_Prim_Status := TRUE;"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(PDI_CONNECTION_ESTABLISHED.class,
						"when(T17_Prim_Status_Report_Complete) /",
						"T15_Msg_Initialisation_Completed := TRUE;"
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
				"Exit /",
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
	
	public class PDI_VERSION_UNEQUAL extends State {
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
					"T32_PDI_Connection_Closed := TRUE;"),
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
