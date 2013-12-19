package com.google.javascript.jscomp;

import java.util.ArrayList;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class FuncVal {
	
	private String func_name;
	private String ret_type;
	private String ret_value;
	
	private Node f_node; //the node of FUNCTION type 
	@SuppressWarnings("unused")
	private Node start_node; // first block node;
	private Compiler f_compiler;
	private ControlFlowAnalysis f_cfa;
	private ControlFlowGraph<Node> f_cfg;
	private SymbolTable f_symbolTable;
	private SourceFile f_src;
	
	private boolean PRINT_FUNC = false;
	
	private ArrayList<VarVal> arguList = new ArrayList<VarVal>();
	
	FuncVal(Node function, SymbolTable st, SourceFile src)
	{
		f_node = function;
		f_symbolTable = st;
		f_src = src;
		
		decompose();
		process();
		
		return;
	}
	
	public String getRetType()
	{
		return ret_type;
	}
	public String getRetValue()
	{
		return ret_value;
	}
	public String getName()
	{
		return func_name;
	}
	public ArrayList<VarVal> getArgus()
	{
		return arguList;
	}
	
	public void process()
	{
		f_compiler = new Compiler();
		f_compiler.setScope(f_node);
		f_cfa = new ControlFlowAnalysis(f_compiler,true,true);
		f_cfa.process(null, f_node);
		f_cfg = f_cfa.getCfg();
		
		ConstantPropagation f_cpf = new ConstantPropagation();
		f_cpf.init(f_cfg, f_symbolTable, f_src);
		f_cpf.process();
		
		ret_type = f_cpf.getRetValue().getType();
		ret_value = f_cpf.getRetValue().getValue();
		
		if(PRINT_FUNC) {
			System.out.println("#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%");
			PrintFlowGraph f_pfg = new PrintFlowGraph(f_cfg);
			f_pfg.RecursivePrintGraph(f_cfg);
			System.out.println("#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%#$%");
		}
	}
	
	private void decompose()
	{
		for(Node i : f_node.children())
		{
			switch(i.getType())
			{
			case Token.NAME:
				func_name = i.getString();
				break;
			case Token.PARAM_LIST:
				for(Node j : i.children())
				{
					arguList.add(new VarVal(j.getString()));
				}
				break;
			case Token.BLOCK:
				start_node = i;
				break;
			}
		}
	}
	
	public final String toString()
	{
		return ret_value.toString() + " & " + ret_type.toString();
	}
}
