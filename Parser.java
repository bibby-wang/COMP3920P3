// The University of Newcastle
// School of Electrical Engineering and Computing
// COMP3290 Compiler Design
// Semester 2, 2019
// Project Part 3 A Recursive-Descent Parser for CD19 (15%) 
// Due: September 27th 
// Binbin.Wang C3214157
// 
// Parser LL1
//
// TreeNode  Call is: TreeNode.printTree(outfile, rootOfTree);

public class Parser {
	private Scanner scanner;  // scan and get token
	private Token currentToken; // Current Token
	//private TreeNode theNode;
	private Token followToken;////next follow token
	private SymbolTable symbolTable;
	private String errorList;
	public Parser(Scanner scanner){
		this.scanner = scanner;
		symbolTable = new SymbolTable(null);
		errorList = null;
	}
	
	// all errors
	public String getErrorList(){
		return errorList;
	}
	
	
	// check each token for errors and return the appropriate information.
	private boolean checkToken(int expected, String message){
		//test
		System.out.println("===cT: " + currentToken.value());
		
		if(currentToken.value() != expected){	
			if(currentToken.value() == Token.TUNDF){
				//the undefined token TUNDF
				System.out.println("=Error(Lexical): " + currentToken.getStr());
				errorList+="=Error(Lexical): " + currentToken.getStr();// lexeme

			}else{
				// Syntax 
				System.out.println("=Error(Syntax): " + message);
				errorList+="=Error(Syntax): "+ message;

			}
			// location of the error in source code
			errorList+=" [Line: "+ currentToken.getLn()// line
						+" Column: "+ currentToken.getPos()// column 
						+"].\r\n";
			return false;
		}
		return true;
	}
	//start!!
	public TreeNode getSyntaxTree(){
		return program();
	}
	// NPROG
	// <program> ::= CD19 <id> <globals> <funcs> <mainbody>
	public TreeNode program(){
		
		
		TreeNode node = new TreeNode(TreeNode.NPROG);

		//need CD19 token
		currentToken = scanner.getToken(); 
		followToken = scanner.getToken();
		if(!checkToken(Token.TCD19, "Not found the Keyword 'CD19'")) return null;
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
		//need TIDEN token
		if(!checkToken(Token.TIDEN, "Invalid initialisation: Not found ID name.")) return null;
		StRec stRec = new StRec();			
		stRec.setName(currentToken.getStr());
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
		

		//Add to symbol table
		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);

		node.setLeft(globals());
		node.setMiddle(funcs());
		node.setRight(mainbody());
		
