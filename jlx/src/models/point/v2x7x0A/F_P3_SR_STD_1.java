package models.point.v2x7x0A;

import jlx.behave.*;

/**
 * Generic interface and subsystem requirements (v3.0)
 * Page 38
 */
public class F_P3_SR_STD_1 extends F_P3_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OPERATING.class,
					"cOp1_Init();"
				)
			};
		}
	}
	
	public class OPERATING extends CompositeState {
		public class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(WAITING_FOR_INITIALISING.class,
						"/"
					)
				};
			}
		}
		
		public class WAITING_FOR_INITIALISING extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Junction0.class,
						"when(D20_F_EST_EfeS_Gen_SR_State = \"INITIALISING\") /"
					)
				};
			}
		}
		
		public class Junction0 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(NO_END_POSITION.class,
						"[cOp3_No_End_Position()] /"
					),
					new Outgoing(ALL_LEFT.class,
						"[cOp2_All_Left()] /"
					),
					new Outgoing(ALL_RIGHT.class,
						"[cOp4_All_Right()] /"
					),
					new Outgoing(TRAILED.class,
						"[cOp5_Trailed() and cOp7_Is_Trailable()] /"
					)
				};
			}
		}
		
		public class NO_END_POSITION extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					//"DT20_Point_Position := \"PointPos.NO_END_POSITION\";",
					"D20_Point_Position := \"PointPos.NO_END_POSITION\";", //Replaces line above.
					"T20_Point_Position := TRUE;",
					"Mem_Current_Point_Position := \"PointPos.NO_END_POSITION\";",
					"T4_Information_No_End_Position := TRUE;",
					"D6_Detection_State := \"Detection_State.NO_END_POSITION\";"
				);
			}
			
			@Override
			public LocalTransition onExit() {
				return new LocalTransition(
					"Exit /" //Different from specification, which says:
//					"T4_Information_No_End_Position := FALSE;"
				);
			}
			
			@Override
			public LocalTransition[] onDo() {
				return new LocalTransition[] {
					new LocalTransition(
						"when(T40_Report_Status) /",
						//"DT20_Point_Position := \"PointPos.NO_END_POSITION\";", //Changed enum.
						"D20_Point_Position := \"PointPos.NO_END_POSITION\";", //Changed enum. + Replaces line above.
						"T20_Point_Position := TRUE;"
					)
				};
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(ALL_LEFT.class,
						"when(cOp2_All_Left()) /"
					),
					new Outgoing(ALL_RIGHT.class,
						"when(cOp4_All_Right()) /"
					),
					new Outgoing(WAITING_FOR_INITIALISING.class,
						"when(cOp13_Not_Initialised()) /"
					),
					//WE ADDED THIS TO MATCH THE SOFTWARE SIMULATOR:
					new Outgoing(TRAILED.class,
						"when(cOp5_Trailed()) [cOp7_Is_Trailable()] /"
					)
				};
			}
		}
		
		public class ALL_LEFT extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					//"DT20_Point_Position := \"LEFT\";",
					"D20_Point_Position := \"LEFT\";", //Replaces line above.
					"T20_Point_Position := TRUE;",
					"Mem_Current_Point_Position := \"LEFT\";",
					"T5_Info_End_Position_Arrived := TRUE;",
					"D6_Detection_State := \"END_POSITION\";"
				);
			}
			
			@Override
			public LocalTransition onExit() {
				return new LocalTransition(
					"Exit /" //Different from specification, which says:
//					"Exit / T5_Info_End_Position_Arrived := FALSE;"
				);
			}
			
			@Override
			public LocalTransition[] onDo() {
				return new LocalTransition[] {
					new LocalTransition(
						"when(T40_Report_Status) /",
						//"DT20_Point_Position := \"LEFT\";",
						"D20_Point_Position := \"LEFT\";", //Replaces line above.
						"T20_Point_Position := TRUE;"
					)
				};
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(NO_END_POSITION.class,
						"when(cOp3_No_End_Position()) /"
					),
					new Outgoing(ALL_RIGHT.class,
						"when(cOp4_All_Right()) /"
					),
					new Outgoing(WAITING_FOR_INITIALISING.class,
						"when(cOp13_Not_Initialised()) /"
					),
					new Outgoing(TRAILED.class,
						"when(cOp5_Trailed()) [cOp7_Is_Trailable()] /"
					)
				};
			}
		}
		
		public class ALL_RIGHT extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					//"DT20_Point_Position := \"RIGHT\";",
					"D20_Point_Position := \"RIGHT\";", //Replaces line above.
					"T20_Point_Position := TRUE;",
					"Mem_Current_Point_Position := \"RIGHT\";",
					"T5_Info_End_Position_Arrived := TRUE;",
					"D6_Detection_State := \"END_POSITION\";"
				);
			}
			
			@Override
			public LocalTransition onExit() {
				return new LocalTransition(
					"Exit /" //Different from specification, which says:
//					"Exit / T5_Info_End_Position_Arrived := FALSE;"
				);
			}
			
			@Override
			public LocalTransition[] onDo() {
				return new LocalTransition[] {
					new LocalTransition(
						"when(T40_Report_Status) /",
						//"DT20_Point_Position := \"RIGHT\";",
						"D20_Point_Position := \"RIGHT\";", //Replaces line above.
						"T20_Point_Position := TRUE;"
					)
				};
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(NO_END_POSITION.class,
						"when(cOp3_No_End_Position()) /"
					),
					new Outgoing(ALL_LEFT.class,
						"when(cOp2_All_Left()) /"
					),
					new Outgoing(WAITING_FOR_INITIALISING.class,
						"when(cOp13_Not_Initialised()) /"
					),
					new Outgoing(TRAILED.class,
						"when(cOp5_Trailed()) [cOp7_Is_Trailable()] /"
					)
				};
			}
		}
		
		public class TRAILED extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry /",
					//"DT20_Point_Position := \"PointPos.TRAILED\";",
					"D20_Point_Position := \"PointPos.TRAILED\";", //Replaces line above.
					"T20_Point_Position := TRUE;",
					"Mem_Current_Point_Position := \"PointPos.TRAILED\";",
					"T6_Information_Trailed_Point := TRUE;",
					"D6_Detection_State := \"Detection_State.TRAILED\";"
				);
			}
			
			@Override
			public LocalTransition onExit() {
				return new LocalTransition(
					"Exit / " //Different from specification, which says:
					// "Exit / T6_Information_Trailed_Point := FALSE;"
				);
			}
			
			@Override
			public LocalTransition[] onDo() {
				return new LocalTransition[] {
					new LocalTransition(
						"when(T40_Report_Status) /",
						//"DT20_Point_Position := \"PointPos.TRAILED\";",
						"D20_Point_Position := \"PointPos.TRAILED\";", //Replaces line above.
						"T20_Point_Position := TRUE;"
					)
				};
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(NO_END_POSITION.class,
						"when(cOp3_No_End_Position()) /"
					),
					new Outgoing(ALL_LEFT.class,
						"when(cOp2_All_Left()) /"
					),
					new Outgoing(ALL_RIGHT.class,
						"when(cOp4_All_Right()) /"
					),
					new Outgoing(WAITING_FOR_INITIALISING.class,
						"when(cOp13_Not_Initialised()) /"
					)
				};
			}
		}
		
		public class Initial2 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(STOPPED.class,
						"/"
					)
				};
			}
		}
		
		public class STOPPED extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry / D25_Redrive := FALSE;",
					"D5_Drive_State := \"STOPPED\";"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Junction1.class,
						"when(T1_Move) [DT1_Move_Target = \"LEFT\"] /"
					),
					new Outgoing(Junction2.class,
						"when(T1_Move) [DT1_Move_Target = \"RIGHT\"] /"
					),
					new Outgoing(MOVING_LEFT.class,
						"when(cOp11_Redrive_Left()) [cOp9_Redrive_Enabled()] /",
						"D25_Redrive := TRUE;"
					),
					new Outgoing(MOVING_RIGHT.class,
						"when(cOp10_Redrive_Right()) [cOp9_Redrive_Enabled()] /",
						"D25_Redrive := TRUE;"
					)
				};
			}
		}
		
		public class Junction1 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(STOPPED.class,
						"[DT1_Move_Target = Mem_Current_Point_Position] /",
						//"DT20_Point_Position := Mem_Current_Point_Position;", //Not in specification.
						"D20_Point_Position := Mem_Current_Point_Position;", //Not in specification. + Replaces line above.
						"T20_Point_Position := TRUE;"
					),
					new Outgoing(MOVING_LEFT.class,
						"[DT1_Move_Target <> Mem_Current_Point_Position] /"
					)
				};
			}
		}
		
		public class Junction2 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(STOPPED.class,
						"[DT1_Move_Target = Mem_Current_Point_Position] /",
						//"DT20_Point_Position := Mem_Current_Point_Position;", //Not in specification.
						"D20_Point_Position := Mem_Current_Point_Position;", //Not in specification. + Replaces line above.
						"T20_Point_Position := TRUE;"
					),
					new Outgoing(MOVING_RIGHT.class,
						"[DT1_Move_Target <> Mem_Current_Point_Position] /"
					)
				};
			}
		}
		
		public class MOVING_LEFT extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry / D10_Move_Left := TRUE;",
					"D5_Drive_State := \"MOVING\";",
					"Mem_last_Target_Requested := \"LEFT\";"
				);
			}
			
			@Override
			public LocalTransition onExit() {
				return new LocalTransition(
					"Exit / D10_Move_Left := FALSE;"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(MOVING_RIGHT.class,
						"when(T1_Move) [DT1_Move_Target = \"RIGHT\"] /"
					),
					new Outgoing(STOPPED.class,
						"after(D4_Con_tmax_Point_Operation) / cOp12_Timeout();"
					),
					new Outgoing(STOPPED.class,
						"when(cOp2_All_Left()) /"
					),
					new Outgoing(STOPPED.class,
						"[cOp13_Not_Initialised()] /"
					)
				};
			}
		}
		
		public class MOVING_RIGHT extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition(
					"Entry / D11_Move_Right := TRUE;",
					"D5_Drive_State := \"MOVING\";",
					"Mem_last_Target_Requested := \"RIGHT\";"
				);
			}
			
			@Override
			public LocalTransition onExit() {
				return new LocalTransition(
					"Exit / D11_Move_Right := FALSE;"
				);
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(MOVING_LEFT.class,
						"when(T1_Move) [DT1_Move_Target = \"LEFT\"] /"
					),
					new Outgoing(STOPPED.class,
						"after(D4_Con_tmax_Point_Operation) / cOp12_Timeout();"
					),
					new Outgoing(STOPPED.class,
						"when(cOp4_All_Right()) /"
					),
					new Outgoing(STOPPED.class,
						"[cOp13_Not_Initialised()] /"
					)
				};
			}
		}
	}
}

