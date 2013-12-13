package com.google.javascript.jscomp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;

public class Parser {
	
	private static final String EXTERNS = "";
	
    static SourceFile fromEmptyCode() { // TODO: why is this method needed??
            return fromCode("", new String("dummy.js"));
    }
    
    static SourceFile fromCode(String source, String fileName) {
        return SourceFile.fromCode(fileName, source);
    }

	public static void main(String args[]) throws IOException
	{
		File file = new File("foo.js");
	
		SourceFile src = SourceFile.fromFile(file);
		//System.out.println(src.getName());
		
		JsAst js = new JsAst(src);
		System.out.println(src.getCode());
		
		Compiler compiler = new Compiler();
		
		compiler.setScope(js.getAstRoot(compiler));
		CompilerOptions options = new CompilerOptions();
		ControlFlowAnalysis cfgPass = new ControlFlowAnalysis(compiler,true,true);
		
		cfgPass.process(null, js.getAstRoot(compiler));
		ControlFlowGraph<Node> cfg = cfgPass.getCfg();
		
		//System.out.println(cfg.getEntry().getValue());
		//System.out.println(cfg.getEntry().getOutEdges().get(0).getClass());
		
		PrintFlowGraph pfg = new PrintFlowGraph(cfg);
		pfg.RecursivePrintGraph(cfg);
		
		List<SourceFile> inputs = Lists.newArrayList(SourceFile.fromFile(file));
		List<SourceFile> externs = Lists.newArrayList(SourceFile.fromCode("externs1", EXTERNS));

		compiler.compile(externs, inputs, options);
		SymbolTable symbol_table = compiler.buildKnownSymbolTable();
		
		/*for(Symbol i : symbol_table.getAllSymbols())
		{
			if(!i.inExterns())
			{	
				System.err.println(i);
			}
		}
		for(SymbolScope i : symbol_table.getAllScopes())
		{
			System.out.println(i.getSymbolForScope() + i.toString());
		}*/
		
		ConstantPropagation cpf = new ConstantPropagation();
		cpf.init(cfg, symbol_table);
		cpf.process();
	}
}