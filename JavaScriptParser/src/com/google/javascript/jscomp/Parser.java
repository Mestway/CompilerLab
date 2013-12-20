package com.google.javascript.jscomp;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;

public class Parser {
	
	ConstantPropagation cpf;
	Boolean PRINT_FLOW = true;
	
	private static final String EXTERNS = "";
	
    static SourceFile fromEmptyCode() { 
            return fromCode("", new String("dummy.js"));
    }
    
    static SourceFile fromCode(String source, String fileName) {
        return SourceFile.fromCode(fileName, source);
    }

	public void process(String code)
	{	
		SourceFile src = SourceFile.fromCode("foo.js", code);
		//File file = new File("foo.js");
		//SourceFile src = SourceFile.fromFile(file);
		
		JsAst js = new JsAst(src);
		//System.out.println(src.getCode());
		
		Compiler compiler = new Compiler();
		
		compiler.setScope(js.getAstRoot(compiler));
		CompilerOptions options = new CompilerOptions();
		ControlFlowAnalysis cfgPass = new ControlFlowAnalysis(compiler,true,true);
		
		cfgPass.process(null, js.getAstRoot(compiler));
		ControlFlowGraph<Node> cfg = cfgPass.getCfg();
		
		if(PRINT_FLOW) {
			PrintFlowGraph pfg = new PrintFlowGraph(cfg);
			pfg.RecursivePrintGraph(cfg);
		}
		List<SourceFile> inputs = Lists.newArrayList(SourceFile.fromCode("foo.js",code));
		List<SourceFile> externs = Lists.newArrayList(SourceFile.fromCode("externs1", EXTERNS));

		compiler.compile(externs, inputs, options);
		SymbolTable symbol_table = compiler.buildKnownSymbolTable();
		
		cpf = new ConstantPropagation();
		cpf.init(cfg, symbol_table, src);
		cpf.process();
	}
}