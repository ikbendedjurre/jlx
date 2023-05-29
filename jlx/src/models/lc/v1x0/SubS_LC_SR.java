package models.lc.v1x0;

import jlx.blocks.ibd2.InterfacePort;
import models.generic.v3x0x0A.EULYNX_Field_Element_Subsystem_SR;

/**
 * Requirements specification SCI-LC Eu.Doc.108 v1.0
 * Page 25/47
 */
public class SubS_LC_SR {
	public static class Block extends EULYNX_Field_Element_Subsystem_SR.Block {
		public final F_SCI_LC_SR.Block flc = new F_SCI_LC_SR.Block();
		public final F_LC_Functions_SR.Block functions = new F_LC_Functions_SR.Block();
		public final InterfacePort SCI_LC = new InterfacePort();
		public final InterfacePort LC4 = new InterfacePort();
		public final InterfacePort LC5 = new InterfacePort();
		public final InterfacePort LC6 = new InterfacePort();
		
		@Override
		public void connectFlows() {
			//Top:
			sec.T9_Status_Report_Completed.connect(flc.T52_All_Status_send);
			sec.T6_Start_Status_Report.connect(functions.T49_Report_Status);
			sec.D50_PDI_Connection_State.connect(flc.D50_PDI_Connection_State);
			est.D51_EST_EfeS_State.connect(functions.D50_EST_EfeS_State);
			
			//left
			flc.DT10_Msg_Local_Operation_Handover.connect(SCI_LC);
			flc.DT11_Msg_Local_Request.connect(SCI_LC);
			flc.DT1_Cd_Activation.connect(SCI_LC);
			flc.DT3_Cd_Local_Operation_Handover.connect(SCI_LC);
			flc.DT4_Cd_Isolate_LC.connect(SCI_LC);
			flc.DT5_Msg_LC_Functional_Status.connect(SCI_LC);
			flc.DT6_Msg_LC_Monitoring_Status.connect(SCI_LC);
			flc.DT7_Msg_LC_Failure_Status.connect(SCI_LC);
			flc.DT8_Msg_Detection_Element_Status.connect(SCI_LC);
			flc.DT9_Msg_Obstacle_Detection_Status.connect(SCI_LC);
			flc.T10_Msg_Local_Operation_Handover.connect(SCI_LC);
			flc.T11_Msg_Local_Request.connect(SCI_LC);
			flc.T1_Cd_Activation.connect(SCI_LC);
			flc.T2_Cd_Deactivation.connect(SCI_LC);
			flc.T3_Cd_Local_Operation_Handover.connect(SCI_LC);
			flc.T4_Cd_Isolate_LC.connect(SCI_LC);
			flc.T5_Msg_LC_Functional_Status.connect(SCI_LC);
			flc.T6_Msg_LC_Monitoring_Status.connect(SCI_LC);
			flc.T7_Msg_LC_Failure_Status.connect(SCI_LC);
			flc.T8_Msg_Detection_Element_Status.connect(SCI_LC);
			flc.T9_Msg_Obstacle_Detection_Status.connect(SCI_LC);
			
			//middle
			flc.DT101_Realise_Activation.connect(functions.DT1_Cd_Activation);
			flc.DT103_Realise_Local_Operation_Handover.connect(functions.DT3_Cd_Local_Operation_Handover);
			flc.DT104_Realise_Isolate_LC.connect(functions.DT4_Cd_Isolate_LC);
			flc.DT105_Report_LC_Functional_Status.connect(functions.DT5_Msg_LC_Functional_Status);
			flc.DT106_Report_LC_Monitoring_Status.connect(functions.DT6_Msg_LC_Monitoring_Status);
			flc.DT107_Report_LC_Failure_Status.connect(functions.DT7_Msg_LC_Failure_Status);
			flc.DT108_Report_Detection_Element_Status.connect(functions.DT18_Msg_Detection_Element_Status);
			flc.DT109_Report_Obstacle_Detection_Status.connect(functions.DT91_Msg_Obstacle_Detection_Status);
			flc.DT110_Report_Local_Operation_Handover.connect(functions.DT9_Msg_Local_Operation_Handover);
			flc.DT111_Report_Local_Request.connect(functions.DT8_Msg_Local_Request);
			flc.T101_Realise_Activation.connect(functions.T1_Cd_Activation);
			flc.T102_Realise_Deactivation.connect(functions.T2_Cd_Deactivation);
			flc.T103_Realise_Local_Operation_Handover.connect(functions.T3_Cd_Local_Operation_Handover);
			flc.T104_Realise_Isolate_LC.connect(functions.T4_Cd_Isolate_LC);
			flc.T105_Report_LC_Functional_Status.connect(functions.T5_Msg_LC_Functional_Status);
			flc.T106_Report_LC_Monitoring_Status.connect(functions.T6_Msg_LC_Monitoring_Status);
			flc.T107_Report_LC_Failure_Status.connect(functions.T7_Msg_LC_Failure_Status);
			flc.T108_Report_Detection_Element_Status.connect(functions.T18_Msg_Detection_Element_Status);
			flc.T109_Report_Obstacle_Detection_Status.connect(functions.T91_Msg_Obstacle_Detection_Status);
			flc.T110_Report_Local_Operation_Handover.connect(functions.T9_Msg_Local_Operation_Handover);
			flc.T111_Report_Local_Request.connect(functions.T8_Msg_Local_Request);
			flc.T199_All_Status_Send.connect(functions.T99_Msg_All_Status_Send);
			
			//right
			functions.DT30_Status_LCPF.connect(LC4);
			functions.T30_Status_LCPF.connect(LC4);
			functions.T31_Activate_LCPF.connect(LC4);
			functions.T32_Deactivate_LCPF.connect(LC4);
			functions.T33_Pre_Activate_LCPF.connect(LC4);
			functions.T34_National_Specific_State_LCPF.connect(LC4);
			functions.T108_Detection_Element_Status.connect(LC5);
			functions.DT108_Detection_Element_Status.connect(LC5);
			functions.D108_Con_Use_Detection_Element.connect(LC5);
			functions.T40_Activate_By_Local_Operator.connect(LC6);
			functions.T41_Deactivate_By_Local_Operator.connect(LC6);
			functions.T42_Output_Initiated_Handover_To_Local_Operator.connect(LC6);
			functions.T43_Output_Established_Handover_To_Local_Operator.connect(LC6);
			functions.T44_Output_No_Handover_To_Local_Operator.connect(LC6);
			functions.T45_Input_Allow_Handover_To_Local_Operator.connect(LC6);
			functions.T46_Input_Return_Handover_To_Local_Operator.connect(LC6);
		}
	}
}
