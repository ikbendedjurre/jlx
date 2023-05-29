package models.point.v2x7x0A.scen;

import jlx.asal.j.*;
import jlx.blocks.ibd1.OutPort;
import jlx.scenario.*;
import models.point.v2x7x0A.*;
import models.point.types.*;

public abstract class BOOTING_1PM_Scenario extends Scenario {
	public final SCI_P_PDI_SR.Block p;
	public final SubS_P_SR.Block fe;
	public final PointPos initPM1Pos;
	public final OutPort<JPulse> pulse;
	public final PointPos d20Value;
	
	public BOOTING_1PM_Scenario(SCI_P_PDI_SR.Block p, SubS_P_SR.Block fe, PointPos initPM1Pos, OutPort<JPulse> pulse, PointPos d20Value) {
		this.p = p;
		this.fe = fe;
		this.initPM1Pos = initPM1Pos;
		this.pulse = pulse;
		this.d20Value = d20Value;
	}
	
	@Override
	public Step[] getSteps() {
		return new Step[] {
			setInput(fe.est.T1_Power_On_Detected, JPulse.TRUE),
			stabilize(),
			setInput(fe.est.T4_Booted, JPulse.TRUE),
			expectPulse(pulse),
			expectOutput(Output.from(fe.p3.T20_Point_Position, JPulse.FALSE), Output.from(fe.p3.T20_Point_Position, JPulse.TRUE, fe.p3.D20_Point_Position, d20Value), Output.from(fe.p3.T20_Point_Position, JPulse.FALSE)),
			stabilize()
		};
	}
}



