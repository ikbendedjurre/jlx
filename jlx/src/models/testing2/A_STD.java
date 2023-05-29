package models.testing2;

import jlx.behave.*;

/**
 * Eu.Doc.36
 * Requirements specification for subsystem Point (v2.7/0.A)
 * Page 32
 */
public class A_STD extends A_SR.Block implements StateMachine {
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
					"when(T1_Cmd) / DT2_Pos := DT1_Pos; T2_Cmd := TRUE;"
				)
			};
		}
	}
}
