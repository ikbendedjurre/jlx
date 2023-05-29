package models.lc.v1x0;

import jlx.behave.*;

/**
 * Requirements specification SCI-LC Eu.Doc.108 v1.0
 * Page 38/47
 */
public class LC_STD_1 extends F_LC_Functions_SR.Block implements StateMachine{
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(MONITOR_LC.class, "/cOp1_Init();")
			};
		}
	}
	
	public class MONITOR_LC extends CompositeState {
		//First region
		public class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(IDLE.class, "")
				};
			}
		}
		
		public class IDLE extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(CLOSURE_TIMER_IS_RUNNING.class, "when( Mem_Closure_Timer_Running = TRUE )[D63_Con_Use_Closure_Timer = TRUE]/")
				};
			}
		}
		
		public class CLOSURE_TIMER_EXPIRED extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(IDLE.class, "when( Mem_Closure_Timer_Running = FALSE )/cOp3_React_On_No_Closure_Timer_Overrun();  Mem_Closure_Timer_Expired := FALSE;")
				};
			}
		}

		public class CLOSURE_TIMER_IS_RUNNING extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(IDLE.class, "when( Mem_Closure_Timer_Running = FALSE )/DT6_Msg_LC_Monitoring_Status := \"No Closure timer overrun\";T6_Msg_LC_Monitoring_Status := TRUE;"),
					new Outgoing(CLOSURE_TIMER_EXPIRED.class, "after( D61_Con_tmax_Closure_Timer )/cOp2_React_On_Closure_Timer_Overrun();Mem_Closure_Timer_Expired := TRUE;")
				};
			}
		}
		
		
		//Second region
		public class Initial2 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(IDLE2.class, "")
				};
			}
		}
		
		public class IDLE2 extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(INITIAL_OUTPUT_STATES.class, "when( D50_EST_EfeS_State = \"BOOTING\" )/")
				};
			}
		}
		
		public class INITIAL_OUTPUT_STATES extends ReferenceState<LC_STD_1_1> {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(INITIAL_OUTPUT_STATES.class, "when( D50_EST_EfeS_State = \"NO_OPERATING_VOLTAGE\" )/"),
					new Outgoing(OPERATIONAL.class, "when( D50_EST_EfeS_State = \"OPERATIONAL\" )/"),
					new Outgoing(FALLBACK_MODE.class, "when( D50_EST_EfeS_State = \"FALLBACK_MODE\" )/")
				};
			}
		}
		
		public class OPERATIONAL extends ReferenceState<LC_STD_1_2> {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(INITIAL_OUTPUT_STATES.class, "when( D50_EST_EfeS_State = \"BOOTING\" or D50_EST_EfeS_State = \"NO_OPERATING_VOLTAGE\" )/"),
					new Outgoing(PDI_CONNECTION_CLOSED.class, "when( D50_EST_EfeS_State = \"INITIALISING\" )/"),
					new Outgoing(FALLBACK_MODE.class, "when( D50_EST_EfeS_State = \"FALLBACK_MODE\" )/")
				};
			}
		}
		
		public class FALLBACK_MODE extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry/T34_National_Specific_State_LCPF := TRUE;Mem_Closure_Timer_Running := FALSE;"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(INITIAL_OUTPUT_STATES.class, "when( D50_EST_EfeS_State = \"BOOTING\" or D50_EST_EfeS_State = \"NO_OPERATING_VOLTAGE\" )/"),
				};
			}
		}
		
		public class PDI_CONNECTION_CLOSED extends ReferenceState<LC_STD_1_3> {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry/Mem_Closure_Timer_Running := FALSE;"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(INITIAL_OUTPUT_STATES.class, "when( D50_EST_EfeS_State = \"BOOTING\" or D50_EST_EfeS_State = \"NO_OPERATING_VOLTAGE\" )/"),
					new Outgoing(OPERATIONAL.class, "when( D50_EST_EfeS_State = \"OPERATIONAL\" )/"),
					new Outgoing(FALLBACK_MODE.class, "when( D50_EST_EfeS_State = \"FALLBACK_MODE\" )/")
				};
			}
		}
		
		//Third region
		public class Initial3 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(REPORT_STATUSES.class, "")
				};
			}
		}
		public class REPORT_STATUSES extends ReferenceState<LC_STD_1_4> {}
		
		//Fourth Region
		public class Initial4 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(HANDLE_LOCAL_OPERATIONS.class, "")
				};
			}
		}
		public class HANDLE_LOCAL_OPERATIONS extends ReferenceState<LC_STD_1_5> {}
	}
}
