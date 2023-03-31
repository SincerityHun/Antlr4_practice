grammar Expr;

/*grammar name and file name must match*/

@header {
	package antlr;
}

// Start Variable, labeling 하기 #으로
prog: (decl | expr)+ EOF	# Program
	;
	
decl: ID ':' INT_TYPE '=' NUM	# Declaration
	;
	
/*Antlr resolves ambiguities in favor of the alternative given first. */
expr: expr '*' expr			# Multiplication
	| expr '+' expr			# Addition
	| ID					# Variable
	| NUM					# Number
	;
	
/* Token */
ID : [a-z][a-zA-Z0-9_]*; //Identifiers
NUM : '0' | '-'?[1-9][0-9]*;
INT_TYPE : 'INT';
COMMENT : '--' ~[\r\n]* -> skip;
WS : [ \t\n]+ -> skip;