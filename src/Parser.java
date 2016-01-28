import java.util.HashMap;
import java.util.Map;

/* 		OBJECT-ORIENTED RECOGNIZER FOR SIMPLE EXPRESSIONS
expr    -> term   (+ | -) expr | term
term    -> factor (* | /) term | factor
factor  -> int_lit | '(' expr ')'     
*/

public class Parser {
	public static void main(String[] args) {
		System.out.println("Enter an expression, end with semi-colon!\n");
		Lexer.lex();
		new Program();
		Code.output();
		
	}
}

//program -> decls stmts end
class Program{
	
	Stmts stms;
	Decls d;

	public Program(){
	//	Lexer.lex();
		d = new Decls();

	//	Lexer.lex();
		stms = new Stmts();
		
		System.out.println(Lexer.nextToken);
		if(Token.KEY_END == Lexer.nextToken){
			Code.code[Code.codeptr]= "return";
			Code.codeptr++;
		}
	//	Code.output();
	}
}

// decls   ->  int idlist ';'
class Decls {
	
	// Check for int?
	Idlist idl;
	public Decls(){
		
		System.out.println(Lexer.nextToken);
		
		Lexer.lex();
		idl = new Idlist();
		if(Lexer.nextToken != Token.SEMICOLON)
			Lexer.error("missing semicolon");
		Lexer.lex();
	}
}


// stmts   ->  stmt [ stmts ]
		
class Stmts{
	Stmt st;
	Stmts sts;
	public Stmts(){
		st = new Stmt();
		if(Lexer.nextToken!=Token.KEY_END && Lexer.nextToken != Token.RIGHT_BRACE)
		{
			sts = new Stmts();
		}
		
		
		
	}
}

//stmt -> assign ';'| cmpd | cond | loop
class Stmt{
	Assign ass;
	Cmpd cmp;
	Cond con;
	Loop lop;
	
	
	//stmt    ->  assign ';'| cmpd | cond | loop
	public Stmt(){
	    switch(Lexer.nextToken){
    	case Token.LEFT_BRACE:
	    	cmp = new Cmpd();
    	    break;
	    case Token.KEY_IF:
	        con = new Cond();
    	    break;
	    case Token.KEY_FOR:
    		lop = new Loop();
    		break;
	    case Token.ID: // Assign value to identifier
	    {
        	ass = new Assign();
        	Lexer.lex(); //skipping through semicolon
        	break;
        }
	    default:
	    	break;
    	}
	}
}
// idlist  ->  id [',' idlist ]

class Idlist{
	
	char id; 
	Idlist idl;
	static Map<Character, Integer> variableValue = new HashMap<Character,Integer>();
	static int i = 1;
	
	public Idlist()
	{

	//	System.out.println(Lexer.ident);
		variableValue.put(Lexer.ident, i);
		i=i+1;
		// character
		Lexer.lex();
		if(Lexer.nextToken == Token.COMMA)
		{
			Lexer.lex();
			idl = new Idlist();
		}
	}
}

class Loop {
	Assign ass1,ass2;
	Rexp rex;
    Stmt s;
    int codePtr;
    int codePtr1;
    String[] localstore = new String[10];
    Boolean flag = false ;
    public Loop(){
    	Lexer.lex();
    	if(Lexer.nextToken != Token.LEFT_PAREN)
    		Lexer.error("missing left parenthesis");
    	Lexer.lex();
    	
    	
    	if(Lexer.nextToken != Token.SEMICOLON)
    	{
    		
    		ass1 = new Assign();
    	}
    	codePtr = Code.codeptr;
    	Lexer.lex();
    	if(Lexer.nextToken != Token.SEMICOLON)
    	{
    		rex = new Rexp();
    	}
    	
		Lexer.lex();
		
		
		
		
    	if(Lexer.nextToken != Token.RIGHT_PAREN)
    	{
    		flag = true;
    		codePtr1 = Code.codeptr;
    		ass2 = new Assign();
    		for(int i= 0 ; i<=3; i++){
    			localstore[i]= Code.code[codePtr1];
    			codePtr1 +=1;
    		}
    	Code.codeptr -=4;
    	}
    	Lexer.lex();
    	
    	s = new Stmt();
    	
    	if(true==flag)
    	{
    		for(int i=0 ; i<=3 ; i++)
    		{
    			Code.code[Code.codeptr]=localstore[i];
    			Code.codeptr++;
    		}
    	}
    	
    	Code.gen(Code.genGoto());
    	Code.code[rex.ptrLocation] += Code.codeptr;
    	
    	Code.code[Code.codeptr-3] += codePtr;
    	
    	}
}

// cond    ->  if '(' rexp ')' stmt [ else stmt ]

