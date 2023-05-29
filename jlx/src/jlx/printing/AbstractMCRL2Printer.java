package jlx.printing;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

import jlx.asal.j.*;
import jlx.asal.ops.ASALOp;
import jlx.asal.vars.*;
import jlx.behave.StateMachine;
import jlx.behave.proto.*;
import jlx.models.UnifyingBlock;
import jlx.models.UnifyingBlock.ReprBlock;
import jlx.utils.UnusedNames;

public abstract class AbstractMCRL2Printer extends AbstractPrinter<UnifyingBlock> {
	protected final List<String> varNames;
	protected final List<String> funcNames;
	protected final List<String> stateNames;
	protected final Map<Class<? extends JType>, String> namePerType;
	protected final Map<String, Class<? extends JType>> typePerName;
	protected final Map<Class<? extends JType>, String> namePerConstrDecl;
	protected final Map<String, Class<? extends JType>> constrDeclPerName;
	protected final Map<String, String> elemNamePerEnvName;
	protected final Map<String, String> envNamePerElemName;
	protected final List<String> elemAndEnvNames;
	
	protected final Map<String, String> customKeywords;
	
	public AbstractMCRL2Printer(UnifyingBlock target) {
		super(target);
		
		registerKeywords();
		customKeywords = getCustomKeywords();
		
		varNames = extractVarNames();
		funcNames = extractFuncNames();
		stateNames = extractStateNames();
		namePerType = extractNamePerType();
		typePerName = extractTypePerName();
		namePerConstrDecl = extractStrPerConstrDecl();
		constrDeclPerName = extractConstrDeclPerStr();
		
		elemNamePerEnvName = extractElemPerEnvName();
		envNamePerElemName = extractEnvPerElemName();
		elemAndEnvNames = extractElemAndEnvNames();
	}
	
	private void registerKeywords() {
		getUnusedName("act");
		getUnusedName("allow");
		getUnusedName("block");
		getUnusedName("comm");
		getUnusedName("cons");
		getUnusedName("delay");
		getUnusedName("div");
		getUnusedName("end");
		getUnusedName("eqn");
		getUnusedName("exists");
		getUnusedName("forall");
		getUnusedName("glob");
		getUnusedName("hide");
		getUnusedName("if");
		getUnusedName("in");
		getUnusedName("init");
		getUnusedName("lambda");
		getUnusedName("map");
		getUnusedName("mod");
		getUnusedName("mu");
		getUnusedName("nu");
		getUnusedName("pbes");
		getUnusedName("proc");
		getUnusedName("rename");
		getUnusedName("sort");
		getUnusedName("struct");
		getUnusedName("sum");
		getUnusedName("val");
		getUnusedName("var");
		getUnusedName("whr");
		getUnusedName("yaled");
		getUnusedName("Bag");
		getUnusedName("Bool");
		getUnusedName("Int");
		getUnusedName("List");
		getUnusedName("Nat");
		getUnusedName("Pos");
		getUnusedName("Real");
		getUnusedName("Set");
		getUnusedName("delta");
		getUnusedName("false");
		getUnusedName("nil");
		getUnusedName("tau");
		getUnusedName("true");
		
		//TODO some keywords are missing, such as 'head' / 'rtail' / ...
	}
	
	@Override
	protected void println(String s) {
		String newS = s;
		
		for (Map.Entry<String, String> entry : customKeywords.entrySet()) {
			newS = newS.replace(entry.getKey(), entry.getValue());
		}
		
		super.println(newS);
	}
	
