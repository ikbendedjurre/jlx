package models.point.unclearversion;

import jlx.blocks.ibd2.*;
import models.generic.unclearversion.*;

/**
 * Requirements specification for subsystem Point (v3.0/3A)
 * Page 29
 */
public class SCI_P_PDI_SR {
	public static class Block extends EfeS_SCI_XX_PDI_SR.Block {
//		public final S_SCI_EfeS_Prim_SR.Block prim = new S_SCI_EfeS_Prim_SR.Block();
//		public final F_SCI_EfeS_Sec_SR.Block sec = new F_SCI_EfeS_Sec_SR.Block();
		public final S_SCI_P_SR.Block sp = new S_SCI_P_SR.Block();
		public final F_SCI_P_SR.Block fp = new F_SCI_P_SR.Block();
//		public final InterfacePort SAP_SubS_EIL = new InterfacePort();
		public final InterfacePort SAP_SubS_P = new InterfacePort();
		
		@Override
		public void connectFlows() {
			prim.D50_PDI_Connection_State.connect(sp.D21_S_SCI_EfeS_Gen_SR_State);
			
			sec.D50_PDI_Connection_State.connect(fp.D21_F_SCI_EfeS_Gen_SR_State);
			sec.T6_Start_Status_Report.connect(fp.T18_Start_Status_Report); //Wrong prefix in diagram: T6
			sec.T9_Status_Report_Completed.connect(fp.T23_Sending_Status_Report_Completed); //Wrong prefix in diagram: T9
			
			sp.T1_Cd_Move_Point.connect(fp.T1_Cd_Move_Point);
			sp.DT1_Move_Point_Target.connect(fp.DT1_Move_Point_Target);
			sp.T2_Msg_Point_Position.connect(fp.T2_Msg_Point_Position);
			sp.DT2_Point_Position.connect(fp.DT2_Point_Position);
			sp.T3_Msg_Timeout.connect(fp.T3_Msg_Timeout);
			
			sp.T10_Move_Point.connect(SAP_SubS_EIL);
			sp.DT10_Move_Point.connect(SAP_SubS_EIL);
			sp.T20_Point_Position.connect(SAP_SubS_EIL);
			sp.DT20_Point_Position.connect(SAP_SubS_EIL);
			sp.T30_Timeout.connect(SAP_SubS_EIL);
			
			fp.T10_Move.connect(SAP_SubS_P);
			fp.DT10_Move_Target.connect(SAP_SubS_P);
			fp.T11_Stop_Operation.connect(SAP_SubS_P);
			fp.T20_Point_Position.connect(SAP_SubS_P);
			fp.DT20_Point_Position.connect(SAP_SubS_P);
			fp.T40_Send_Status_Report.connect(SAP_SubS_P);
			fp.T30_Report_Timeout.connect(SAP_SubS_P);
		}
	}
}
