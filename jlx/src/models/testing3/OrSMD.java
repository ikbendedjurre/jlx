package models.testing3;

import jlx.behave.*;

public class OrSMD extends OrIBD1.Block implements StateMachine {
	public class Initial1 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(Main.class, "/")
			};
		}
	}
	
	public class Main extends State {
		@Override
		public LocalTransition[] onDo() {
			return new LocalTransition[] {
				new LocalTransition(
					"when(T1_Input) /",
					"DT2_Output := DT1_Input or disjunct;",
					"T2_Output := TRUE;")
			};
		}
	}
}
