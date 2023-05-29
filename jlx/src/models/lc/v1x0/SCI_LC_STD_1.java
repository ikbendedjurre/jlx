package models.lc.v1x0;

import jlx.behave.*;

/**
 * Requirements specification SCI-LC Eu.Doc.108 v1.0
 * Page 28/47
 */
public class SCI_LC_STD_1 extends S_SCI_LC_SR.Block implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(WATING_FOR_START_OF_REPORT_STATUS.class,
					"cOp1_Init();"
				)
			};
		}
	}
	
	public class WATING_FOR_START_OF_REPORT_STATUS extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(REPORT_STATUS.class,
					"[D50_PDI_Connection_State = \"RECEIVING_STATUS\"]/"
				),
				new Outgoing(REPORT_STATUS.class,
					"when( D50_PDI_Connection_State = \"RECEIVING_STATUS\" )/"
				)
			};
		}
	}
	
	public class REPORT_STATUS extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(TRANSMIT_COMMANDS_OR_MESSAGES.class,
					"when( D50_PDI_Connection_State = \"ESTABLISHED\" )/"
				),
				new Outgoing(WATING_FOR_START_OF_REPORT_STATUS.class,
						"when( D50_PDI_Connection_State = \"INIT_TIMEOUT\" or D50_PDI_Connection_State = \"PROTOCOL_ERROR\"" + 
						" or D50_PDI_Connection_State = \"TELEGRAM_ERROR\" )/"
					)
			};
		}
		
		@Override
		public LocalTransition[] onDo() {
			return new LocalTransition[] {
				new LocalTransition("when( T105_Msg_LC_Functional_Status )/DT5_Report_LC_Functional_Status := DT105_Msg_LC_Functional_Status;" + 
				"T5_Report_LC_Functional_Status := TRUE;"),
				new LocalTransition("when( T106_Msg_LC_Monitoring_Status )/DT6_Report_LC_Monitoring_Status := DT106_Msg_LC_Monitoring_Status;" + 
				"T6_Report_LC_Monitoring_Status := TRUE;"),
				new LocalTransition("when( T107_Msg_LC_Failure_Status )/DT7_Report_LC_Failure_Status := DT107_Msg_LC_Failure_Status;" + 
				"T7_Report_LC_Failure_Status := TRUE;"),
				new LocalTransition("when( T108_Msg_Detection_Element_Status )/DT8_Report_Detection_Element_Status := DT108_Msg_Detection_Element_Status;" + 
				"T8_Report_Detection_Element_Status := TRUE;"),
				new LocalTransition("when( T109_Msg_Obstacle_Detection_Status )/DT9_Report_Obstacle_Detection_Status := DT109_Msg_Obstacle_Detection_Status;" + 
				"T9_Report_Obstacle_Detection_Status := TRUE;"),
			};
		}
	}
	
	public class TRANSMIT_COMMANDS_OR_MESSAGES extends State {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(WATING_FOR_START_OF_REPORT_STATUS.class,
					"when( D50_PDI_Connection_State <> \"ESTABLISHED\" )/"
				)
			};
		}
		
		@Override
		public LocalTransition[] onDo() {
			return new LocalTransition[] {
				new LocalTransition("when( T1_Realise_Activation )/DT101_Cd_Activation := DT1_Realise_Activation;T101_Cd_Activation := TRUE;"),
				new LocalTransition("when( T2_Realise_Deactivation )/T102_Cd_Deactivation := TRUE;"),
				new LocalTransition("when( T3_Realise_Local_Operation_Handover )/DT103_Cd_Local_Operation_Handover := DT3_Realise_Local_Operation_Handover;T103_Cd_Local_Operation_Handover := TRUE;"),
				new LocalTransition("when( T4_Realise_Isolate_LC )/DT104_Cd_Isolate_LC := DT4_Realise_Isolate_LC;T104_Cd_Isolate_LC := TRUE;"),
				new LocalTransition("when( T105_Msg_LC_Functional_Status )/DT5_Report_LC_Functional_Status := DT105_Msg_LC_Functional_Status;T5_Report_LC_Functional_Status := TRUE;"),
				new LocalTransition("when( T106_Msg_LC_Monitoring_Status )/DT6_Report_LC_Monitoring_Status := DT106_Msg_LC_Monitoring_Status;T6_Report_LC_Monitoring_Status := TRUE;"),
				new LocalTransition("when( T107_Msg_LC_Failure_Status )/DT7_Report_LC_Failure_Status := DT107_Msg_LC_Failure_Status;T7_Report_LC_Failure_Status := TRUE;"),
				new LocalTransition("when( T108_Msg_Detection_Element_Status )/DT8_Report_Detection_Element_Status := DT108_Msg_Detection_Element_Status;T8_Report_Detection_Element_Status := TRUE;"),
				new LocalTransition("when( T109_Msg_Obstacle_Detection_Status )/DT9_Report_Obstacle_Detection_Status := DT109_Msg_Obstacle_Detection_Status;T9_Report_Obstacle_Detection_Status := TRUE;"),
				new LocalTransition("when( T110_Msg_Local_Operation_Handover )/DT10_Report_Local_Operation_Handover := DT110_Msg_Local_Operation_Handover;T10_Report_Local_Operation_Handover := TRUE;"),
				new LocalTransition("when( T111_Msg_Local_Request )/DT11_Report_Local_Request := DT111_Msg_Local_Request;T11_Report_Local_Request := TRUE;")
			};
		}
	}
}
