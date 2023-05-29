package models.point.v2x7x0A;

import jlx.behave.*;

/**
 * Eu.Doc.36
 * Requirements specification for subsystem Point (v2.7/0.A)
 * Page 34
 */
public class SCI_P_STD_2 extends F_SCI_P_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ESTABLISHING_PDI_CONNECTION.class,
					"cOp1_init();"
				)
			};
		}
	}
	
	public class ESTABLISHING_PDI_CONNECTION extends CompositeState {
		public class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(WATING.class, "/")
				};
			}
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ESTABLISHING_PDI_CONNECTION.class,
					"when(D21_F_SCI_EfeS_Gen_SR_State = \"CLOSED\") /"
				)
			};
		}
		
		public class WATING extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(REPORT_STATUS.class,
						"when(T18_Start_Status_Report) /",
						"T40_Send_Status_Report := TRUE;"
					)
				};
			}
		}
		
		public class REPORT_STATUS extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(STATUS_REPORTED.class,
						"when(T20_Point_Position) /",
						//"DT2_Point_Position := DT20_Point_Position;",
						"D2_Point_Position := D20_Point_Position;", //Replaces line above.
						"T2_Msg_Point_Position := TRUE;",
						//"Mem_Point_Position := DT20_Point_Position;",
						"Mem_Point_Position := D20_Point_Position;", //Replaces line above.
//						"Mem_Last_DT20 := DT20_Point_Position;",
						"T23_Sending_Status_Report_Completed := TRUE;"
					)
				};
			}
		}
		
		public class STATUS_REPORTED extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(PDI_CONNECTION_ESTABLISHED.class,
						"when(D21_F_SCI_EfeS_Gen_SR_State = \"ESTABLISHED\")",
						"[Mem_Point_Position = D20_Point_Position] /", //Replaced DT20_Point_Position by D20_Point_Position
						"Mem_Point_Position := \"NO_END_POSITION\";" //Reset to reduce state space
						//, "Mem_Last_DT20 := \"NO_END_POSITION\";" //Reset to reduce state space
					),
					new Outgoing(PDI_CONNECTION_ESTABLISHED.class,
						"when(D21_F_SCI_EfeS_Gen_SR_State = \"ESTABLISHED\")",
						"[Mem_Point_Position <> D20_Point_Position] /", //Replaced DT20_Point_Position by D20_Point_Position
						//"DT2_Point_Position := D20_Point_Position;",
						"D2_Point_Position := D20_Point_Position;", //Replaces line above.
						"T2_Msg_Point_Position := TRUE;",
						"Mem_Point_Position := \"NO_END_POSITION\";" //Reset to reduce state space
						//, "Mem_Last_DT20 := \"NO_END_POSITION\";" //Reset to reduce state space
					)
				};
			}
			
//			//Added self-loop to keep track of the last received DT20 value:
//			@Override
//			public LocalTransition[] onDo() {
//				return new LocalTransition[] {
//					new LocalTransition(
//						"when(T20_Point_Position)",
//						"[D21_F_SCI_EfeS_Gen_SR_State <> \"ESTABLISHED\"] /",
//						"Mem_Last_DT20 := DT20_Point_Position;"
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
					"when(T1_Cd_Move_Point) /",
					"DT10_Move_Target := DT1_Move_Point_Target;",
					"T10_Move := TRUE;"
				),
				new LocalTransition(
					"when(T20_Point_Position) /",
					//"DT2_Point_Position := DT20_Point_Position;",
					"D2_Point_Position := D20_Point_Position;", //Replaces line above.
					"T2_Msg_Point_Position := TRUE;"
				),
				new LocalTransition(
					"when(T30_Report_Timeout) / ",
					"T3_Msg_Timeout := TRUE;"
				)
			};
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ESTABLISHING_PDI_CONNECTION.class,
					"when(D21_F_SCI_EfeS_Gen_SR_State <> \"ESTABLISHED\") /"
				)
			};
		}
	}
}
