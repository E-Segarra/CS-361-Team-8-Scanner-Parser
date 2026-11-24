// ConcreteSyntax.java

// Implementation of the Recursive Descent Parser algorithm

//  Each method corresponds to a concrete syntax grammar rule, 
//  which appears as a comment next to the beginning of the method.

public class ConcreteSyntax {
	
	// Instance variables
	public Token token; // current token that is considered from the input stream
	public TokenStream input; // stream of tokens for supplying tokens to the recursive descent parser, generated in by the lexical analysis.

	// Constructor
	public ConcreteSyntax(TokenStream ts) { // Open the source program
		input = ts; // as a TokenStream, and
		token = input.nextToken(); // retrieve its first Token
	}

	// Method that prints a syntax error message
	private String SyntaxError(String tok) {
		String s = "Syntax error - Expecting: " + tok + " But saw: "
				+ token.getType() + " = " + token.getValue();
		System.out.println(s);
		return s;
		// System.exit(0);
	}

	// Match a string with the value of a token. If no problem, go to the next
	// token otherwise generate an error message
	private void match(String s) {
		if (token.getValue().equals(s))
			token = input.nextToken();
		else
			throw new RuntimeException(SyntaxError(s));
	}

	// Implementation of the Recursive Descent Parser
	public Program program() { // Program --> void main() '{' Declarations Statements '}'

		//COMPLETED
		String[] header = {"void", "main", "(", ")", "{"}; // header of the program
		Program p = new Program();

        for (int i = 0; i < header.length; i++) 
            match(header[i]);

		// call methods, execute them, and store their returned results.
   		p.decpart = declarationS();
    	p.body = statements();

   	 	match("}");
				if (!input.isEndofFile() || !token.getValue().equals(""))
			throw new RuntimeException(SyntaxError("end of file"));
	return p;
	}

	private Declarations declarationS() { // Declarations --> { Declaration }*
		
		//COMPLETED 
		// if the declarion has multiples forms but reserves the token structure:
		Declarations ds = new Declarations();
		while (token.getValue().equals("int")
				|| token.getValue().equals("boolean")) {
			declaration(ds);
		}
		return ds;
	}

	private void declaration(Declarations ds) { // Declaration --> Type Identifiers ;

		Type t = type();
		identifiers(ds, t);
		match(";");
	}

	private Type type() { // Type --> int | boolean
		
		// COMPLETED
		Type t = null;

		if (token.getValue().equals("int"))
			t = new Type(token.getValue()); //value "int" gets stored inside a new Type object, and the variable t holds a reference to that object.
		else if (token.getValue().equals("boolean"))
			t = new Type(token.getValue());

		else
			throw new RuntimeException(SyntaxError("int | boolean"));

		token = input.nextToken(); // moves the parser to the next token in the source code.
		return t;
	}

	private void identifiers(Declarations ds, Type t) { // Identifiers --> Identifier { , Identifier }*
		
		Declaration d = new Declaration(); // first declaration

		d.t = t; //assigning the previously recognized type to the declaration.

		if (token.getType().equals("Identifier")) {
			d.v = new Variable(); 
			d.v.id = token.getValue(); // stores the variable's name 
			ds.addElement(d);

			token = input.nextToken();

			while (token.getValue().equals(",")) {
				d = new Declaration(); // next declaration
				d.t = t; // to assign the same type we saved earlier.
				token = input.nextToken();

				if (token.getType().equals("Identifier")) {
					d.v = new Variable(); 
					d.v.id = token.getValue(); //stores the current identifier’s name ("y") inside that object.
					ds.addElement(d); //adds the curretn declaration to the ds stream
					token = input.nextToken(); // get "," or ";"
				} else
					throw new RuntimeException(SyntaxError("Identifier"));
			}
		} else
			throw new RuntimeException(SyntaxError("Identifier"));
	}

	private Statement statement() { // Statement --> ; | Block | Assignment | IfStatement | WhileStatement

		Statement s = new Skip();
		if (token.getValue().equals(";")) { // Skip
			token = input.nextToken();
			return s;
		} else if (token.getValue().equals("{")) { // Block
			token = input.nextToken();
			s = statements();
			match("}");
		} else if (token.getValue().equals("if")) // IfStatement
			s = ifStatement();

			//COMPLETED
		else if (token.getValue().equals("while")) {
			s = whileStatement();
		} else if (token.getType().equals("Identifier")) { // Assignment
			s = assignment();
		} else
			throw new RuntimeException(SyntaxError("Statement"));
		return s;
	}

	private Block statements() { // Block --> '{' Statements '}'
		
		Block b = new Block();
		while (!token.getValue().equals("}")) { //continue parsing until the current token is "}"
			b.blockmembers.addElement(statement()); //parses one statement from the token stream and returns a Statement object.
		}
		return b; //block with all its parsed statements
	}

	private Assignment assignment() { // Assignment --> Identifier := Expression ;
		
		Assignment a = new Assignment();

		//COMPLETED
		if (token.getType().equals("Identifier")) {
			a.target = new Variable();
			a.target.id = token.getValue();
			token = input.nextToken();
			match("=");
			a.source = expression(); //Parse the expression and store its returned AST
			match(";");
		} else
			throw new RuntimeException(SyntaxError("Identifier"));
		return a;
	}

