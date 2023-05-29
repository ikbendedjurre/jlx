package models.point.v2x7x0A.scen;

import jlx.asal.j.*;
import jlx.scenario.*;
import models.point.v2x7x0A.*;
import models.point.types.*;

public abstract class REPEATING_1PM_Scenario extends OPERATING_1PM_Scenario {
	public final PointPos instrDir;
	
	public REPEATING_1PM_Scenario(SCI_P_PDI_SR.Block p, SubS_P_SR.Block fe, PointPos initPM1Pos, PointPos instrDir) {
		super(p, fe, initPM1Pos);
		
		this.instrDir = instrDir;
	}
	
	@Override
	public Step[] getSteps() {
		return new Step[] {
			setInput(p.sp.DT10_Move_Point, instrDir),
			setInput(p.sp.T10_Move_Point, JPulse.TRUE),
			expectOutput(Output.from(getBoolPort(instrDir), JBool.TRUE)),
			stabilize(),
			setInput(p.sp.DT10_Move_Point, instrDir),
			setInput(p.sp.T10_Move_Point, JPulse.TRUE),
			stabilize(),
			setInput(fe.p3.D21_PM1_Position, instrDir),
			expectOutput(Output.from(fe.p3.T20_Point_Position, JPulse.FALSE), Output.from(fe.p3.T20_Point_Position, JPulse.TRUE, fe.p3.D20_Point_Position, instrDir), Output.from(fe.p3.T20_Point_Position, JPulse.FALSE)),
			expectPulse(getPulsePort(instrDir))
		};
	}
}



