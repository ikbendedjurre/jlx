package jlx.printing;

import java.util.*;

import jlx.asal.*;
import jlx.asal.j.*;
import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.api.*;
import jlx.asal.vars.ASALVariable;

public class ASALA2mCRL2Visitor extends ASALVisitor<List<String>> {
	public final AbstractMCRL2ModelPrinter printer;
	
	public ASALA2mCRL2Visitor(AbstractMCRL2ModelPrinter printer) {
		this.printer = printer;
	}
	
	@Override
	public List<String> visitFctDef(ASALOp fct) {
		List<String> result = new ArrayList<String>();
		result.add(printer.getName(fct));
		return result;
	}
	
	@Override
	public List<String> handle(ASALVariable leaf) {
		List<String> result = new ArrayList<String>();
		result.add(printer.getName(leaf.getLegacy()));
		return result;
	}
	
	@Override
	public List<String> handle(ASALVarRef node, List<String> var) {
		List<String> result = new ArrayList<String>();
		
		switch (node.getResolvedVar().getOrigin()) {
			case FCT_PARAM:
				result.add("{{{asala.pushLocalVar}}}(" + var.get(0) + ")");
				break;
			case STM_PORT:
			case STM_VAR:
				result.add("{{{asala.pushGlobalVar}}}(" + var.get(0) + ")");
				break;
			default:
				break;
		}
		
		return result;
	}
	
	@Override
	public List<String> handle(ASALAssignStatement node, List<String> var, List<String> expr) {
		List<String> result = new ArrayList<String>();
		result.addAll(expr);
		
		switch (node.getResolvedVar().getOrigin()) {
			case FCT_PARAM:
				result.add("{{{asala.setLocalVar}}}(" + var.get(0) + ")");
				break;
			case STM_PORT:
			case STM_VAR:
				result.add("{{{asala.setGlobalVar}}}(" + var.get(0) + ")");
				break;
			default:
				break;
		}
		
		result.add("{{{asala.pop}}}");
		return result;
	}
	
	@Override
	public List<String> handle(ASALBinaryExpr node, List<String> lhs, List<String> rhs) {
		List<String> result = new ArrayList<String>();
		result.addAll(lhs);
		result.addAll(rhs);
		result.add("{{{asala.op2}}}({{{op2." + node.getOp() + "}}})");
		return result;
	}
	
	@Override
	public List<String> handle(ASALEmptyStatement node) {
		return new ArrayList<String>();
	}
	
	@Override
	public List<String> handle(ASALOp leaf, List<String> stat) {
		throw new Error("Should not happen!");
	}
	
	@Override
	public List<String> handle(ASALFunctionCall node, List<String> fct, List<List<String>> params) {
		List<String> result = new ArrayList<String>();
		
		for (List<String> param : params) {
			result.addAll(param);
		}
		
		result.add("{{{asala.fct}}}(" + fct.get(0) + ")");
		return result;
	}
	
	@Override
	public List<String> handle(ASALFunctionCallStatement node, List<String> fct, List<List<String>> params) {
		List<String> result = new ArrayList<String>();
		
		for (List<String> param : params) {
			result.addAll(param);
		}
		
		result.add("{{{asala.fct}}}(" + fct.get(0) + ")");
		
		if (!node.getResolvedOperation().getReturnType().equals(JVoid.class)) {
			result.add("{{{asala.pop}}}");
		}
		
		return result;
	}
	
	@Override
	public List<String> handle(ASALIfStatement node, List<String> cond, List<String> thenBranch, List<String> elseBranch) {
		List<String> result = new ArrayList<String>();
		result.addAll(cond);
		
		if (elseBranch != null) {
			//Move to the end of the THEN branch, then to a jump instruction, and then one further:
			int delta1 = thenBranch.size() + 1 + 1;
			
			//Move to the end of the ELSE branch, and then one further:
			int delta2 = elseBranch.size() + 1;
			
			result.add("{{{asala.jumpIfFalse}}}(" + delta1 + ")");
			result.addAll(thenBranch);
			result.add("{{{asala.jump}}}(" + delta2 + ")");
			result.addAll(elseBranch);
		} else {
			//Move to the end of the THEN branch, then jump one further:
			int delta1 = thenBranch.size() + 1;
			
			result.add("{{{asala.jumpIfFalse}}}(" + delta1 + ")");
			result.addAll(thenBranch);
		}
		
		return result;
	}
	
	@Override
	public List<String> handle(ASALWhileStatement node, List<String> cond, List<String> body) {
		List<String> result = new ArrayList<String>();
		
		//Move to the end of the body, then to a jump instruction, and then one further:
		int delta1 = body.size() + 1 + 1;
		
		//Move to the start of the body, then to a jump instruction, and then to the start of the condition:
		int delta2 = -body.size() - 1 - cond.size();
		
		result.addAll(cond);
		result.add("{{{asala.jumpIfFalse}}}(" + delta1 + ")");
		result.addAll(body);
		result.add("{{{asala.jump}}}(" + delta2 + ")");
		
		return result;
	}
	
	@Override
	public List<String> handle(ASALLiteral leaf) {
		List<String> result = new ArrayList<String>();
		result.add("{{{asala.pushValue}}}(" + printer.literalToStr(leaf, FlatTarget.JTYPE) + ")");
		
//		if (leaf.getResolvedType().equals(JBool.class)) {
//			result.add("{{{asala.pushValue}}}(Value_Bool(" + leaf.getText().toLowerCase() + "))");
//		} else if (leaf.getResolvedType().equals(JInt.class)) {
//			result.add("{{{asala.pushValue}}}(Value_Int(" + leaf.getText() + "))");
//		} else if (leaf.getResolvedType().equals(JPulse.class)) {
//			result.add("{{{asala.pushValue}}}(Value_Bool(" + leaf.getText() + "))");
//		} else {
//			result.add("{{{asala.pushValue}}}(Value_String(" + printer.getName(leaf.getResolvedConstructor()) + "))");
//		}
		
		return result;
	}
	
	@Override
	public List<String> handle(ASALReturnStatement node, List<String> expr) {
		List<String> result = new ArrayList<String>();
		result.addAll(expr);
		result.add("{{{asala.return}}}");
		return result;
	}
	
	@Override
	public List<String> handle(ASALSeqStatement node, List<String> first, List<String> second) {
		List<String> result = new ArrayList<String>();
		result.addAll(first);
		result.addAll(second);
		return result;
	}
	
	@Override
	public List<String> handle(ASALUnaryExpr node, List<String> expr) {
		List<String> result = new ArrayList<String>();
		result.addAll(expr);
		result.add("{{{asala.op1}}}({{{op1." + node.getOp() + "}}})");
		return result;
	}
}




