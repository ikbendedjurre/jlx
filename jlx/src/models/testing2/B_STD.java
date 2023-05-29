package models.testing2;

import jlx.behave.*;

/**
 * Eu.Doc.36
 * Requirements specification for subsystem Point (v2.7/0.A)
 * Page 32
 */
public class B_STD extends B_SR.Block implements StateMachine {
	public class S1 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(S2.class,
					"cOp1_Init();"
				)
			};
		}
	}
	
	public class S2 extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(S2.class,
					"when(T3_Cmd) / DT4_Pos := DT3_Pos; T4_Cmd := TRUE;"
				)
			};
		}
	}
}
