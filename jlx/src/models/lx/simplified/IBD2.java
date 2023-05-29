package models.lx.simplified;

import jlx.blocks.ibd2.Type2IBD;

public class IBD2 {
	public static class Block extends Type2IBD {
		public final IL_IBD1.Block IL = new IL_IBD1.Block();
		public final LX_IBD1.Block LX = new LX_IBD1.Block();
		
		@Override
		public void connectFlows() {
			LX.T7_Cd_PDI_Version_Check.connect(IL.T7_Cd_PDI_Version_Check);
			LX.DT7_PDI_Version.connect(IL.DT7_PDI_Version);
			LX.T8_Cd_Initialisation_Request.connect(IL.T8_Cd_Initialisation_Request);
			LX.T13_Msg_PDI_Version_Check.connect(IL.T13_Msg_PDI_Version_Check);
			LX.DT13_Result.connect(IL.DT13_Result);
			LX.DT13_Checksum_Data.connect(IL.DT13_Checksum_Data);
			LX.T14_Msg_Start_Initialisation.connect(IL.T14_Msg_Start_Initialisation);
			LX.T29_Msg_Status_Report_Completed.connect(IL.T25_Msg_Status_Report_Completed);
			LX.T24_Msg_Status_Report_Completed.connect(IL.T26_Msg_Status_Report_Completed);
			LX.T15_Msg_Initialisation_Completed.connect(IL.T15_Msg_Initialisation_Completed);
			
			LX.T6_Conn_Req.connect(IL.T5_Conn_Req);
			LX.T7_Conn_Resp.connect(IL.T6_Conn_Resp);
			LX.T8_HB.connect(IL.T7_HB);
			LX.T11_Disc_Req.connect(IL.T9_Disc_Req);
			LX.T9_Disc_Req.connect(IL.T8_Disc_Req);
			
			IL.T101_Msg_XX.connect(LX.T100_Msg_XX);
			IL.DT101_Type.connect(LX.DT100_Type);
			IL.T100_Cd_XX.connect(LX.T101_Msg_XX); //-> Used to be T101_Cd_XX 
			IL.DT100_Type.connect(LX.DT101_Type);
		}
	}
}

