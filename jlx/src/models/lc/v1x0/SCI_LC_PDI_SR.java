package models.lc.v1x0;

import jlx.blocks.ibd2.*;
import models.generic.v3x0x0A.EfeS_SCI_XX_PDI_SR;

/**
 * Requirements specification SCI-LC Eu.Doc.108 v1.0
 * Page 24/47
 */
public class SCI_LC_PDI_SR {
	public static class Block extends EfeS_SCI_XX_PDI_SR.Block {
		public final S_SCI_LC_SR.Block slc = new S_SCI_LC_SR.Block();
		public final F_SCI_LC_SR.Block flc = new F_SCI_LC_SR.Block();
		public final InterfacePort SAP_SubS_LC = new InterfacePort();
		
		@Override
		public void connectFlows() {
			//left
			slc.DT10_Report_Local_Operation_Handover.connect(SAP_SubS_EIL);
			slc.DT11_Report_Local_Request.connect(SAP_SubS_EIL);
			slc.DT1_Realise_Activation.connect(SAP_SubS_EIL);
			slc.DT3_Realise_Local_Operation_Handover.connect(SAP_SubS_EIL);
			slc.DT4_Realise_Isolate_LC.connect(SAP_SubS_EIL);
			slc.DT5_Report_LC_Functional_Status.connect(SAP_SubS_EIL);
			slc.DT6_Report_LC_Monitoring_Status.connect(SAP_SubS_EIL);
			slc.DT7_Report_LC_Failure_Status.connect(SAP_SubS_EIL);
			slc.DT8_Report_Detection_Element_Status.connect(SAP_SubS_EIL);
			slc.DT9_Report_Obstacle_Detection_Status.connect(SAP_SubS_EIL);
			slc.T10_Report_Local_Operation_Handover.connect(SAP_SubS_EIL);
			slc.T11_Report_Local_Request.connect(SAP_SubS_EIL);
			slc.T1_Realise_Activation.connect(SAP_SubS_EIL);
			slc.T2_Realise_Deactivation.connect(SAP_SubS_EIL);
			slc.T3_Realise_Local_Operation_Handover.connect(SAP_SubS_EIL);
			slc.T4_Realise_Isolate_LC.connect(SAP_SubS_EIL);
			slc.T5_Report_LC_Functional_Status.connect(SAP_SubS_EIL);
			slc.T6_Report_LC_Monitoring_Status.connect(SAP_SubS_EIL);
			slc.T7_Report_LC_Failure_Status.connect(SAP_SubS_EIL);
			slc.T8_Report_Detection_Element_Status.connect(SAP_SubS_EIL);
			slc.T9_Report_Obstacle_Detection_Status.connect(SAP_SubS_EIL);
			
			//middle
			slc.DT101_Cd_Activation.connect(flc.DT1_Cd_Activation);
			slc.DT103_Cd_Local_Operation_Handover.connect(flc.DT3_Cd_Local_Operation_Handover);
			slc.DT104_Cd_Isolate_LC.connect(flc.DT4_Cd_Isolate_LC);
			slc.DT105_Msg_LC_Functional_Status.connect(flc.DT5_Msg_LC_Functional_Status);
			slc.DT106_Msg_LC_Monitoring_Status.connect(flc.DT6_Msg_LC_Monitoring_Status);
			slc.DT107_Msg_LC_Failure_Status.connect(flc.DT7_Msg_LC_Failure_Status);
			slc.DT108_Msg_Detection_Element_Status.connect(flc.DT8_Msg_Detection_Element_Status);
			slc.DT109_Msg_Obstacle_Detection_Status.connect(flc.DT9_Msg_Obstacle_Detection_Status);
			slc.DT110_Msg_Local_Operation_Handover.connect(flc.DT10_Msg_Local_Operation_Handover);
			slc.DT111_Msg_Local_Request.connect(flc.DT11_Msg_Local_Request);
			slc.T101_Cd_Activation.connect(flc.T1_Cd_Activation);
			slc.T102_Cd_Deactivation.connect(flc.T2_Cd_Deactivation);
			slc.T103_Cd_Local_Operation_Handover.connect(flc.T3_Cd_Local_Operation_Handover);
			slc.T104_Cd_Isolate_LC.connect(flc.T4_Cd_Isolate_LC);
			slc.T105_Msg_LC_Functional_Status.connect(flc.T5_Msg_LC_Functional_Status);
			slc.T106_Msg_LC_Monitoring_Status.connect(flc.T6_Msg_LC_Monitoring_Status);
			slc.T107_Msg_LC_Failure_Status.connect(flc.T7_Msg_LC_Failure_Status);
			slc.T108_Msg_Detection_Element_Status.connect(flc.T8_Msg_Detection_Element_Status);
			slc.T109_Msg_Obstacle_Detection_Status.connect(flc.T9_Msg_Obstacle_Detection_Status);
			slc.T110_Msg_Local_Operation_Handover.connect(flc.T10_Msg_Local_Operation_Handover);
			slc.T111_Msg_Local_Request.connect(flc.T11_Msg_Local_Request);
			
			//right
			flc.DT101_Realise_Activation.connect(SAP_SubS_LC);
			flc.DT103_Realise_Local_Operation_Handover.connect(SAP_SubS_LC);
			flc.DT104_Realise_Isolate_LC.connect(SAP_SubS_LC);
			flc.DT105_Report_LC_Functional_Status.connect(SAP_SubS_LC);
			flc.DT106_Report_LC_Monitoring_Status.connect(SAP_SubS_LC);
			flc.DT107_Report_LC_Failure_Status.connect(SAP_SubS_LC);
			flc.DT108_Report_Detection_Element_Status.connect(SAP_SubS_LC);
			flc.DT109_Report_Obstacle_Detection_Status.connect(SAP_SubS_LC);
			flc.DT110_Report_Local_Operation_Handover.connect(SAP_SubS_LC);
			flc.DT111_Report_Local_Request.connect(SAP_SubS_LC);
			flc.T101_Realise_Activation.connect(SAP_SubS_LC);
			flc.T102_Realise_Deactivation.connect(SAP_SubS_LC);
			flc.T103_Realise_Local_Operation_Handover.connect(SAP_SubS_LC);
			flc.T104_Realise_Isolate_LC.connect(SAP_SubS_LC);
			flc.T105_Report_LC_Functional_Status.connect(SAP_SubS_LC);
			flc.T106_Report_LC_Monitoring_Status.connect(SAP_SubS_LC);
			flc.T107_Report_LC_Failure_Status.connect(SAP_SubS_LC);
			flc.T108_Report_Detection_Element_Status.connect(SAP_SubS_LC);
			flc.T109_Report_Obstacle_Detection_Status.connect(SAP_SubS_LC);
			flc.T110_Report_Local_Operation_Handover.connect(SAP_SubS_LC);
			flc.T111_Report_Local_Request.connect(SAP_SubS_LC);
			flc.T199_All_Status_Send.connect(SAP_SubS_LC);
			
			//top
			slc.D50_PDI_Connection_State.connect(prim.D50_PDI_Connection_State);
			flc.D50_PDI_Connection_State.connect(sec.D50_PDI_Connection_State);
			flc.T52_All_Status_send.connect(sec.T9_Status_Report_Completed);
		}
	}
}
