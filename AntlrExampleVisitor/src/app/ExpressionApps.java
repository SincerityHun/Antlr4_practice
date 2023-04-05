package app;

import java.io.IOException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import antlr.ExprLexer;
import antlr.ExprParser;
import expression.AntlrToProgram;
import expression.ExpressionProcessor;
import expression.MyErrorListener;
import expression.Program;

public class ExpressionApps {
	public static void main(String[] args) {
		//1. check file name is given to input
		if(args.length!= 1) {
			System.err.print("Usage: file name");
		}
		else {
			String fileName = args[0];
			ExprParser parser = getParser(fileName);
			
			// 2. Tell Antlr to build a parse tree
			// parse from the start symbol 'prog'
			ParseTree antlrAST = parser.prog();
			
			if(MyErrorListener.hasError) {
				// let the syntax error be reported
			}
			else {
				// 3. Create a visitor for converting the parse tree into Program/Expression object
				// 이렇게 에러를 커스텀한 이유는 syntax error와 symantic error가 동시에 뜨게 하고싶지 않기 떄문이다.
				// 애초에 parsing단계에서 에러가 뜨면 바로 프로그램이 뻑가게하는게 훨씬 낫기에!
				// 중단점을 이용하는걸 확인해야한다.
				AntlrToProgram progVisitor = new AntlrToProgram(); //이게 끝난 뒤에 Parse tree 보고싶
				Program  prog = progVisitor.visit(antlrAST); //이게 끝난 뒤에 AST를 보고싶다.
				
				//4. parsing이 끝난뒤 에러가 있나 없나로 시
				if(progVisitor.semanticErrors.isEmpty()) {
					//4-1. parsing에 에러가 없다면 
					ExpressionProcessor ep = new ExpressionProcessor(prog.expressions);
					for(String evaluation: ep.getEvaluationResults()) {
						System.out.println(evaluation);
					}
				}
				else {
					for(String err: progVisitor.semanticErrors) {
						System.out.println(err);
					}
				}
			}
		}
	}
	//매번 새롭게 문법을 정의할때마다 antlr에서 lexer와 parser은 꼭 재정의해야한다.
	/*
	 * Here the types of parser and lexer are specific to the
	 * grammar name Expr.g4.
	 */
	private static ExprParser getParser(String fileName) {
		ExprParser parser = null;
		
		try {
			CharStream input = CharStreams.fromFileName(fileName);
			//1.Lexer 돌리기
			ExprLexer lexer = new ExprLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			//2. Parser 돌리기
			parser = new ExprParser(tokens);
			
			//3. syntax error handling
			parser.removeErrorListeners();
			parser.addErrorListener(new MyErrorListener());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return parser;
	}
}