	private Map<String, String> getCustomKeywords() {
		Map<String, String> result = new HashMap<String, String>();
		
		//We reserve the following identifiers for our own goals:
		addCustomKeyword(result, "env", "Environment");
		addCustomKeyword(result, "value", "Value");
		addCustomKeyword(result, "int", "Value_Int");
		addCustomKeyword(result, "bool", "Value_Bool");
		addCustomKeyword(result, "pulse", "Value_Bool");
		addCustomKeyword(result, "code", "ASALCode");
		addCustomKeyword(result, "getCode", "getASALCode");
		addCustomKeyword(result, "isCode", "isASALCode");
		addCustomKeyword(result, "string", "Value_String");
		addCustomKeyword(result, "string.lit", "String");
		addCustomKeyword(result, "op1", "ASALUnaryOp");
		addCustomKeyword(result, "op1.+", "ASALUnaryOp_Plus");
		addCustomKeyword(result, "op1.-", "ASALUnaryOp_Minus");
		addCustomKeyword(result, new String[] { "op1.not", "op1.NOT" }, "ASALUnaryOp_Negation");
		addCustomKeyword(result, "op2", "ASALBinaryOp");
		addCustomKeyword(result, "op2.+", "ASALBinaryOp_Add");
		addCustomKeyword(result, "op2.-", "ASALBinaryOp_Subtract");
		addCustomKeyword(result, "op2.*", "ASALBinaryOp_Mult");
		addCustomKeyword(result, "op2./", "ASALBinaryOp_Div");
		addCustomKeyword(result, "op2.%", "ASALBinaryOp_Mod");
		addCustomKeyword(result, "op2.=", "ASALBinaryOp_Eq");
		addCustomKeyword(result, "op2.<>", "ASALBinaryOp_Neq");
		addCustomKeyword(result, "op2.<=", "ASALBinaryOp_Leq");
		addCustomKeyword(result, "op2.>=", "ASALBinaryOp_Geq");
		addCustomKeyword(result, "op2.<", "ASALBinaryOp_Less");
		addCustomKeyword(result, "op2.>", "ASALBinaryOp_Greater");
		addCustomKeyword(result, "op2.and", "ASALBinaryOp_And");
		addCustomKeyword(result, "op2.or", "ASALBinaryOp_Or");
		addCustomKeyword(result, "op2.xor", "ASALBinaryOp_Xor");
		
		addCustomKeyword(result, "asala.pushGlobalVar", "ASALA_PushGlobalVar");
		addCustomKeyword(result, "asala.setGlobalVar", "ASALA_SetGlobalVar");
		addCustomKeyword(result, "asala.pushLocalVar", "ASALA_PushLocalVar");
		addCustomKeyword(result, "asala.setLocalVar", "ASALA_SetLocalVar");
		addCustomKeyword(result, "asala.op1", "ASALA_Op1");
		addCustomKeyword(result, "asala.op2", "ASALA_Op2");
		addCustomKeyword(result, "asala.fct", "ASALA_Fct");
		addCustomKeyword(result, "asala.jump", "ASALA_Jump");
		addCustomKeyword(result, "asala.jumpIfFalse", "ASALA_JumpIfFalse");
		addCustomKeyword(result, "asala.pushValue", "ASALA_PushValue");
		addCustomKeyword(result, "asala.return", "ASALA_Return");
		addCustomKeyword(result, "asala.pop", "ASALA_Pop");
		addCustomKeyword(result, "asala.pause", "ASALA_Pause");
		
		addCustomKeyword(result, "expr", "ASALExpr");
		addCustomKeyword(result, "expr.lit", "AExpr_Lit");
		addCustomKeyword(result, "expr.getPort", "AExpr_PortRef");
		addCustomKeyword(result, "expr.getParam", "AExpr_ParamRef");
		addCustomKeyword(result, "expr.op1", "AExpr_Unary");
		addCustomKeyword(result, "expr.op2", "AExpr_Binary");
		addCustomKeyword(result, "expr.fct", "AExpr_FunctionCall");
		addCustomKeyword(result, "stat", "ASALStat");
		addCustomKeyword(result, "stat.empty", "ASALStat_Empty");
		addCustomKeyword(result, "stat.setPort", "AStat_AssignPort");
		addCustomKeyword(result, "stat.setParam", "AStat_AssignParam");
		addCustomKeyword(result, "stat.fct", "AStat_FunctionCall");
		addCustomKeyword(result, "stat.if", "AStat_If");
		addCustomKeyword(result, "stat.while", "AStat_While");
		addCustomKeyword(result, "stat.return", "AStat_Return");
		addCustomKeyword(result, "stat.seq", "AStat_Seq");
		addCustomKeyword(result, "reg.key", "ASALRegEntry");
		addCustomKeyword(result, "reg.exitFuncFlag", "ASALExitFuncFlag");
		addCustomKeyword(result, "reg.pending", "ASALPendingCode");
		addCustomKeyword(result, "reg.top", "ASALStackTop");
		addCustomKeyword(result, "reg.port", "ASALPort");
		addCustomKeyword(result, "reg.param", "ASALParam");
		addCustomKeyword(result, "reg", "ASALReg");
		addCustomKeyword(result, "reg.init", "InitASALReg");
		addCustomKeyword(result, "reg.isDone", "isASALRegDone");
		addCustomKeyword(result, "reg.resume", "resumeASALRegCode");
		addCustomKeyword(result, "applyOp1", "applyUnaryOp");
		addCustomKeyword(result, "applyOp2", "applyBinaryOp");
		addCustomKeyword(result, "applyFct", "applyASALFunc");
		addCustomKeyword(result, "getPreFctReg", "getPreFctReg");
		addCustomKeyword(result, "evalExpr", "evalASALExpr");
		addCustomKeyword(result, "evalStat", "evalASALStat");
		
		addCustomKeyword(result, "port_channel");
		addCustomKeyword(result, "port_channels");
		addCustomKeyword(result, "pc.src.getComp", "port_channel_sender");
		addCustomKeyword(result, "pc.src.getPort", "port_channel_send_port");
		addCustomKeyword(result, "pc.tgt.getComp", "port_channel_receiver");
		addCustomKeyword(result, "pc.tgt.getPort", "port_channel_rcv_port");
		
		addCustomKeyword(result, "VarName");
		addCustomKeyword(result, "FunctionName");
		addCustomKeyword(result, "StateName");
		addCustomKeyword(result, "ComponentName");
		addCustomKeyword(result, "Event");
		
		addCustomKeyword(result, "func_params", "getFunctionParams");
		addCustomKeyword(result, "func_bodies", "getFunctionBodies");
		
		addCustomKeyword(result, "sm_config", "StateConfig");
		
		addCustomKeyword(result, "transition", "Transition");
		addCustomKeyword(result, "transitions");
		
		addCustomKeyword(result, "entry_action");
		addCustomKeyword(result, "exit_action");
		addCustomKeyword(result, "do_actions");
		
		return result;
	}
	
