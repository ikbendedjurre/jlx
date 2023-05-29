package models.point.v2x7x0A.scen;

import jlx.asal.j.*;
import jlx.scenario.*;
import models.point.v2x7x0A.*;
import models.point.types.*;

public abstract class REVERSING_1PM_Scenario extends OPERATING_1PM_Scenario {
	public final PointPos instrDir1;
	public final PointPos instrDir2;
	
	public REVERSING_1PM_Scenario(SCI_P_PDI_SR.Block p, SubS_P_SR.Block fe, PointPos initPM1Pos, PointPos instrDir1, PointPos instrDir2) {
		super(p, fe, initPM1Pos);
		
		this.instrDir1 = instrDir1;
		this.instrDir2 = instrDir2;
	}
	
	@Override
	public Step[] getSteps() {
		return new Step[] {
			setInput(p.sp.DT10_Move_Point, instrDir1),
			setInput(p.sp.T10_Move_Point, JPulse.TRUE),
			stabilize(),
			setInput(fe.p3.D21_PM1_Position, PointPos.NO_END_POSITION),
			expectOutput(Output.from(fe.p3.T20_Point_Position, JPulse.FALSE), Output.from(fe.p3.T20_Point_Position, JPulse.TRUE, fe.p3.D20_Point_Position, PointPos.NO_END_POSITION), Output.from(fe.p3.T20_Point_Position, JPulse.FALSE)),
			expectPulse(fe.p3.T4_Information_No_End_Position),
			stabilize(),
			setInput(p.sp.DT10_Move_Point, instrDir2),
			setInput(p.sp.T10_Move_Point, JPulse.TRUE),
			stabilize(),
			setInput(fe.p3.D21_PM1_Position, instrDir2),
			expectOutput(Output.from(fe.p3.T20_Point_Position, JPulse.FALSE), Output.from(fe.p3.T20_Point_Position, JPulse.TRUE, fe.p3.D20_Point_Position, instrDir2), Output.from(fe.p3.T20_Point_Position, JPulse.FALSE)),
			expectPulse(getPulsePort(instrDir2)),
			stabilize()
		};
	}
}



