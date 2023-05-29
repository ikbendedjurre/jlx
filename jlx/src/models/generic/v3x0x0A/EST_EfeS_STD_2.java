package models.generic.v3x0x0A;

import jlx.behave.*;

/**
 * Page 12.
 */
public class EST_EfeS_STD_2 extends F_EST_EfeS_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(NO_OPERATING_VOLTAGE.class, "/ cOp1_Init();")
			};
		}
	}
	
	public class NO_OPERATING_VOLTAGE extends State {
		@Override
		public LocalTransition onEntry() {
			return new LocalTransition(
				"Entry / D51_EST_EfeS_State := \"NO_OPERATING_VOLTAGE\";"
			);
		}
		
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(
					OPERATING_VOLTAGE_SUPPLIED.class,
					"when(T1_Power_On_Detected) /"
				)
			};
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(OPERATING_VOLTAGE_SUPPLIED.class,
					"when(T2_Power_Off_Detected) /",
					"T18_Not_Ready_For_PDI_Connection := TRUE;",
					"T22_Data_Update_Stop := TRUE;"
				)
			};
		}
	}
	
	public class OPERATING_VOLTAGE_SUPPLIED extends CompositeState {
		public class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(BOOTING.class, "/")
				};
			}
		}
		
		public class Junction0 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(INITIALISING.class,
						"[not D20_Con_MDM_Used] /",
						"T21_Ready_For_PDI_Connection := TRUE;"
					),
					new Outgoing(INITIALISING.class,
						"[D20_Con_MDM_Used] /",
						"T13_Data_Update_After_Booting := TRUE;"
					)
				};
			}
		}
		
		public class Junction1 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(INITIALISING.class,
						"[not D20_Con_MDM_Used] /",
						"T21_Ready_For_PDI_Connection := TRUE;"
					),
					new Outgoing(INITIALISING.class,
						"[D20_Con_MDM_Used] /",
						"T14_Data_Update_After_Operational := TRUE;"
					)
				};
			}
		}
		
		public class BOOTING extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition("Entry / D51_EST_EfeS_State := \"BOOTING\";");
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Junction0.class, "when(T4_Booted) /"),
					new Outgoing(FALLBACK_MODE.class, "when(T5_SIL_Not_Fulfilled) /"),
					new Outgoing(FALLBACK_MODE.class, "when(T7_Invalid_Or_Missing_Basic_Data) /")
				};
			}
		}
		
		public class FALLBACK_MODE extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition("Entry / D51_EST_EfeS_State := \"FALLBACK_MODE\";");
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(BOOTING.class, "when(T3_Reset) /")
				};
			}
		}
		
		public class OPERATIONAL extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition("Entry / D51_EST_EfeS_State := \"OPERATIONAL\";");
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Junction1.class, "when(T10_PDI_Connection_Closed) /"),
					new Outgoing(BOOTING.class, "when(T3_Reset) / T18_Not_Ready_For_PDI_Connection := TRUE;"),
					new Outgoing(FALLBACK_MODE.class, "when(T5_SIL_Not_Fulfilled) / T18_Not_Ready_For_PDI_Connection := TRUE;")
				};
			}
		}
		
		public class INITIALISING extends State {
			@Override
			public LocalTransition onEntry() {
				return new LocalTransition("Entry / D51_EST_EfeS_State := \"INITIALISING\";");
			}
			
			@Override
			public LocalTransition[] onDo() {
				return new LocalTransition[] {
					new LocalTransition("when(T10_PDI_Connection_Closed) [D20_Con_MDM_Used] / T15_Data_Update_In_Initialising := TRUE;"),
					new LocalTransition("when(T10_PDI_Connection_Closed) [not D20_Con_MDM_Used] / T21_Ready_For_PDI_Connection := TRUE;"),
					new LocalTransition("when(T17_Data_Update_Finished) [D20_Con_MDM_Used] / T21_Ready_For_PDI_Connection := TRUE;")
				};
			}
			
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(BOOTING.class,
						"when(T16_Data_Installation_Complete) [D20_Con_MDM_Used] /"
					),
					new Outgoing(BOOTING.class,
						"when(T3_Reset) /",
						"T18_Not_Ready_For_PDI_Connection := TRUE;",
						"T22_Data_Update_Stop := TRUE;"
					),
					new Outgoing(OPERATIONAL.class,
						"when(T9_PDI_Connection_Established) /"
					),
					new Outgoing(FALLBACK_MODE.class,
						"when(T5_SIL_Not_Fulfilled) /",
						"T18_Not_Ready_For_PDI_Connection := TRUE;",
						"T22_Data_Update_Stop := TRUE;"
					)
				};
			}
		}
	}
}
