package models.lx;

import jlx.behave.*;

/**
 * Requirements specification SCI-LX Eu.Doc.111 (v1.0)
 * Page 24
 */
public class SCI_LX_Prim_STD_1 extends S_SCI_LX_Prim_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(PDI_CONNECTION_CLOSED.class,
					"cOp1_Init();"
				)
			};
		}
	}
	
	public class PDI_CONNECTION_CLOSED extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(PROCESSING_STATUS_REPORTS.class,
					"when(T3_Establishing_PDI_Connection) /"
				)
			};
		}
	}
	
	public class PROCESSING_STATUS_REPORTS extends CompositeState {
		public class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(WAITING_FOR_CHECK.class, "/")
				};
			}
		}
		
		public class WAITING_FOR_CHECK extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(CHECKING_RECEIVED_STATUS.class,
						"when(T18_Check_Sec_Status) /"
					)
				};
			}
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(PDI_CONNECTION_CLOSED.class,
					"when(T2_PDI_Connection_Closed) /"
				),
				new Outgoing(PDI_CONNECTION_ESTABLISHED.class,
					"when(T1_PDI_Connection_Established) /"
				)
			};
		}
		
		public class CHECKING_RECEIVED_STATUS extends State {
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
					new Outgoing(RECEIVED_STATUS_REPORT_COMPLETE.class,
						"when(T22_Adj_Status_Report_Complete) /"
					)
				};
			}
		}
		
		public class RECEIVED_STATUS_REPORT_COMPLETE extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					"T23_Sec_Status_Report_Complete := TRUE;"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(SENDING_STATUS_REPORT.class,
						"when(T24_Start_Prim_Status_Report) /"
					)
				};
			}
		}
		
		public class SENDING_STATUS_REPORT extends State {
			@Override
			public LocalTransition[] onDo() {
				return new LocalTransition[] {
					new LocalTransition(
						"when(T20_Internal_Input)/",
						"cOp3_Internal_GenerateMessage(DT20_Type);"
					)
				};
			}
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(STATUS_REPORT_COMPLETED.class,
						"when(T25_Own_Status_Report_Completed) /"
					)
				};
			}
		}
		
		public class STATUS_REPORT_COMPLETED extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					"T26_Prim_Status_Report_Completed := TRUE;"
				);
			}
		}
	}
	
	public class PDI_CONNECTION_ESTABLISHED extends State {
		@Override
		public LocalTransition[] onDo() {
			return new LocalTransition[] {
				new LocalTransition(
					"when(T101_Msg_XX) /",
					"cOp2_SCI_LX_GenerateCommand(DT101_Type);"
				),
				new LocalTransition(
					"when(T20_Internal_Input) /",
					"cOp3_Internal_GenerateMessage(DT20_Type);"
				)
			};
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(PDI_CONNECTION_CLOSED.class,
					"when(T2_PDI_Connection_Closed) /"
				)
			};
		}
	}
}
