package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.utils.*;

public class ASALSeqStatement extends ASALStatement {
	private ASALStatement statement;
	private ASALStatement successor;
	
	public ASALSeqStatement(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		statement = createAPI("statement", ASALStatement.class, false);
		successor = createAPI("successor", ASALStatement.class, false);
	}
	
	public ASALSeqStatement(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree, ASALStatement statement, ASALStatement successor) {
		super(parent, tree);
		
		//this.statement = statement.getTree().createAPI(this, ASALStatement.class);
		//this.successor = successor.getTree().createAPI(this, ASALStatement.class);
		
		this.statement = statement;
		this.successor = successor;
	}
	
	public ASALStatement getStatement() {
		return statement;
	}
	
	public ASALStatement getSuccessor() {
		return successor;
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		statement.validateAndCrossRef(scope, JVoid.class);
		successor.validateAndCrossRef(scope, JVoid.class);
	}
	
	@Override
	public void confirmReturnValue() throws ASALException {
		try {
			statement.confirmReturnValue();
		} catch (ASALException e) {
			successor.confirmReturnValue();
		}
	}
	
	@Override
	public boolean containsNonEmpty() {
		return statement.containsNonEmpty() || successor.containsNonEmpty();
	}
	
	@Override
	public List<String> toText(Indentation indent, TextOptions options) {
		List<String> result = new ArrayList<String>();
		result.addAll(statement.toText(indent, options));
		result.addAll(successor.toText(indent, options));
		return result;
	}
	
	public static ASALStatement from(ASALStatement first, ASALStatement second) {
		if (first instanceof ASALEmptyStatement) {
			return second;
		}
		
		if (second instanceof ASALEmptyStatement) {
			return first;
		}
		
		if (first instanceof ASALSeqStatement) {
			if (((ASALSeqStatement)first).successor instanceof ASALEmptyStatement) {
				ASALStatement s = ((ASALSeqStatement)first).statement;
				return new ASALSeqStatement(s, s.getTree(), s, second);
			}
		}
		
		return new ASALSeqStatement(first, first.getTree(), first, second);
	}
	
//	public static ASALStatement fromList(ASALStatement... statements) {
//		List<ASALStatement> list = new ArrayList<ASALStatement>();
//		
//		for (ASALStatement s : statements) {
//			list.add(s);
//		}
//		
//		return fromList(statements);
//	}
	
	public static ASALStatement fromList(List<ASALStatement> statements) {
		//List<ASALStatement> stats = toList(statements);
		List<ASALStatement> stats = statements;
		
		switch (stats.size()) {
			case 0:
				return new ASALEmptyStatement(null, null);
			case 1:
				return stats.get(0);
			default:
				ASALStatement result = stats.get(0);
				
				for (int index = 1; index < stats.size(); index++) {
					result = from(result, stats.get(index));
				}
				
				return result;
		}
	}
}
