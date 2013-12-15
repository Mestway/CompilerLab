package com.google.javascript.jscomp;

import java.util.ArrayList;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class WH_AstPreworker {
	
	private Node root;
	private int TempCounter = 0;
	
	public WH_AstPreworker(Node _root) {
		root = _root;
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
		if (isAssignFamily(t) && !t.getParent().isExprResult() && !t.getParent().isVar()) {
			Node s = t, s2 = t;
			ArrayList<Node> ancestors = new ArrayList<Node>();  
			while (!(s.isBlock() || s.isScript())) {
				s2 = s;
				s = s.getParent();
				ancestors.add(s);
			}
			for (int i = ancestors.size() - 2; i > 0; i--) {
				Node father = ancestors.get(i);
				Node son = ancestors.get(i - 1);
				while (!father.getFirstChild().isEquivalentTo(son)) {
					Node tempNode1 = new Node(Token.VAR);
					Node tempNode2 = new Node(Token.NAME);
					tempNode2.setString("WH_TemplateVariable_" + (TempCounter ++));
					tempNode1.addChildToBack(tempNode2);
					tempNode2.addChildToBack(father.getFirstChild());
					s.addChildBefore(tempNode1,s2);
					father.removeFirstChild();
				}
			}
			Node tempNode3 = Node.newString(Token.NAME, t.getFirstChild().getQualifiedName());
			t.getParent().addChildAfter(tempNode3, t);
			t.getParent().removeChild(t);
			Node checkpoint = t.getParent();
			s.addChildBefore(t, s2);
		}
		else {
			for (Node i : t.children()) Rebuilding(i);
		}
	}
	
	/**
	 * To rebuild the AST
	 */
	public void process() {
		Rebuilding(root);
	}
}
