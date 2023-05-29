package models.generic.unclearversion;

import jlx.behave.*;

/**
 * Page 25.
 */
public class SCI_EfeS_Sec_STD_1 extends F_SCI_EfeS_Sec_SR.Block implements StateMachine {
	public static class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(PDI_CONNECTION_CLOSED.class, "/ cOp1_init();")
			};
		}
	}
	
	public static class PDI_CONNECTION_CLOSED extends CompositeState {
		public static class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(NOT_READY_FOR_CONNECTION.class, "")
				};
			}
		}
		
		public static class NOT_READY_FOR_CONNECTION extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition("Entry / D50_PDI_Connection_State := \"CLOSED\";");
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(READY_FOR_CONNECTION.class, "when(T1_Ready_For_PDI_Connection) /")
				};
			}
		}
		
		public static class READY_FOR_CONNECTION extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition("Entry / D50_PDI_Connection_State := \"CLOSED_READY\";");
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(NOT_READY_FOR_CONNECTION.class, "when(T18_Not_Ready_For_PDI_Connection) /")
				};
			}
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ESTABLISHING_PDI_CONNECTION.class,
					"when(T5_SCP_Connection_Established) /"
				)
			};
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(CLOSING_PDI_CONNECTION.class, "when(T10_SCP_Connection_Terminated) / T17_PDI_Connection_Closed := TRUE;"),
				new Incoming(PDI_VERSION_UNEQUAL.class, "when(T10_SCP_Connection_Terminated) / T17_PDI_Connection_Closed := TRUE;"),
				new Incoming(PDI_PROTOCOL_ERROR.class, "when(T10_SCP_Connection_Terminated) / T17_PDI_Connection_Closed := TRUE;")
			};
		}
	}
	
	public static class ESTABLISHING_PDI_CONNECTION extends CompositeState {
		public static class Initial2 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(READY_FOR_VERSION_CHECK.class, "")
				};
			}
		}
		
		public static class READY_FOR_VERSION_CHECK extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition("Entry / D50_PDI_Connection_State := \"READY_FOR_VERSION_CHECK\";");
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(
						Junction0.class,
						"when(T7_Cd_PDI_Version_Check) /"
					)
				};
			}
		}
		
		public static class Junction0 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(
						Junction1.class,
						"[DT7_PDI_Version <> D3_Con_PDI_Version] /"
					),
					new Outgoing(
						Junction2.class,
						"[DT7_PDI_Version = D3_Con_PDI_Version] /"
					)
				};
			}
		}
		
		public static class Junction1 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(
						PDI_VERSION_UNEQUAL.class,
						"[D23_Con_Checksum_Data_Used] /",
						"DT13a_Result := \"not match\";",
						"DT13b_Checksum_Data := D4_Con_Checksum_Data;",
						"T13_Msg_PDI_Version_Check := TRUE;"
					),
					new Outgoing(
						PDI_VERSION_UNEQUAL.class,
						"[not D23_Con_Checksum_Data_Used] /",
						"DT13a_Result := \"not match\";",
						"DT13b_Checksum_Data := \"not applicable\";",
						"T13_Msg_PDI_Version_Check := TRUE;"
					),
				};
			}
		}
		
		public static class Junction2 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(
						READY_FOR_INITIALISATION.class,
						"[D23_Con_Checksum_Data_Used] /",
						"DT13a_Result := \"match\";",
						"DT13b_Checksum_Data := D4_Con_Checksum_Data;",
						"T13_Msg_PDI_Version_Check := TRUE;"
					),
					new Outgoing(
						READY_FOR_INITIALISATION.class,
						"[not D23_Con_Checksum_Data_Used] /",
						"DT13a_Result := \"match\";",
						"DT13b_Checksum_Data := \"not applicable\";",
						"T13_Msg_PDI_Version_Check := TRUE;"
					),
				};
			}
		}
		
		public static class READY_FOR_INITIALISATION extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition("Entry / D50_PDI_Connection_State := \"READY_FOR_INITIALISATION\";");
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(
						SENDING_STATUS.class,
						"when(T8_Cd_Initialisation_Request) /",
						"T14_Msg_Start_Initialisation := TRUE;"
					)
				};
			}
		}
		
		public static class SENDING_STATUS extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					"D50_PDI_Connection_State := \"SENDING_STATUS\";",
					"T6_Start_Status_Report := TRUE;"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(
						PDI_CONNECTION_ESTABLISHED.class,
						"when(T9_Status_Report_Completed) /",
						"T15_Msg_Initialisation_Completed := TRUE;"
					)
				};
			}
		}
	}
	
	public static class PDI_CONNECTION_ESTABLISHED extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"D50_PDI_Connection_State := \"ESTABLISHED\";",
				"T11_PDI_Connection_Established := TRUE;"
			);
		}
	}
	
	public static class CLOSING_PDI_CONNECTION extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"D50_PDI_Connection_State := \"CLOSING\";",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(ESTABLISHING_PDI_CONNECTION.class, "when(T18_Not_Ready_For_PDI_Connection) /"),
				new Incoming(PDI_CONNECTION_ESTABLISHED.class, "when(T18_Not_Ready_For_PDI_Connection) /")
			};
		}
	}
	
	public static class PDI_VERSION_UNEQUAL extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"D50_PDI_Connection_State := \"VERSION_UNEQUAL\";",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
	}
	
	public static class PDI_PROTOCOL_ERROR extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"D50_PDI_Connection_State := \"PROTOCOL_ERROR\";",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(ESTABLISHING_PDI_CONNECTION.class, "when(T20_Protocol_Error) /"),
				new Incoming(PDI_CONNECTION_ESTABLISHED.class, "when(T20_Protocol_Error) /")
			};
		}
	}
	
	public static class PDI_TELEGRAM_ERROR extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"D50_PDI_Connection_State := \"TELEGRAM_ERROR\";",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(ESTABLISHING_PDI_CONNECTION.class, "when(T21_Formal_Telegram_Error) /"),
				new Incoming(PDI_CONNECTION_ESTABLISHED.class, "when(T21_Formal_Telegram_Error) /"),
				new Incoming(ESTABLISHING_PDI_CONNECTION.class, "when(T22_Content_Telegram_Error) /"),
				new Incoming(PDI_CONNECTION_ESTABLISHED.class, "when(T22_Content_Telegram_Error) /")
			};
		}
	}
	
	public static class PDI_CONNECTION_IMPERMISSIBLE extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"D50_PDI_Connection_State := \"IMPERMISSIBLE\";",
				"T12_Terminate_SCP_Connection := TRUE;"
			);
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(PDI_TELEGRAM_ERROR.class, "when(T10_SCP_Connection_Terminated) /")
			};
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(Final1.class, "/")
			};
		}
	}
	
	public static class Final1 extends FinalState {
		//Do nothing.
	}
}
