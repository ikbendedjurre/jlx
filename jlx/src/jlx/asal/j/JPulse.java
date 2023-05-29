package jlx.asal.j;

public class JPulse extends JUserType<JPulse> {
	@JTypeDefaultValue
	@JTypeTextify(format = "FALSE") //We define this because otherwise "JPulse.TRUE" is the automatic format.
	public final static class FALSE extends JPulse {}
	
	@JTypeTextify(format = "TRUE") //We define this because otherwise "JPulse.TRUE" is the automatic format.
	public final static class TRUE extends JPulse {}
	
	public final static FALSE FALSE = new FALSE();
	public final static TRUE TRUE = new TRUE();
}
