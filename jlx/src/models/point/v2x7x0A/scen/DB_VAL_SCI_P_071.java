package models.point.v2x7x0A.scen;

import jlx.asal.j.*;
import jlx.scenario.*;
import models.point.v2x7x0A.*;
import models.point.types.*;

public abstract class DB_VAL_SCI_P_071 extends OPERATING_1PM_Scenario {
	public DB_VAL_SCI_P_071(SCI_P_PDI_SR.Block p, SubS_P_SR.Block fe) {
		super(p, fe, PointPos.LEFT);
	}
	
	@Override
	public Step[] getSteps() {
		return new Step[] {
			setInput(p.sp.DT10_Move_Point, PointPos.RIGHT),
			setInput(p.sp.T10_Move_Point, JPulse.TRUE),
			stabilize(),
			setInput(fe.p3.D21_PM1_Position, PointPos.NO_END_POSITION),
			stabilize(),
			setInput(p.sp.DT10_Move_Point, PointPos.RIGHT),
			setInput(p.sp.T10_Move_Point, JPulse.TRUE),
			stabilize(),
			triggerTimeout(fe.p3.D4_Con_tmax_Point_Operation),
			expectPulse(p.sp.T30_Timeout),
			stabilize()
		};
	}
}



