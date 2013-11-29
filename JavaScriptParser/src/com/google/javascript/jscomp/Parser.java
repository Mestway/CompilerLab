package com.google.javascript.jscomp;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.SymbolTable.Symbol;
import com.google.javascript.jscomp.SymbolTable.SymbolScope;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.jscomp.graph.LinkedDirectedGraph;
import com.google.javascript.rhino.Node;

public class Parser {
	 /**
     * Empty JavaScript code snippet; used by the Rhino parser.
     */
	static final String ACTIVE_X_OBJECT_DEF =
		      "/**\n" +
		      " * @param {string} progId\n" +
		      " * @param {string=} opt_location\n" +
		      " * @constructor\n" +
		      " * @see http://msdn.microsoft.com/en-us/library/7sw4ddf8.aspx\n" +
		      " */\n" +
		      "function ActiveXObject(progId, opt_location) {}\n";
	
  static final String DEFAULT_EXTERNS =
	      "/** @constructor \n * @param {*=} opt_value */ " +
	      "function Object(opt_value) {}" +
	      "/** @constructor \n * @param {*} var_args */ " +
	      "function Function(var_args) {}" +
	      "/** @type {!Function} */ Function.prototype.apply;" +
	      "/** @type {!Function} */ Function.prototype.bind;" +
	      "/** @type {!Function} */ Function.prototype.call;" +
	      "/** @constructor \n * @param {*=} arg \n @return {string} */" +
	      "function String(arg) {}" +
	      "/** @param {number} sliceArg */\n" +
	      "String.prototype.slice = function(sliceArg) {};" +
	      "/** @type {number} */ String.prototype.length;" +
	      "/** @constructor \n * @param {*} var_args \n @return {!Array} */" +
	      "function Array(var_args) {}\n" +
	      "/** @type {number} */ Array.prototype.length;\n" +
	      "/**\n" +
	      " * @param {...T} var_args\n" +
	      " * @return {number} The new length of the array.\n" +
	      " * @this {{length: number}|Array.<T>}\n" +
	      " * @template T\n" +
	      " * @modifies {this}\n" +
	      " */\n" +
	      "Array.prototype.push = function(var_args) {};" +
	      "/** @constructor */\n" +
	      "function Arguments() {}\n" +
	      "/** @type {number} */\n" +
	      "Arguments.prototype.length;\n" +
	      "/** @type {!Arguments} */\n" +
	      "var arguments;" +
	      "" + ACTIVE_X_OBJECT_DEF;
	
	private static final String EXTERNS = DEFAULT_EXTERNS + "\nfunction customExternFn(customExternArg) {}";

	
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
		//pfg.RecursivePrintGraph(cfg);
		
		List<SourceFile> inputs = Lists.newArrayList(SourceFile.fromFile(file));
		List<SourceFile> externs = Lists.newArrayList(SourceFile.fromCode("externs1", EXTERNS));
		

		compiler.compile(externs, inputs, options);
		SymbolTable symbol_table = compiler.buildKnownSymbolTable();
		for(Symbol i : symbol_table.getAllSymbols())
		{
			if(!i.inExterns())
			{	
				System.err.println(i);
			}
		}
		for(SymbolScope i : symbol_table.getAllScopes())
		{
			System.out.println(i.getSymbolForScope() + i.toString());
		}
		
		ConstantPropagation cpf = new ConstantPropagation();
		cpf.init(cfg, symbol_table);
	}
}
