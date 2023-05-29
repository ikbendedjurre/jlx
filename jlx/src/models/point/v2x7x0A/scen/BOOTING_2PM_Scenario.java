package models.point.v2x7x0A.scen;

import jlx.asal.j.*;
import jlx.blocks.ibd1.OutPort;
import jlx.scenario.*;
import models.generic.types.EST_EfeS_State;
import models.generic.types.PDI_Checksum_Data;
import models.generic.types.PDI_Checksum_Result;
import models.generic.types.PDI_Connection_State;
import models.generic.types.PDI_Version;
import models.generic.v3x0x0A.*;
import models.point.v2x7x0A.*;
import models.point.types.*;

public abstract class BOOTING_2PM_Scenario extends Scenario {
	public final SCI_P_PDI_SR.Block p;
	public final SubS_P_SR.Block fe;
	public final PointPos initPM1Pos;
	public final PointPos initPM2Pos;
	public final OutPort<JPulse> pulse;
	public final PointPos d20Value;
	
	public BOOTING_2PM_Scenario(SCI_P_PDI_SR.Block p, SubS_P_SR.Block fe, PointPos initPM1Pos, PointPos initPM2Pos, OutPort<JPulse> pulse, PointPos d20Value) {
		this.p = p;
		this.fe = fe;
		this.initPM1Pos = initPM1Pos;
		this.initPM2Pos = initPM2Pos;
		this.pulse = pulse;
		this.d20Value = d20Value;
	}
	
	public S_SCI_EfeS_Prim_SR.Block get_prim() {
		return p.prim;
	}
	
	public F_SCI_EfeS_Sec_SR.Block get_sec() {
		return p.sec;
	}
	
	public F_EST_EfeS_SR.Block get_est() {
		return fe.est;
	}
	
	public F_P3_SR.Block get_p3() {
		return fe.p3;
	}
	
	public F_SCI_P_SR.Block get_fp() {
		return fe.fp;
	}
	
	public S_SCI_P_SR.Block get_sp() {
		return p.sp;
	}
	
