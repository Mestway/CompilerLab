package com.google.javascript.jscomp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;

public class WH_AstPreworker_Tester {
private static final String EXTERNS = "";
	
    static SourceFile fromEmptyCode() { // TODO: why is this method needed??
            return fromCode("", new String("dummy.js"));
    }
    
    static SourceFile fromCode(String source, String fileName) {
        return SourceFile.fromCode(fileName, source);
    }

    private static void PrintAST(Node t, int depth) {
    	for (int i = 0; i < depth; i++) System.out.print("--");;
    	System.out.println(t.toString());
    	for (Node p : t.children()) PrintAST(p, depth + 1);
    }
    
	public static void main(String args[]) throws IOException
	{
		File file = new File("test1.js");
	
		SourceFile src = SourceFile.fromFile(file);
		
		JsAst js = new JsAst(src);
		//System.out.println(src.getCode());
		
		Compiler compiler = new Compiler();
		
		compiler.setScope(js.getAstRoot(compiler));
		CompilerOptions options = new CompilerOptions();
		ControlFlowAnalysis cfgPass = new ControlFlowAnalysis(compiler,true,true);
		
		cfgPass.process(null, js.getAstRoot(compiler));
		ControlFlowGraph<Node> cfg = cfgPass.getCfg();
		
		//System.out.println(cfg.getEntry().getValue());
		//System.out.println(cfg.getEntry().getOutEdges().get(0).getClass());
		
		
		WH_AstPreworker astpreworker = new WH_AstPreworker(cfg.getEntry().getValue());
		astpreworker.process();
		WH_CodeGenerator cg = new WH_CodeGenerator(cfg.getEntry().getValue());
		PrintAST(cfg.getEntry().getValue(), 0);
		System.out.println(cg.generate());
	
		Parser ps = new Parser();
		ps.process(cg.generate());
		
		System.out.println("#$%^%$#$%^%$#$%^%$#$%^%$#$%^%$#$%^%$#$%^&^%$#$%^&^%$#$%^&^%%#\n");
		
		WH_CodeGenerator cg2 = new WH_CodeGenerator(ps.cpf.getCFG().getEntry().getValue());
		System.out.println(cg2.generate());
	}
}
