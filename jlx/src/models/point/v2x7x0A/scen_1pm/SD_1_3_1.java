package models.point.v2x7x0A.scen_1pm;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import jlx.scenario.*;
import models.point.v2x7x0A.*;
import models.point.types.*;

public abstract class SD_1_3_1 extends SubS_P_SD {
	public SD_1_3_1(SCI_P_PDI_SR.Block p, SubS_P_SR.Block fe) {
		super(p, fe);
	}
	
	public abstract OutPort<JPulse> getDetectionPort();
	public abstract PointPos getReportedPointPos();
	
	@Override
	public Step[] getSteps() {
		return new Step[] {
			expectOutput(
				Output.from(p.fp.T2_Msg_Point_Position, JPulse.FALSE),
				Output.from(p.fp.T2_Msg_Point_Position, JPulse.TRUE, p.fp.D2_Point_Position, getReportedPointPos()),
				Output.from(p.fp.T2_Msg_Point_Position, JPulse.FALSE)
			)
		};
	}
}
