package models.generic.unclearversion;

import jlx.behave.*;

/**
 * Page 8.
 */
public class EST_EfeS_STD_1 extends F_EST_EfeS_SR implements StateMachine {
	public static class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(NO_OPERATING_VOLTAGE.class,
					"/"
				)
			};
		}
	}
	
	public static class NO_OPERATING_VOLTAGE extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(OPERATING_VOLTAGE_SUPPLIED.class,
					"when(T1_Power_On_Detected) /"
				)
			};
		}
		
		@Override
		public Incoming[] getIncoming() {
			return new Incoming[] {
				new Incoming(OPERATING_VOLTAGE_SUPPLIED.class,
					"when(T2_Power_Off_Detected) /"
				)
			};
		}
	}
	
	public static class OPERATING_VOLTAGE_SUPPLIED extends CompositeState {
		public static class Initial1 extends InitialState {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(BOOTING.class,
						"/"
					)
				};
			}
		}
		
		public static class BOOTING extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(FALLBACK_MODE.class,
						"when(T5_SIL_Not_Fulfilled) /"
					),
					new Outgoing(FALLBACK_MODE.class,
						"when(T7_Invalid_Or_Missing_Basic_Data) /"
					),
					new Outgoing(INITIALISING.class,
						"when(T4_Booted) /",
						"Mem_t_Ini_Delay := 0;"
					)
				};
			}
		}
		
		public static class INITIALISING extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(Junction0.class,
						"when(T11_Data_Transmission_Timeout) /"
					),
					new Outgoing(Junction0.class,
						"when(T10_SCP_Connection_Terminated) /"
					),
					new Outgoing(Junction0.class,
						"when(T6_Data_Invalid) /"
					),
					new Outgoing(FALLBACK_MODE.class,
						"when(T5_SIL_Not_Fulfilled) /",
						"T12_Terminate_SCP_Connection := TRUE;"
					),
					new Outgoing(BOOTING.class,
						"when(T8_Data_Installation_Complete) /"
					),
					new Outgoing(BOOTING.class,
						"when(T3_Reset) /",
						"T12_Terminate_SCP_Connection := TRUE;"
					),
					new Outgoing(OPERATIONAL.class,
						"when(T9_PDI_Connection_Established) /"
					)
				};
			}
		}
		
		public static class FALLBACK_MODE extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(BOOTING.class,
						"when(T3_Reset) /"
					)
				};
			}
		}
		
		public static class Junction0 extends JunctionVertex {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(INITIALISING.class,
						"[0 = Mem_t_Ini_Delay or Mem_t_Ini_Delay = Con_t_Ini_Def_Delay] /",
						"Mem_t_Ini_Delay := Con_t_Ini_Def_Delay;"
					),
					new Outgoing(INITIALISING.class,
						"[Con_t_Ini_Step <= Mem_t_Ini_Delay or Mem_t_Ini_Delay < Con_t_Ini_Max] /",
						"Mem_t_Ini_Delay := Mem_t_Ini_Delay + Con_t_Ini_Step;"
					),
					new Outgoing(INITIALISING.class,
						"[Mem_t_Ini_Delay >= Con_t_Ini_Max] /",
						"Mem_t_Ini_Delay := Con_t_Ini_Max;"
					)
				};
			}
		}
		
		public static class OPERATIONAL extends State {
			@Override
			public Outgoing[] getOutgoing() {
				return new Outgoing[] {
					new Outgoing(BOOTING.class,
						"when(T3_Reset) /",
						"T12_Terminate_SCP_Connection := TRUE;"
					),
					new Outgoing(FALLBACK_MODE.class,
						"when(T5_SIL_Not_Fulfilled) /",
						"T12_Terminate_SCP_Connection := TRUE;"
					),
					new Outgoing(INITIALISING.class,
						"when(T10_SCP_Connection_Terminated) /",
						"Mem_t_Ini_Delay := Con_t_Ini_Def_Delay;"
					)
				};
			}
		}
	}
}
