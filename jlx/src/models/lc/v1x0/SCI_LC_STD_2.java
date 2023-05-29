package models.lc.v1x0;

import jlx.behave.*;

/**
 * Requirements specification SCI-LC Eu.Doc.108 v1.0
 * Page 32/47
 */
public class SCI_LC_STD_2 extends F_SCI_LC_SR.Block implements StateMachine{
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
					"[D50_PDI_Connection_State = \"SENDING_STATUS\"]/"
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
						"when( D50_PDI_Connection_State = \"PROTOCOL_ERROR\"" + 
						" or D50_PDI_Connection_State = \"TELEGRAM_ERROR\" or D50_PDI_Connection_State = \"CLOSING\")/"
					)
			};
		}
		
		@Override
		public LocalTransition[] onDo() {
			return new LocalTransition[] {
					new LocalTransition("when( T199_All_Status_Send )/T52_All_Status_send := TRUE;"),
					new LocalTransition("when( T105_Report_LC_Functional_Status )/DT5_Msg_LC_Functional_Status := DT105_Report_LC_Functional_Status;T5_Msg_LC_Functional_Status := TRUE;"),
					new LocalTransition("when( T106_Report_LC_Monitoring_Status )/DT6_Msg_LC_Monitoring_Status := DT106_Report_LC_Monitoring_Status;T6_Msg_LC_Monitoring_Status := TRUE;"),
					new LocalTransition("when( T107_Report_LC_Failure_Status )/DT7_Msg_LC_Failure_Status := DT107_Report_LC_Failure_Status;T7_Msg_LC_Failure_Status := TRUE;"),
					new LocalTransition("when( T108_Report_Detection_Element_Status )/DT8_Msg_Detection_Element_Status := DT108_Report_Detection_Element_Status;T8_Msg_Detection_Element_Status := TRUE;"),
					new LocalTransition("when( T109_Report_Obstacle_Detection_Status )/DT9_Msg_Obstacle_Detection_Status := DT109_Report_Obstacle_Detection_Status;T9_Msg_Obstacle_Detection_Status := TRUE;")
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
					new LocalTransition("when( T1_Cd_Activation )/DT101_Realise_Activation := DT1_Cd_Activation;T101_Realise_Activation := TRUE;"),
					new LocalTransition("when( T2_Cd_Deactivation )/T102_Realise_Deactivation := TRUE;"),
					new LocalTransition("when( T3_Cd_Local_Operation_Handover )/DT103_Realise_Local_Operation_Handover := DT3_Cd_Local_Operation_Handover;T103_Realise_Local_Operation_Handover := TRUE;"),
					new LocalTransition("when( T4_Cd_Isolate_LC )/DT104_Realise_Isolate_LC := DT4_Cd_Isolate_LC;T104_Realise_Isolate_LC := TRUE;"),
					new LocalTransition("when( T105_Report_LC_Functional_Status )/DT5_Msg_LC_Functional_Status := DT105_Report_LC_Functional_Status;T5_Msg_LC_Functional_Status := TRUE;"),
					new LocalTransition("when( T106_Report_LC_Monitoring_Status )/DT6_Msg_LC_Monitoring_Status := DT106_Report_LC_Monitoring_Status;T6_Msg_LC_Monitoring_Status := TRUE;"),
					new LocalTransition("when( T107_Report_LC_Failure_Status )/DT7_Msg_LC_Failure_Status := DT107_Report_LC_Failure_Status;T7_Msg_LC_Failure_Status := TRUE;"),
					new LocalTransition("when( T108_Report_Detection_Element_Status )/DT8_Msg_Detection_Element_Status := DT108_Report_Detection_Element_Status;T8_Msg_Detection_Element_Status := TRUE;"),
					new LocalTransition("when( T109_Report_Obstacle_Detection_Status )/DT9_Msg_Obstacle_Detection_Status := DT109_Report_Obstacle_Detection_Status;T9_Msg_Obstacle_Detection_Status := TRUE;"),
					new LocalTransition("when( T110_Report_Local_Operation_Handover )/DT10_Msg_Local_Operation_Handover := DT110_Report_Local_Operation_Handover;T10_Msg_Local_Operation_Handover := TRUE;"),
					new LocalTransition("when( T111_Report_Local_Request )/DT11_Msg_Local_Request := DT111_Report_Local_Request;T11_Msg_Local_Request := TRUE;")
			};
		}
	}
}
