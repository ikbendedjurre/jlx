package models.point.v2x7x0A.scen;

import jlx.asal.j.*;
import jlx.scenario.*;
import models.point.v2x7x0A.*;
import models.point.types.*;

public abstract class DB_VAL_SCI_P_053 extends OPERATING_1PM_Scenario {
	public DB_VAL_SCI_P_053(SCI_P_PDI_SR.Block p, SubS_P_SR.Block fe) {
		super(p, fe, PointPos.NO_END_POSITION);
	}
	
	@Override
	public Step[] getSteps() {
		return new Step[] {
			setInput(p.sp.DT10_Move_Point, PointPos.LEFT),
			setInput(p.sp.T10_Move_Point, JPulse.TRUE),
			stabilize(),
			setInput(p.sp.DT10_Move_Point, PointPos.RIGHT),
			setInput(p.sp.T10_Move_Point, JPulse.TRUE),
			stabilize(),
			setInput(fe.p3.D21_PM1_Position, PointPos.RIGHT),
			expectOutput(Output.from(fe.p3.T20_Point_Position, JPulse.FALSE), Output.from(fe.p3.T20_Point_Position, JPulse.TRUE, fe.p3.D20_Point_Position, PointPos.RIGHT), Output.from(fe.p3.T20_Point_Position, JPulse.FALSE)),
			expectPulse(fe.p3.T5_Info_End_Position_Arrived),
			stabilize()
		};
	}
}



