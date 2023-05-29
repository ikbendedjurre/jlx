package models.lx.simplified;

import jlx.behave.*;

public class LX_STD_1 extends LX_IBD1.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(Main.class, "/cOp1_Init();")
			};
		}
	}
	
	public class Main extends CompositeState {
		//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
		//SCP region starts here
		//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
		public class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(CLOSED.class, "/")
				};
			}
		}
		
		public class CLOSED extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(START.class, "[SCP_Connection_Allowed] / T6_Conn_Req := TRUE;")
				};
			}
		}
		
		public class START extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(CLOSED.class, "after(D100_Mem_Tmax) /"),
					new Outgoing(CLOSED.class, "when(T11_Disc_Req) /"),
					new Outgoing(CLOSED.class, "[NOT SCP_Connection_Allowed] / "),
					new Outgoing(UP.class, "when(T7_Conn_Resp) [SCP_Connection_Allowed]  / T8_HB := TRUE; SCP_Connection_Established := TRUE;")
				};
			}
		}
		
		public class UP extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Junction0.class, "when(T3_HB_inacceptable_delayed) /"),
					new Outgoing(Junction0.class, "when(T11_Disc_Req) /"),
					new Outgoing(Junction0.class, "when(T7_Conn_Resp) /"),
					new Outgoing(Junction0.class, "[NOT SCP_Connection_Allowed] /"),
				};
			}
		}
		
		public class Junction0 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(CLOSED.class, "/T9_Disc_Req := TRUE; SCP_Connection_Established := FALSE;"),
				};
			}
		}
	
		
		//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
		//Generic adjacent region starts here
		//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
		public class Initial2 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(PDI_CONNECTION_CLOSED.class, "/")
				};
			}
		}
		
		public class PDI_CONNECTION_CLOSED extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					"SCP_Connection_Allowed := TRUE;"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(ESTABLISHING_PDI_CONNECTION.class, "when(SCP_Connection_Established) /")
				};
			}
			
			@Override
			public Incoming[] getIncoming() {
				return new Incoming[] {
					new Incoming(ESTABLISHING_PDI_CONNECTION.class, "when(NOT SCP_Connection_Established) /"),
					new Incoming(PDI_CONNECTION_ESTABLISHED.class, "when(NOT SCP_Connection_Established) /"),
					new Incoming(PDI_INIT_TIMEOUT.class, "when(NOT SCP_Connection_Established) /"),
					new Incoming(PDI_PROTOCOL_ERROR.class, "when(NOT SCP_Connection_Established) /")
				};
			}
		}
		
		public class ESTABLISHING_PDI_CONNECTION extends CompositeState {
			public class Initial3 extends InitialState {
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
						new Outgoing(PDI_VERSION_UNEQUAL.class, "[DT13_Result = \"not match\"] /"), 
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
							"[DT13_Checksum_Data <> D4_Con_Checksum_Data] /"
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
						"T19_Check_Received_Status := TRUE;"
					);
				}
				@Override
				public Outgoing[] getOutgoing() {
					return new Outgoing[] {
						new Outgoing(SENDING_PRIM_STATUS.class,
							"when(T22_Adj_Status_Report_Complete) /"
						)
					};
				}
			}
			
			public class SENDING_PRIM_STATUS extends State {
				@Override
				public LocalTransition[] onDo() {
					return new LocalTransition[] {
						new LocalTransition(
							"when(T20_Internal_Input)/",
							"cOp3_Internal_GenerateMessage(DT101_Type);"
						)
					};
				}
				@Override
				public Outgoing[] getOutgoing() {
					return new Outgoing[] {
						new Outgoing(WAITING_FOR_INIT_COMPLETION.class,
							"when(T25_Own_Status_Report_Completed) /"
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
			public LocalTransition[] onDo() {
				return new LocalTransition[] {
					new LocalTransition(
						"when(T20_Internal_Input) /",
						"cOp2_SCI_LX_GenerateCommand(DT20_Type);"
					),
					new LocalTransition(
						"when(T101_Msg_XX) /",
						"cOp3_Internal_GenerateMessage(DT101_Type);"
					)
				};
			}
		}
		
		public class PDI_CONNECTION_IMPERMISSIBLE extends State {
			@Override
			public Incoming[] getIncoming() {
				return new Incoming[] {
					new Incoming(PDI_VERSION_UNEQUAL.class, "when(NOT SCP_Connection_Established) /"),
					new Incoming(PDI_CHECKSUM_UNEQUAL.class, "when(NOT SCP_Connection_Established) /"),
					new Incoming(PDI_TELEGRAM_ERROR.class, "when(NOT SCP_Connection_Established) /")
				};
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(PDI_CONNECTION_CLOSED.class, "when( T30_Reset_Connection )/")
				};
			}
		}
		
		public class PDI_INIT_TIMEOUT extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					"SCP_Connection_Allowed := FALSE;"
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
					"SCP_Connection_Allowed := FALSE;"
				);
			}
		}
		
		public class PDI_CHECKSUM_UNEQUAL extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					"SCP_Connection_Allowed := FALSE;"
				);
			}
		}
		
		public class PDI_PROTOCOL_ERROR extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					"SCP_Connection_Allowed := FALSE;"
				);
			}
			
			@Override
			public Incoming[] getIncoming() {
				return new Incoming[] {
					new Incoming(ESTABLISHING_PDI_CONNECTION.class, "when( T20_Protocol_Error )/")
				};
			}
		}
		
		public class PDI_TELEGRAM_ERROR extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					"SCP_Connection_Allowed := FALSE;"
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
	}
}
