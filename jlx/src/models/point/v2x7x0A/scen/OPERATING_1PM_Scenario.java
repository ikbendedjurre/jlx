package models.point.v2x7x0A.scen;

import jlx.asal.j.*;
import jlx.blocks.ibd1.OutPort;
import jlx.scenario.*;
import models.point.v2x7x0A.*;
import models.point.types.*;

public abstract class OPERATING_1PM_Scenario extends Scenario {
	public final SCI_P_PDI_SR.Block p;
	public final SubS_P_SR.Block fe;
	public final PointPos initPM1Pos;
	
	public OPERATING_1PM_Scenario(SCI_P_PDI_SR.Block p, SubS_P_SR.Block fe, PointPos initPM1Pos) {
		this.p = p;
		this.fe = fe;
		this.initPM1Pos = initPM1Pos;
	}
	
	protected OutPort<JBool> getBoolPort(PointPos pos) {
		if (JType.isEqual(pos, PointPos.LEFT)) {
			return fe.p3.D10_Move_Left;
		}
		
		if (JType.isEqual(pos, PointPos.RIGHT)) {
			return fe.p3.D11_Move_Right;
		}
		
		throw new Error("Should not happen!");
	}
	
	protected OutPort<JPulse> getPulsePort(PointPos pos) {
		if (JType.isEqual(pos, PointPos.NO_END_POSITION)) {
			return fe.p3.T4_Information_No_End_Position;
		}
		
		if (JType.isEqual(pos, PointPos.LEFT) || JType.isEqual(pos, PointPos.RIGHT)) {
			return fe.p3.T5_Info_End_Position_Arrived;
		}
		
		if (JType.isEqual(pos, PointPos.TRAILED)) {
			return fe.p3.T6_Information_Trailed_Point;
		}
		
		throw new Error("Should not happen!");
	}
}



