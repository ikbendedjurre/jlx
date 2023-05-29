package models.lc.v1x0;

import jlx.behave.*;

/**
 * Requirements specification SCI-LC Eu.Doc.108 v1.0
 * Page 42/47
 */
public class LC_STD_1_3 implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(IN_STATE_PDI_CONNECTION_CLOSED.class, "")
			};
		}
	}
	
	public class IN_STATE_PDI_CONNECTION_CLOSED extends CompositeState {
		public class Initial1 extends InitialState {
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
					new Outgoing(ISOLATED_LC.class, "[Mem_Last_LC_State = \"Isolated LC\"]/"),
					new Outgoing(ACTIVATED.class, "[NOT (Mem_Last_LC_State = \"Activated and protected\" or Mem_Last_LC_State = \"Activated and unprotected\" or Mem_Last_LC_State = \"Isolated LC\")]/"), //was else, which we don't support yet
					new Outgoing(ACTIVATED.UNPROTECTED.class, "[Mem_Last_LC_State = \"Activated and unprotected\"]/"),
					new Outgoing(ACTIVATED.PROTECTED.class, "[Mem_Last_LC_State = \"Activated and protected\"]/")
				};
			}
		}
		
		public class ISOLATED_LC extends State {}
		
		public class ACTIVATED extends CompositeState {
			public class Initial2 extends InitialState {
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
						"Entry/Mem_Last_LC_State := \"Activated and unprotected\";"
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
						"Entry/Mem_Last_LC_State := \"Activated and protected\";"
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
					new Outgoing(DEACTIVATED.class, "after( D62_Con_t_PDI_Con_Loss_Deactivation_Timer )/")
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
						"Entry/Mem_Last_LC_State := \"Deactivated and unprotected\";"
					);
				}
				
				@Override
				public LocalTransition[] onDo() {
					return new LocalTransition[] {
						new LocalTransition("when( T30_Status_LCPF )[DT30_Status_LCPF = \"Idle\"]/Mem_Closure_Timer_Running := FALSE;"),
					};
				}
			}
		}
	}
}