	private Conditional ifStatement() { // IfStatement --> if ( Expression ) Statement { else Statement }opt
		
		//COMPLETED
		Conditional c = new Conditional();
		match("if");
		match("(");

		c.test = expression(); // parse condition() and store its AST
		match(")"); 

		c.thenbranch = statement(); // parse the statement for the true branch
		if (token.getValue().equals("else")) { // check whether optional else exists
			match("else");
			c.elsebranch = statement(); // Parse the false branch
		}

    return c;
	}

	private Loop whileStatement() { // WhileStatement --> while ( Expression ) Statement

		//COMPLETED
		Loop l = new Loop();
		match("while");
		match("(");

		l.test = expression();
		match(")");

		l.body = statement();
		return l;
	}

	private Expression expression() { // Expression --> Conjunction { || Conjunction }*
		
		Binary b;
		Expression e;

		e = conjunction();

		while (token.getValue().equals("||")) {
			b = new Binary();
			b.term1 = e; //attach the previous AST as the left child of the new node

			b.op = new Operator(token.getValue()); //represents the || operator in the AST

			token = input.nextToken();//moves past || to the next token 
			b.term2 = conjunction(); //parses the next right conjuction() as the right operand 
			e = b; //update e to reference the new root of the AST constructed so far
		}
		return e; //root returned 
	}

	private Expression conjunction() { // Conjunction --> Relation { && Relation }*
		
		Binary b;
		Expression e;

		e = relation();

		//COMPLETED
		while (token.getValue().equals("&&")) {
			b = new Binary();
			b.term1 = e; //attach the previous AST as the left child of the new node

			b.op = new Operator(token.getValue()); //stores the && operator in the AST

			token = input.nextToken();
			b.term2 = relation(); //parse the next relation() as the right operand
			e = b; // updates e to reference the root of the complete AST.
		}
		return e;
	}

	private Expression relation() { // Relation --> Addition [ < | <= | > | >= | == | != ] Addition }*
		
		Binary b;
		Expression e;

		e = addition();

		//COMPLETED
		while (token.getValue().equals("<") 
				|| token.getValue().equals("<=")
				|| token.getValue().equals(">")
				|| token.getValue().equals(">=")
				|| token.getValue().equals("==")
				|| token.getValue().equals("!=")) {
			b = new Binary();
			b.term1 = e;

			b.op = new Operator(token.getValue()); //store the relational operator

			token = input.nextToken();
			b.term2 = addition();
			e = b;
		}
		return e; //return the completed AST
	}

	private Expression addition() { // Addition --> Term { [ + | - ] Term }*
		
		Binary b;
		Expression e;

		e = term();

		//COMPLETED
		while (token.getValue().equals("+") || token.getValue().equals("-")) {
			b = new Binary();
			b.term1 = e;

			b.op = new Operator(token.getValue());

			token = input.nextToken();
			b.term2 = term();
			e = b;
		}
		return e;
	}

	private Expression term() { // Term --> Negation { [ '*' | / ] Negation }*
		
		Binary b;
		Expression e;

		e = negation();

		//COMPLETED 
		while (token.getValue().equals("*") || token.getValue().equals("/")) {
			b = new Binary();
			b.term1 = e;

			b.op = new Operator(token.getValue()); //store the * or / operator
				
			token = input.nextToken();
			b.term2 = negation();
			e = b;
		}
		return e;
	}

	private Expression negation() { // Negation --> { ! }opt Factor
		
		Unary u;
		if (token.getValue().equals("!")) { //check whether the negation operator exists
			u = new Unary();
			u.op = new Operator(token.getValue()); //store the ! operator in the AST

			token = input.nextToken();
			u.term = factor(); //parses the factor and attach its AST as the operand
			return u; 
		} else
			return factor(); // if no negation exists, return the factor() AST directly
	}

	private Expression factor() { // Factor --> Identifier | Literal | ( Expression )
		
		Expression e = null; 

		if (token.getType().equals("Identifier")) {

			Variable v = new Variable(); // create a Variable AST node
			v.id = token.getValue(); //store the identifier's name
			e = v;
			token = input.nextToken();

		} else if (token.getType().equals("Literal")) {
			Value v = null; 

			if (isInteger(token.getValue()))
				v = new Value((new Integer(token.getValue())).intValue()); //convert the token into an integer Value

			else if (token.getValue().equals("true"))
				v = new Value(true);
			else if (token.getValue().equals("false"))
				v = new Value(false);
			else
				throw new RuntimeException(SyntaxError("Literal"));
			e = v;
			token = input.nextToken();

		} else if (token.getValue().equals("(")) {
			token = input.nextToken();
			e = expression(); //recursively parses the expression and store its AST
			match(")");

		} else
			throw new RuntimeException(SyntaxError("Identifier | Literal | ("));
		return e;
	}

	private boolean isInteger(String s) {
		boolean result = !s.isEmpty(); //rejects empty strings

		for (int i = 0; i < s.length(); i++)
			if ('0' > s.charAt(i) || '9' < s.charAt(i))
				result = false;

		return result;
	}
}
