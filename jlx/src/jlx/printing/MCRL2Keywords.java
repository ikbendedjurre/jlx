package jlx.printing;

import java.util.*;

public class MCRL2Keywords {
	public final static SortedSet<String> ALL = getAll();
	
	private static SortedSet<String> getAll() {
		SortedSet<String> result = new TreeSet<String>();
		
		//From https://www.mcrl2.org/web/user_manual/language_reference/lex.html:
		result.add("act");
		result.add("allow");
		result.add("block");
		result.add("comm");
		result.add("cons");
		result.add("delay");
		result.add("div");
		result.add("end");
		result.add("eqn");
		result.add("exists");
		result.add("forall");
		result.add("glob");
		result.add("hide");
		result.add("if");
		result.add("in");
		result.add("init");
		result.add("lambda");
		result.add("map");
		result.add("mod");
		result.add("mu");
		result.add("nu");
		result.add("pbes");
		result.add("proc");
		result.add("rename");
		result.add("sort");
		result.add("struct");
		result.add("sum");
		result.add("val");
		result.add("var");
		result.add("whr");
		result.add("yaled");
		result.addAll(getPredefinedSorts());
		result.addAll(getPredefinedConstants());
		
		//Not from that specific URL:
		result.add("head");
		result.add("tail");
		result.add("rhead");
		result.add("rtail");
		result.add("min");
		result.add("max");
		result.add("pred");
		result.add("succ");
		result.add("exp");
		result.add("abs");
		result.add("floor");
		result.add("ceil");
		result.add("round");
		result.add("count");
		
		//We define many conversion functions that do not exist in mCRL2 (for convenience): 
		for (String sort1 : getPredefinedSorts()) {
			for (String sort2 : getPredefinedSorts()) {
				result.add(sort1 + "2" + sort2);
			}
		}
		
		return Collections.unmodifiableSortedSet(result);
	}
	
	private static SortedSet<String> getPredefinedSorts() {
		SortedSet<String> result = new TreeSet<String>();
		
		result.add("Bag");
		result.add("Bool");
		result.add("Int");
		result.add("List");
		result.add("Nat");
		result.add("Pos");
		result.add("Real");
		result.add("Set");
		
		return Collections.unmodifiableSortedSet(result);
	}
	
	private static SortedSet<String> getPredefinedConstants() {
		SortedSet<String> result = new TreeSet<String>();
		
		result.add("delta");
		result.add("false");
		result.add("nil");
		result.add("tau");
		result.add("true");
		
		return Collections.unmodifiableSortedSet(result);
	}
}

