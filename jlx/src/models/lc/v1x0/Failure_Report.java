package models.lc.v1x0;

import jlx.asal.j.JTypeName;
import jlx.asal.j.JUserType;

public class Failure_Report extends JUserType<Failure_Report> {
	@JTypeName(s = "undefined")
	public final static class undefined extends Failure_Report {}
	public final static undefined undefined = new undefined();
	@JTypeName(s = "non critical failure report")
	public final static class non_critical_failure_report extends Failure_Report {}
	public final static non_critical_failure_report non_critical_failure_report = new non_critical_failure_report();
	@JTypeName(s = "critical failure report")
	public final static class critical_failure_report extends Failure_Report {}
	public final static critical_failure_report critical_failure_report = new critical_failure_report();
	@JTypeName(s = "no failure report")
	public final static class no_failure_report extends Failure_Report {}
	public final static no_failure_report no_failure_report = new no_failure_report();
}