class Cond{
	Rexp r;
	Stmt s1,s2;
	int ptrLocation;
	public Cond(){
		if(Lexer.nextToken == Token.KEY_IF)///codeptr = 10
		{
			Lexer.lex();
			if(Lexer.nextToken == Token.LEFT_PAREN)
			{
				Lexer.lex();
				r = new Rexp();
				if(Lexer.nextToken != Token.RIGHT_PAREN)
				{
					Lexer.error("right enclosing Parenthesis missing");
				}
				Lexer.lex();
			}
			s1 = new Stmt();
			if(Lexer.nextToken == Token.SEMICOLON)
				Lexer.lex();
			
			if(Lexer.nextToken == Token.KEY_ELSE)
			{
				ptrLocation = Code.codeptr;
				Code.gen(Code.genGoto());
				Code.code[r.ptrLocation] += Code.codeptr;
				
				Lexer.lex();
				s2 = new Stmt();
				Code.code[ptrLocation] += Code.codeptr;
			}//code == 20
			else
			{
				Code.code[r.ptrLocation] += Code.codeptr;
				}
			
			
		}
		
		//code[local_codeptr_if+1]=21 
	}
}
// i = 5;

//    assign  ->  id '=' expr
class Assign   {
	char id;
	Expr e;
	
	public Assign()  {
		id= Lexer.ident;
		Lexer.lex(); // skipping through identifier
		Lexer.lex();// skipping through =
		e = new Expr(); // solving expression for Assignment
		Code.gen(Code.store(Idlist.variableValue.get(id)));
	}
}

// cmpd    ->  '{' stmts '}'
class Cmpd   {
	Stmts s;
	public Cmpd()  {
		if(Lexer.nextToken == Token.LEFT_BRACE)
		{
			Lexer.lex();
			s = new Stmts();
			if(Lexer.nextToken != Token.RIGHT_BRACE)
				Lexer.error("Missing Right Brace");
			Lexer.lex();
		}
	}
}

class Expr   { // expr -> term (+ | -) expr | term
	Term t;
	Expr e;
	char op;

	public Expr() {
		t = new Term();
		if (Lexer.nextToken == Token.ADD_OP || Lexer.nextToken == Token.SUB_OP) {
			op = Lexer.nextChar;
			Lexer.lex();
			e = new Expr();
			Code.gen(Code.opcode(op));	 
		}
	}
}

class Term    { // term -> factor (* | /) term | factor
	Factor f;
	Term t;
	char op;

	public Term() {
		f = new Factor();
		if (Lexer.nextToken == Token.MULT_OP || Lexer.nextToken == Token.DIV_OP) {
			op = Lexer.nextChar;
			Lexer.lex();
			t = new Term();
			Code.gen(Code.opcode(op));
			}
	}
}

class Factor { // factor -> number | '(' expr ')' | ID
	Expr e;
	int i;

	public Factor() {
		switch (Lexer.nextToken) {
		case Token.INT_LIT: // number
			i = Lexer.intValue;
			Code.gen(Code.intcode(i));
			Lexer.lex();
			break;
		case Token.LEFT_PAREN: // '('
			Lexer.lex();
			e = new Expr();
			Lexer.lex(); // skip over ')'
			break;
		case Token.ID:
			Code.gen(Code.load(Idlist.variableValue.get(Lexer.ident)));
			Lexer.lex();
			break;
		default:
			break;
		}
	}
}

// rexp -> expr('<'|'>'|'=='|'!=')expr
class Rexp{
	Expr e1,e2;
	int op;
	public int ptrLocation;
	public Rexp()
	{
		e1 = new Expr();
		op = Lexer.nextToken;
		Lexer.lex();
		e2 = new Expr();
		
		ptrLocation = Code.codeptr;
		Code.gen(Code.opcode(op));
		
	}
}

class Code {
	static String[] code = new String[100];
	static int codeptr = 0;
	
	public static void gen(String s) {
		code[codeptr] = s;
		codeptr++;
		if(s.contains("bipush") || s.contains("iload ") || s.contains("istore ")){
			codeptr= codeptr + 1;
		}
		if(s.contains("sipush") || s.contains("if_icmple") 
				|| s.contains("if_icmpge") || s.contains("if_icmpne") 
				|| s.contains("if_icmpeq") || s.contains("goto"))
		{
			codeptr = codeptr + 2;
		}
		
	}
	
	public static String genGoto() {
		return "goto ";
	}

	public static String load(Integer integer) {
		if(integer<=3)
		{
			return "iload_"+integer;
		}
		return "iload " + integer;
		
	}

	public static String store(Integer integer) {
		if(integer<=3)
		{
			return "istore_"+integer;
		}
		return "istore " + integer;
	}

	public static String intcode(int i) {
		if (i > 127) return "sipush " + i;
		if (i > 5) return "bipush " + i;
		return "iconst_" + i;
	}
	
	public static String opcode(char op) {
		switch(op) {
		case '+' : return "iadd";
		case '-':  return "isub";
		case '*':  return "imul";
		case '/':  return "idiv";
		default: return "";
		}
	}
	
	public static String opcode(int t){
		switch(t){
		case Token.GREATER_OP : return "if_icmple ";
		case Token.LESSER_OP : return "if_icmpge ";
		case Token.EQ_OP : return "if_icmpne ";
		case Token.NOT_EQ : return "if_icmpeq ";
		default: return "";
		}
	}
	
	public static void output() {
		for (int i=0; i<codeptr; i++)
		{
			if(code[i] == null) continue;
			System.out.println(i + ":" + code[i]);} 
		}
	}
