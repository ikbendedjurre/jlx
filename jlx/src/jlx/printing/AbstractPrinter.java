package jlx.printing;

import java.io.*;
import java.util.*;

import jlx.utils.UnusedNames;

public abstract class AbstractPrinter<T> {
	private Map<Object, ElementPrinter> printerPerElem;
	private Map<Object, String> namePerObject;
	private UnusedNames unusedNames;
	private ElementPrinter elemPrinter;
	
	protected static class ElementPrinter {
		private final Object elem;
		private final Set<PrintStream> outs;
		
		private ElementPrinter(Object origin) {
			this.elem = origin;
			this.outs = new HashSet<PrintStream>();
		}
		
		public final void toFile(String filename) {
			try {
				File targetFile = new File(filename);
				System.out.println("Target file: " + targetFile.getCanonicalPath());
				targetFile.getCanonicalFile().getParentFile().mkdirs();
				toStream(new PrintStream(new File(filename)));
			} catch (FileNotFoundException e) {
				throw new Error(e);
			} catch (IOException e) {
				throw new Error(e);
			}
		}
		
		public final void toStream(PrintStream o) {
			outs.add(o);
		}
		
		public final void toOut() {
			toStream(System.out);
		}
	}
	
	protected final T target;
	
	public AbstractPrinter(T target) {
		this.printerPerElem = new HashMap<Object, ElementPrinter>();
		this.namePerObject = new HashMap<Object, String>();
		this.unusedNames = new UnusedNames();
		this.target = target;
	}
	
	private ElementPrinter getOrCreateElemPrinter(Object origin) {
		ElementPrinter result = printerPerElem.get(origin);
		
		if (result == null) {
			result = new ElementPrinter(origin);
			printerPerElem.put(origin, result);
		}
		
		return result;
	}
	
	protected final ElementPrinter selectElemPrinter(Object elem) {
		elemPrinter = getOrCreateElemPrinter(elem);
		return elemPrinter;
	}
	
	protected final <S> S getElemPrinter(Class<S> clz) {
		return clz.cast(elemPrinter.elem);
	}
	
	public final void setName(Object obj, String name) {
		namePerObject.put(obj, name);
	}
	
	public final String getName(Object obj) {
		return namePerObject.get(obj);
	}
	
	private int indentation = 0;
	
	protected void println(String s, int indent) {
		println(s);
		indentation += indent;
	}
	
	protected void println(int indent, String s) {
		indentation += indent;
		println(s);
	}
	
	protected void println(String s) {
		s = "\t".repeat(indentation) + s;
		
		for (PrintStream out : elemPrinter.outs) {
			out.println(s);
		}
	}
	
	protected final void println__(String s) {
		println("\t" + s);
	}
	
	protected final void println____(String s) {
		println("\t\t" + s);
	}
	
	protected final void println______(String s) {
		println("\t\t\t" + s);
	}
	
	protected final void println________(String s) {
		println("\t\t\t\t" + s);
	}
	
	protected final void println__________(String s) {
		println("\t\t\t\t\t" + s);
	}
	
	protected final void printlines(String... lines) {
		printlines(0, "", lines);
	}
	
	protected final void printlines(int indent, String sep, Collection<String> lines) {
		Iterator<String> q = lines.iterator();
		
		while (q.hasNext()) {
			println("\t".repeat(indent) + q.next() + (q.hasNext() ? sep : ""));
		}
	}
	
	protected final void printlines(int indent, String sep, Collection<String> lines, Collection<String> afterSepLines) {
		if (lines.size() != afterSepLines.size()) {
			throw new Error("Should not happen!");
		}
		
		Iterator<String> q = lines.iterator();
		Iterator<String> c = afterSepLines.iterator();
		
		while (q.hasNext()) {
			println("\t".repeat(indent) + q.next() + (q.hasNext() ? sep : "") + c.next());
		}
	}
	
	protected final void printlines(int indent, String sep, Map<String, String> afterSepPerLine) {
		Iterator<Map.Entry<String, String>> q = afterSepPerLine.entrySet().iterator();
		
		while (q.hasNext()) {
			Map.Entry<String, String> e = q.next();
			println("\t".repeat(indent) + e.getKey() + (q.hasNext() ? sep : "") + e.getValue());
		}
	}
	
	protected final void printlines(int indent, String sep, String... lines) {
		printlines(indent, sep, List.of(lines));
	}
	
	protected final String getUnusedName(String base) {
		return unusedNames.generateUnusedName(base);
	}
	
	protected String applyNamingConventions(String base) {
		return base;
	}
	
	public final UnusedNames cloneUnusedNames() {
		UnusedNames result = new UnusedNames();
		result.addAll(unusedNames);
		return result;
	}
	
	protected abstract void initElemPrinters(String baseName);
	protected abstract void print();
	
	private void flushElemPrinters() {
		for (ElementPrinter elemPrinter : printerPerElem.values()) {
			for (PrintStream out : elemPrinter.outs) {
				out.flush();
				out.close();
			}
		}
	}
	
	public final AbstractPrinter<T> printAndPop(String baseName) {
		printerPerElem.clear();
		selectElemPrinter(target); //We add a printer for the target before we add any other printers.
		initElemPrinters(baseName); //We can add additional printers here.
		selectElemPrinter(target); //Initially, we start printing the target.
		print();
		flushElemPrinters();
		return this;
	}
	
	protected static String concat(Collection<String> elems, String sep) {
		if (elems.isEmpty()) {
			return "";
		}
		
		Iterator<String> q = elems.iterator();
		String result = q.next();
		
		while (q.hasNext()) {
			result += sep + q.next();
		}
		
		return result;
	}
}
