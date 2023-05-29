package jlx.behave.stable.files;

import java.util.*;

import jlx.utils.*;

public class DecaStableFile {
	public static class Scope {
		private final int id;
		private final String name;
		
		public Scope(int id, String name) {
			this.id = id;
			this.name = name;
		}
		
		public int getId() {
			return id;
		}
		
		public String getName() {
			return name;
		}
	}
	
	public static class Port {
		private final int id;
		private final String name;
		private final Scope owner;
		private final String typeName;
		private final int portIndex;
		private final String adapterLabel;
		private final boolean isPortToEnvironment;
		private final boolean isTimeoutPort;
		private final int executionTime;
		
		public Port(int id, String name, Scope owner, String typeName, int portIndex, String adapterLabel, boolean isPortToEnvironment, boolean isTimeoutPort, int executionTime) {
			this.id = id;
			this.name = name;
			this.owner = owner;
			this.typeName = typeName;
			this.portIndex = portIndex;
			this.adapterLabel = adapterLabel;
			this.isPortToEnvironment = isPortToEnvironment;
			this.isTimeoutPort = isTimeoutPort;
			this.executionTime = executionTime;
		}
		
		public int getId() {
			return id;
		}
		
		public String getName() {
			return name;
		}
		
		public Scope getOwner() {
			return owner;
		}
		
		public String getTypeName() {
			return typeName;
		}
		
		/**
		 * Index of the associated pulse port.
		 * Equals -1 for a T-port or D-port.
		 */
		public int getPortIndex() {
			return portIndex;
		}
		
		public String getAdapterLabel() {
			return adapterLabel;
		}
		
		public boolean isPortToEnvironment() {
			return isPortToEnvironment;
		}
		
		public boolean isTimeoutPort() {
			return isTimeoutPort;
		}
		
		public int getExecutionTime() {
			return executionTime;
		}
		
		@Override
		public String toString() {
			return owner.getName() + "::" + name;
		}
	}
	
	public static class Vertex {
		private final int id;
		private final Map<Scope, String> statePerScope;
		private final Map<Scope, String> clzsPerScope;
		private final Map<Port, String> valuation;
		private final List<Transition> outgoing;
		private final Set<Transition> incoming;
		
		public Vertex(int id, Map<Scope, String> statePerScope, Map<Scope, String> clzsPerScope) {
			this.id = id;
			this.statePerScope = statePerScope;
			this.clzsPerScope = clzsPerScope;
			
			valuation = new HashMap<Port, String>();
			outgoing = new ArrayList<Transition>();
			incoming = new HashSet<Transition>();
		}
		
		public int getId() {
			return id;
		}
		
		public Map<Scope, String> getStatePerScope() {
			return statePerScope;
		}
		
		public Map<Scope, String> getClzsPerScope() {
			return clzsPerScope;
		}
		
		public Map<Port, String> getValuation() {
			return valuation;
		}
		
		public List<Transition> getOutgoing() {
			return outgoing;
		}
		
		public Set<Transition> getIncoming() {
			return incoming;
		}
		
		public Set<InputChanges> getOutgoingInputChanges() {
			Set<InputChanges> result = new HashSet<InputChanges>();
			
			for (Transition t : outgoing) {
				if (!result.add(t.getInputChanges())) {
					//					if (!t.getInputChanges().isHiddenTimerTrigger()) {
					System.err.println(t.getInputChanges().toString());
					throw new Error("Duplicate transition labels!!");
					//					}
				}
			}
			
			return result;
		}
		
		public Transition getOutgoingTransition(InputChanges inputChanges) {
			Transition result = null;
			
			for (Transition t : outgoing) {
				if (t.inputChanges == inputChanges) {
					if (result != null) {
						throw new Error("Should not happen!");
					}
					
					result = t;
				}
			}
			
			if (result == null) {
				System.out.println("ic = " + inputChanges);
				throw new Error("Should not happen!");
			}
			
			return result;
		}
		
		public SortedMap<Integer, List<Transition>> getTransitionsPerTgtId() {
			SortedMap<Integer, List<Transition>> result = new TreeMap<Integer, List<Transition>>();
			
			for (Transition t : outgoing) {
				List<Transition> xs = result.get(t.getTgt().getId());
				
				if (xs == null) {
					xs = new ArrayList<Transition>();
					result.put(t.getTgt().getId(), xs);
				}
				
				xs.add(t);
			}
			
			return result;
		}
	}
	
	public static class InputChanges {
		private final int id;
		private final Map<Port, String> newValuePerPort;
		private final Port durationPort;
		private final boolean isHiddenTimerTrigger;
		
