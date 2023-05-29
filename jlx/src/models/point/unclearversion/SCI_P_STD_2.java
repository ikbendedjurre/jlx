package models.point.unclearversion;

import jlx.behave.*;

/**
 * Generic interface and subsystem requirements (v3.0)
 * Page 34
 */
public class SCI_P_STD_2 extends S_SCI_P_SR.Block implements StateMachine {
	public static class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ESTABLISHING_PDI_CONNECTION.class,
					"cOp1_init();"
				)
			};
		}
	}
	
	public static class ESTABLISHING_PDI_CONNECTION extends CompositeState {
		public static class Initial1 extends InitialState {
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
		
		public static class WATING extends State {
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
		
		public static class REPORT_STATUS extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(STATUS_REPORTED.class,
						"when(T20_Point_Position) /",
						"DT2_Point_Position := DT20_Point_Position;",
						"T2_Msg_Point_Position := TRUE;",
						"Mem_Point_Position := DT20_Point_Position;",
						"T23_Sending_Status_Report_Completed := TRUE;"
					)
				};
			}
		}
		
		public static class STATUS_REPORTED extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(PDI_CONNECTION_ESTABLISHED.class,
						"when(D21_F_SCI_EfeS_Gen_SR_State = \"ESTABLISHED\")",
						"[Mem_Point_Position = DT20_Point_Position] /"
					),
					new Outgoing(PDI_CONNECTION_ESTABLISHED.class,
						"when(D21_F_SCI_EfeS_Gen_SR_State = \"ESTABLISHED\")",
						"[Mem_Point_Position <> DT20_Point_Position] /",
						"Mem_Point_Position := DT20_Point_Position;",
						"DT2_Point_Position := DT20_Point_Position;",
						"T2_Msg_Point_Position := TRUE;"
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
					"when(T1_Cd_Move_Point) /",
					"DT10_Move_Target := DT1_Move_Point_Target;",
					"T10_Move := TRUE;"
				),
				new LocalTransition(
					"when(T20_Point_Position) /",
					"DT2_Point_Position := DT20_Point_Position;",
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
