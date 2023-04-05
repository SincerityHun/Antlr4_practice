package expression;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

import antlr.ExprBaseVisitor;
import antlr.ExprParser.AdditionContext;
import antlr.ExprParser.DeclarationContext;
import antlr.ExprParser.MultiplicationContext;
import antlr.ExprParser.NumberContext;
import antlr.ExprParser.VariableContext;

// tree -> node implement!!@!
public class AntlrToExpression extends ExprBaseVisitor<Expression> {

	/*
	 * Given that all visit_* methods are called in a top-down fashion,
	 * we can be sure that the order in which we add declared variables in the 'vars' is
	 * identical to how they are declared in the input program.
	 * [node가 트리에서 어떻게 방문하니?]
	 * The way nodes are visited corresponds to the order of lines in input prog.
	 */
	private List<String> vars;// 이미 저장된 변수 기록 stores all the variables declared in the program so far.
	private List<String> semanticErrors; //1. Duplicate declaration, 2. reference to undeclared variable 
	//Note that semantic errors are different from syntax errors(의미적으로 틀린 에러, 문법적이 아닌)
	public AntlrToExpression(List<String> semanticErrors) {
		vars = new ArrayList<>();
		this.semanticErrors = semanticErrors;
	}
	
	
	@Override
	public Expression visitDeclaration(DeclarationContext ctx) {
		//ID() is a method generated to correspond to the token ID in the source grammar.
		Token idToken = ctx.ID().getSymbol(); //equivalent to: ctx.getChild(0).getSymbol()
		//g4파일에서 토큰의 ID토큰을 이용할 수 있다. 단 하나의 ctx에서 여러개의 ID토큰이 존재한다면 작동 안함 ㅇㅇ
		int line = idToken.getLine();
		int column = idToken.getCharPositionInLine() + 1; //토큰에서 ID위치
		String id = ctx.getChild(0).getText();
		// Maintaining the vars list for semantic error reporting
		if(vars.contains(id)) {
			semanticErrors.add("Error: variable "+id+" already declared ("+line+", "+ column + ")");
		}
		else {
			vars.add(id);
		}
		
		String type = ctx.getChild(2).getText();
		int value = Integer.parseInt(ctx.NUM().getText()); 
		
		return new VariableDeclaration(id,type, value);
	}

	@Override
	public Expression visitMultiplication(MultiplicationContext ctx) {
		//Context가 이미 Multiplication이니 operator을 따로 확인할 필요는 없다.
		//1. 전체 parse tree를 recursive 하게 visit하는 함수를 antlr에서 사용할 수 있다.
		Expression left = visit(ctx.getChild(0)); //Recursively visit the left subtree of the current Multiplication node
		Expression right = visit(ctx.getChild(2));
		return new Multiplication(left,right);
	}

	@Override
	public Expression visitAddition(AdditionContext ctx) {
		//Context가 이미 addition이니 operator을 따로 확인할 필요는 없다.
		//Expression left = ctx.getChild(0); 가 불가능, 왜? parse tree전체를 child하나로 묶을 수 없기 떄문이다.
		//1. 전체 parse tree를 recursive 하게 visit하는 함수를 antlr에서 사용할 수 있다.
		Expression left = visit(ctx.getChild(0)); //Recursively visit the left subtree of the current Multiplication node
		Expression right = visit(ctx.getChild(2));
		return new Addition(left,right);
	}

	@Override
	public Expression visitVariable(VariableContext ctx) {
		//이미 variable이 존재하는지 어떻게 알지?
		//가능한 에러, 1. 이미 존재하지 않은 variable call, 2.
		//parsing할때 handling할 수 있다.
		Token idToken = ctx.ID().getSymbol(); //symbol을 따면 라인과 콜럼을 딸 수 있다.
		int line = idToken.getLine();
		int column = idToken.getCharPositionInLine() + 1;
		
		String id = ctx.getChild(0).getText();
		if(!vars.contains(id)) {
			semanticErrors.add("Error: variable "+id+" not declared ("+line+", "+ column + ")");
		}
		return new Variable(id);
	}

	@Override
	public Expression visitNumber(NumberContext ctx) {
		// getChild index는 띄어쓰기 기준?
		String numText = ctx.getChild(0).getText();
		int num = Integer.parseInt(numText);
		return new Number(num);
	}
	
}
