package models.point.v2x7x0A;

import jlx.behave.*;

/**
 * Eu.Doc.36
 * Requirements specification for subsystem Point (v2.7/0.A)
 * Page 32
 */
public class SCI_P_STD_1 extends S_SCI_P_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(RECEIVING_STATUS_REPORT.class,
					"cOp1_Init();"
				)
			};
		}
	}
	
	public class RECEIVING_STATUS_REPORT extends CompositeState {
		public class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(REPORT_STATUS.class, "/")
				};
			}
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(RECEIVING_STATUS_REPORT.class,
					"when(D21_S_SCI_EfeS_Gen_SR_State = \"CLOSED\") /"
				)
			};
		}
		
		public class REPORT_STATUS extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(STATUS_REPORTED.class,
						"when(T2_Msg_Point_Position) /",
						"DT20_Point_Position := D2_Point_Position;",
						"T20_Point_Position := TRUE;",
						"Mem_Point_Position := D2_Point_Position;"
						//, "Mem_Last_DT2 := DT2_Point_Position;" //Added
					)
				};
			}
		}
		
		public class STATUS_REPORTED extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(PDI_CONNECTION_ESTABLISHED.class,
						"when(D21_S_SCI_EfeS_Gen_SR_State = \"ESTABLISHED\")",
						"[Mem_Point_Position = D2_Point_Position] /", //Replaced DT2_Point_Position by D2_Point_Position
						"Mem_Point_Position := \"NO_END_POSITION\";" //Reset to reduce state space
						//, "Mem_Last_DT2 := \"NO_END_POSITION\";" //Reset to reduce state space
					),
					new Outgoing(PDI_CONNECTION_ESTABLISHED.class,
						"when(D21_S_SCI_EfeS_Gen_SR_State = \"ESTABLISHED\")",
						"[Mem_Point_Position <> D2_Point_Position] /", //Replaced DT2_Point_Position by D2_Point_Position
						"DT20_Point_Position := D2_Point_Position;",
						"T20_Point_Position := TRUE;",
						"Mem_Point_Position := \"NO_END_POSITION\";" //Reset to reduce state space
						//"Mem_Last_DT2 := \"NO_END_POSITION\";" //Reset to reduce state space
					)
				};
			}
			
//			//Added self-loop to keep track of the last received DT2 value:
//			@Override
//			public LocalTransition[] onDo() {
//				return new LocalTransition[] {
//					new LocalTransition(
//						"when(T2_Msg_Point_Position)",
//						"[D21_S_SCI_EfeS_Gen_SR_State <> \"ESTABLISHED\"] /",
//						"Mem_Last_DT2 := DT2_Point_Position;"
//					)
//				};
//			}
		}
	}
	
	public class PDI_CONNECTION_ESTABLISHED extends State {
		@Override
		public LocalTransition[] onDo() {
			return new LocalTransition[] {
				new LocalTransition(
					"when(T10_Move_Point) /",
					"DT1_Move_Point_Target := DT10_Move_Point;",
					"T1_Cd_Move_Point := TRUE;"
				),
				new LocalTransition(
					"when(T2_Msg_Point_Position) /",
					"DT20_Point_Position := D2_Point_Position;", //Replaced DT2_Point_Position by D2_Point_Position
					"T20_Point_Position := TRUE;"
				),
				new LocalTransition(
					"when(T3_Msg_Timeout) / ",
					"T30_Timeout := TRUE;"
				)
			};
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(RECEIVING_STATUS_REPORT.class,
					"when(D21_S_SCI_EfeS_Gen_SR_State <> \"ESTABLISHED\") /"
				)
			};
		}
	}
}
