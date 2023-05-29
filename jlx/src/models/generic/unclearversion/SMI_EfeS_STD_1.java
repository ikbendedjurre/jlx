package models.generic.unclearversion;

import jlx.behave.*;

/**
 * Page 32.
 */
public class SMI_EfeS_STD_1 extends F_SMI_EfeS_SR.Block implements StateMachine {
	public static class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(IDLE.class, "/ cOp1_init();")
			};
		}
	}
	
	public static class IDLE extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(Junction0.class,
					"when(T15_Data_Update_In_Initialising) / "
				),
				new Outgoing(DATA_UPDATE.class,
					"when(T14_Data_Update_After_Operational) /",
					"Mem_t_Ini_Delay := D1_Con_t_Ini_Def_Delay;"
				),
				new Outgoing(DATA_UPDATE.class,
					"when(T13_Data_Update_After_Booting) /",
					"Mem_t_Ini_Delay := 0;"
				)
			};
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(DATA_UPDATE.class, "when(T22_Data_Update_Stop) /"),
				new Incoming(DATA_UPDATE.class, "") //Triggered when Final0 or Final1 is reached.
			};
		}
	}
	
	public static class DATA_UPDATE extends CompositeState {
		public static class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(DELAY_TIME_EXPIRING.class, "")
				};
			}
		}
		
		public static class DELAY_TIME_EXPIRING extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(CHECKING_UP_TO_DATENESS.class, "after(Mem_t_Ini_Delay) / T19_Validate_Data := TRUE;")
				};
			}
		}
		
		public static class CHECKING_UP_TO_DATENESS extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Final0.class, "when(T6_Data_Up_To_Date) / T21_Data_Update_Finished := TRUE;"),
					new Outgoing(Final0.class, "after(D4_tmax_Response_MDM) / T21_Data_Update_Finished := TRUE;"),
					//Mistake in diagram: D4_Con_tmax_Response_MDM
					new Outgoing(DATA_TRANSMISSION.class, "when(T7_Data_Not_Up_To_Date) / T20_Ready_For_Update_Of_Data := TRUE;")
				};
			}
		}
		
		public static class Final0 extends FinalState {
			//Empty.
		}
		
		public static class DATA_TRANSMISSION extends CompositeState {
			public static class Initial2 extends InitialState {
				@Override
				public Outgoing[] getOutgoing() {
					return new Outgoing[] {
						new Outgoing(WAITING_FOR_DATA.class, "")
					};
				}
			}
			
			public static class WAITING_FOR_DATA extends State {
				@Override
				public Outgoing[] getOutgoing() {
					return new Outgoing[] {
						new Outgoing(RECEIVING_DATA.class, "when(T8_Data) /")
					};
				}
			}
			
			public static class RECEIVING_DATA extends State {
				@Override
				public Outgoing[] getOutgoing() {
					return new Outgoing[] {
						new Outgoing(CHECKING_DATA.class, "when(T9_Transmission_Complete) /")
					};
				}
			}
			
			public static class CHECKING_DATA extends State {
				@Override
				public Outgoing[] getOutgoing() {
					return new Outgoing[] {
						new Outgoing(INSTALLING_DATA.class, "when(T10_Data_Valid) /")
					};
				}
			}
		}
		
		public static class INSTALLING_DATA extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Final1.class,
						"when(T12_Data_Installation_Successfully) /",
						"T16_Data_Installation_Complete := TRUE;"
					)
				};
			}
		}
		
		public static class Final1 extends FinalState {
			//Empty.
		}
	}
	
	public static class Junction0 extends JunctionVertex {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(DATA_UPDATE.class,
					"[Mem_t_Ini_Delay >= D3_Con_t_Ini_Max] /",
					"Mem_t_Ini_Delay := D3_Con_t_Ini_Max;"
				),
				new Outgoing(DATA_UPDATE.class,
					"[D2_Con_t_Ini_Step <= Mem_t_Ini_Delay and Mem_t_Ini_Delay < D3_Con_t_Ini_Max] /",
					"Mem_t_Ini_Delay := Mem_t_Ini_Delay + D2_Con_t_Ini_Step;"
				),
				new Outgoing(DATA_UPDATE.class,
					"[0 = Mem_t_Ini_Delay or Mem_t_Ini_Delay = D1_Con_t_Ini_Def_Delay] /",
					"Mem_t_Ini_Delay := D2_Con_t_Ini_Step;"
				)
			};
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(DATA_UPDATE.DATA_TRANSMISSION.class, "after(D5_tmax_DataTransmission) / "),
				// Mistake in diagram: D5_Con_tmax_DataTransmission
				new Incoming(DATA_UPDATE.DATA_TRANSMISSION.CHECKING_DATA.class, "when(T11_Data_Invalid) / ")
			};
		}
	}
}