	private void addCustomKeyword(Map<String, String> dest, String value) {
		addCustomKeyword(dest, value, value);
	}
	
	private void addCustomKeyword(Map<String, String> dest, String key, String value) {
		if (dest.containsKey(key)) {
			throw new Error("Duplicate custom keyword entry: " + key);
		}
		
		dest.put("{{{" + key + "}}}", value);
	}
	
	private void addCustomKeyword(Map<String, String> dest, String[] keys, String value) {
		String unusedValue = getUnusedName(value);
		
		for (String key : keys) {
			if (dest.containsKey(key)) {
				throw new Error("Duplicate custom keyword entry: " + key);
			}
			
			dest.put("{{{" + key + "}}}", unusedValue);
		}
	}
	
	private static List<String> toSortedUnmodifiableList(Collection<? extends String> source) {
		List<String> result = new ArrayList<String>(source);
		Collections.sort(result);
		return Collections.unmodifiableList(result);
	}
	
	private static void addVarName(Map<String, Set<JType>> varsPerName, String varName, JType var) {
		Set<JType> vars = varsPerName.get(varName);
		
		if (vars == null) {
			vars = new HashSet<JType>();
			varsPerName.put(varName, vars);
		}
		
		vars.add(var);
	}
	
	private List<String> extractVarNames() {
		Map<String, Set<JType>> varsPerName = new HashMap<String, Set<JType>>();
		
		for (UnifyingBlock.ReprBlock rb : target.getBlocks()) {
			for (UnifyingBlock.ReprProperty rp : rb.getProperties()) {
				//rp.
				addVarName(varsPerName, rp.getName(), rp.getLegacy());
			}
			
			for (UnifyingBlock.ReprPort rp : rb.getPorts()) {
				addVarName(varsPerName, rp.getName(), rp.getLegacy());
			}
			
			if (rb.getTritoStateMachine() != null) {
				for (Entry<String, ASALOp> entry : rb.getOperationPerName().entrySet()) {
					for (ASALParam v : entry.getValue().getParams()) {
						addVarName(varsPerName, v.getName(), v.getLegacy());
					}
				}
			}
		}
		
		Set<String> result = new HashSet<String>();
		
		for (Map.Entry<String, Set<JType>> entry : varsPerName.entrySet()) {
			//Variable names are automatically mCRL2 compatible.
			String name = getUnusedName(entry.getKey());
			result.add(name);
			
			//Blocks can reuse the names of variables of other blocks:
			for (JType v : entry.getValue()) {
				setName(v, name);
			}
		}
		
		return toSortedUnmodifiableList(result);
	}
	
	private List<String> extractFuncNames() {
		Set<String> result = new HashSet<String>();
		
		//Function names need to be unique globally
		//(we use function names to look up the function body in a mapping):
		for (ReprBlock rb : target.getBlocks()) {
			for (Map.Entry<String, ASALOp> entry : rb.getOperationPerName().entrySet()) {
				String name = getUnusedName(rb.getNarrowInstance().getClass().getSimpleName() + "_" + entry.getKey());
				setName(entry.getValue(), name);
				result.add(name);
			}
		}
		
		return toSortedUnmodifiableList(result);
	}
	