		return node;

	}

	// NGLOB 
	// <globals> ::= <consts> <types> <arrays>
	private TreeNode globals(){
		
		TreeNode node = new TreeNode(TreeNode.NGLOB);
		node.setLeft(consts());
		node.setMiddle(types());
		node.setRight(arrays());

		return node;

	}
	
	// <consts> ::= constants <initlist> | ε
	private TreeNode consts(){
		
		if(currentToken.value() != Token.TCONS) return null;
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();

		return initlist();
	}

	//NILIST
	//<initlist> ::= <init> <initlistb>
	private TreeNode initlist(){
		TreeNode node = new TreeNode(TreeNode.NILIST);
		node.setLeft(init());
		node.setRight(initlistb());

		return node;
	}
	
	// <initlistb> ::= , <initlist> | ε
	private TreeNode initlistb(){
		if(currentToken.value() != Token.TCOMA) return null;
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
		return initlist();
	}
	
	// NINIT
	//<init> ::= <id> = <expr>
	private TreeNode init(){
		TreeNode node = new TreeNode(TreeNode.NINIT);
		//need TIDEN token
		if(!checkToken(Token.TIDEN, "Invalid initialisation: Not found ID name.")) return null;
		StRec stRec = new StRec();
		stRec.setName(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();
		
		//need TEQUL token
		if(!checkToken(Token.TEQUL, "Invalid initialisation: Not found '=' .")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		
		node.setLeft(expr());
		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);

		return node;
	}
	
	// <types> ::= types <typelist> | ε
	private TreeNode types(){
		if(currentToken.value() != Token.TTYPS)return null;
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
		return typelist();
	}
	
	// <arrays> ::= arrays <arrdecls> | ε
	private TreeNode arrays(){
		if(currentToken.value() != Token.TARRS)return null;
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
		return arrdecls();
	}
	
	// NFUNCS
	//<funcs> ::= <func> <funcs> | ε
	private TreeNode funcs(){
		if(currentToken.value() != Token.TFUNC)return null;
		TreeNode node = new TreeNode(TreeNode.NFUNCS);
		node.setLeft(func());
		node.setRight(funcs());

		return node;
	}
	
	// NMAIN
	//<mainbody> ::= main <slist> begin <stats> end CD19 <id>
	private TreeNode mainbody(){
		String errMsg="Invalid Mainbody: ";

		//need TMAIN token
		if(!checkToken(Token.TMAIN, errMsg+"Keyword missing: Not found 'MAIN'.")) return null;
		TreeNode node = new TreeNode(TreeNode.NMAIN);
		currentToken = followToken;
		followToken = scanner.getToken();
		
		node.setLeft(slist());

		//need TBEGN token
		if(!checkToken(Token.TBEGN, errMsg+"Keyword missing: Not found 'BEGIN'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		
		node.setRight(stats());

		//need TEND token
		if(!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		
		//need TCD19 token
		if(!checkToken(Token.TCD19, errMsg+"Keyword missing: Not found 'CD19'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		
		//need TIDEN token
		if(!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return null;
		StRec stRec = new StRec();
		stRec.setName(currentToken.getStr());
		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);
		//need TEOF token
		currentToken = followToken;
		followToken = scanner.getToken();
		if(!checkToken(Token.TEOF, "Terminator not found.")) return null;

		return node;
	}
	
	// NSDLST
	//<slist> ::= <sdecl> <slistb>
	private TreeNode slist(){
		TreeNode node = new TreeNode(TreeNode.NSDLST);
		
		node.setLeft(sdecl());
		node.setRight(slistb());

		return node;
	}
	
	// <slistb> ::= , <slist> | ε
	private TreeNode slistb(){
		
		if(currentToken.value() != Token.TCOMA)return null;
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();

		return slist();
	}
	
	// NTYPEL
	// <typelist> ::= <type> <typelistb>
	private TreeNode typelist(){
		TreeNode node = new TreeNode(TreeNode.NTYPEL);
		node.setLeft(type());
		node.setRight(typelistb());
		return node;
	}
	
	// <typelistb> ::= <typelist> | ε
	private TreeNode typelistb(){
		if(currentToken.value() != Token.TIDEN)return null;
		return typelist();
	}

	// NRTYPE  NATYPE
	//<type> ::= <structid> is <fields> end | <typeid> is array [ <expr> ] of <structid>
	private TreeNode type(){
		TreeNode node = new TreeNode(TreeNode.NUNDEF);

		String errMsg="Invalid struct or array declaration: ";
		
		//need TIDEN token
		if(!checkToken(Token.TIDEN, errMsg+"Not found StructID or TypeID name.")) return null;
		
		StRec stRec = new StRec();//first id		
		stRec.setName(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();


		node.setSymbol(stRec);

		// need TIS token
		if(!checkToken(Token.TIS, errMsg+"Keyword missing: Not found 'IS'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		

		//NRTYPE node or NATYPE node
		if(currentToken.value() != Token.TARAY){
			//NRTYPE
			stRec.setType("Struct");
			symbolTable.put(stRec.getName(), stRec);
			
			node.setValue(TreeNode.NRTYPE);
			node.setLeft(fields());
			//need TEND token
			if(!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'.")) return null;
			currentToken = followToken;
			followToken = scanner.getToken();

			return node;
		}else{
			//NATYPE
			node.setValue(TreeNode.NATYPE);
			currentToken = followToken;
		followToken = scanner.getToken();
			stRec.setType("Type");
			symbolTable.put(stRec.getName(), stRec);
			//need TLBRK token
			if(!checkToken(Token.TLBRK, errMsg+"Not found '['.")) return null;
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setLeft(expr());

			//need TRBRK token
			if(!checkToken(Token.TRBRK, errMsg+"Not found ']'.")) return null;
			currentToken = followToken;
			followToken = scanner.getToken();
			
			//need TOF token
			if(!checkToken(Token.TOF, errMsg+"Keyword missing: Not found 'OF'.")) return null;
			currentToken = followToken;
		followToken = scanner.getToken();
			
			//need TIDEN token
			if(!checkToken(Token.TIDEN, errMsg+"Not found StructID name.")) return null;
			
			//StReak for the array struct
			StRec stRec2 = new StRec();
			stRec2.setName(currentToken.getStr());		
			stRec2.setType("Struct");
			node.setType(stRec2);
			symbolTable.put(stRec2.getName(), stRec2);	
			
			currentToken = followToken;
			followToken = scanner.getToken();
			
			return node;
		}		

	}

	// NFLIST
	//<fields> ::= <sdecl> <fieldsb>
	private TreeNode fields(){
		TreeNode node = new TreeNode(TreeNode.NFLIST);

		node.setLeft(sdecl());
		node.setRight(fieldsb());

		return node;
	}
	
	// <fieldsb> ::= , <fields> | ε
	private TreeNode fieldsb(){
		

		if(currentToken.value() != Token.TCOMA)return null;
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
		
		
		return fields();
	}
	/// ??????
	// NSDECL
	//<sdecl> ::= <id> : <stype>
	private TreeNode sdecl(){
		String errMsg = "Invalid variable declaration: ";
		TreeNode node = new TreeNode(TreeNode.NSDECL);
		StRec stRec = new StRec();
		
		//need TIDEN token
		if(!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return null;
		stRec.setName(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();
		

		//need TCOLN token
		if(!checkToken(Token.TCOLN, errMsg+"Not found ':'")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		

		//Resolution type TINTG or TREAL or TBOOL token
		//<stype> ::= integer | real | boolean
		if(currentToken.value() == Token.TINTG){
			stRec.setType("integer");
		}else if(currentToken.value() == Token.TREAL){
			stRec.setType("real");
		}else if(currentToken.value() == Token.TBOOL){
			stRec.setType("boolean");
		}else{
			if(!checkToken(Token.TINTG, errMsg+"Not found integer or real or boolen type")) return null;
		}
		currentToken = followToken;
		followToken = scanner.getToken();
		

		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);
		return node;		

	}
	
	// NALIST
	//<arrdecls> ::= <arrdecl> <arrdeclsb>
	private TreeNode arrdecls(){
		TreeNode node = new TreeNode(TreeNode.NALIST);

		node.setLeft(arrdecl());
		node.setRight(arrdeclsb());

		return node;
	}
	
	// <arrdeclsb> ::= , <arrdecls> | ε
	private TreeNode arrdeclsb(){
		if(currentToken.value() != Token.TCOMA) return null;	
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
		
		return arrdecls();
	}
	
	// NARRD
	//<arrdecl> ::= <id> : <typeid>
	private TreeNode arrdecl(){
		String errMsg = "Invalid array declaration: ";
		TreeNode node = new TreeNode(TreeNode.NARRD);
		StRec stRec = new StRec();

		//need TIDEN token
		if(!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return null;
		stRec.setName(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();
		

		//need TCOLN token
		if(!checkToken(Token.TCOLN, errMsg+"Not found ':'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		
		
		//need TIDEN token
		if(!checkToken(Token.TIDEN, errMsg+"Not found TypeID name.")) return null;
		stRec.setType(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();
		

		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);

		return node;
	}
	
	// NFUND
	//<func> ::= function <id> ( <plist> ) : <rtype> <locals> begin <stats> end
	private TreeNode func(){
		String errMsg = "Invalid function declaration";
		TreeNode node = new TreeNode(TreeNode.NFUND);
		StRec stRec = new StRec();

		//need TFUNC token
		if(!checkToken(Token.TFUNC, errMsg+"Keyword missing: Not found 'FUNCTION'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		

		//neeed TIDEN token
		if(!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return null;
		stRec.setName(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();
		

		//need TLPAR token
		if(!checkToken(Token.TLPAR, errMsg+"Not found '('.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		

		node.setLeft(plist());

		//need TRPAR token
		if(!checkToken(Token.TRPAR, errMsg+"Not found ')'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		

		//need TCOLN token
		if(!checkToken(Token.TCOLN, errMsg+"Not found ':'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		

		//Resolution type TINTG or TREAL or TBOOL token
		//<rtype> ::= integer | real | boolean | void
		if(currentToken.value() == Token.TINTG){
			stRec.setType("integer");
		}else if(currentToken.value() == Token.TREAL){
			stRec.setType("real");
		}else if(currentToken.value() == Token.TBOOL){
			stRec.setType("boolean");
		}else if(currentToken.value() == Token.TVOID){
			stRec.setType("void");
		}else{
			if(!checkToken(Token.TINTG, errMsg+"Not found integer or real or boolen or void type.")) return null;
		}
		currentToken = followToken;
		followToken = scanner.getToken();
		

		node.setMiddle(locals());

		//need TBEGN token
		if(!checkToken(Token.TBEGN, errMsg+"Keyword missing: Not found 'BEGIN'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		

		node.setRight(stats());

		//need TEND token
		if(!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		

		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);

		return node;
	}
	

	// <plist> ::= <params> | ε
	private TreeNode plist(){
		if(currentToken.value() == Token.TIDEN || currentToken.value() == Token.TCONS){
			return params();
		}
		return null;
	}
	
	//  NPLIST
	//<params> ::= <param> <paramsb> 
	private TreeNode params(){
		TreeNode node = new TreeNode(TreeNode.NPLIST);
		node.setLeft(param());
		node.setRight(paramsb());
		return node;
	}
	
	//  <paramsb> ::= , <params> | ε
	private TreeNode paramsb(){
		if(currentToken.value() != Token.TCOMA)return null;
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
		
		
		return params();
	}
	
	// NSIMP NARRP NARRC
	//<param> ::= <decl> | const <arrdecl>
	private TreeNode param(){
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		if(currentToken.value() == Token.TCONS){
			//get next token
			currentToken = followToken;
			followToken = scanner.getToken();
			

			node.setValue(TreeNode.NARRC);
			node.setLeft(arrdecl());
			return node;
		}
		//??
		// NSIMP or NARRP
		TreeNode checkNode = decl();
		if(checkNode.getValue() == TreeNode.NARRD){
			node.setValue(TreeNode.NARRP);
		}else if(checkNode.getValue() == TreeNode.NSDECL){
			node.setValue(TreeNode.NSIMP);
		}else{
			return null;
		}
		node.setLeft(checkNode);
		return node;
	}
	
	
	//<locals> ::= <dlist> | ε
	private TreeNode locals(){
		if(currentToken.value() != Token.TIDEN)return null;

		return dlist();
	}
	
	// NDLIST
	//<dlist> ::= <decl> <dlistb>
	private TreeNode dlist(){
		
		TreeNode node = new TreeNode(TreeNode.NDLIST);

		node.setLeft(decl());
		node.setRight(dlistb());

		return node;
	}
	
	// <dlistb> ::= , <dlist> | ε
	private TreeNode dlistb(){
		// null 
		if(currentToken.value() != Token.TCOMA) return null;

		currentToken = followToken;
		followToken = scanner.getToken();
		return dlist();
	}
	
	// <decl> ::= <sdecl> | <arrdecl>
	private TreeNode decl(){

		TreeNode node = sdecl();

		if(node == null){
			node = arrdecl();
			if(node == null) return null;
		}
		currentToken = followToken;
		followToken = scanner.getToken();
		

		return node;
	}
	
	
	// NSTATS
	//<stats> ::= <stat> ; <statsb> | <strstat> <statsb> 
	private TreeNode stats(){
		String errMsg = "Invalid statements declaration.";

		TreeNode node = new TreeNode(TreeNode.NSTATS);
		
		if(currentToken.value() == Token.TFOR || currentToken.value() == Token.TIFTH){
			node.setLeft(strstat());
			node.setRight(statsb());
			return node;

		}else{
			node.setLeft(stat());
			//need TSEMI token
			if(!checkToken(Token.TSEMI, "Invalid statements declaration: Not found ';'." )) return null;
			currentToken = followToken;
		followToken = scanner.getToken();

			node.setRight(statsb());
			return node;
		}
	}
	
	// <statsb> ::= <stats>  | ε
	private TreeNode statsb(){
		// stats: for or if or repeat or id or input or print or printline or return
		if(currentToken.value() == Token.TFOR 
			|| currentToken.value() == Token.TIFTH
			|| currentToken.value() == Token.TREPT
			|| currentToken.value() == Token.TIDEN
			|| currentToken.value() == Token.TINPT
			|| currentToken.value() == Token.TPRIN
			|| currentToken.value() == Token.TPRLN
			|| currentToken.value() == Token.TRETN){
				return stats();
		}else{
			return null;
		}
	}
	
	// <strstat> ::= <forstat> | <ifstat>
	private TreeNode strstat(){
		// "if" or "for"
		if(currentToken.value() == Token.TFOR){
			return forstat();
		}else{
			return ifstat();
		}
	}
	
	// <stat> ::= <repstat> | <returnstat> | <iostat> | <id> <statb>
	private TreeNode stat(){

		//Lookahead for next non terminal
		if(currentToken.value() == Token.TREPT){
			return repstat();
		}else if(currentToken.value() == Token.TRETN){
			return returnstat();
		}else if(currentToken.value() == Token.TINPT || currentToken.value() == Token.TPRIN || currentToken.value() == Token.TPRLN){
			return iostat();
		}else{
			//need TIDEN token
			if(!checkToken(Token.TIDEN, "Invalid statement declaration: Invalid Keyword.")) return null;
			currentToken = followToken;
			followToken = scanner.getToken();
			return statb();

		}
	}

	//<statb> ::= <asgnstat> | <callstat> 
	private TreeNode statb(){
		
		if(currentToken.value() == Token.TLPAR){
			return callstat();
		}else{
			return asgnstat();
		}
	}
	// NFOR
	//<forstat> ::= for ( <asgnlist> ; <bool> ) <stats> end
	private TreeNode forstat(){
		String errMsg = "Invalid For structure: ";
		TreeNode node = new TreeNode(TreeNode.NFOR);
		//need TFOR token
		if(!checkToken(Token.TFOR, errMsg+"Keyword missing: Not found 'FOR'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TLPAR token
		if(!checkToken(Token.TLPAR, errMsg+"Not found '('.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setLeft(asgnlist());

		//need TSEMI token
		if(!checkToken(Token.TSEMI, errMsg+"Not found ';'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setMiddle(bool());

		//Cneed TRPAR token
		if(!checkToken(Token.TRPAR, errMsg+"Not found ')'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setRight(stats());

		//need TEND token
		if(!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		return node;
	}
	
	// NREPT
	//<repstat> ::= repeat ( <asgnlist> ) <stats> until <bool>
	private TreeNode repstat(){
		String errMsg = "Invalid repeat structure: ";
		TreeNode node = new TreeNode(TreeNode.NREPT);

		//need TREPT token
		if(!checkToken(Token.TREPT, errMsg+"Keyword missing: Not found 'REPEAT'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TLPAR token
		if(!checkToken(Token.TLPAR, errMsg+"Not found '('.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setLeft(asgnlist());

		//need TRPAR token
		if(!checkToken(Token.TRPAR, errMsg+"Not found ')'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setMiddle(stats());

		//need TUNTL token
		if(!checkToken(Token.TUNTL, errMsg+"Keyword missing: Not found 'UNTIL'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setRight(bool());

		return node;
	}
	
	// <asgnlist> ::= <alist> | ε
	private TreeNode asgnlist(){
		if(currentToken.value() != Token.TIDEN)return null;
		return alist();
	}
	
	// NASGNS
	//<alist> ::= <id> <asgnstat> <alistb>
	private TreeNode alist(){
		TreeNode node = new TreeNode(TreeNode.NASGNS);

		node.setLeft(asgnstat());
		node.setRight(alistb());

		return node;
	}
	
	// <alistb> ::= , <alist> | ε
	private TreeNode alistb(){
		
		if(currentToken.value() != Token.TCOMA)	return null;

		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
		
		return alist();
	}
	
	// <asgnstat> ::= <var> <asgnop> <bool>
	private TreeNode asgnstat(){
		TreeNode node = new TreeNode(TreeNode.NASGNS);

		node.setLeft(var());
		node.setMiddle(asgnop());
		node.setRight(bool());

		return node;
	}
	
	// <asgnop> ::= = | += | -= | *= | /= // NASGN, NPLEQ, NMNEQ, NSTEQ, NDVEQ
	private TreeNode asgnop(){
		TreeNode node  = new TreeNode(TreeNode.NUNDEF);

		if(currentToken.value() == Token.TEQUL){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setValue(TreeNode.NASGN);
		}
		else if(currentToken.value() == Token.TPLEQ){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setValue(TreeNode.NPLEQ);
		}
		else if(currentToken.value() == Token.TMNEQ){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setValue(TreeNode.NMNEQ);
		}
		else if(currentToken.value() == Token.TSTEQ){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setValue(TreeNode.NSTEQ);
		}
		else if(currentToken.value() == Token.TDVEQ){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setValue(TreeNode.NDVEQ);
		}
		else{
			checkToken(Token.TEQUL, "Invalid assignment: Not found assignment operator.");
			return null;
		}
		return node;
	}
		
	// NIFTH NIFTE
	//<ifstat> ::= if ( <bool> ) <stats> end 
	//<ifstat> ::= if ( <bool> ) <stats>  else <stats> end
	private TreeNode ifstat(){
		String errMsg = "Invalid if statement: ";

		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		//need TIFTH token
		if(!checkToken(Token.TIFTH, errMsg+"Keyword missing: Not found 'IF'")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TLPAR token
		if(!checkToken(Token.TLPAR, errMsg+"Not found '('")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setLeft(bool());

		//need TRPAR token
		if(!checkToken(Token.TRPAR, errMsg+"Not found ')'")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();


		node.setMiddle(stats());


		//NIFTH or NIFTE
		if(currentToken.value() == Token.TELSE){
			currentToken = followToken;
			followToken = scanner.getToken();

			node.setRight(stats());

			//need TEND token
			if(!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'")) return null;
			currentToken = followToken;
			followToken = scanner.getToken();

			node.setValue(TreeNode.NIFTE);
			return node;
		}
		
		//need TEND token
		if(!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		node.setValue(TreeNode.NIFTH);
		return node;

		
	}

	
	// NINPUT  NPRINT  NPRLN
	private TreeNode iostat(){

		TreeNode node = new TreeNode(TreeNode.NUNDEF);

		if(currentToken.value() == Token.TINPT){
			currentToken = followToken;
			followToken = scanner.getToken();
			node.setValue(TreeNode.NINPUT);
			node.setLeft(vlist());
		}else if(currentToken.value() == Token.TPRIN){
			currentToken = followToken;
			followToken = scanner.getToken();
			node.setValue(TreeNode.NPRINT);
			node.setLeft(prlist());
		}else if(currentToken.value() == Token.TPRLN){
			currentToken = followToken;
			followToken = scanner.getToken();
			node.setValue(TreeNode.NPRLN);
			node.setLeft(prlist());
		}else{
			checkToken(Token.TINPT, "Invalid I/O statement.");
			return null;
		}
		return node;
	}
	
	// NCALL
	//<callstat> ::= <id> ( <callstatb> ) 
	//<callstatb> ::= <elist> | ε
	private TreeNode callstat(){
		String errMsg = "Invalid call statement: ";
		TreeNode node = new TreeNode(TreeNode.NCALL);
		StRec stRec = new StRec();

		//need TIDEN token
		if(!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return null;
		stRec.setName(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TLPAR token
		if(!checkToken(Token.TLPAR, errMsg+"Not found '('.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		if(currentToken.value() == Token.TRPAR){
			//get next token
			currentToken = followToken;
			followToken = scanner.getToken();
		}else{
			node.setLeft(elist());
			//need right paranthesis
			if(!checkToken(Token.TRPAR, errMsg+"Not found ')'.")) return null;
			currentToken = followToken;
			followToken = scanner.getToken();
		}

		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);
		return node;
	}
	
	// NRETN
	//<returnstat> ::= return <returnstatb>

	private TreeNode returnstat(){

		TreeNode node = new TreeNode(TreeNode.NRETN);

		//need TRETN token
		if(!checkToken(Token.TRETN, "Invalid return statement.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		node.setLeft(expr());
		return node;
	}
	
	//<returnstatb> ::= <expr> | ε 
	private TreeNode returnstatb(){
		if(currentToken.value() == Token.TIDEN 
			|| currentToken.value() == Token.TILIT
			|| currentToken.value() == Token.TFLIT
			|| currentToken.value() == Token.TTRUE
			|| currentToken.value() == Token.TFALS
			|| currentToken.value() == Token.TLPAR){
			return expr();
		}else{
			return null;
		}
	}
	
	// NVLIST
	//<vlist> ::= <var> <vlistb>
	private TreeNode vlist(){
		TreeNode node = new TreeNode(TreeNode.NVLIST);

		node.setLeft(var());
		node.setRight(vlistb());

		return node;
	}
	
	// <vlistb> ::= , <vlist> | ε
	private TreeNode vlistb(){

		if(currentToken.value() != Token.TCOMA)	return null;
		//get next Token
		currentToken = followToken;
		followToken = scanner.getToken();
		return vlist();
	}
	
	// NSIMV  NARRV
	//<var> ::= <id> | <id> [<expr>] . <id>
	private TreeNode var(){
		String errMsg = "Invalid variable declaration.";
		if(!checkToken(Token.TIDEN, errMsg+"  ")) return null;
		StRec stRec = new StRec(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();
		

		// NSIMV or NARRV
		if(currentToken.value() == Token.TLBRK){
			TreeNode node = new TreeNode(TreeNode.NARRV);
			//get next token
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setLeft(expr());

			//need TRBRK token 
			if(!checkToken(Token.TRBRK, errMsg+"")) return null;
			currentToken = followToken;
			followToken = scanner.getToken();
			

			//need TDOT token
			if(!checkToken(Token.TDOT, errMsg+"")) return null;
			currentToken = followToken;
			followToken = scanner.getToken();
			

			//need TIDEN token 
			if(!checkToken(Token.TIDEN, errMsg+"")) return null;
			StRec stRec2 = new StRec(currentToken.getStr());
			currentToken = followToken;
			followToken = scanner.getToken();
			

			node.setSymbol(stRec2);
			symbolTable.put(stRec.getName(), stRec2);

			return node;
		}else{
			TreeNode node = new TreeNode(TreeNode.NSIMV);
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}
	}
	
	// NEXPL
	//<elist> ::= <bool> <elistb>
	private TreeNode elist(){
		TreeNode node = new TreeNode(TreeNode.NEXPL);

		node.setLeft(bool());
		node.setRight(elistb());

		return node;
	}
	
	// <elistb> ::= , <elist> | ε 
	private TreeNode elistb(){
		if(currentToken.value() != Token.TCOMA)return null;
		//get next Token
		currentToken = followToken;
		followToken = scanner.getToken();
		
		return elist();
	}
	


	// NBOOL
	//<bool> ::=  <rel> <boolb> 
	private TreeNode bool(){
		TreeNode node = new TreeNode(TreeNode.NBOOL);
		node.setLeft(rel());
		node.setRight(boolb());		
		return node;
	}

	// <boolb> ::= <logop> <rel> <boolb> | ε
	private TreeNode boolb(){

		TreeNode node = new TreeNode(TreeNode.NBOOL);
		if(currentToken.value() == Token.TAND 
			|| currentToken.value() == Token.TOR 
			|| currentToken.value() == Token.TXOR){

			node.setLeft(logop());
			node.setMiddle(rel());
			node.setRight(boolb());
			return node;
		}
		return null;
		
	}
	
	// NNOT
	//<rel> ::= not <expr> <relop> <expr> | <expr> <relb> 
	private TreeNode rel(){
		TreeNode node = new TreeNode(TreeNode.NBOOL);
		if(currentToken.value() == Token.TNOT){
			node.setValue(TreeNode.NNOT);
			//get next Token
			currentToken = followToken;
			followToken = scanner.getToken();
			

			node.setLeft(expr());
			node.setMiddle(relop());
			node.setRight(expr());

			return node;
		}
		node.setLeft(expr());
		node.setRight(relb());
		return node;
	}
	
	// <relb> ::= <relop> <expr>  | ε
	private TreeNode relb(){
		TreeNode node = new TreeNode(TreeNode.NBOOL);
		
		if(currentToken.value() != Token.TEQEQ
		|| currentToken.value() != Token.TNEQL
		|| currentToken.value() != Token.TGRTR
		|| currentToken.value() != Token.TLEQL
		|| currentToken.value() != Token.TLESS
		|| currentToken.value() != Token.TGEQL) return null;
		
		node.setLeft(relop());
		node.setRight(expr());
		return node;
	}
	
	
	// NAND, NOR, NXOR
	// <logop> ::= and | or | xor     
	private TreeNode logop(){

		TreeNode node  = new TreeNode(TreeNode.NUNDEF);

		if(currentToken.value() == Token.TAND){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setValue(TreeNode.NAND);
		}
		else if(currentToken.value() == Token.TOR){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setValue(TreeNode.NOR);
		}
		else if(currentToken.value() == Token.TXOR){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setValue(TreeNode.NXOR);
		}
		else{
			checkToken(Token.TAND, "Invalid logic operation.");
			return null;
		}
		return node;
	}
	
	// NEQL, NNEQ, NGRT, NLEQ, NLSS, NGEQ
	// <relop> ::= == | != | > | <= | < | >=
	private TreeNode relop(){

		TreeNode node  = new TreeNode(TreeNode.NUNDEF);

		if(currentToken.value() == Token.TEQEQ){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setValue(TreeNode.NEQL);
		}
		else if(currentToken.value() == Token.TNEQL){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setValue(TreeNode.NNEQ);
		}
		else if(currentToken.value() == Token.TGRTR){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setValue(TreeNode.NGRT);
		}
		else if(currentToken.value() == Token.TLEQL){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setValue(TreeNode.NLEQ);
		}
		else if(currentToken.value() == Token.TLESS){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setValue(TreeNode.NLSS);
		}
		else if(currentToken.value() == Token.TGEQL){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setValue(TreeNode.NGEQ);
		}
		else{
			checkToken(Token.TEQEQ, "Invalid logic operation.");
			return null;
		}
		return node;
	}
	
//////////////////////////////////////////////////////////////////////////	
//////////////////////////////////////////////////////////////////////////	
//////////////////////////////////////////////////////////////////////////	
//////////////////////////////////////////////////////////////////////////	
	// plus-minus method
	// <expr> ::= <term> <exprb>
	private TreeNode expr(){
		TreeNode tempNode;
		tempNode = term();
		return exprb(tempNode);
	}
	
	// NADD  NSUB
	//<exprb> ::= + <term> <exprb> | - <term> <exprb> | ε
	private TreeNode exprb(TreeNode leftNode){
		TreeNode parent;
		if(currentToken.value() == Token.TPLUS){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			parent = new TreeNode(TreeNode.NADD);
			parent.setLeft(leftNode);
			parent.setRight(term());
			return(exprb(parent));
		}else if(currentToken.value() == Token.TMINS){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			parent = new TreeNode(TreeNode.NSUB);
			parent.setLeft(leftNode);
			parent.setRight(term());
			return(exprb(parent));
		}else{
			return leftNode;
		}
		
	}
	
	// multiply operation
	//<term> ::= <fact> <termb>
	private TreeNode term(){
		TreeNode tempNode;
		tempNode = fact();
		return termb(tempNode);
	}
	
	// NMUL, NDIV, NMOD
	//<termb> ::= * <fact> <termb> | / <fact> <termb> | % <fact> <termb> | ε
	private TreeNode termb(TreeNode leftNode){
		TreeNode parent;
		if(currentToken.value() == Token.TSTAR){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			parent = new TreeNode(TreeNode.NMUL);
			parent.setLeft(leftNode);
			parent.setRight(fact());
			return(termb(parent));
		}else if(currentToken.value() == Token.TDIVD){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			parent = new TreeNode(TreeNode.NDIV);
			parent.setLeft(leftNode);
			parent.setRight(fact());
			return(termb(parent));
		}else if(currentToken.value() == Token.TPERC){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			parent = new TreeNode(TreeNode.NMOD);
			parent.setLeft(leftNode);
			parent.setRight(fact());
			return(termb(parent));
		}else{
			return leftNode;
		}
	}
	
	// exponential operation
	//<fact> ::=  <exponent> <factb> 
	private TreeNode fact(){
		TreeNode tempNode;
		tempNode = exponent();
		return factb(tempNode);
	}
	
	// NPOW 
	//<factb> ::=  ^ <exponent> <factb> | ε
	private TreeNode factb(TreeNode leftNode){
		TreeNode parent;
		if(currentToken.value() == Token.TCART){
			currentToken = followToken;
			followToken = scanner.getToken();
			
			parent = new TreeNode(TreeNode.NPOW);
			parent.setLeft(leftNode);
			parent.setRight(exponent());
			return(factb(parent));
		}else{
			return leftNode;
		}
	}
	
	// NILIT,  NFLIT,  NTRUE,  NFALS
	//<exponent> ::= <var>| <fncall> | <intlit> | <reallit>  | true | false | ( <bool> )
	//<exponent> ::= <exponentb> |  <intlit> | <reallit>  | true | false | ( <bool> )
	//<exponentb> ::= <var>| <fncall> 
	private TreeNode exponent(){

		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		StRec stRec = new StRec();
		if(currentToken.value() == Token.TILIT){
			node.setValue(TreeNode.NILIT);
			stRec.setName(currentToken.getStr());
			//get next token
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}else if(currentToken.value() == Token.TFLIT){
			node.setValue(TreeNode.NFLIT);
			stRec.setName(currentToken.getStr());
			//get next token
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}else if(currentToken.value() == Token.TIDEN){
			if(followToken.value() == Token.TLPAR){
				return fncall();
			}else{
				return var();
			}
		}else if(currentToken.value() == Token.TTRUE){
			node.setValue(TreeNode.NTRUE);
			stRec.setName(currentToken.getStr());
			//get next token
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}else if(currentToken.value() == Token.TFALS){
			node.setValue(TreeNode.NFALS);
			stRec.setName(currentToken.getStr());
			//get next token
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}else{

			//need TLPAR token
			if(!checkToken(Token.TLPAR, "Invalid exponent operation: Not found '('. ")) return null;
			currentToken = followToken;
			followToken = scanner.getToken();
			

			TreeNode tempNode;
			tempNode = bool();

			//need TRPAR token
			if(!checkToken(Token.TRPAR, "Invalid exponent operation: Not found ')'. ")) return null;
			currentToken = followToken;
			followToken = scanner.getToken();
			

			return tempNode;
		}
	}

	////////////////////////////////////////////////
	////////////////////////////////////////////////
	////////////////////////////////////////////////
	// NFCALL
	//<fncall> ::= <id> ( <fncallb> )
	private TreeNode fncall(){
		String errMsg = "Invalid function call: ";
		TreeNode node = new TreeNode(TreeNode.NFCALL);
		StRec stRec = new StRec();

		//need TIDEN token
		if(!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return null;
		stRec.setName(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();
		

		//need TLPAR token
		if(!checkToken(Token.TLPAR, errMsg+"Not found '('. ")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		
		
		node.setLeft(fncallb());

		//Check for right paranthesis token
		if(!checkToken(Token.TRPAR, errMsg+"Not found ')'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		
		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);
		return node;
	}
	
	// <fncallb> ::= <elist> | ε
	private TreeNode fncallb(){
		
		if(currentToken.value() != Token.TRPAR){
			return elist();
		}else{
			return null;
		}
		
	}
	
	// NPRLST
	//<prlist> ::= <printitem> <prlistb>
	private TreeNode prlist(){
		TreeNode node = new TreeNode(TreeNode.NPRLST);

		node.setLeft(printitem());
		node.setRight(prlistb());

		return node;
	}
	
	// <prlistb> ::= , <prlist> | ε
	private TreeNode prlistb(){
		if(currentToken.value() != Token.TCOMA)return null;

		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
		
		return prlist();
	}
	
	// NSTRG
	//<printitem> ::= <expr> | <string>
	private TreeNode printitem(){
		if(currentToken.value() == Token.TSTRG){
			TreeNode node = new TreeNode(TreeNode.NSTRG);
			StRec stRec = new StRec(currentToken.getStr());
			//get next token
			currentToken = followToken;
			followToken = scanner.getToken();
			
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}
		return expr();
		
	}


}