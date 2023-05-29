package models.point.unclearversion;

import jlx.behave.*;

/**
 * Generic interface and subsystem requirements (v3.0)
 * Page 32
 */
public class SCI_P_STD_1 extends F_SCI_P_SR.Block implements StateMachine {
	public static class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(RECEIVING_STATUS_REPORT.class,
					"cOp1_Init();"
				)
			};
		}
	}
	
	public static class RECEIVING_STATUS_REPORT extends CompositeState {
		public static class Initial1 extends InitialState {
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
		
		public static class REPORT_STATUS extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(STATUS_REPORTED.class,
						"when(T2_Msg_Point_Position) /",
						"DT20_Point_Position := DT2_Point_Position;",
						"T20_Point_Position := TRUE;",
						"Mem_Point_Position := DT2_Point_Position;"
					)
				};
			}
		}
		
		public static class STATUS_REPORTED extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(PDI_CONNECTION_ESTABLISHED.class,
						"when(D21_S_SCI_EfeS_Gen_SR_State = \"ESTABLISHED\")",
						"[Mem_Point_Position = DT2_Point_Position] /"
					),
					new Outgoing(PDI_CONNECTION_ESTABLISHED.class,
						"when(D21_S_SCI_EfeS_Gen_SR_State = \"ESTABLISHED\")",
						"[Mem_Point_Position <> DT2_Point_Position] /",
						"Mem_Point_Position := DT2_Point_Position;",
						"DT20_Point_Position := DT2_Point_Position;",
						"T20_Point_Position := TRUE;"
					)
				};
			}
		}
	}
	
	public static class PDI_CONNECTION_ESTABLISHED extends State {
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
					"DT20_Point_Position := DT2_Point_Position;",
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
