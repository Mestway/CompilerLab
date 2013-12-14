package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;

public class WH_AstPreworker {
	
	private Node root;
	
	public WH_AstPreworker(Node _root) {
		root = _root;
	}
	
	private void Rebuilding(Node t, boolean inAssign) {
		if (t.isAssign() && !inAssign) {
			for (Node i : t.children()) Rebuilding(i, true);
		}
		else
		if (t.isAssign() && inAssign) {
			Node s = t, s2;
			while (!s.isBlock() || s.isScript()) {
				s2 = s;
				s = s.getParent();
			}
			Node temp = new Node(t.getType());
			t.getParent().removeChild(t);
			//s.addChildBefore(t,);
		}
	}
	
	/**
	 * To rebuild the AST
	 */
	public void process() {
		Rebuilding(root, false);
	}
}
