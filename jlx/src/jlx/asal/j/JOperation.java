package jlx.asal.j;

public class JOperation extends JUserType<JOperation> {
	@JTypeTextify(format = "[before] !")
	public final static class NOT extends JOperation {}
	@JTypeTextify(format = "&&")
	public final static class AND extends JOperation {}
	@JTypeTextify(format = "||")
	public final static class OR extends JOperation {}
	@JTypeTextify(format = "+")
	public final static class PLUS extends JOperation {}
	@JTypeTextify(format = "-")
	public final static class MINUS extends JOperation {}
	@JTypeTextify(format = "*")
	public final static class TIMES extends JOperation {}
	@JTypeTextify(format = "/")
	public final static class DIVIDE extends JOperation {}
	@JTypeTextify(format = "%")
	public final static class MODULO extends JOperation {}
	@JTypeTextify(format = "<=")
	public final static class LEQ extends JOperation {}
	@JTypeTextify(format = ">=")
	public final static class GEQ extends JOperation {}
	@JTypeTextify(format = "<")
	public final static class LESSER extends JOperation {}
	@JTypeTextify(format = ">")
	public final static class GREATER extends JOperation {}
	
	public final static NOT NOT = new NOT();
	public final static AND AND = new AND();
	public final static OR OR = new OR();
	public final static PLUS PLUS = new PLUS();
	public final static MINUS MINUS = new MINUS();
	public final static TIMES TIMES = new TIMES();
	public final static DIVIDE DIVIDE = new DIVIDE();
	public final static MODULO MODULO = new MODULO();
	public final static LEQ LEQ = new LEQ();
	public final static GEQ GEQ = new GEQ();
	public final static LESSER LESSER = new LESSER();
	public final static GREATER GREATER = new GREATER();
}
