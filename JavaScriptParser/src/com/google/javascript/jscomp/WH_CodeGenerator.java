package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class WH_CodeGenerator {
	Node root;

	/**
	 * An root of the AST is required to construct a WH_CodeGenerator
	 * @param _root
	 */
	public WH_CodeGenerator(Node _root) {
		root = _root;
	}
	
	/**
	 * Get the kth child of Node t
	 * @param t
	 * @param k
	 * @return
	 */
	private Node getChildAtIndex(Node t, int k) {
		for (Node p : t.children()) {
			if ((-- k) == 0) {
				return p;
			}
		}
		return null;
	}
	
	/**
	 * generate tabs for t times
	 * @param t - the times of tabs to generate
	 * @return a string of t times tab
	 */
	private String tabs(int times) {
		String str = "";
		for (int i = 0; i < times; i++) str += "\t";
		return str;
	}
	
	/**
	 * generate code for an AST
	 * @param t - the root of the AST
	 * @param depth - the indent
	 * @return a string of generated code
	 */
	private String generateAST(Node t, int depth) {
		switch(t.getType())
		{
		case Token.ADD:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " + " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.SUB:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " - " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.MUL:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " * " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.DIV:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " / " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.AND:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " & " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.OR:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " | " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.BITAND:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " && " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.BITOR:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " || " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.BITXOR:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " ^^ " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.BREAK:
			return tabs(depth) + "break;\n";
		case Token.DEC:
			if (t.getParent().getType() == Token.EXPR_RESULT) 
				return tabs(depth) + generateAST(getChildAtIndex(t, 1), 0) + "--";
			else
				return "(" + tabs(depth) + generateAST(getChildAtIndex(t, 1), 0) + "--)";
		case Token.INC:
			if (t.getParent().getType() == Token.EXPR_RESULT) 
				return tabs(depth) + generateAST(getChildAtIndex(t, 1), 0) + "++";
			else
				return "(" + generateAST(getChildAtIndex(t, 1), 0) + "++)";
		case Token.EQUALS:
			return tabs(depth) + "(" + 
					generateAST(getChildAtIndex(t, 1), 0) + " == " + 
					generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.FALSE:
			return "false";
		case Token.TRUE:
			return "true";
		case Token.GE:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " >= " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.GT:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " > " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.LE:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " <= " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.LT:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " < " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.IF:
			String strs = tabs(depth) + "if (" + 
				generateAST(getChildAtIndex(t, 1), 0) + ")\n" + 
				generateAST(getChildAtIndex(t, 2), depth);
			if (getChildAtIndex(t, 3) != null) 
				strs += tabs(depth) + "else\n" + generateAST(getChildAtIndex(t, 3), depth);
			return strs;
		case Token.NE:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " != " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.MOD:
			return tabs(depth) + "(" + 
				generateAST(getChildAtIndex(t, 1), 0) + " % " + 
				generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.NOT:
			return tabs(depth) + "!" + generateAST(getChildAtIndex(t, 1), 0);
		case Token.WHILE:
			return "while (" + 
				generateAST(getChildAtIndex(t, 1), 0) + ")\n" + 
				generateAST(getChildAtIndex(t, 2), depth);
		case Token.GETPROP:
			return tabs(depth) + 
				generateAST(getChildAtIndex(t, 1), 0) + "." + 
				generateAST(getChildAtIndex(t, 2), 0);
		case Token.GETELEM:
			return tabs(depth) + 
				generateAST(getChildAtIndex(t, 1), 0) + "[" + 
				generateAST(getChildAtIndex(t, 2), 0) + "]";
		case Token.ASSIGN:
			if (t.getParent().isExprResult())
				return tabs(depth) + 
					generateAST(getChildAtIndex(t, 1), 0) + " = " + 
					generateAST(getChildAtIndex(t, 2), 0);
			else 
				return tabs(depth) + "(" + 
					generateAST(getChildAtIndex(t, 1), 0) + " = " + 
					generateAST(getChildAtIndex(t, 2), 0) + ")";
		case Token.FUNCTION:
			return tabs(depth) + "function " + 
				generateAST(getChildAtIndex(t, 1), depth) + 
				generateAST(getChildAtIndex(t, 2), 0) + "\n" + 
				generateAST(getChildAtIndex(t, 3), depth);
		case Token.SCRIPT:
			String str = "";
			for (Node p : t.children()) {
				str += generateAST(p, 0);
			}
			return str;
		case Token.NAME:
			String strt = t.getString();
			if (t.getFirstChild() != null) {
				strt += " = " + generateAST(t.getFirstChild(), 0);
			}
			return strt;
		case Token.PARAM_LIST:
			String str1 = "";
			boolean isFirst = true;
			for (Node p : t.children()) {
				if (!isFirst) str1 += ", ";
				isFirst = false;
				str1 += generateAST(p, 0);
			}
			return "(" + str1 + ")";
		case Token.BLOCK:
			String str2 = "";
			for (Node p : t.children()) {
				str2 += generateAST(p, depth + 1);
			}
			return tabs(depth) + "{\n" + str2 + tabs(depth) + "}\n";
		case Token.RETURN:
			return tabs(depth) + "return " + generateAST(t.getFirstChild(), 0) + ";\n";
		case Token.EXPR_RESULT:
			return tabs(depth) + generateAST(t.getFirstChild(), 0) + ";\n";
		case Token.STRING:
			if (t.getParent().isGetProp())
				return t.getString();
			else
				if (t.getString().substring(0, Math.min(3, t.getString().length())).equals("nb#"))
					return t.getString().substring(3);
				else 
					return tabs(depth) + "\"" + t.getString() + "\"";
		case Token.NUMBER:
			return tabs(depth) + t.getDouble();
		case Token.CALL:
			String str3 = "";
			int k = 0;
			for (Node p : t.children()) {
				k++;
				if (k == 2) str3 += "(";
				if (k > 2) str3 += ", ";
				if (k == 1) str3 += generateAST(p, depth + 1);
				else str3 += generateAST(p, 0);
			}
			return str3 + ")";
		case Token.VAR:
			String str4 = tabs(depth) + "var ";
			k = 0;
			for (Node p : t.children()) {
				k++;
				if (k == 2) str4 += ", ";
				str4 += generateAST(p, 0);
			}
			return str4 + ";\n";
		case Token.NEW:
			return tabs(depth) + "new(" + generateAST(t.getFirstChild(), 0) + ")";
		}
		return "";
	}

	/**
	 * Generate code from stored root
	 * @return the code
	 */
	public String generate() {
		return generateAST(root, 0);
	}

}