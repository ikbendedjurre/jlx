package models.lc.v1x0;

import jlx.behave.*;

/**
 * Requirements specification SCI-LC Eu.Doc.108 v1.0
 * Page 39/47
 */
public class LC_STD_1_1 implements StateMachine{
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ACTIVATE_LCPF.class, "")
			};
		}
	}
	
	public class ACTIVATE_LCPF extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ACTIVATED_UNPROTECTED.class,
					"/Mem_Closure_Timer_Running := TRUE;"
				)
			};
		}
	}
	
	public class ACTIVATED_UNPROTECTED extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ACTIVATED_PROTECTED.class,
					"when( T30_Status_LCPF )[DT30_Status_LCPF = \"Protected\"]/"
				),
			};
		}
		
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry/Mem_Last_LC_State := \"Activated and unprotected\";"
			);
		}
	}
	
	public class ACTIVATED_PROTECTED extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(ACTIVATED_UNPROTECTED.class,
					"when( T30_Status_LCPF )[DT30_Status_LCPF = \"Unprotected\"]/"
				)
			};
		}
		
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry/Mem_Last_LC_State := \"Activated and protected\";"
			);
		}
	}
}