	private List<String> extractStateNames() {
		Map<String, Set<TritoVertex>> verticesPerName = new HashMap<String, Set<TritoVertex>>();
		Set<TritoVertex> rootVertices = new HashSet<TritoVertex>();
		
		//State names only need to be unique for each state machine, but
		//nested states cannot have the same name as a composite state:
		for (TritoStateMachine sm : target.reprStateMachines) {
			UnusedNames unusedNames = new UnusedNames();
			
			for (TritoVertex v : sm.vertices) {
				if (StateMachine.class.isAssignableFrom(v.getSysmlClz())) {
					rootVertices.add(v);
				} else {
					String name = unusedNames.generateUnusedName(v.getSysmlClz().getSimpleName());
					Set<TritoVertex> vertices = verticesPerName.get(name);
					
					if (vertices == null) {
						vertices = new HashSet<TritoVertex>();
						verticesPerName.put(name, vertices);
					}
					
					vertices.add(v);
				}
			}
		}
		
		Set<String> result = new HashSet<String>();
		//The set of string should always contain "root", which is the parent state of top level states
		result.add("root");
		
		for (TritoVertex v : rootVertices) {
			setName(v, "root");
		}
		
		for (Map.Entry<String, Set<TritoVertex>> entry : verticesPerName.entrySet()) {
			//State names are automatically mCRL2 compatible:
			String name = getUnusedName(entry.getKey());
			result.add(name);
			
			for (TritoVertex v : entry.getValue()) {
				setName(v, name);
				//@ensures getName(v).equals(name);
			}
		}
		
		return toSortedUnmodifiableList(result);
	}
	
	protected final String typeToStr(Class<?> clz) {
		return "Value";
	}
	
	private Map<Class<? extends JType>, String> extractNamePerType() {
		Map<Class<? extends JType>, String> result = new HashMap<Class<? extends JType>, String>();
		
		for (JTypeLibrary.Type t : target.lib.getTypes()) {
			String typeName = getUnusedName(t.getLegacy().getSimpleName());
			result.put(t.getLegacy(), typeName);
			setName(t, typeName);
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private Map<String, Class<? extends JType>> extractTypePerName() {
		Map<String, Class<? extends JType>> result = new HashMap<String, Class<? extends JType>>();
		
		for (Map.Entry<Class<? extends JType>, String> entry : namePerType.entrySet()) {
			result.put(entry.getValue(), entry.getKey());
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private Map<Class<? extends JType>, String> extractStrPerConstrDecl() {
		Map<Class<? extends JType>, String> result = new HashMap<Class<? extends JType>, String>();
		
		//Types do NOT reuse each others' constructors' names:
		for (JTypeLibrary.Type t : target.lib.getTypes()) {
			for (Map.Entry<String, JTypeLibrary.Constructor> entry : t.getConstructorsPerPreferredName().entrySet()) {
				Class<? extends JType> c = entry.getValue().getLegacy();
				String constructorName = getUnusedName(toFailsafeStr("STR", entry.getKey()));
				result.put(c, constructorName);
				setName(c, constructorName);
				
				for (Field f : entry.getValue().getFieldsPerName().values()) {
					setName(f, getUnusedName(f.getName()));
				}
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private Map<String, Class<? extends JType>> extractConstrDeclPerStr() {
		Map<String, Class<? extends JType>> result = new HashMap<String, Class<? extends JType>>();
		
		for (Map.Entry<Class<? extends JType>, String> entry : namePerConstrDecl.entrySet()) {
			result.put(entry.getValue(), entry.getKey());
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private static String toFailsafeStr(String prefix, String s) {
		final char escapeChar = 'e';
		String result = prefix + "_";
		
		for (int index = 0; index < s.length(); index++) {
			char c = s.charAt(index);
			
			if (Character.isLetter(c) || c == '_') {
				if (s.charAt(index) == escapeChar) {
					result += escapeChar;
					result += escapeChar;
				} else {
					result += c;
				}
			} else {
				result += escapeChar;
				result += String.valueOf((int)c);
			}
		}
		
		return result;
	}
	
	private Map<String, String> extractElemPerEnvName() {
		Map<String, String> result = new HashMap<String, String>();
		
		for (UnifyingBlock.ReprBlock rb : target.getBlocks()) {
			String rbName = getUnusedName(toFailsafeStr("BEQ", rb.getNarrowInstance().getName()));
			String rbEnvName = getUnusedName(toFailsafeStr("ENV", rb.getNarrowInstance().getName()));
			setName(rb, rbName);
			result.put(rbEnvName, rbName);
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private Map<String, String> extractEnvPerElemName() {
		Map<String, String> result = new HashMap<String, String>();
		
		for (Map.Entry<String, String> entry : elemNamePerEnvName.entrySet()) {
			result.put(entry.getValue(), entry.getKey());
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private List<String> extractElemAndEnvNames() {
		Set<String> result = new HashSet<String>();
		result.addAll(elemNamePerEnvName.keySet());
		result.addAll(envNamePerElemName.keySet());
		return toSortedUnmodifiableList(result);
	}
}




