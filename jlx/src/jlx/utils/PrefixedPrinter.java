package jlx.utils;

import java.io.PrintStream;
import java.util.Stack;

public class PrefixedPrinter {
	private PrintStream out;
	private Stack<String> header;
	private Stack<String> items;
	
	public PrefixedPrinter(PrintStream out) {
		this.out = out;
		
		header = new Stack<String>();
		header.push("");
		
		items = new Stack<String>();
		items.push("");
	}
	
	public void indent(int index, int maxIndex) {
		if (index < maxIndex) {
			header.push(items.peek() + "|-");
			items.push(items.peek() + "| ");
		} else {
			header.push(items.peek() + "\\-");
			items.push(items.peek() + "  ");
		}
	}
	
	public void unindent() {
		header.pop();
		items.pop();
	}
	
	public void println(String line) {
		out.println(header.peek() + line);
	}
}
