package models.point.unclearversion;

import jlx.blocks.ibd2.*;
import models.generic.unclearversion.*;

/**
 * Requirements specification for subsystem Point (v3.0/3A)
 * Page 31
 */
public class SubS_P_SR {
	public static class Block extends EULYNX_Field_Element_Subsystem_SR.Block {
//		public final F_SCI_EfeS_Sec_SR.Block sec = new F_SCI_EfeS_Sec_SR.Block();
		public final F_SCI_P_SR.Block fp = new F_SCI_P_SR.Block();
//		public final F_EST_EfeS_SR.Block est = new F_EST_EfeS_SR.Block();
		public final F_P3_SR.Block p3 = new F_P3_SR.Block();
		
		public final InterfacePort SCI_P = new InterfacePort();
		public final InterfacePort P3 = new InterfacePort();
		
		@Override
		public void connectFlows() {
			fp.T1_Cd_Move_Point.connect(SCI_P);
			fp.DT1_Move_Point_Target.connect(SCI_P);
			fp.T2_Msg_Point_Position.connect(SCI_P);
			fp.DT2_Point_Position.connect(SCI_P);
			fp.T3_Msg_Timeout.connect(SCI_P);
			
			p3.D21_PM1_Position.connect(P3);
			p3.D13_PM2_Activation.connect(P3);
			p3.D22_PM2_Position.connect(P3);
			p3.D10_Move_Left.connect(P3);
			p3.D11_Move_Right.connect(P3);
			p3.D4_Con_tmax_Point_Operation.connect(P3);
			p3.D5_Drive_State.connect(P3);
			p3.D6_Detection_State.connect(P3);
			p3.T5_Info_End_Position_Arrived.connect(P3);
			p3.T4_Information_No_End_Position.connect(P3);
			p3.T6_Information_Trailed_Point.connect(P3);
			p3.T7_Information_Out_Of_Sequence.connect(P3);
			
			fp.T10_Move.connect(p3.T1_Move);
			fp.DT10_Move_Target.connect(p3.DT1_Move_Target);
			fp.T11_Stop_Operation.connect(p3.T2_Stop_Operation);
			fp.T20_Point_Position.connect(p3.T20_Point_Position);
			fp.DT20_Point_Position.connect(p3.DT20_Point_Position);
			fp.T40_Send_Status_Report.connect(p3.T40_Report_Status);
			fp.T30_Report_Timeout.connect(p3.T30_Report_Timeout);
			
			sec.T9_Status_Report_Completed.connect(fp.T23_Sending_Status_Report_Completed); //Wrong prefix in diagram: T9
			sec.T6_Start_Status_Report.connect(fp.T18_Start_Status_Report); //Wrong prefix in diagram: T6
			sec.D50_PDI_Connection_State.connect(fp.D21_F_SCI_EfeS_Gen_SR_State); //Wrong prefix in diagram: D50
			
			est.D51_EST_EfeS_State.connect(p3.D20_F_EST_EfeS_Gen_SR_State); //Wrong prefix in diagram: D51
		}
	}
}