		public InputChanges(int id, Map<Port, String> newValuePerPort, Port durationPort, boolean isHiddenTimerTrigger) {
			this.id = id;
			this.newValuePerPort = newValuePerPort;
			this.durationPort = durationPort;
			this.isHiddenTimerTrigger = isHiddenTimerTrigger;
		}
		
		public int getId() {
			return id;
		}
		
		public Map<Port, String> getNewValuePerPort() {
			return newValuePerPort;
		}
		
		public Port getDurationPort() {
			return durationPort;
		}
		
		public boolean isHiddenTimerTrigger() {
			return isHiddenTimerTrigger;
		}
		
		@Override
		public String toString() {
			Set<String> elems = new TreeSet<String>();
			
			if (isHiddenTimerTrigger) {
				elems.add("AFTER <<hidden-timer-trigger>>");
			}
			
			if (durationPort != null) {
				elems.add("AFTER " + durationPort.toString());
			}
			
			for (Map.Entry<Port, String> e : newValuePerPort.entrySet()) {
				elems.add(e.getKey().toString() + "=" + e.getValue());
			}
			
			return "IN " + Texts.concat(elems, " | ");
		}
	}
	
	public static class OutputEvolution {
		private final int id;
		private final List<Map<Port, String>> evolution;
		private final boolean isExternal;
		
		public OutputEvolution(int id, List<Map<Port, String>> evolution) {
			this.id = id;
			this.evolution = evolution;
			
			isExternal = extractIsExternal();
		}
		
		private boolean extractIsExternal() {
			for (Map<Port, String> e1 : evolution) {
				for (Port e2 : e1.keySet()) {
					if (e2.isPortToEnvironment) {
						return true;
					}
				}
			}
			
			return false;
		}
		
		public int getId() {
			return id;
		}
		
		public List<Map<Port, String>> getEvolution() {
			return evolution;
		}
		
		public Map<Port, String> getLast() {
			return evolution.get(evolution.size() - 1);
		}
		
		public boolean isExternal() {
			return isExternal;
		}
		