	@Override
	public Step[] getSteps() {
		return new Step[] {
			expectStateChange(get_fp(), "fp[0][WATING]"),
			expectStateChange(get_sp(), "sp[0][REPORT_STATUS]"),
			expectStateChange(get_p3(), "p3[0][WAITING_FOR_INITIALISING+STOPPED]"),
			expectStateChange(get_sec(), "sec[0][NOT_READY_FOR_CONNECTION]"),
			expectStateChange(get_prim(), "prim[0][PDI_CONNECTION_CLOSED]", "prim[1][PDI_CONNECTION_CLOSED]"),
			expectStateChange(get_est(), "est[0][NO_OPERATING_VOLTAGE]"),
			expectOutput(Output.from(get_prim().T6_Establish_SCP_Connection, JPulse.TRUE), Output.from(get_prim().T6_Establish_SCP_Connection, JPulse.FALSE)),
			stabilize(),
			setInput(get_est().T1_Power_On_Detected, JPulse.TRUE),
			expectStateChange(get_fp(), "fp[0][WATING]"),
			expectStateChange(get_sp(), "sp[0][REPORT_STATUS]"),
			expectStateChange(get_p3(), "p3[0][WAITING_FOR_INITIALISING+STOPPED]"),
			expectStateChange(get_sec(), "sec[0][NOT_READY_FOR_CONNECTION]"),
			expectStateChange(get_prim(), "prim[1][PDI_CONNECTION_CLOSED]"),
			expectStateChange(get_est(), "est[0][NO_OPERATING_VOLTAGE]", "est[1][BOOTING]"),
			expectOutput(Output.from(get_est().D51_EST_EfeS_State, new EST_EfeS_State.NO_OPERATING_VOLTAGE()), Output.from(get_est().D51_EST_EfeS_State, new EST_EfeS_State.BOOTING())),
			stabilize(),
			setInput(get_est().T4_Booted, JPulse.TRUE),
			expectStateChange(get_fp(), "fp[0][WATING]"),
			expectStateChange(get_sp(), "sp[0][REPORT_STATUS]"),
			expectStateChange(get_p3(), "p3[0][WAITING_FOR_INITIALISING+STOPPED]"),
			expectStateChange(get_sec(), "sec[0][NOT_READY_FOR_CONNECTION]"),
			expectStateChange(get_prim(), "prim[1][PDI_CONNECTION_CLOSED]"),
			expectStateChange(get_est(), "est[1][BOOTING]", "est[4][INITIALISING]"),
			expectOutput(Output.from(get_est().T21_Ready_For_PDI_Connection, JPulse.FALSE), Output.from(get_est().T21_Ready_For_PDI_Connection, JPulse.TRUE), Output.from(get_est().T21_Ready_For_PDI_Connection, JPulse.FALSE)),
			expectOutput(Output.from(get_p3().T6_Information_Trailed_Point, JPulse.FALSE), Output.from(get_p3().T6_Information_Trailed_Point, JPulse.TRUE), Output.from(get_p3().T6_Information_Trailed_Point, JPulse.FALSE)),
			expectOutput(Output.from(get_p3().D20_Point_Position, new PointPos.UNKNOWN()), Output.from(get_p3().D20_Point_Position, new PointPos.TRAILED())),
			expectOutput(Output.from(get_p3().T20_Point_Position, JPulse.FALSE), Output.from(get_p3().T20_Point_Position, JPulse.TRUE), Output.from(get_p3().T20_Point_Position, JPulse.FALSE)),
			expectOutput(Output.from(get_p3().D6_Detection_State, new Detection_State.FALSE()), Output.from(get_p3().D6_Detection_State, new Detection_State.TRAILED())),
			expectOutput(Output.from(get_sec().D50_PDI_Connection_State, new PDI_Connection_State.CLOSED()), Output.from(get_sec().D50_PDI_Connection_State, new PDI_Connection_State.CLOSED_READY())),
			expectOutput(Output.from(get_est().D51_EST_EfeS_State, new EST_EfeS_State.BOOTING()), Output.from(get_est().D51_EST_EfeS_State, new EST_EfeS_State.INITIALISING())),
			stabilize(),
			setInput(get_sec().T5_SCP_Connection_Established, JPulse.TRUE),
			expectStateChange(get_fp(), "fp[0][WATING]"),
			expectStateChange(get_sp(), "sp[0][REPORT_STATUS]"),
			expectStateChange(get_p3(), "p3[11][TRAILED+STOPPED]"),
			expectStateChange(get_sec(), "sec[1][READY_FOR_CONNECTION]", "sec[2][READY_FOR_VERSION_CHECK]"),
			expectStateChange(get_prim(), "prim[1][PDI_CONNECTION_CLOSED]"),
			expectStateChange(get_est(), "est[8][INITIALISING]"),
			expectOutput(Output.from(get_sec().D50_PDI_Connection_State, new PDI_Connection_State.CLOSED_READY()), Output.from(get_sec().D50_PDI_Connection_State, new PDI_Connection_State.READY_FOR_VERSION_CHECK())),
			stabilize(),
			setInput(get_prim().T5_SCP_Connection_Established, JPulse.TRUE),
			expectStateChange(get_fp(), "fp[0][WATING]"),
			expectStateChange(get_sp(), "sp[0][REPORT_STATUS]"),
			expectStateChange(get_p3(), "p3[11][TRAILED+STOPPED]"),
			expectStateChange(get_sec(), "sec[2][READY_FOR_VERSION_CHECK]"),
			expectStateChange(get_prim(), "prim[1][PDI_CONNECTION_CLOSED]", "prim[2][WAITING_FOR_VERSION_CHECK]"),
			expectStateChange(get_est(), "est[8][INITIALISING]"),
			expectOutput(Output.from(get_sec().T13_Msg_PDI_Version_Check, JPulse.FALSE), Output.from(get_sec().DT13_Checksum_Data, new PDI_Checksum_Data.NotApplicable(), get_sec().T13_Msg_PDI_Version_Check, JPulse.TRUE, get_sec().DT13_Result, new PDI_Checksum_Result.Match()), Output.from(get_sec().T13_Msg_PDI_Version_Check, JPulse.FALSE)),
			expectOutput(Output.from(get_prim().T7_Cd_PDI_Version_Check, JPulse.FALSE), Output.from(get_prim().DT7_PDI_Version, new PDI_Version.V1(), get_prim().T7_Cd_PDI_Version_Check, JPulse.TRUE), Output.from(get_prim().T7_Cd_PDI_Version_Check, JPulse.FALSE)),
			expectOutput(Output.from(get_fp().T40_Send_Status_Report, JPulse.FALSE), Output.from(get_fp().T40_Send_Status_Report, JPulse.TRUE), Output.from(get_fp().T40_Send_Status_Report, JPulse.FALSE)),
			expectOutput(Output.from(get_fp().T2_Msg_Point_Position, JPulse.FALSE), Output.from(get_fp().T2_Msg_Point_Position, JPulse.TRUE), Output.from(get_fp().T2_Msg_Point_Position, JPulse.FALSE)),
			expectOutput(Output.from(get_sp().T20_Point_Position, JPulse.FALSE), Output.from(get_sp().DT20_Point_Position, new PointPos.TRAILED(), get_sp().T20_Point_Position, JPulse.TRUE), Output.from(get_sp().T20_Point_Position, JPulse.FALSE)),
			expectOutput(Output.from(get_sec().T15_Msg_Initialisation_Completed, JPulse.FALSE), Output.from(get_sec().T15_Msg_Initialisation_Completed, JPulse.TRUE), Output.from(get_sec().T15_Msg_Initialisation_Completed, JPulse.FALSE)),
			expectOutput(Output.from(get_p3().T20_Point_Position, JPulse.FALSE), Output.from(get_p3().T20_Point_Position, JPulse.TRUE), Output.from(get_p3().T20_Point_Position, JPulse.FALSE)),
			expectOutput(Output.from(get_fp().D2_Point_Position, new PointPos.NONE()), Output.from(get_fp().D2_Point_Position, new PointPos.TRAILED())),
			expectOutput(Output.from(get_sec().T14_Msg_Start_Initialisation, JPulse.FALSE), Output.from(get_sec().T14_Msg_Start_Initialisation, JPulse.TRUE), Output.from(get_sec().T14_Msg_Start_Initialisation, JPulse.FALSE)),
			expectOutput(Output.from(get_sec().T6_Start_Status_Report, JPulse.FALSE), Output.from(get_sec().T6_Start_Status_Report, JPulse.TRUE), Output.from(get_sec().T6_Start_Status_Report, JPulse.FALSE)),
			expectOutput(Output.from(get_sec().D50_PDI_Connection_State, new PDI_Connection_State.READY_FOR_VERSION_CHECK()), Output.from(get_sec().D50_PDI_Connection_State, new PDI_Connection_State.READY_FOR_INITIALISATION()), Output.from(get_sec().D50_PDI_Connection_State, new PDI_Connection_State.SENDING_STATUS()), Output.from(get_sec().D50_PDI_Connection_State, new PDI_Connection_State.ESTABLISHED())),
			expectOutput(Output.from(get_sec().T11_PDI_Connection_Established, JPulse.FALSE), Output.from(get_sec().T11_PDI_Connection_Established, JPulse.TRUE), Output.from(get_sec().T11_PDI_Connection_Established, JPulse.FALSE)),
			expectOutput(Output.from(get_fp().T23_Sending_Status_Report_Completed, JPulse.FALSE), Output.from(get_fp().T23_Sending_Status_Report_Completed, JPulse.TRUE), Output.from(get_fp().T23_Sending_Status_Report_Completed, JPulse.FALSE)),
			expectOutput(Output.from(get_est().D51_EST_EfeS_State, new EST_EfeS_State.INITIALISING()), Output.from(get_est().D51_EST_EfeS_State, new EST_EfeS_State.OPERATIONAL())),
			expectOutput(Output.from(get_prim().T8_Cd_Initialisation_Request, JPulse.FALSE), Output.from(get_prim().T8_Cd_Initialisation_Request, JPulse.TRUE), Output.from(get_prim().T8_Cd_Initialisation_Request, JPulse.FALSE)),
			expectOutput(Output.from(get_prim().D50_PDI_Connection_State, new PDI_Connection_State.CLOSED_REQUESTED()), Output.from(get_prim().D50_PDI_Connection_State, new PDI_Connection_State.WAITING_FOR_VERSION_CHECK()), Output.from(get_prim().D50_PDI_Connection_State, new PDI_Connection_State.WAITING_FOR_INITIALISATION()), Output.from(get_prim().D50_PDI_Connection_State, new PDI_Connection_State.RECEIVING_STATUS()), Output.from(get_prim().D50_PDI_Connection_State, new PDI_Connection_State.ESTABLISHED())),
			stabilize()
		};
	}
}



