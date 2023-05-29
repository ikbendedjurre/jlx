package models.point.v2x6x0A;

import jlx.blocks.ibd2.*;
import models.generic.unclearversion.*;
import models.point.machines.F_PM_Gen;

/**
 * Eu.Doc.36
 * Requirements specification for subsystem Point; 2.6 (0.A)
 * Page 42
 */
public class SubS_P_SR {
	public static class Block extends EULYNX_Field_Element_Subsystem_SR.Block {
		public final F_SCI_P_SR.Block fp = new F_SCI_P_SR.Block();
		public final F_P3_SR.Block p3 = new F_P3_SR.Block();
		public final F_PM_Gen.Block pm1 = new F_PM_Gen.Block();
		public final F_PM_Gen.Block pm2 = new F_PM_Gen.Block();
		public final InterfacePort SCI_P = new InterfacePort();
		
		@Override
		public void connectFlows() {
			//Top left:
			sec.T9_Status_Report_Completed.connect(fp.T23_Sending_Status_Report_Completed);
			sec.T6_Start_Status_Report.connect(fp.T18_Start_Status_Report);
			sec.D50_PDI_Connection_State.connect(fp.D21_F_SCI_EfeS_Gen_SR_State);
			
			//Top right:
			est.D51_EST_EfeS_State.connect(p3.D20_F_EST_EfeS_Gen_SR_State);
			
			//Left:
			fp.T1_Cd_Move_Point.connect(SCI_P);
			fp.DT1_Move_Point_Target.connect(SCI_P);
			fp.T2_Msg_Point_Position.connect(SCI_P);
			fp.DT2_Point_Position.connect(SCI_P);
			fp.T3_Msg_Timeout.connect(SCI_P);
			
			//Middle:
			fp.T10_Move.connect(p3.T1_Move);
			fp.DT10_Move_Target.connect(p3.DT1_Move_Target);
			fp.T11_Stop_Operation.connect(p3.T2_Stop_Operation);
			fp.T20_Point_Position.connect(p3.T20_Point_Position);
			fp.DT20_Point_Position.connect(p3.DT20_Point_Position);
			fp.T40_Send_Status_Report.connect(p3.T40_Report_Status);
			fp.T30_Report_Timeout.connect(p3.T30_Report_Timeout);
			
			//Right:
			p3.D13_Activate_PM1.connect(pm1.D11_Active);
			p3.T12_Reset_PMs.connect(pm1.T2_Reset);
			p3.D21_PM1_Position.connect(pm1.D1_Position_Out);
			p3.T12_Reset_PMs.connect(pm2.T2_Reset);
			p3.D22_PM2_Position.connect(pm2.D1_Position_Out);
		}
	}
}
