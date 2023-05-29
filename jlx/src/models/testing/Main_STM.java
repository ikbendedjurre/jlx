package models.testing;

import jlx.behave.*;

public class Main_STM extends Main.Block implements StateMachine {
	public static class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(WAITING.class,
					"/debug := 0; sleep := FALSE; request := FALSE; value2 := FALSE; finished2 := FALSE; finished1 := FALSE; value1 := FALSE;"
				)
			};
		}
	}
	
	public static class WAITING extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(fork0.class,
					"when(request)[not sleep]/start := TRUE;"
				)
			};
		}
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"debug := 1;"
			);
		}
		@Override
		public LocalTransition onExit() {
			return new LocalTransition(
				"Entry /",
				"debug := 2;"
			);
		}
		@Override
		public LocalTransition[] onDo() {
			return new LocalTransition[] {
				new LocalTransition(
						"[sleep]/sleep := FALSE; debug := 3;"
					),
				new LocalTransition(
						"[not sleep]/sleep := TRUE; debug := 4;"
					)
			};
		}
	}
	
	public static class fork0 extends ForkVertex {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OPERATING.COMPUTING1.class,
					"/debug := 5;"
				),
				new Outgoing(OPERATING.COMPUTING2.class,
					"/debug := 6;"
				)
			};
		}
	}
	
	public static class OPERATING extends CompositeState {
		public static class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(COMPUTING1.class,
							"/debug := 7;"
						)
				};
			}
		}
		
		public static class Initial2 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(COMPUTING2.class,
							"/debug := 8;"
						)
				};
			}
		}
		
		public static class COMPUTING1 extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(final0.class,
							"when(finished1)/debug := 9;"
						),
					new Outgoing(join0.class,
							"/debug := 10;"
						)
				};
			}
		}
		
		public static class COMPUTING2 extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(final1.class,
							"when(finished2)/debug := 11;"
						),
					new Outgoing(join0.class,
							"/debug := 12;"
						),
					new Outgoing(OPERATING.class,
							"/debug := 24;"
						)
				};
			}
		}
		
		public static class final0 extends FinalState {
			
		}
		
		public static class final1 extends FinalState {
			
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(junction0.class,
						"/debug := 13;"
					),
				new Outgoing(ABORTED.class,
						"after(10)/debug := 14;"
					)
			};
		}
		
		@Override
		public LocalTransition onExit() {
			return new LocalTransition(
				"Exit /",
				"sleep := TRUE;",
				"debug := 15;"
			);
		}
	}
	
	public static class join0 extends JoinVertex {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OPERATING.class,
						"/debug := 16;"
					)
			};
		}
	}
	
	public static class junction0 extends JunctionVertex {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(junction1.class,
						"[value1 and value2]/dt_result1 := 5; debug := 17;"
					),
				new Outgoing(choice0.class,
						"[not (value1 and value2)]/ debug := 18;"
					)
			};
		}
	}
	
	public static class junction1 extends JunctionVertex {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(FINISHED.class,
						"/dt_result2 := \"done\"; debug := 19;"
					)
			};
		}
	}
	
	public static class choice0 extends ChoiceVertex {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(junction1.class,
						"[value1]/dt_result1 := 15; debug := 20;"
					),
				new Outgoing(junction1.class,
						"[not value1]/dt_result1 := 10; debug := 21;"
					)
			};
		}
	}
	
		
	public static class FINISHED extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"t_result := TRUE;",
				"debug := 22;"
			);
		}
	}
	
	public static class ABORTED extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry /",
				"debug := 23;"
			);
		}
	}
	
}

