package jlx.behave.proto;

import java.time.LocalTime;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.vars.*;
import jlx.behave.StateMachine;
import jlx.common.reflection.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.CLI;
import jlx.utils.HashMaps;

/**
 * This state machine applies (if enabled) "run-to-maximal-output-changes" semantics.
 */
public class DecaThreeBStateMachine {
	public final JScope scope;
	public final StateMachine instance;
//	public final Set<InputEquivClz> inputEquivClzs;
	public final Set<PulsePackMap> inputs;
	public final Map<DecaThreeOutputRun, DecaThreeBVertex> vertices;
	public final DecaThreeBVertex initialVertex;
	public final PulsePackMap initialInputs;
	public final Set<ASALVariable> propsAndOutputs;
	public final Set<DecaThreeBTransition> transitions;
	public final Map<ASALPort, ASALPort> timeoutPortPerDurationPort;
	public final DecaThreeStateMachine legacy;
	
	public DecaThreeBStateMachine(DecaThreeStateMachine source, boolean enabled) throws ClassReflectionException, ModelException {
		instance = source.instance;
		scope = source.scope;
		legacy = source;
		
		timeoutPortPerDurationPort = new HashMap<ASALPort, ASALPort>(source.timeoutPortPerDurationPort);
		
		DecaThreeOutputRun initialOutputRun = new DecaThreeOutputRun(source.initialVertex, source.initialInputs);
		
		initialInputs = source.initialInputs;
		inputs = new HashSet<PulsePackMap>();
		propsAndOutputs = new HashSet<ASALVariable>(source.propsAndOutputs);
		vertices = new HashMap<DecaThreeOutputRun, DecaThreeBVertex>();
		vertices.put(initialOutputRun, new DecaThreeBVertex(initialOutputRun));
		
		for (InputEquivClz i : source.inputEquivClzs) {
			inputs.addAll(i.getInputVals());
		}
		
		long x = 1L;
		
		for (ASALVariable v : propsAndOutputs) {
			if (v instanceof ReprPort) {
				ReprPort rp = (ReprPort)v;
				x = x * rp.getPossibleValues().size();
			}
		}
		
		System.out.println("x = " + x);
		CLI.waitForEnter();
		
		Set<DecaThreeBVertex> fringe = new HashSet<DecaThreeBVertex>();
		Set<DecaThreeBVertex> newFringe = new HashSet<DecaThreeBVertex>();
		fringe.addAll(vertices.values());
		
		while (fringe.size() > 0 && false) {
			newFringe.clear();
			
			for (DecaThreeBVertex v : fringe) {
				newFringe.addAll(computeOutgoing(v));
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
			
			System.out.println("[" + LocalTime.now() + "] #beenHere = " + vertices.size() + " (+" + fringe.size() + ")");
		}
		
		initialVertex = vertices.get(initialOutputRun);
		transitions = new HashSet<DecaThreeBTransition>();
		
		for (DecaThreeBVertex v : vertices.values()) {
			for (Set<DecaThreeBTransition> trs : v.getOutgoing().values()) {
				transitions.addAll(trs);
			}
		}
	}
	
	private DecaThreeBVertex getVertex(DecaThreeOutputRun v, Set<DecaThreeBVertex> fringe) {
		DecaThreeBVertex result = vertices.get(v);
		
		if (result == null) {
			result = new DecaThreeBVertex(v);
			vertices.put(v, result);
			fringe.add(result);
		}
		
		return result;
	}
	
	private Set<DecaThreeBVertex> computeOutgoing(DecaThreeBVertex src) {
		Set<DecaThreeBVertex> result = new HashSet<DecaThreeBVertex>();
		
		for (Map.Entry<Set<DecaThreeOutputRun>, Set<PulsePackMap>> e : src.getLegacy().getVertex().computeOutputRuns().entrySet()) {
			Set<DecaThreeBVertex> tgts = new HashSet<DecaThreeBVertex>();
			
			for (DecaThreeOutputRun tgt : e.getKey()) {
				tgts.add(getVertex(tgt, result));
			}
			
			DecaThreeBTransition t = new DecaThreeBTransition(src, tgts, e.getValue());
			
			for (PulsePackMap i : t.getInputs()) {
				HashMaps.inject(src.getOutgoing(), i, t);
			}
		}
		
		return result;
	}
}