		public boolean contains(Port p, String v) {
			for (Map<Port, String> m : evolution) {
				if (v.equals(m.get(p))) {
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public String toString() {
			List<String> xs = new ArrayList<String>();
			
			for (Map<Port, String> m : evolution) {
				List<String> elems = new ArrayList<String>();
				
				for (Map.Entry<Port, String> e : m.entrySet()) {
					elems.add(e.getKey().getOwner().getName() + "::" + e.getKey().getName() + " = " + e.getValue());
				}
				
				xs.add("{ " + Texts.concat(elems, "; ") + "}");
			}
			
			return Texts.concat(xs, " -> ");
		}
	}
	
	public static class Transition {
		private final Vertex src;
		private final Vertex tgt;
		private final InputChanges inputChanges;
		private final Set<OutputEvolution> outputEvolutions;
		private final Set<OutputEvolution> externalOutputEvolutions;
		private final int id;
		
		public int level;
		public Transition predFromInit;
		public Transition predFromStart;
		public boolean touched;
		public long visitTimestamp;
		
		public Transition(int id, Vertex src, Vertex tgt, InputChanges inputChanges, Set<OutputEvolution> outputEvolutions) {
			this.id = id;
			this.src = src;
			this.tgt = tgt;
			this.inputChanges = inputChanges;
			this.outputEvolutions = outputEvolutions;
			
			externalOutputEvolutions = new HashSet<OutputEvolution>();
			
			for (OutputEvolution evo : outputEvolutions) {
				if (evo.isExternal()) {
					externalOutputEvolutions.add(evo);
				}
			}
		}
		
		public int getId() {
			return id;
		}
		
		public Vertex getSrc() {
			return src;
		}
		
		public Vertex getTgt() {
			return tgt;
		}
		
		public InputChanges getInputChanges() {
			return inputChanges;
		}
		
		public Set<OutputEvolution> getOutputEvolutions() {
			return outputEvolutions;
		}
		
		public Set<OutputEvolution> getExternalOutputEvolutions() {
			return externalOutputEvolutions;
		}
		
		public boolean isInteresting() {
			for (OutputEvolution evo : outputEvolutions) {
				if (evo.getEvolution().size() > 1) {
					return true;
				}
			}
			
			return false;
		}
		
		public Map<Port, List<String>> constructChangesPerPort() {
			Map<Port, List<String>> result = new HashMap<Port, List<String>>();
			
			for (OutputEvolution evo : outputEvolutions) {
				for (Map<Port, String> x : evo.getEvolution()) {
					for (Map.Entry<Port, String> e : x.entrySet()) {
						List<String> dest = result.get(e.getKey());
						
						if (dest == null) {
							dest = new ArrayList<String>();
							result.put(e.getKey(), dest);
						}
						
						dest.add(e.getValue());
					}
				}
			}
			
			return result;
		}
		
		public void print(String prefix) {
			System.out.println(prefix + inputChanges.toString());
			boolean output = false;
			
			for (Map.Entry<Port, List<String>> e : constructChangesPerPort().entrySet()) {
//				if (e.getValue().size() > 1) {
					System.out.println(prefix + "  OUT " + e.getKey().getName() + " = " + Texts.concat(e.getValue(), " -> "));
					output = true;
//				}
			}
			
			if (!output) {
				System.out.println(prefix + "  OUT <<none>>");
			}
			
			if (src != null) {
				for (Map.Entry<Scope, String> e : src.clzsPerScope.entrySet()) {
					String newClzs = tgt.clzsPerScope.get(e.getKey());
					
					if (newClzs.equals(e.getValue())) {
						System.out.println(prefix + " " + e.getKey().getName() + " stays in " + e.getValue());
					} else {
						System.out.println(prefix + " " + e.getKey().getName() + " moves from " + e.getValue() + " to " + newClzs);
					}
				}
			} else {
				for (Map.Entry<Scope, String> e : tgt.clzsPerScope.entrySet()) {
					System.out.println(prefix + " " + e.getKey().getName() + " starts in " + e.getValue());
				}
			}
		}
	}
	
	public static class InitState {
		public final Map<Scope, String> statePerScope;
		public final Map<Port, String> valuation;
		public final int hashCode;
		
		public InitState(Vertex vtx) {
			statePerScope = extractStatePerScope(vtx);
			valuation = vtx.getValuation();
			hashCode = Objects.hash(statePerScope, valuation);
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InitState other = (InitState) obj;
			return Objects.equals(statePerScope, other.statePerScope) && Objects.equals(valuation, other.valuation);
		}
	}
	
	public static class Response {
		private final Set<OutputEvolution> evos;
		private final Map<Scope, String> statePerScope;
		private final Map<Port, String> valuation;
		private final int hashCode;
		
		public Response(Transition t) {
			evos = t.getOutputEvolutions();
			statePerScope = extractStatePerScope(t.getTgt());
			valuation = t.getTgt().getValuation();
			hashCode = Objects.hash(statePerScope, evos, valuation);
		}
		
		public Response(Set<OutputEvolution> evos, Map<Scope, String> statePerScope, Map<Port, String> valuation) {
			this.evos = evos;
			this.statePerScope = statePerScope;
			this.valuation = valuation;
			hashCode = Objects.hash(statePerScope, evos, valuation);
		}
		
		public Set<OutputEvolution> getEvos() {
			return evos;
		}
		
		public Map<Scope, String> getStatePerScope() {
			return statePerScope;
		}
		
		public Map<Port, String> getValuation() {
			return valuation;
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Response other = (Response) obj;
			return Objects.equals(statePerScope, other.statePerScope) && Objects.equals(evos, other.evos) && Objects.equals(valuation, other.valuation);
		}
	}
	
	private static Map<Scope, String> extractStatePerScope(Vertex vtx) {
		return Collections.emptyMap();
		//		return vtx.getStatePerScope();
		//		return vtx.getClzsPerScope();
	}
	
	public static class Trace {
		private final int id;
		private final List<Transition> transitions;
		
//		public Trace() {
//			transitions = new ArrayList<Transition>();
//		}
		
		/**
		 * There must be at least one transition.
		 */
		public Trace(int id, List<Transition> transitions) {
			this.id = id;
			this.transitions = transitions;
		}
		
		public int getId() {
			return id;
		}
		
		public List<Transition> getTransitions() {
			return transitions;
		}
		
		public int getInterestingness() {
			int result = 0;
			
			for (Transition t : transitions) {
				 if (t.isInteresting()) {
					 result++;
				 }
			}
			
			return result;
		}
	}
	
	public static class CharSet {
		private final Vertex vtx;
		private final Set<Trace> traces;
		
		public CharSet(Vertex vtx, Set<Trace> traces) {
			this.vtx = vtx;
			this.traces = traces;
		}
		
		public Vertex getVertex() {
			return vtx;
		}
		
		public Set<Trace> getTraces() {
			return traces;
		}
	}
}
