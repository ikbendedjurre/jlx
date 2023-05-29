package jlx.printing;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.api.*;
import jlx.asal.vars.ASALVariable;
import jlx.behave.*;
import jlx.behave.proto.*;
import jlx.common.ReflectionUtils;
import jlx.models.*;
import jlx.models.UnifyingBlock.*;
import jlx.utils.Dir;

public abstract class AbstractMCRL2ModelPrinter extends AbstractMCRL2Printer implements IMCRL2Printer {
	public final static String JTYPE = "Value";
	public final PrintingOptions options;
	public Set<ReprPort> dataParameterPorts;
	public Map<ReprPort, List<ReprPort>> dataParameterPortsPerPulsePort;
	
	public class ASALExprNames {
		public final ReprBlock context;
		public final Set<ASALExpr> exprs;
		
		public final String id;
		public final String description;
		
		private ASALExprNames(ReprBlock context, Set<ASALExpr> exprs, String id, String description) {
			this.context = context;
			this.exprs = Collections.unmodifiableSet(exprs);
			this.id = id;
			this.description = description;
		}
	}
	
	public class ASALStatementNames {
		public final ReprBlock context;
		public final ASALStatement statement;
		
		public final String id;
		
		private ASALStatementNames(ReprBlock context, ASALStatement statement) {
			this.context = context;
			this.statement = statement;
			
			id = getUnusedName("StatementId");
		}
	}
	
	public final Map<ASALExpr, ASALExprNames> namesPerExpr;
	public final Map<ASALStatement, ASALStatementNames> namesPerStat;
	public final Set<ASALVariable> unreferencedVars;
	
	public AbstractMCRL2ModelPrinter(UnifyingBlock target, PrintingOptions options) {
		super(target);
		
		this.options = options;
		
		namesPerExpr = extractNamesPerExpr();
		namesPerStat = extractNamesPerStat();
		unreferencedVars = extractUnreferencedVars();
		dataParameterPorts = extractDataParameterPorts();
		dataParameterPortsPerPulsePort = exctractDataParameterPortsPerPulsePort();
	}
	
	private Map<ReprPort, List<ReprPort>> exctractDataParameterPortsPerPulsePort() {
		Map<ReprPort, List<ReprPort>> result = new HashMap<ReprPort, List<ReprPort>>();
		Set<ReprPort> seenPorts = new HashSet<ReprPort>();
		for (ReprBlock rb : target.getBlocks()) {
			for (ReprPort port : rb.getPorts()) {
				if (!seenPorts.contains(port) && port.getType().equals(JPulse.class)) {
					List<ReprPort> paramsOfPulsePort = new ArrayList<ReprPort>();
					for (ReprPort dt_port : port.getDataPorts()) {
						paramsOfPulsePort.add(dt_port);
					}
					result.put(port, paramsOfPulsePort);
				}
			}
		}
		return result;
	}
	
	private Set<ReprPort> extractDataParameterPorts() {
		Set<ReprPort> result = new HashSet<ReprPort>();
		Set<ReprPort> seenPorts = new HashSet<ReprPort>();
		for (ReprBlock rb : target.getBlocks()) {
			for (ReprPort port : rb.getPorts()) {
				if (!seenPorts.contains(port) && port.getType().equals(JPulse.class)) {
					for (ReprPort dt_port : port.getDataPorts()) {
						result.add(dt_port);
					}
				}
			}
		}
		return result;
	}
	
	private static <K, V> void addToMapOfSets(Map<K, Set<V>> dest, K key, V addedValue) {
		Set<V> values = dest.get(key);
		
		if (values == null) {
			values = new HashSet<V>();
			dest.put(key, values);
		}
		
		values.add(addedValue);
	}
	
