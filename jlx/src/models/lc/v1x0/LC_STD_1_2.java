package models.lc.v1x0;

import jlx.behave.*;

/**
 * Requirements specification SCI-LC Eu.Doc.108 v1.0
 * Page 40/47
 */
public class LC_STD_1_2 implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(Junction0.class, "")
			};
		}
	}
	
	public class Junction0 extends JunctionVertex {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ISOLATED.class, "[Mem_Last_LC_State = \"Isolated LC\"]/"),
				new Outgoing(DEACTIVATED.UNPROTECTED.class, "[Mem_Last_LC_State = \"Deactivated and unprotected\"]/"),
				new Outgoing(ACTIVATED.UNPROTECTED.class, "[Mem_Last_LC_State = \"Activated and unprotected\"]/"),
				new Outgoing(ACTIVATED.PROTECTED.class, "[Mem_Last_LC_State = \"Activated and protected\"]/")
			};
		}
	}
	
	public class ACTIVATED extends CompositeState {
		public class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(ACTIVATE_LCPF.class, "")
				};
			}
		}
		
		public class ACTIVATE_LCPF extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry/T31_Activate_LCPF := TRUE;"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(UNPROTECTED.class, "/Mem_Closure_Timer_Running := TRUE;")
				};
			}
		}
		
		public class UNPROTECTED extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry/DT5_Msg_LC_Functional_Status := \"Activated and unprotected\";" + 
					"T5_Msg_LC_Functional_Status := TRUE;" + 
					"Mem_Last_LC_State := \"Activated and unprotected\";"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(PROTECTED.class, "when( T30_Status_LCPF )[DT30_Status_LCPF = \"Protected\"]/")
				};
			}
		}
		
		public class PROTECTED extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry/DT5_Msg_LC_Functional_Status := \"Activated and protected\";" + 
					"T5_Msg_LC_Functional_Status := TRUE;" + 
					"Mem_Last_LC_State := \"Activated and protected\";"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(UNPROTECTED.class, "when( T30_Status_LCPF )[DT30_Status_LCPF = \"Unprotected\"]/")
				};
			}
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(DEACTIVATED.class, "when( T2_Cd_Deactivation )/")
			};
		}
	}
	
	public class DEACTIVATED extends CompositeState {
		public class Initial3 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(DEACTIVATE_LCPF.class, "")
				};
			}
		}
		
		public class DEACTIVATE_LCPF extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry/T32_Deactivate_LCPF := TRUE;"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(UNPROTECTED.class, "")
				};
			}
		}
		
		public class UNPROTECTED extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry/DT5_Msg_LC_Functional_Status := \"Deactivated and unprotected\";" + 
					"T5_Msg_LC_Functional_Status := TRUE;" + 
					"Mem_Last_LC_State := \"Deactivated and unprotected\";"
				);
			}
			
			@Override
			public LocalTransition[] onDo() {
				return new LocalTransition[] {
					new LocalTransition("when( T30_Status_LCPF )[DT30_Status_LCPF = \"Idle\"]/Mem_Closure_Timer_Running := FALSE;"),
				};
			}
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ACTIVATED.class, "when( T1_Cd_Activation )[DT1_Cd_Activation = \"Activation\"]/"),
				new Outgoing(PRE_ACTIVATION.class, "when( T1_Cd_Activation )[DT1_Cd_Activation = \"Pre-Activation\" and D65_Con_Use_Pre_Activation]/"),
				new Outgoing(ISOLATED.class, "when( T4_Cd_Isolate_LC )[DT4_Cd_Isolate_LC = \"Isolate LC enable\" and D67_Con_Use_Isolation = TRUE]/")
			};
		}
	}
	
	public class PRE_ACTIVATION extends CompositeState {
		public class Initial2 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(PRE_ACTIVATE_LCPF.class, "")
				};
			}
		}
		
		public class PRE_ACTIVATE_LCPF extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry/T33_Pre_Activate_LCPF := TRUE;"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(PRE_ACTIVATED.class, "")
				};
			}
		}
		
		public class PRE_ACTIVATED extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry/DT5_Msg_LC_Functional_Status := \"Pre-Activated\";" + 
					"T5_Msg_LC_Functional_Status := TRUE;" + 
					"Mem_Last_LC_State := \"Pre-Activated\";"
				);
			}
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ACTIVATED.class, "when( T1_Cd_Activation)[DT1_Cd_Activation = \"Activation\" ]/"),
				new Outgoing(DEACTIVATED.class, "when( T2_Cd_Deactivation )/")
			};
		}
	}
	
	public class ISOLATED extends CompositeState {
		public class Initial4 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(REPORT_ISOLATED.class, "")
				};
			}
		}
		
		public class REPORT_ISOLATED extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry/DT5_Msg_LC_Functional_Status := \"Isolated LC\";" + 
					"T5_Msg_LC_Functional_Status := TRUE;" + 
					"Mem_Last_LC_State := \"Isolated LC\";"
				);
			}
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(DEACTIVATED.UNPROTECTED.class, "when( T4_Cd_Isolate_LC )[DT4_Cd_Isolate_LC = \"Isolate LC disable\"]/")
			};
		}
	}
}
