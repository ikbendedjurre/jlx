package jlx.asal.parsing;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import jlx.utils.PrefixedPrinter;

@SuppressWarnings("serial")
public class ASALSyntaxTree extends HashMap<String, ASALSyntaxTree> {
	private ASALCode code;
	private ASALRule rule;
	private ASALToken firstToken;
	private SortedSet<ASALToken> tokens;
	private Map<String, String> properties;
	private ASALSyntaxTree parent;
	
	public ASALSyntaxTree(ASALCode code) {
		this.code = code;
		
		properties = new HashMap<String, String>();
		tokens = new TreeSet<ASALToken>();
	}
	
	public ASALSyntaxTree(ASALSyntaxTree source, ASALSyntaxTree newParent) {
		code = source.code;
		properties = new HashMap<String, String>(source.properties);
		firstToken = source.firstToken;
		tokens = new TreeSet<ASALToken>(source.tokens);
		rule = source.rule;
		parent = newParent;
		
		for (Map.Entry<String, ASALSyntaxTree> entry : source.entrySet()) {
			put(entry.getKey(), new ASALSyntaxTree(entry.getValue(), this));
		}
	}
	
	public ASALCode getCode() {
		return code;
	}
	
	public ASALRule getRule() {
		return rule;
	}
	
	public void setRule(ASALRule rule) {
		this.rule = rule;
	}
	
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}
	
	public String getPropery(String key) {
		return properties.get(key);
	}
	
	public ASALSyntaxTree getParent() {
		return parent;
	}
	
	public void setParent(ASALSyntaxTree parent) {
		this.parent = parent;
	}
	
	public String getType() {
		return properties.get("type");
	}
	
	public ASALToken getFirstToken() {
		return firstToken;
	}
	
	public SortedSet<ASALToken> getTokens() {
		return Collections.unmodifiableSortedSet(tokens);
	}
	
	public String getTokenText() {
		Iterator<ASALToken> q = tokens.iterator();
		
		if (q.hasNext()) {
			String result = q.next().text;
			
			while (q.hasNext()) {
				result += " " + q.next().text;
			}
			
			return result;
		}
		
		return "";
	}
	
	public void setFirstToken(ASALToken firstToken) {
		if (firstToken != null) {
			if (this.firstToken == null) {
				this.firstToken = firstToken;
			}
			
			tokens.add(firstToken);
		}
	}
	
//	public <T extends ASALSyntaxTreeAPI> T createAPI(ASALSyntaxTreeAPI parent, Class<T> clz) {
//		return createAPI(parent.getCode(), parent, clz);
//	}
	
	@SuppressWarnings("unchecked")
	public <T extends ASALSyntaxTreeAPI> T createAPI(ASALSyntaxTreeAPI parentAPI, Class<T> clz) {
		if (!clz.isAssignableFrom(rule.getClz())) {
			throw new Error(clz.getSimpleName() + " should be a superclass of " + rule.getClz().getSimpleName() + "!");
		}
		
		try {
			Constructor<?> cstr = rule.getClz().getConstructor(ASALSyntaxTreeAPI.class, ASALSyntaxTree.class);
			return (T)cstr.newInstance(parentAPI, this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new Error("Should not happen!", e);
		}
	}
	
	public void print(String name, PrintStream out) {
		print(name, new PrefixedPrinter(out));
	}
	
	private void print(String name, PrefixedPrinter prefix) {
		if (rule.getClz() != null) {
			prefix.println(name + ": " + rule.getClz().getSimpleName());
		} else {
			prefix.println(name);
		}
		
		int count1 = 2;
		int count12 = count1 + properties.size();
		int count123 = count12 + size();
		
		int index = 0;
		
		{
			prefix.indent(index, count123 - 1);
			prefix.println("RULE_DEF " + rule.getDef());
			prefix.unindent();
			index++;
		}
		
		{
			prefix.indent(index, count123 - 1);
			prefix.println("FIRST_TOKEN " + firstToken);
			prefix.unindent();
			index++;
		}
		
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			prefix.indent(index, count123 - 1);
			prefix.println("PROPERTY " + entry.getKey() + " := " + entry.getValue());
			prefix.unindent();
			index++;
		}
		
		for (Map.Entry<String, ASALSyntaxTree> entry : entrySet()) {
			prefix.indent(index, count123 - 1);
			entry.getValue().print(entry.getKey(), prefix);
			prefix.unindent();
			index++;
		}
	}
}