	private Map<ASALExpr, ASALExprNames> extractNamesPerExpr() {
		Map<ASALExpr, ASALExprNames> result = new HashMap<ASALExpr, ASALExprNames>();
		
		for (ReprBlock rb : target.getBlocks()) {
			Map<String, Set<ASALExpr>> exprsPerString = new HashMap<String, Set<ASALExpr>>();
			
			for (TritoTransition t : rb.getTritoStateMachine().getAllTransitions()) {
				if (t.getGuard() != null) {
					addToMapOfSets(exprsPerString, exprEqnToStr(rb, t.getGuard(), "valuation"), t.getGuard());
				}
				
				if (t.getEvent() instanceof ASALTrigger) {
					ASALTrigger x = (ASALTrigger) t.getEvent();
					addToMapOfSets(exprsPerString, exprEqnToStr(rb, x.getExpr(), "valuation"), x.getExpr());
				}
			}
			
			for (Map.Entry<String, Set<ASALExpr>> entry : exprsPerString.entrySet()) {
				ASALExprNames names = new ASALExprNames(rb, entry.getValue(), getUnusedName("ExprId"), entry.getKey());
				
				for (ASALExpr expr : entry.getValue()) {
					result.put(expr, names);
				}
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private Map<ASALStatement, ASALStatementNames> extractNamesPerStat() {
		Map<ASALStatement, ASALStatementNames> result = new HashMap<ASALStatement, ASALStatementNames>();
		
		for (ReprBlock rb : target.getBlocks()) {
			if (rb.getTritoStateMachine() != null) {
				for (TritoTransition t : rb.getTritoStateMachine().getAllTransitions()) {
					if (t.getStatement() != null) {
						result.put(t.getStatement(), new ASALStatementNames(rb, t.getStatement()));
					}
				}
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private Set<ASALVariable> extractUnreferencedVars() {
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		
		for (ReprBlock rb : target.getBlocks()) {
			if (rb.getTritoStateMachine() != null) {
				for (ASALVariable unreferencedVar : ASALUnreferencedVars.getUnreferencedVars(rb.getTritoStateMachine())) {
					result.add(rb.getVarPerJType().get(unreferencedVar.getLegacy()));
				}
			}
		}
		
		return Collections.unmodifiableSet(result);
	}
	
	protected static String transformTomCRL2List(List<String> list) {
		return "[" + concat(list, ", ") + "]";
	}
	
	protected void printHeader(String header, String... optionalLines) {
		println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		println("%% " + header);
		
		for (String optionalLine : optionalLines) {
			println("%% " + optionalLine);
		}
		
		println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	}
	
	protected void printStdStruct(String name, Collection<?> items) {
		if (items.size() > 0) {
			Iterator<?> q = items.iterator();
			
			println("sort");
			println("\t" + name + " = struct " + q.next());
			
			while (q.hasNext()) {
				println("\t\t| " + q.next());
			}
			
			println("\t;");
		} else {
			println("sort");
			println("\t" + name + " = struct " + getUnusedName("dummy") + ";");
		}
	}
	
	protected void printStdMapping(String name, String keyType, String itemType, Map<? extends Object, ? extends Object> items) {
		if (items.size() > 0) {
			println("map");
			println("\t" + name + ": " + keyType + " -> " + itemType + ";");
			println("eqn");
			
			for (Map.Entry<?, ?> entry : items.entrySet()) {
				println("\t" + name + "(" + String.valueOf(entry.getKey()) + ") = " + String.valueOf(entry.getValue()) + ";");
			}
		} else {
			println("map " + name + ": " + keyType + " -> " + itemType + ";");
		}
	}
	
	private static String instructionsToStr(List<String> instructions) {
		if (instructions.size() > 0) {
			String result = instructions.get(0);
			
			for (int index = 1; index < instructions.size(); index++) {
				result += ", " + instructions.get(index);
			}
			
			return "[" + result + "]";
		}
		
		return "[]";
	}
	
	protected String exprToStr(ASALExpr expr, String fallback) {
		if (expr == null) {
			return fallback;
		}
		
		return instructionsToStr(new ASALA2mCRL2Visitor(this).visitExpr(expr));
	}
	
	protected String statToStr(TritoTransition t) {
		if (t != null) {
			return statToStr(t.getStatement());
		}
		
		return "[]";
	}
	
	protected String statToStr(ASALStatement stat) {
		if (stat == null) {
			return "";
		}
		
		return instructionsToStr(new ASALA2mCRL2Visitor(this).visitStat(stat));
	}
	
	private String eventToStr(ASALEvent event, TritoStateMachine sm) {
		if (event == null) {
			return "none";
		}
		
		if (event instanceof ASALTimeout) {
			return "TimeoutEvent";
		} else if (event instanceof ASALCall) {
			return "CallEvent(" + ((ASALCall) event).getMethodName() + ")";
		} else if (event instanceof ASALTrigger) {
			ASALExpr expr = ((ASALTrigger) event).getExpr();
			return "ChangeEvent(" + exprToStr(expr, "") + ")";
		} else {
			return "UnknownEventType";
		}
	}
	
	private String fieldToStr(Field f) {
		String getterName = getName(f);
		
		if (ReflectionUtils.isPrimitive(f)) {
			if (f.getType().equals(boolean.class)) {
				return getterName + ": Bool";
			}
			
			if (f.getType().equals(int.class)) {
				return getterName + ": Int";
			}
			
			throw new Error("Should not happen!");
		}
		
		return getterName + ": " + JTYPE;
	}
	
	private String generateListFromTemplate(String template, String seperator, int start, int end) {
		String result = "";
		for (int i = start; i <= end; i++) {
			if (i > start) {
				result += seperator;
			}
			result += String.format(template, i);
		}
		return result;
	}
	
	protected void printTypes() {
		SortedMap<String, String> constructors = new TreeMap<String, String>();
		constructors.put("DUMMY_CUSTOM_VALUE", " % (In case there are no custom values)");
		
		for (JTypeLibrary.Type t : target.lib.getTypes()) {
			//Standard data types are defined in static.mcrl2
			if (t.getLegacy() != JVoid.class && t.getLegacy() != JPulse.class && t.getLegacy() != JBool.class && t.getLegacy() != JInt.class) {
				//System.out.println(t.legacy.getSimpleName());
				for (JTypeLibrary.Constructor c : t.getConstructorsPerPreferredName().values()) {
					
					List<String> params = new ArrayList<String>();
					
					for (Field f : c.getFieldsPerName().values()) {
						params.add(fieldToStr(f));
					}
					
					String s = namePerConstrDecl.get(c.getLegacy());
					
					if (params.size() > 0) {
						s += "(" + concat(params, ", ") + ")";
					}
					
					constructors.put(s, " % " + t.getLegacy().getCanonicalName());
				}
			}
		}
		
		println("sort");
		println__("Custom_" + JTYPE + " = struct");
		printlines(2, " |", constructors);
		println__(";");
	}
	
	protected void printModel() {
		printHeader("Struct containing the data types:");
		printTypes();
		
		printHeader("Struct containing the names of all components/statemachines:");
		printStdStruct("CompName", elemAndEnvNames);
		
		printHeader("Struct containing all port/variable names:");
		printStdStruct("VarName", varNames);
		
		//		List<String> stringConstructors = new ArrayList<String>();
		//
		//		for (JTypeLibrary.Type type : target.lib.getTypePerDecl().values()) {
		//			for (JTypeLibrary.Constructor c : type.constructorsPerName.values()) {
		//				String constructorName = getName(c.getClass());
		//				List<String> constructorFields = new ArrayList<String>();
		//
		//				for (Map.Entry<String, Field> entry : c.fieldsPerName.entrySet()) {
		//					constructorFields.add(getName(entry.getValue()) + ": " + typeToStr(entry.getValue().getType()));
		//				}
		//
		//				if (constructorFields.isEmpty()) {
		//					stringConstructors.add(constructorName);
		//				} else {
		//					stringConstructors.add(constructorName + "(" + concat(constructorFields, ", ") + ")");
		//				}
		//			}
		//		}
		//
		//		printHeader("Struct containing all concrete strings:");
		//		printStdStruct("String", stringConstructors);
		
		printHeader("Struct containing all state names. The names are prefixed with the name of the statemachine (TODO is this required? or can we optimize?):");
		printStdStruct("StateName", stateNames); //This includes "root".
		
		printHeader("Functions:");
		println("% All function names:");
		printStdStruct("FunctionName", funcNames);
		
		Map<String, String> paramsPerFunc = new HashMap<String, String>();
		Map<String, String> bodyPerFunc = new HashMap<String, String>();
		
		for (ReprBlock rb : target.getBlocks()) {
			for (Map.Entry<String, ASALOp> entry : rb.getOperationPerName().entrySet()) {
				String fctName = getName(entry.getValue());
				String paramsStr;
				
				if (entry.getValue().getParams().size() > 0) {
					paramsStr = entry.getValue().getParams().get(0).getName();
					
					for (int index = 1; index < entry.getValue().getParams().size(); index++) {
						paramsStr += ", " + entry.getValue().getParams().get(index).getName();
					}
				} else {
					paramsStr = "";
				}
				
				paramsPerFunc.put(fctName, "[" + paramsStr + "]");
				
				//After preprocessing ASAL, all operations have been injected:
				bodyPerFunc.put(fctName, statToStr(entry.getValue().getBody()));
			}
		}
		
		printStdMapping("getFunctionParams", "FunctionName", "List(VarName)", paramsPerFunc);
		printStdMapping("getFunctionBodies", "FunctionName", "Instructions", bodyPerFunc);
		
		Map<String, String> transitionsPerComp = new HashMap<String, String>();
		Map<String, String> inPortNamesPerComp = new HashMap<String, String>();
		Map<String, String> outPortNamesPerComp = new HashMap<String, String>();
		Map<String, String> pulsePortNamesPerComp = new HashMap<String, String>();
		Map<String, String> dataParamPortNamesPerComp = new HashMap<String, String>();
		Map<String, String> paramsPerPulsePort = new HashMap<String, String>();
		Map<String, String> synchronousPortNamesPerComp = new HashMap<String, String>();
		Map<String, String> functionNamesPerComp = new HashMap<String, String>();
		Map<String, String> statesPerComp = new HashMap<String, String>();
		Map<String, String> stateInfo = new HashMap<String, String>();
		
		for (ReprBlock rb : target.getBlocks()) {
			String component = getName(rb);
			List<String> transitions = new ArrayList<String>();
			List<String> inPortNames = new ArrayList<String>();
			List<String> outPortNames = new ArrayList<String>();
			List<String> pulsePortNames = new ArrayList<String>();
			List<String> dataParamPortNames = new ArrayList<String>();
			List<String> synchronousPortNames = new ArrayList<String>();
			List<String> functionNames = new ArrayList<String>();
			List<String> states = new ArrayList<String>();
			
			if (rb.getTritoStateMachine() != null) {
				TritoStateMachine sm = rb.getTritoStateMachine();
				
				for (Map.Entry<String, ASALOp> entry : rb.getOperationPerName().entrySet()) {
					String fctName = getName(entry.getValue());
					functionNames.add(fctName);
				}
				
				for (TritoVertex v : sm.vertices) {
					if (StateMachine.class.isAssignableFrom(v.getSysmlClz())) {
						String key = component + ")(root";
						stateInfo.put(key, "RootVertex");
						//(root vertices do not have transitions)
					} else {
						states.add(getName(v));
						String key = component + ")(" + getName(v);
						String parent = getName(v.getParentVertex());
						if (CompositeState.class.isAssignableFrom(v.getSysmlClz()) || ReferenceState.class.isAssignableFrom(v.getSysmlClz())) {
							stateInfo.put(key, "CompositeState(" + parent + "," + statToStr(v.getOnEntry()) + "," + statToStr(v.getOnExit()) + ")");
						}
						if (State.class.isAssignableFrom(v.getSysmlClz()) && !CompositeState.class.isAssignableFrom(v.getSysmlClz()) && !ReferenceState.class.isAssignableFrom(v.getSysmlClz())) {
							stateInfo.put(key, "SimpleState(" + parent + "," + statToStr(v.getOnEntry()) + "," + statToStr(v.getOnExit()) + ")");
						}
						if (InitialState.class.isAssignableFrom(v.getSysmlClz())) {
							stateInfo.put(key, "InitialState(" + parent + ")");
						}
						if (ForkVertex.class.isAssignableFrom(v.getSysmlClz())) {
							stateInfo.put(key, "ForkVertex(" + parent + ")");
						}
						if (JoinVertex.class.isAssignableFrom(v.getSysmlClz())) {
							stateInfo.put(key, "JoinVertex(" + parent + ")");
						}
						if (JunctionVertex.class.isAssignableFrom(v.getSysmlClz())) {
							stateInfo.put(key, "JunctionVertex(" + parent + ")");
						}
						if (FinalState.class.isAssignableFrom(v.getSysmlClz())) {
							stateInfo.put(key, "FinalState(" + parent + ")");
						}
						if (ChoiceVertex.class.isAssignableFrom(v.getSysmlClz())) {
							stateInfo.put(key, "ChoiceVertex(" + parent + ")");
						}
						if (State.class.isAssignableFrom(v.getSysmlClz())) {
							for (TritoTransition t : v.getOnDo()) {
								String x = "\n\t\t\t\t" + getName(v) + ", % source internal";
								x += "\n\t\t\t\t" + eventToStr(t.getEvent(), sm) + ", % trigger";
								x += "\n\t\t\t\t" + exprToStr(t.getGuard(), "[ASALA_DefaultGuard]") + ", % guard";
								x += "\n\t\t\t\t" + statToStr(t.getStatement()) + ", % effect";
								x += "\n\t\t\t\t" + getName(v) + ", % target";
								x += "\n\t\t\t\ttrue % internal";
								transitions.add("Transition(" + x + "\n\t\t\t)");
							}
						}
					}
				}
				
				for (TritoTransition t : sm.transitions) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					t.printDebugText(new PrintStream(baos), "\t\t\t% ");
					String x = "\n\t\t\t\t" + getName(t.getSourceVertex()) + ", % source";
					x += "\n\t\t\t\t" + eventToStr(t.getEvent(), sm) + ", % trigger";
					x += "\n\t\t\t\t" + exprToStr(t.getGuard(), "[ASALA_DefaultGuard]") + ", % guard";
					x += "\n\t\t\t\t" + statToStr(t.getStatement()) + ", % effect";
					x += "\n\t\t\t\t" + getName(t.getTargetVertex()) + ", % target";
					x += "\n\t\t\t\tfalse % internal";
					transitions.add("\n" + baos.toString() + "\t\t\tTransition(" + x + "\n\t\t\t)");
				}
			}
			
			statesPerComp.put(getName(rb), transformTomCRL2List(states));
			transitionsPerComp.put(getName(rb), transformTomCRL2List(transitions));
			
			for (ReprPort port : rb.getPorts()) {
				if (port.getDir() == Dir.IN) {
					if (!inPortNames.contains(getName(port.getLegacy()))) {
						inPortNames.add(getName(port.getLegacy()));
					}
				}
				if (port.getDir() == Dir.OUT) {
					if (!outPortNames.contains(getName(port.getLegacy()))) {
						outPortNames.add(getName(port.getLegacy()));
					}
				}
				if (!pulsePortNames.contains(getName(port.getLegacy())) && port.getType().equals(JPulse.class)) {
					pulsePortNames.add(getName(port.getLegacy()));
					List<String> paramNamesOfPulsePort = new ArrayList<String>();
					List<ReprPort> paramsOfPulsePort = new ArrayList<ReprPort>();
					for (ReprPort dt_port : port.getDataPorts()) {
						dataParamPortNames.add(getName(dt_port.getLegacy()));
						paramNamesOfPulsePort.add(getName(dt_port.getLegacy()));
						paramsOfPulsePort.add(dt_port);
					}
					String key = component + ")(" + getName(port.getLegacy());
					paramsPerPulsePort.put(key, transformTomCRL2List(paramNamesOfPulsePort));
				}
				if (!synchronousPortNames.contains(getName(port.getLegacy())) && port.isSynchronous()) {
					synchronousPortNames.add(getName(port.getLegacy()));
				}
			}
			inPortNamesPerComp.put(component, transformTomCRL2List(inPortNames));
			outPortNamesPerComp.put(component, transformTomCRL2List(outPortNames));
			pulsePortNamesPerComp.put(component, transformTomCRL2List(pulsePortNames));
			dataParamPortNamesPerComp.put(component, transformTomCRL2List(dataParamPortNames));
			synchronousPortNamesPerComp.put(component, transformTomCRL2List(synchronousPortNames));
			functionNamesPerComp.put(component, transformTomCRL2List(functionNames));
		}
		
		println("% The following mappings are never updated:");
		printStdMapping("transitions", "CompName", "List(Transition)", transitionsPerComp);
		printStdMapping("inPorts", "CompName", "List(VarName)", inPortNamesPerComp);
		printStdMapping("outPorts", "CompName", "List(VarName)", outPortNamesPerComp);
		printStdMapping("pulsePorts", "CompName", "List(VarName)", pulsePortNamesPerComp);
		printStdMapping("dataParamPorts", "CompName", "List(VarName)", dataParamPortNamesPerComp);
		printStdMapping("paramsPerPulsePort", "CompName -> VarName", "List(VarName)", paramsPerPulsePort);
		printStdMapping("synchronousPorts", "CompName", "List(VarName)", synchronousPortNamesPerComp);
		printStdMapping("functions", "CompName", "List(FunctionName)", functionNamesPerComp);
		printStdMapping("states", "CompName", "List(StateName)", statesPerComp);
		printStdMapping("stateInfo", "CompName -> StateName", "StateInfo", stateInfo);
		
		class VarValuePair {
			String Variable;
			String Value;
			
			VarValuePair(String var, String val) {
				this.Variable = var;
				this.Value = val;
			}
		}
		
		Map<String, String> initialStateConfigPerComp = new HashMap<String, String>();
		Map<String, List<VarValuePair>> varNamesAndInitialPerComp = new HashMap<String, List<VarValuePair>>();
		int maxNumVars = 0;
		
		for (ReprBlock rb : target.getBlocks()) {
			TritoStateMachine sm = rb.getTritoStateMachine();
			
			if (sm != null) {
				List<VarValuePair> varsWithInitial = new ArrayList<VarValuePair>();
				List<String> initialVertices = new ArrayList<String>();
				
				for (TritoVertex v : sm.rootVertex.getInitialVertices()) {
					initialVertices.add("StateConfig(" + getName(v) + ", [])");
				}
				
				String initialStateConfig = "StateConfig(" + getName(sm.rootVertex) + ", " + transformTomCRL2List(initialVertices) + ")";
				initialStateConfigPerComp.put(getName(rb), initialStateConfig);
				
				for (ReprProperty rp : rb.getProperties()) {
					varsWithInitial.add(new VarValuePair(getName(rp.getLegacy()), literalToStr(rp.getInitialValue())));
				}
				
				for (ReprPort rp : rb.getPorts()) {
					varsWithInitial.add(new VarValuePair(getName(rp.getLegacy()), literalToStr(rp.getInitialValue())));
				}
				
				if (varsWithInitial.size() > maxNumVars) {
					maxNumVars = varsWithInitial.size();
				}
				varNamesAndInitialPerComp.put(getName(rb), varsWithInitial);
			}
		}
		
		println("% The following mappings is never updated:");
		printStdMapping("initialStateConfig", "CompName", "StateConfig", initialStateConfigPerComp);
		
		Map<String, String> SMDefPerComp = new HashMap<String, String>();
		Map<String, String> ComponentDefPerComp = new HashMap<String, String>();
		for (ReprBlock block : target.getBlocks()) {
			String b = getName(block);
			SMDefPerComp.put(b, String.format("StateMachine(transitions(%s),initialStateConfig(%s),states(%s),stateInfo(%s),initialValuation(%s))", b, b, b, b, b));
			ComponentDefPerComp.put(b, String.format("Component(%s,SMDefs(%s),inPorts(%s),outPorts(%s),pulsePorts(%s),dataParamPorts(%s),paramsPerPulsePort(%s),synchronousPorts(%s),functions(%s))", b, b, b, b, b, b, b, b, b));
		}
		printStdMapping("SMDefs", "CompName", "StateMachine", SMDefPerComp);
		printStdMapping("compDefs", "CompName", "Component", ComponentDefPerComp);
		
		printlines("sort", "	ValueStorage = struct");
		boolean first = true;
		for (ReprBlock block : target.getBlocks()) {
			if (block.getTritoStateMachine() != null) {
				List<VarValuePair> varsWithInitial = varNamesAndInitialPerComp.get(getName(block));
				if (first) {
					println__("ValueStorage_" + getName(block) + "(");
					first = false;
				} else {
					println__("| ValueStorage_" + getName(block) + "(");
				}
				first = true;
				for (VarValuePair vvp : varsWithInitial) {
					if (first) {
						println____(getName(block) + "_" + vvp.Variable + ": Value");
						first = false;
					} else {
						println____("," + getName(block) + "_" + vvp.Variable + ": Value");
					}
					
				}
				println__(")" + "?is_" + getName(block) + "_VS");
			}
		}
		println__(";");
		
		printlines("map", "	getValue: ValueStorage#VarName -> Value;", "	setValue: ValueStorage#VarName#Value -> ValueStorage;", "var", "	v: Value;");
		println__(generateListFromTemplate("v%s", ",", 1, maxNumVars) + ":Value;");
		println("eqn");
		for (ReprBlock block : target.getBlocks()) {
			if (block.getTritoStateMachine() != null) {
				List<VarValuePair> varsWithInitial = varNamesAndInitialPerComp.get(getName(block));
				int numVars = varsWithInitial.size();
				int varIndex = 1;
				for (VarValuePair vvp : varsWithInitial) {
					println__("getValue(ValueStorage_" + getName(block) + "(" + generateListFromTemplate("v%s", ",", 1, numVars) + ")," + vvp.Variable + ") = v" + varIndex + ";");
					println__("setValue(ValueStorage_" + getName(block) + "(" + generateListFromTemplate("v%s", ",", 1, numVars) + ")," + vvp.Variable + ",v)");
					String newValue = "v";
					if (varIndex != numVars) {
						newValue = newValue + ",";
					}
					if (varIndex != 1) {
						newValue = "," + newValue;
					}
					println____("= ValueStorage_" + getName(block) + "(" + generateListFromTemplate("v%s", ",", 1, varIndex - 1) + newValue + generateListFromTemplate("v%s", ",", varIndex + 1, numVars) + ");");
					varIndex++;
				}
			}
		}
		
		printlines("% The following mapping is never updated:", "map", "	initialValuation: CompName -> ValueStorage;", "eqn");
		for (ReprBlock block : target.getBlocks()) {
			if (block.getTritoStateMachine() != null) {
				List<VarValuePair> varsWithInitial = varNamesAndInitialPerComp.get(getName(block));
				println__("initialValuation(" + getName(block) + ") = ValueStorage_" + getName(block) + "(");
				first = true;
				for (VarValuePair vvp : varsWithInitial) {
					if (first) {
						println____(vvp.Value + " % " + vvp.Variable);
						first = false;
					} else {
						println____(", " + vvp.Value + " % " + vvp.Variable);
					}
				}
				println__(");");
			}
		}
		
		printHeader("Mapping defining order of states, used to normalise state configurations:");
		Map<String, String> indexPerStateName = new HashMap<String, String>();
		int i = 0;
		for (String s : stateNames) {
			indexPerStateName.put(s, String.valueOf(i));
			i++;
		}
		printStdMapping("stateIndex", "StateName", "Nat", indexPerStateName);
	}
	
	private static String _eq(String lhs, String rhs) {
		if (lhs.equals(rhs)) {
			return "true";
		}
		
		return "(" + lhs + " == " + rhs + ")";
	}
	
	@Override
	public String jintToInt(String s) {
		JTypeLibrary.Constructor c = target.lib.getConstructor(JInt.LITERAL.class);
		Field f = c.getFieldsPerName().values().iterator().next();
		return getName(f) + "(" + s + ")";
	}
	
	@Override
	public String intToJInt(String s) {
		return constrToStr(JInt.LITERAL.class) + "(" + s + ")";
	}
	
	@Override
	public String boolToJBool(String s) {
		return _if(s, constrToStr(JBool.TRUE.class), constrToStr(JBool.FALSE.class));
	}
	
	@Override
	public String boolToJPulse(String s) {
		return _if(s, constrToStr(JPulse.TRUE.class), constrToStr(JPulse.FALSE.class));
	}
	
	@Override
	public String jboolToBool(String s) {
		return _eq(s, constrToStr(JBool.TRUE.class));
	}
	
	@Override
	public String jboolToPulse(String s) {
		return _if(s + " == " + constrToStr(JBool.TRUE.class), constrToStr(JPulse.TRUE.class), constrToStr(JPulse.FALSE.class));
	}
	
	@Override
	public String jpulseToJBool(String s) {
		return boolToJBool(jpulseToBool(s));
	}
	
	@Override
	public String jpulseToBool(String s) {
		return _eq(s, constrToStr(JPulse.TRUE.class));
	}
	
	private String constrToStr(Class<? extends JType> constrDecl) {
		if (constrDecl.equals(JBool.FALSE.class)) {
			return "Value_Bool(false)";
		}
		
		if (constrDecl.equals(JBool.TRUE.class)) {
			return "Value_Bool(true)";
		}
		
		if (constrDecl.equals(JPulse.FALSE.class)) {
			return "Value_Bool(false)";
		}
		
		if (constrDecl.equals(JPulse.TRUE.class)) {
			return "Value_Bool(true)";
		}
		
		return "Value_Custom(" + namePerConstrDecl.get(constrDecl) + ")";
	}
	
	protected String literalToStr(JType lit) {
		if (lit.getClass().equals(JInt.LITERAL.class)) {
			JInt.LITERAL i = (JInt.LITERAL) lit;
			return "Value_Int(" + i.value + ")";
		}
		return constrToStr(lit.getClass());
	}
	
	private static boolean isFalse(ASALLiteral leaf) {
		return leaf.getResolvedConstructor().equals(JBool.FALSE.class) || leaf.getResolvedConstructor().equals(JPulse.FALSE.class);
	}
	
	private static boolean isTrue(ASALLiteral leaf) {
		return leaf.getResolvedConstructor().equals(JBool.TRUE.class) || leaf.getResolvedConstructor().equals(JPulse.TRUE.class);
	}
	
	@Override
	public String literalToStr(ASALLiteral leaf, FlatTarget target) {
		switch (target) {
			case JTYPE:
				if (leaf.getResolvedConstructor().equals(JBool.FALSE.class)) {
					return constrToStr(JBool.FALSE.class);
				}
				
				if (leaf.getResolvedConstructor().equals(JBool.TRUE.class)) {
					return constrToStr(JBool.TRUE.class);
				}
				
				if (leaf.getResolvedConstructor().equals(JPulse.FALSE.class)) {
					return constrToStr(JPulse.FALSE.class);
				}
				
				if (leaf.getResolvedConstructor().equals(JPulse.TRUE.class)) {
					return constrToStr(JPulse.TRUE.class);
				}
				
				if (leaf.getResolvedConstructor().equals(JInt.LITERAL.class)) {
					return "Value_Int(" + leaf.getText() + ")";
				}
				
				//TODO Note that we do not support types with fields yet (other than JInt)!!
				//				if (leaf.getResolvedConstructor().fieldsPerName.size() > 0) {
				//					throw new Error("Not supported!");
				//				}
				
				return constrToStr(leaf.getResolvedConstructor());
			case JBOOL:
				if (isFalse(leaf)) {
					return constrToStr(JBool.FALSE.class);
				}
				
				if (isTrue(leaf)) {
					return constrToStr(JBool.TRUE.class);
				}
				
				throw new Error("Should not happen!");
			case JPULSE:
				if (isFalse(leaf)) {
					return constrToStr(JPulse.FALSE.class);
				}
				
				if (isTrue(leaf)) {
					return constrToStr(JPulse.TRUE.class);
				}
				
				throw new Error("Should not happen!");
			case RAW_MCRL2:
				if (isFalse(leaf)) {
					return "false";
				}
				
				if (isTrue(leaf)) {
					return "true";
				}
				
				if (leaf.getResolvedConstructor().equals(JInt.LITERAL.class)) {
					return leaf.getText();
				}
				
				throw new Error("Should not happen!");
		}
		
		throw new Error("Should not happen!");
	}
	
	@Override
	public String _if(String condition, String thenBranch, String elseBranch) {
		if (condition.equals("true")) {
			return thenBranch;
		}
		
		if (condition.equals("false")) {
			return elseBranch;
		}
		
		if (thenBranch.equals(elseBranch)) {
			return thenBranch;
		}
		
		return "if(" + condition + ", " + thenBranch + ", " + elseBranch + ")";
	}
	
	private String exprEqnToStr(ReprBlock reprBlock, ASALExpr expr, String valuation) {
		ASAL2mCRL2Flattener f = new ASAL2mCRL2Flattener(this);
		ExprFlat oldMap = new ExprFlat();
		
		//State variables are extracted from the state:
		for (JType ref : reprBlock.getVarPerJType().keySet()) {
			oldMap.put(ref, valuation + "(" + getName(ref) + ")");
		}
		
		ExprFlat newMap = f.applyExpr(oldMap, expr, FlatTarget.JBOOL); //(we expect a JBool)
		
		//We constructed an expression, which should not have side effects:
		for (JType v : oldMap.getVars()) {
			if (!newMap.get(v).equals(oldMap.get(v))) {
				throw new Error("Expression may not have side effects!");
			}
		}
		
		return newMap.constructedJExpr;
	}
	
//	private String statEqnToStr(ReprBlock reprBlock, ASALStatement statement, String valuation) {
//		ASAL2mCRL2Flattener f = new ASAL2mCRL2Flattener(this);
//		StatFlat oldMap = new StatFlat();
//		
//		//State variables are extracted from the state:
//		for (JType ref : reprBlock.getVarPerJType().keySet()) {
//			oldMap.put(ref, valuation + "(" + getName(ref) + ")");
//		}
//		
//		StatFlat newMap = f.applyStat(oldMap, statement);
//		String result = valuation;
//		
//		for (Map.Entry<JType, ASALVariable> e : reprBlock.getVarPerJType().entrySet()) {
//			if (!newMap.get(e.getKey()).equals(oldMap.get(e.getKey()))) {
//				if (e.getValue().getType().equals(JPulse.class)) {
//					//Do nothing.
//				} else {
//					//TODO autocasting is not necessary here, right?
//					String autocastedNewValue = "autocastToVarType(" + getName(reprBlock) + ", " + getName(e.getKey()) + ", " + newMap.get(e.getKey()) + ")";
//					result = result + "[" + getName(e.getKey()) + " -> " + autocastedNewValue + "]";
//				}
//			}
//		}
//		
//		return result;
//	}
//	
//	private String statVarUpdatesToStr(ReprBlock reprBlock, ASALStatement statement, String valuation) {
//		ASAL2mCRL2Flattener f = new ASAL2mCRL2Flattener(this);
//		StatFlat oldMap = new StatFlat();
//		
//		//State variables are extracted from the state:
//		for (JType ref : reprBlock.getVarPerJType().keySet()) {
//			oldMap.put(ref, valuation + "(" + getName(ref) + ")");
//		}
//		
//		StatFlat newMap = f.applyStat(oldMap, statement);
//		List<String> items = new ArrayList<String>();
//		
//		for (JType ref : reprBlock.getVarPerJType().keySet()) {
//			if (!newMap.get(ref).equals(oldMap.get(ref))) {
//				//TODO autocasting is not necessary here, right?
//				String autocastedNewValue = "autocastToVarType(" + getName(reprBlock) + ", " + getName(ref) + ", " + newMap.get(ref) + ")";
//				items.add("VarValuePair(" + getName(ref) + ", " + autocastedNewValue + ")");
//			}
//		}
//		
//		return "[" + concat(items, ", ") + "]";
//	}
}
