package models.lx;

import models.adjacent.*;

/**
 * Requirements specification SCI-LX Eu.Doc.111 v1.0
 * Page 16/23
 */
public class SCI_LX_PDI_SR {
	public static class Block extends AdjS_SCI_XX_PDI_SR.Block { //PDF does not specify this inheritance!
//		public final S_SCI_AdjS_Prim_SR prim = new S_SCI_AdjS_Prim_SR();
//		public final S_SCI_AdjS_Sec_SR sec = new S_SCI_AdjS_Sec_SR();
		public final S_SCI_LX_Prim_SR.Block prim_LX = new S_SCI_LX_Prim_SR.Block();
		public final S_SCI_LX_Sec_SR.Block sec_LX = new S_SCI_LX_Sec_SR.Block();
		
		@Override
		public void connectFlows() {
			super.connectFlows();
			
			//Top middle right:
			prim.T28_Start_Prim_Status_Report.connect(prim_LX.T24_Start_Prim_Status_Report);
			prim.T25_Sec_Status_Report_Complete.connect(prim_LX.T23_Sec_Status_Report_Complete);
			prim.T26_Prim_Status_Report_Completed.connect(prim_LX.T26_Prim_Status_Report_Completed);
			prim.T27_Check_Sec_Status.connect(prim_LX.T18_Check_Sec_Status);
			prim.T31_PDI_Connection_Established.connect(prim_LX.T1_PDI_Connection_Established);
			prim.T32_PDI_Connection_Closed.connect(prim_LX.T2_PDI_Connection_Closed);
			prim.T33_Establishing_PDI_Connection.connect(prim_LX.T3_Establishing_PDI_Connection);
			
			//Top middle left:
			sec.T6_Start_Sec_Status_Report.connect(sec_LX.T12_Start_Sec_Status_Report);
			sec.T17_Prim_Status_Report_Complete.connect(sec_LX.T19_Prim_Status_Report_Complete);
			sec.T24_Sec_Status_Report_Completed.connect(sec_LX.T15_Sec_Status_Report_Complete);
			sec.T27_Check_Prim_Status.connect(sec_LX.T16_Check_Prim_Status);
			sec.T31_PDI_Connection_Established.connect(sec_LX.T1_PDI_Connection_Established);
			sec.T32_PDI_Connection_Closed.connect(sec_LX.T2_PDI_Connection_Closed);
			sec.T33_Establishing_PDI_Connection.connect(sec_LX.T3_Establishing_PDI_Connection);
			
			//Bottom middle
			sec_LX.T101_Msg_XX.connect(prim_LX.T100_Msg_XX);
			sec_LX.DT101_Type.connect(prim_LX.DT100_Type);
			sec_LX.T100_Cd_XX.connect(prim_LX.T101_Msg_XX); //-> Used to be T101_Cd_XX 
			sec_LX.DT100_Type.connect(prim_LX.DT101_Type);
		}
	}
}
