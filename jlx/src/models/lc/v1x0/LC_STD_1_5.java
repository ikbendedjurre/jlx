package models.lc.v1x0;

import jlx.behave.*;

/**
 * Requirements specification SCI-LC Eu.Doc.108 v1.0
 * Page 45/47
 */
public class LC_STD_1_5 implements StateMachine {
	public class Initial0 extends InitialState {
		@Override
		public Outgoing[] getOutgoing() {
			return new Outgoing[] {
				new Outgoing(IN_STATE_HANDLE_LOCAL_OPERATIONS.class, "")
			};
		}
	}
	
	public class IN_STATE_HANDLE_LOCAL_OPERATIONS extends State {
		@Override
		public LocalTransition[] onDo() {
			return new LocalTransition[] {
				new LocalTransition("when( T3_Cd_Local_Operation_Handover )[DT3_Cd_Local_Operation_Handover = \"Handover to local operator initiated\"]/T42_Output_Initiated_Handover_To_Local_Operator := TRUE;"),
				new LocalTransition("when( T45_Input_Allow_Handover_To_Local_Operator )/DT9_Msg_Local_Operation_Handover := \"Allow handover from local operator\";T9_Msg_Local_Operation_Handover := TRUE;"),
				new LocalTransition("when( T3_Cd_Local_Operation_Handover )[DT3_Cd_Local_Operation_Handover = \"Handover to local operator established\"]/T43_Output_Established_Handover_To_Local_Operator := TRUE;"),
				new LocalTransition("when( T46_Input_Return_Handover_To_Local_Operator )/DT9_Msg_Local_Operation_Handover := \"Return handover from local operator\";T9_Msg_Local_Operation_Handover := TRUE;"),
				new LocalTransition("when( T3_Cd_Local_Operation_Handover )[DT3_Cd_Local_Operation_Handover = \"Handover to local operator returned\"]/T44_Output_No_Handover_To_Local_Operator := TRUE;"),
				new LocalTransition("when( T40_Activate_By_Local_Operator )/DT8_Msg_Local_Request := \"Local request to activate the level crossing\";T8_Msg_Local_Request := TRUE;"),
				new LocalTransition("when( T41_Deactivate_By_Local_Operator )/DT8_Msg_Local_Request := \"Local request to deactivate the level crossing\";T8_Msg_Local_Request := TRUE;")
			};
		}
	}
}
