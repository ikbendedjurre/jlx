package models.toypoint.j;

import jlx.asal.j.*;
import jlx.behave.*;

public class Point_STD extends Point_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(STOPPED.class, none())
			};
		}
	}
	
	public class STOPPED extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(entry(assign(T5_stopped, JBool.TRUE)));
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(LEFT.class, when(T1_move, eq(DT1_move_pos, EndPos.LEFT))),
//				new Outgoing(RIGHT.class, when(T1_move, eq(DT1_move_pos, EndPos.LEFT)))
			};
		}
	}
	
	public class LEFT extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(entry(assign(D2_move_left, neq(D4_curr_pos, EndPos.LEFT))));
		}
		
		@Override
		public LocalTransition onExit() {
			return new LocalTransition(exit(assign(D2_move_left, JBool.FALSE)));
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(STOPPED.class, guard(eq(D4_curr_pos, EndPos.LEFT))),
				new Outgoing(LEFT.class, after(D60_timeout_duration, assign(T6_timeout, JPulse.TRUE)))
			};
		}
	}
	
//	public class RIGHT extends State {
//		@Override
//		public LocalTransition onExit() {
//			return new LocalTransition(entry(assign(D3_move_right, JBool.FALSE)));
//		}
//		
//		@Override
//		public LocalTransition[] onDo() {
//			return new LocalTransition[] {
//				new LocalTransition(guard(neq(D4_curr_pos, EndPos.RIGHT), assign(D3_move_right, JBool.TRUE)))
//			};
//		}
//		
//		@Override
//		public Incoming[] getIncoming() {
//			return new Incoming[] {
//				new Incoming(STOPPED.class, when(T1_move, eq(DT1_move_pos, EndPos.RIGHT))),
//				new Incoming(LEFT.class, when(T1_move, eq(DT1_move_pos, EndPos.RIGHT)))
//			};
//		}
//		
//		@Override
//		public Outgoing[] getOutgoing() {
//			return new Outgoing[] {
//				new Outgoing(STOPPED.class, guard(eq(D4_curr_pos, EndPos.RIGHT)))
//			};
//		}
//	}
}
