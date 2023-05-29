package models.lc.v1x0;

import jlx.behave.*;

/**
 * Requirements specification SCI-LC Eu.Doc.108 v1.0
 * Page 44/47
 */
public class LC_STD_1_4 implements StateMachine{
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(IN_STATE_REPORT_STATUSES.class, "")
			};
		}
	}
	
	public class IN_STATE_REPORT_STATUSES extends State {
		@Override
		public LocalTransition[] onDo() {
			return new LocalTransition[] {
					new LocalTransition("when( T30_Status_LCPF )[DT30_Status_LCPF = \"Status_LCPF.Changed_Monitoring_Parameter\"]/DT6_Msg_LC_Monitoring_Status := \"Monitoring_Status.Changed_Monitoring_Parameter\";T6_Msg_LC_Monitoring_Status := TRUE;"),
					new LocalTransition("when( T30_Status_LCPF )[DT30_Status_LCPF = \"Status_LCPF.Failure_detected\"]/DT7_Msg_LC_Failure_Status := \"Failure_Status.Failure_detected\";T7_Msg_LC_Failure_Status := TRUE;"),
					new LocalTransition("when( T49_Report_Status )/DT5_Msg_LC_Functional_Status := Mem_Last_LC_State;T7_Msg_LC_Failure_Status := TRUE;DT91_Msg_Obstacle_Detection_Status := \"Obstacle_Detection_Status.Current_Obstacle_Detection_Status\";T91_Msg_Obstacle_Detection_Status := TRUE;",
							"DT18_Msg_Detection_Element_Status := \"Current Detection Element Status\";T18_Msg_Detection_Element_Status := TRUE;",
							"T99_Msg_All_Status_Send := TRUE;",
							"T5_Msg_LC_Functional_Status := TRUE;",
							"if Mem_Closure_Timer_Expired = TRUE then",
							"	cOp2_React_On_Closure_Timer_Overrun();",
							"if D68_Failure_Status_After_Closure_Timer_Overrun = \"no failure report\" then",
							"	DT7_Msg_LC_Failure_Status := \"Failure_Status.Current_Failure_status\";",
							"end if",
							"elseif Mem_Closure_Timer_Expired = FALSE then",
							"	DT6_Msg_LC_Monitoring_Status := \"Current Monitoring status\";T6_Msg_LC_Monitoring_Status := TRUE;", 
							"	DT7_Msg_LC_Failure_Status := \"Failure_Status.Current_Failure_status\";",
							"end if"),
					
					new LocalTransition("when( T30_Status_LCPF )[(DT30_Status_LCPF = \"Status_LCPF.No_obstacle_in_the_conflict_area\") and (D66_Con_Use_Obstacle_Detection = TRUE)]/DT91_Msg_Obstacle_Detection_Status := \"Obstacle_Detection_Status.No_obstacle_in_the_conflict_area\";T91_Msg_Obstacle_Detection_Status := TRUE;"),
					new LocalTransition("when( T30_Status_LCPF )[DT30_Status_LCPF = \"Status_LCPF.Obstacle_detected_in_the_conflict_area\" and D66_Con_Use_Obstacle_Detection = TRUE]/DT91_Msg_Obstacle_Detection_Status := \"Obstacle_Detection_Status.Obstacle_detected_in_the_conflict_area\";T91_Msg_Obstacle_Detection_Status := TRUE;"),
					new LocalTransition("when( D60_LC_Failure )/DT7_Msg_LC_Failure_Status := \"Failure_Status.Failure_detected\";T7_Msg_LC_Failure_Status := TRUE;"),
					new LocalTransition("when( D60_LC_Failure = FALSE )[NOT (DT30_Status_LCPF = \"Status_LCPF.Failure_detected\")]/DT7_Msg_LC_Failure_Status := \"Failure_Status.No_failure_present\";T7_Msg_LC_Failure_Status := TRUE;"),
					new LocalTransition("when( T30_Status_LCPF )[DT30_Status_LCPF = \"Status_LCPF.No_failure_present\" and D60_LC_Failure = FALSE]/DT7_Msg_LC_Failure_Status := \"Failure_Status.No_failure_present\";T7_Msg_LC_Failure_Status := TRUE;"),
					new LocalTransition("when( T108_Detection_Element_Status )[D108_Con_Use_Detection_Element]/DT18_Msg_Detection_Element_Status := DT108_Detection_Element_Status;T18_Msg_Detection_Element_Status := TRUE;"),
			};
		}
	}
}
