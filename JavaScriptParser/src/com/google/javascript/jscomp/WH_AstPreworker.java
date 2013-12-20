package com.google.javascript.jscomp;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class WH_AstPreworker {
	
	private Node root;
	private int TempVarCounter = 0;
	private int TempFuncCounter = 0;
	private int FunctionLayer = 0;
	
	public WH_AstPreworker(Node _root) {
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
	
	private boolean isAssignFamily(Node t) { 
		return t.getType() == Token.ASSIGN ||
				t.getType() == Token.ASSIGN_ADD ||
				t.getType() == Token.ASSIGN_BITAND ||
				t.getType() == Token.ASSIGN_BITOR ||
				t.getType() == Token.ASSIGN_DIV ||
				t.getType() == Token.ASSIGN_LSH ||
				t.getType() == Token.ASSIGN_MOD ||
				t.getType() == Token.ASSIGN_MUL ||
				t.getType() == Token.ASSIGN_RSH ||
				t.getType() == Token.ASSIGN_SUB ||
				t.getType() == Token.ASSIGN_URSH;
	}
	
	private void Rebuilding(Node t) {
		System.err.println(t.toString());

		if (t.isFunction()) FunctionLayer ++;
		for (Node i : t.children()) {
			Rebuilding(i);
		}
		if (t.isFunction()) FunctionLayer --;
		/*
		 * To remove assignment in assignment
		 */
		if (isAssignFamily(t) && !t.getParent().isExprResult()) {
			Node s = t, s2 = t;
			ArrayList<Node> ancestors = new ArrayList<Node>();
			ancestors.add(s);
			while (!(s.isBlock() || s.isScript())) {
				s2 = s;
				s = s.getParent();
				ancestors.add(s);
			}
			for (int i = ancestors.size() - 2; i > 0; i--) {
				Node father = ancestors.get(i);
				Node son = ancestors.get(i - 1);
				if (isAssignFamily(father)) continue; 
				for (Node p : father.children()) {
					if (p.isEquivalentTo(son)) break;
					Node tempNode1 = new Node(Token.VAR);
					Node tempNode2 = Node.newString(Token.NAME, "_WHTempV" + TempVarCounter);
					Node tempNode3 = Node.newString(Token.NAME, "_WHTempV" + TempVarCounter);
					TempVarCounter++;
					tempNode1.addChildToBack(tempNode2);
					father.addChildBefore(tempNode3, p);
					father.removeChild(p);					tempNode2.addChildToBack(p);
					s.addChildBefore(tempNode1, s2);
				}
			}
			Node tempNode3 = Node.newString(Token.NAME, t.getFirstChild().getQualifiedName());
			Node tempNode4 = new Node(Token.EXPR_RESULT);
			t.getParent().addChildAfter(tempNode3, t);
			t.getParent().removeChild(t);
			s.addChildBefore(tempNode4, s2);
			tempNode4.addChildToFront(t);
		}
		/*
		 * To remove noname-function
		 */
		if (t.isFunction() && t.getParent().isCall()) {
			Node s = t.getParent(), s2 = t;
			while (!(s.isScript() || s.isBlock())) {
				s2 = s;
				s = s.getParent();
			}
			Node tempNode1 = Node.newString(Token.NAME, "___TemplateFunction_" + TempFuncCounter);
			Node tempNode2 = Node.newString(Token.NAME, "___TemplateFunction_" + TempFuncCounter);
			TempFuncCounter ++;
			t.removeFirstChild();
			t.addChildToFront(tempNode1);
			t.getParent().addChildToFront(tempNode2);
			t.getParent().removeChild(t);
			s.addChildBefore(t, s2);;
		}
		/*
		 * To turn "for" into "while"
		 */
		if (t.isFor()) {
			t.setType(Token.WHILE);
			Node tempNode1 = getChildAtIndex(t, 1);
			Node tempNode2 = getChildAtIndex(t, 3);
			Node tempNode3 = getChildAtIndex(t, 4);
			Node tempNode4 = new Node(Token.EXPR_RESULT);
			t.removeChild(tempNode1);
			t.getParent().addChildBefore(tempNode1, t);
			t.removeChild(tempNode2);
			tempNode4.addChildToFront(tempNode2);
			tempNode3.addChildToBack(tempNode4);
		}
		
		/*
		 * To seperate var
		 */
		if (t.isVar()) {
			for (Node p : t.children()) {
				Node tempNode1 = new Node(Token.VAR);
				t.removeChild(p);
				tempNode1.addChildToBack(p);
				t.getParent().addChildAfter(tempNode1, t);
				
				if (p.getFirstChild() != null && (t.getParent().isScript() || t.getParent().isBlock())) {
					Node tempNode2 = new Node(Token.ASSIGN);
					Node tempNode3 = Node.newString(p.getString());
					tempNode3.setType(Token.NAME);
					Node tempNode4 = p.getFirstChild();
					Node tempNode5 = new Node(Token.EXPR_RESULT);
					tempNode5.addChildToFront(tempNode2);
					tempNode2.addChildToFront(tempNode3);
					p.removeChild(tempNode4);
					tempNode2.addChildToBack(tempNode4);
					t.getParent().addChildAfter(tempNode5, tempNode1);
				}
			}
			t.getParent().removeChild(t);
		}
		
		/*
		 * To disapper comma
		 */
		if (t.isComma()) {
		}
		/*
		if (t.isFunction() && FunctionLayer > 0) {
			Node s = t.getParent(), s2 = t;
			while (!(s.isScript())) {
				s2 = s;
				s = s.getParent();
			}
			Node tempNode1 = Node.newString(Token.NAME, t.getFirstChild().getString());
			t.getParent().addChildToFront(tempNode1);
			t.getParent().removeChild(t);
			s.addChildBefore(t, s2);;
		}
		*/
		/*
		 * To remove var does not show directly in script/block
		 */
		/*
		if (t.isVar() && !(t.getParent().isScript() || t.getParent().isVar())) {
			Node s = t;
			while (!(s.isScript() || s.isFunction())) {
				s = s.getParent();
			}
		}*/
	}
	
	private void Renaming(Node t, WH_SymbolTable _sti, String Suffix) {
		WH_SymbolTable sti = _sti;
		System.err.println(t);
		if (t.isFunction()) {
			String tName = t.getFirstChild().getString();
			String TName = tName;
			while (sti.GetSuffixedSymbol(TName) != null) {
				TName = TName + "_";
			}
			sti.AddNewSymbol(tName, TName);
		}
		if (t.isParamList() || t.isVar()) {
			for (Node p : t.children()) {
				String tName = p.getString();
				String TName = tName;
				while (sti.GetSuffixedSymbol(TName) != null) {
					TName = TName + "_";
				}
				sti.AddNewSymbol(tName, TName);
			}
		}
		if (t.isName()) {
			String checkpoint = sti.GetSuffixedSymbol(t.getString());
			t.setString(checkpoint);
		}
		

		if (t.isBlock() || t.isFunction()) {
			sti = new WH_SymbolTable(sti);
		}
		/*
		 * Deal with suffix
		 */
		if (t.isBlock()) {
			Suffix = new String(Suffix + "___Block" + (sti.BlockCounter ++));
		}
		if (t.isFunction()) {
			Suffix = new String(Suffix + "___Function" + (sti.FuncCounter ++));
		}
		for (Node p : t.children()) {
			if (!(p.getLastSibling() == null) || !t.isFunction()) Renaming(p, sti, Suffix);
		}
	}
	/**
	 * To rebuild the AST
	 */
	public void process() {	
		Renaming(root, new WH_SymbolTable(), "");
		Rebuilding(root);
	}
}

class WH_SymbolTable {
	private HashMap<String, String> Symbols;
	public int BlockCounter, FuncCounter;
	
	public WH_SymbolTable() {
		Symbols = new HashMap<String, String>();
		BlockCounter = 0;
		FuncCounter = 0;
	}
	
	public WH_SymbolTable(WH_SymbolTable sti) {
		Symbols = new HashMap<String, String>(sti.Symbols);
		BlockCounter = sti.BlockCounter;
		FuncCounter = sti.FuncCounter;
	}
	
	/**
	 * Add new symbol to symbol table, if already exist such a symbol, replace it.
	 * @param Name - The name of the symbol
	 * @param SuffixedName - The suffixed name of symbol(The future name after variables modification).
	 */
	public void AddNewSymbol(String Name, String SuffixedName) {
		if (Symbols.get(Name) != null) {
			Symbols.remove(Name);
		}
		Symbols.put(Name, SuffixedName);
	}
	
	/**
	 * 
	 */
	public String GetSuffixedSymbol(String Name) {
		return Symbols.get(Name);
	}
}