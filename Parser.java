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
	//private Token followToken;////next follow token
	private SymbolTable symbolTable;
	private String errorList;
	
	public Parser(Scanner scanner){
		this.scanner = scanner;
		symbolTable = new SymbolTable(null);
		errorList = "";
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
				System.out.println("=====Error(Lexical): " + currentToken.getStr());
				errorList+="Error(Lexical): " + currentToken.getStr();// lexeme
			}else{
				// Syntax 
				System.out.println("=====Error(Syntax): " + message);
				errorList+="Error(Syntax): "+ message;
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
		//get token
		currentToken = scanner.getToken(); 		
		return program();
	}
	
	// NPROG
	// <program> ::= CD19 <id> <globals> <funcs> <mainbody>
	private TreeNode program(){
		
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		//need CD19 token		
		if(!checkToken(Token.TCD19, "Not found the Keyword 'CD19'")) return node;
		//get next token
		currentToken = scanner.getToken();		
		//need TIDEN token
		if(!checkToken(Token.TIDEN, "Invalid initialisation: Not found ID name.")) return node;
		StRec stRec = new StRec();			
		stRec.setName(currentToken.getStr());
		//node.setType(stRec);
		symbolTable.put(stRec.getName(), stRec);
		
		//get next token
		currentToken = scanner.getToken();
		//Add to symbol table	
		node.setValue(TreeNode.NPROG);
		node.setSymbol(stRec);
		
		node.setLeft(globals());
		node.setMiddle(funcs());
		node.setRight(mainbody());
		
		return node;
	}
	// NGLOB 
	// <globals> ::= <consts> <types> <arrays>
	private TreeNode globals(){
		return new TreeNode(TreeNode.NGLOB, consts(), types(), arrays());
	}
	// Special
	// <consts> ::=  ε |constants <initlist>
	private TreeNode consts(){
		// need TCONS token
		if(currentToken.value() != Token.TCONS) return null;
		// get next token
		currentToken = scanner.getToken();		
		return initlist();
	}
	// Special
	// <initlist> ::= <init> <initlistb>
	private TreeNode initlist(){
		TreeNode node = init();
		return initlistb(node);
	}
	// NILIST
	// <initlistb> ::= ε | , <initlist>  
	private TreeNode initlistb(TreeNode node){
		if(currentToken.value() == Token.TCOMA){
			//get next token
			currentToken = scanner.getToken();	
			return new TreeNode(TreeNode.NILIST, node, initlist());
		}else{
			return node;
		}
	}
	// NINIT
	//<init> ::= <id> = <expr>
	private TreeNode init(){
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		//need TIDEN token
		if(!checkToken(Token.TIDEN, "Invalid initialisation: Not found ID name.")) return node;
		StRec stRec = new StRec();
		stRec.setName(currentToken.getStr());

		//next token
		currentToken = scanner.getToken();
		//need TEQUL token
		if(!checkToken(Token.TEQUL, "Invalid initialisation: Not found '=' .")) return node;
		currentToken = scanner.getToken();
		// NINIT
		node.setValue(TreeNode.NINIT);
		symbolTable.put(stRec.getName(), stRec);
		node.setSymbol(stRec);		
		node.setLeft(expr());
		return node;
	}
	// Special
	// <types> ::= types <typelist> | ε
	private TreeNode types(){
		//need TTYPS token
		if(currentToken.value() != Token.TTYPS)return null;
		//get next token
		currentToken = scanner.getToken();		
		return typelist();
	}
	// Special
	// <arrays> ::= arrays <arrdecls> | ε
	private TreeNode arrays(){
		//need TARRS token
		if(currentToken.value() != Token.TARRS)return null;
		//get next token
		currentToken = scanner.getToken();		
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
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		//need TMAIN token
		if(!checkToken(Token.TMAIN, errMsg+"Keyword missing: Not found 'MAIN'.")) return node;
		currentToken = scanner.getToken();
		// left node
		node.setLeft(slist());
		//need TBEGN token
		if(!checkToken(Token.TBEGN, errMsg+"Keyword missing: Not found 'BEGIN'.")) return node;
		currentToken = scanner.getToken();
		//right node
		node.setRight(stats());
		//need TEND token
		if(!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'.")) return node;
		currentToken = scanner.getToken();
		//need TCD19 token
		if(!checkToken(Token.TCD19, errMsg+"Keyword missing: Not found 'CD19'.")) return node;
		currentToken = scanner.getToken();		
		//need TIDEN token
		if(!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return node;
		StRec stRec = new StRec();
		stRec.setName(currentToken.getStr());
		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);
		//need TEOF token
		currentToken = scanner.getToken();		
		if(!checkToken(Token.TEOF, "Terminator not found.")) return node;
		node.setValue(TreeNode.NMAIN);
		return node;
	}
	
	// Special
	//<slist> ::= <sdecl> <slistb>
	private TreeNode slist(){
		TreeNode node = sdecl();
		return slistb(node);
	}
	// NSDLST
	// <slistb> ::= ε | , <slist> 
	private TreeNode slistb(TreeNode node){
		// need TCOMA token
		if(currentToken.value() == Token.TCOMA){
			//get next token
			currentToken = scanner.getToken();	
			return new TreeNode(TreeNode.NSDLST, node, slist());
		}else{
			return node;
		}
	}
	
	// Special 
	// <typelist> ::= <type> <typelistb>
	private TreeNode typelist(){
		TreeNode node=type();
		return typelistb(node);
	}
	// NTYPEL
	// <typelistb> ::= <typelist> | ε
	private TreeNode typelistb(TreeNode node){
		if(currentToken.value() == Token.TIDEN){
			return new TreeNode(TreeNode.NILIST, node, typelist());
		}else{
			return node;
		}
	}
	// NRTYPE  NATYPE
	//<type> ::= <structid> is <fields> end | <typeid> is array [ <expr> ] of <structid>
	private TreeNode type(){
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		String errMsg="Invalid struct or array declaration: ";
		
		//need TIDEN token
		if(!checkToken(Token.TIDEN, errMsg+"Not found StructID or TypeID name.")) return node;
		
		StRec stRec = new StRec();//first id		
		stRec.setName(currentToken.getStr());
		currentToken = scanner.getToken();		

		// need TIS token
		if(!checkToken(Token.TIS, errMsg+"Keyword missing: Not found 'IS'.")) return node;
		currentToken = scanner.getToken();		
		
		//NRTYPE node or NATYPE node
		if(currentToken.value() != Token.TARAY){
			//NRTYPE
			
			stRec.setType("Struct");
			symbolTable.put(stRec.getName(), stRec);
			node.setSymbol(stRec);

			node.setLeft(fields());
			//need TEND token
			if(!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'.")) return node;
			// Confirmed as NRTYPE node
			node.setValue(TreeNode.NRTYPE); 
			currentToken = scanner.getToken();
			return node;
		}else{
			//NATYPE

			currentToken = scanner.getToken();		
			stRec.setType("Type");
			symbolTable.put(stRec.getName(), stRec);
			node.setSymbol(stRec);
			
			//need TLBRK token
			if(!checkToken(Token.TLBRK, errMsg+"Not found '['.")) return node;
			currentToken = scanner.getToken();
			node.setLeft(expr());
			//need TRBRK token
			if(!checkToken(Token.TRBRK, errMsg+"Not found ']'.")) return node;
			currentToken = scanner.getToken();	
			//need TOF token
			if(!checkToken(Token.TOF, errMsg+"Keyword missing: Not found 'OF'.")) return node;
			currentToken = scanner.getToken();	
			//need TIDEN token
			if(!checkToken(Token.TIDEN, errMsg+"Not found StructID name.")) return node;
			
			//StReak for the array struct
			StRec stRec2 = new StRec();
			stRec2.setName(currentToken.getStr());		
			stRec2.setType("Struct");
			node.setType(stRec2);
			symbolTable.put(stRec2.getName(), stRec2);	
			// Confirmed as NATYPE node
			node.setValue(TreeNode.NATYPE); 
			currentToken = scanner.getToken();
			return node;
		}
	}
	
	// Special  
	//<fields> ::= <sdecl> <fieldsb>
	private TreeNode fields(){
		TreeNode node = sdecl();
		return fieldsb(node);
	}
	
	// NFLIST	
	// <fieldsb> ::= , <fields> | ε
	private TreeNode fieldsb(TreeNode node){
		if(currentToken.value() == Token.TCOMA){
			//get next token
			currentToken = scanner.getToken();	
			return new TreeNode(TreeNode.NFLIST, node, fields());
		}else{
			return node;
		}
	}
	// NSDECL
	//<sdecl> ::= <id> : <stype>
	private TreeNode sdecl(){
		String errMsg = "Invalid variable declaration: ";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);

		
		//need TIDEN token
		if(!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return node;
		StRec stRec = new StRec();
		stRec.setName(currentToken.getStr());
		currentToken = scanner.getToken();		
		
		//need TCOLN token
		if(!checkToken(Token.TCOLN, errMsg+"Not found ':'")) return node;
		currentToken = scanner.getToken();		
		
		//Resolution type TINTG or TREAL or TBOOL token
		//<stype> ::= integer | real | boolean
		if(currentToken.value() == Token.TINTG){
			stRec.setType("integer");
		}else if(currentToken.value() == Token.TREAL){
			stRec.setType("real");
		}else if(currentToken.value() == Token.TBOOL){
			stRec.setType("boolean");
		}else{
			if(!checkToken(Token.TINTG, errMsg+"Unknown Type: Not found Integer or Real or Boolen type")) return node;
		}
	
		node.setValue(TreeNode.NSDECL);
		node.setSymbol(stRec);
		//node.setType(stRec); //?? need or not ??			
		symbolTable.put(stRec.getName(), stRec);		
		
		currentToken = scanner.getToken();	
		return node;		
	}
	
	//Special
	//<arrdecls> ::= <arrdecl> <arrdeclsb>
	private TreeNode arrdecls(){
		TreeNode node = arrdecl();
		return arrdeclsb(node);
	}
	// NALIST
	// <arrdeclsb> ::= , <arrdecls> | ε
	private TreeNode arrdeclsb(TreeNode node){
		if(currentToken.value() == Token.TCOMA){
			//get next token
			currentToken = scanner.getToken();	
			return new TreeNode(TreeNode.NALIST, node, arrdecls());
		}else{
			return node;
		}
	}
	
	// NARRD
	//<arrdecl> ::= <id> : <typeid>
	private TreeNode arrdecl(){
		String errMsg = "Invalid array declaration: ";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		//need TIDEN token
		if(!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return node;
		StRec stRec = new StRec();
		stRec.setName(currentToken.getStr());
		currentToken = scanner.getToken();		
		//need TCOLN token
		if(!checkToken(Token.TCOLN, errMsg+"Not found ':'.")) return node;
		currentToken = scanner.getToken();		
		//need TIDEN token
		if(!checkToken(Token.TIDEN, errMsg+"Not found TypeID name.")) return node;
		node.setValue(TreeNode.NARRD);
		stRec.setType(currentToken.getStr());
		
		node.setType(stRec);
		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);
		currentToken = scanner.getToken();			
		return node;
	}
	
	// NFUND
	//<func> ::= function <id> ( <plist> ) : <rtype> <locals> begin <stats> end
	private TreeNode func(){
		String errMsg = "Invalid function declaration";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);

		//need TFUNC token
		if(!checkToken(Token.TFUNC, errMsg+"Keyword missing: Not found 'FUNCTION'.")) return node;
		currentToken = scanner.getToken();		
		
		//neeed TIDEN token
		if(!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return node;
		StRec stRec = new StRec();
		stRec.setName(currentToken.getStr());
		currentToken = scanner.getToken();		
		
		//need TLPAR token
		if(!checkToken(Token.TLPAR, errMsg+"Not found '('.")) return node;
		currentToken = scanner.getToken();		
		
		node.setLeft(plist());
		//need TRPAR token
		if(!checkToken(Token.TRPAR, errMsg+"Not found ')'.")) return node;
		currentToken = scanner.getToken();		
		
		//need TCOLN token
		if(!checkToken(Token.TCOLN, errMsg+"Not found ':'.")) return node;
		currentToken = scanner.getToken();		
		
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
			if(!checkToken(Token.TINTG, errMsg+"Unknown Type: Not found Integer or Real or Boolen or Void type.")) return node;
		}
		currentToken = scanner.getToken();		
		symbolTable.put(stRec.getName(), stRec);
		node.setMiddle(locals());
		//need TBEGN token
		if(!checkToken(Token.TBEGN, errMsg+"Keyword missing: Not found 'BEGIN'.")) return node;
		currentToken = scanner.getToken();		
		node.setRight(stats());
		//need TEND token
		if(!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'.")) return node;
		node.setValue(TreeNode.NFUND);
		currentToken = scanner.getToken();		
		node.setSymbol(stRec);
		return node;
	}
	
	// <plist> ::= <params> | ε
	private TreeNode plist(){
		if(currentToken.value() == Token.TIDEN || currentToken.value() == Token.TCONS){
			return params();
		}
		return null;
	}
	
	
	//<params> ::= <param> <paramsb> 
	private TreeNode params(){
		TreeNode node = param();
		return paramsb(node);
	}
	
	//  NPLIST	
	//  <paramsb> ::= , <params> | ε
	private TreeNode paramsb(TreeNode node){
		if(currentToken.value() == Token.TCOMA){
			//get next token
			currentToken = scanner.getToken();	
			return new TreeNode(TreeNode.NPLIST, node, params());
		}else{
			return node;
		}
	}
	
	// NSIMP NARRP NARRC
	//<param> ::= <decl> | const <arrdecl>
	private TreeNode param(){
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		if(currentToken.value() == Token.TCONS){
			//get next token
			currentToken = scanner.getToken();
			node.setValue(TreeNode.NARRC);
			node.setLeft(arrdecl());
			return node;
		}
		// NSIMP or NARRP
		TreeNode checkNode = decl();
		if(checkNode.getValue() == TreeNode.NARRD){
			node.setValue(TreeNode.NARRP);
		}else if(checkNode.getValue() == TreeNode.NSDECL){
			node.setValue(TreeNode.NSIMP);
		}else{
			return node;
		}
		node.setLeft(checkNode);
		return node;
	}
	//<locals> ::= <dlist> | ε
	private TreeNode locals(){
		if(currentToken.value() != Token.TIDEN)return null;
		return dlist();
	}
	//<dlist> ::= <decl> <dlistb>
	private TreeNode dlist(){
		TreeNode node = decl();
		return dlistb(node);
	}
	// NDLIST	
	// <dlistb> ::= , <dlist> | ε
	private TreeNode dlistb(TreeNode node){
		if(currentToken.value() == Token.TCOMA){
			//get next token
			currentToken = scanner.getToken();	
			return new TreeNode(TreeNode.NDLIST, node, dlist());
		}else{
			return node;
		}
	}

	//NSDECL NARRD 
	// <decl> ::= <id> : <declb>
	//<declb> ::= <stype> | <typeid>
	private TreeNode decl(){
		String errMsg = "Invalid variable declaration: ";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);

		StRec stRec = new StRec();
		stRec.setName(currentToken.getStr());
		currentToken = scanner.getToken();		
		
		//need TCOLN token
		if(!checkToken(Token.TCOLN, errMsg+"Not found ':'")) return node;
		currentToken = scanner.getToken();
		
		if (currentToken.value() == Token.TIDEN){
			//NARRD
			node.setValue(TreeNode.NARRD);
			stRec.setType(currentToken.getStr());
			node.setType(stRec);
			
		}else{
			//NSDECL
			//<stype> ::= integer | real | boolean
			if(currentToken.value() == Token.TINTG){
				stRec.setType("integer");
			}else if(currentToken.value() == Token.TREAL){
				stRec.setType("real");
			}else if(currentToken.value() == Token.TBOOL){
				stRec.setType("boolean");
			}else{
				if(!checkToken(Token.TINTG, errMsg+"Unknown Type ")) return node;
			}
			node.setValue(TreeNode.NSDECL);
		}	
		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);	
		currentToken = scanner.getToken();	
		return node;
	}

	//Special 
	//<stats> ::= <stat> ; <statsb> | <strstat> <statsb> 
	private TreeNode stats(){
		// <stat> or <strstat>  
		if(currentToken.value() == Token.TFOR || currentToken.value() == Token.TIFTH){
			TreeNode node = strstat();
			return statsb(node);
		}else{
			TreeNode node = stat();
			//need TSEMI token
			if(!checkToken(Token.TSEMI, "Invalid statements declaration: Not found ';'." )) return node;
			currentToken = scanner.getToken();		
			return statsb(node);
		}
	}
	//NSTATS 
	//<statsb> ::=  <stats> | ε
	private TreeNode statsb(TreeNode node){
		//TreeNode node = new TreeNode(TreeNode.NSTATS);
		// stats: for or if or repeat or id or input or print or printline or return
		
		// <stat> or <strstat>  
		if(currentToken.value() == Token.TFOR 
		|| currentToken.value() == Token.TIFTH
		|| currentToken.value() == Token.TREPT
		|| currentToken.value() == Token.TRETN
		|| currentToken.value() == Token.TINPT
		|| currentToken.value() == Token.TPRIN
		|| currentToken.value() == Token.TPRLN
		|| currentToken.value() == Token.TIDEN){
			return new TreeNode(TreeNode.NSTATS, node, stats());
		}else{
			return node;
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
		}else if(currentToken.value() == Token.TINPT ||
				 currentToken.value() == Token.TPRIN ||
				 currentToken.value() == Token.TPRLN){
			return iostat();
		}else{
			return statb();
		}
	}
	//<statb> ::= <asgnstat> | <callstat> 
	private TreeNode statb(){
		TreeNode node = new TreeNode(TreeNode.NUNDEF);		
		//need TIDEN token
		if(!checkToken(Token.TIDEN, "Invalid statement declaration: Invalid Keyword.")) return node;
		currentToken = scanner.getToken();		
		StRec stRec = new StRec();
		stRec.setName(currentToken.getStr());
		node.setSymbol(stRec);

		
		if(currentToken.value() == Token.TLPAR){
			return callstat(node);
		}else{
			return asgnstat(node);
		}
	}
	// NFOR
	//<forstat> ::= for ( <asgnlist> ; <bool> ) <stats> end
	private TreeNode forstat(){
		String errMsg = "Invalid For structure: ";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		//need TFOR token
		if(!checkToken(Token.TFOR, errMsg+"Keyword missing: Not found 'FOR'.")) return node;
		currentToken = scanner.getToken();		
		//need TLPAR token
		if(!checkToken(Token.TLPAR, errMsg+"Not found '('.")) return node;
		currentToken = scanner.getToken();		
		node.setLeft(asgnlist());
		//need TSEMI token
		if(!checkToken(Token.TSEMI, errMsg+"Not found ';'.")) return node;
		currentToken = scanner.getToken();		
		node.setMiddle(bool());
		//Cneed TRPAR token
		if(!checkToken(Token.TRPAR, errMsg+"Not found ')'.")) return node;
		currentToken = scanner.getToken();		
		node.setRight(stats());
		//need TEND token
		if(!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'.")) return node;
		currentToken = scanner.getToken();		
		node.setValue(TreeNode.NFOR);
		return node;
	}
	// NREPT
	//<repstat> ::= repeat ( <asgnlist> ) <stats> until <bool>
	private TreeNode repstat(){
		String errMsg = "Invalid repeat structure: ";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		//need TREPT token
		if(!checkToken(Token.TREPT, errMsg+"Keyword missing: Not found 'REPEAT'.")) return node;
		currentToken = scanner.getToken();		
		//need TLPAR token
		if(!checkToken(Token.TLPAR, errMsg+"Not found '('.")) return node;
		currentToken = scanner.getToken();		
		node.setLeft(asgnlist());
		//need TRPAR token
		if(!checkToken(Token.TRPAR, errMsg+"Not found ')'.")) return node;
		currentToken = scanner.getToken();		
		node.setMiddle(stats());
		//need TUNTL token
		if(!checkToken(Token.TUNTL, errMsg+"Keyword missing: Not found 'UNTIL'.")) return node;
		currentToken = scanner.getToken();		
		node.setRight(bool());
		node.setValue(TreeNode.NREPT);
		return node;
	}
	
	// <asgnlist> ::= <alist> | ε
	private TreeNode asgnlist(){
		if(currentToken.value() != Token.TIDEN)return null;
		return alist();
	}
	//<alist> ::= <id> <asgnstat> <alistb>
	private TreeNode alist(){
		
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		StRec stRec = new StRec();			
		stRec.setName(currentToken.getStr());
		//node.setType(stRec);
		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);
		currentToken = scanner.getToken();
		return alistb(asgnstat(node));
	}
	// NASGNS	
	// <alistb> ::= ε | , <alist> 
	private TreeNode alistb(TreeNode node){
		if(currentToken.value() == Token.TCOMA){
			//get next token
			currentToken = scanner.getToken();
			return new TreeNode(TreeNode.NASGNS,node,alist());
		}else{
			return node;
		}
	}
	
	// <asgnstat> ::= <var> <asgnop> <bool>
	private TreeNode asgnstat(TreeNode node){
		//TreeNode node = new TreeNode(TreeNode.NUNDEF);
		node.setLeft(var(stRec));
		// <asgnop> ::= = | += | -= | *= | /= // NASGN, NPLEQ, NMNEQ, NSTEQ, NDVEQ
		if(currentToken.value() == Token.TEQUL){
			node.setValue(TreeNode.NASGN);
			currentToken = scanner.getToken();
		}else if(currentToken.value() == Token.TPLEQ){
			node.setValue(TreeNode.NPLEQ);
			currentToken = scanner.getToken();
		}else if(currentToken.value() == Token.TMNEQ){
			node.setValue(TreeNode.NMNEQ);
			currentToken = scanner.getToken();
		}else if(currentToken.value() == Token.TSTEQ){
			node.setValue(TreeNode.NSTEQ);
			currentToken = scanner.getToken();
		}else if(currentToken.value() == Token.TDVEQ){
			node.setValue(TreeNode.NDVEQ);
			currentToken = scanner.getToken();
		}else{
			checkToken(Token.TEQUL, "Invalid assignment: Not found assignment operator.");
		}
		node.setRight(bool());
		return node;
	}


	//Special  
	//<ifstat> ::= if ( <bool> ) <stats> <ifstatb>
	private TreeNode ifstat(){
		String errMsg = "Invalid if statement: ";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		//need TIFTH token
		if(!checkToken(Token.TIFTH, errMsg+"Keyword missing: Not found 'IF'")) return node;
		currentToken = scanner.getToken();		
		//need TLPAR token
		if(!checkToken(Token.TLPAR, errMsg+"Not found '('")) return node;
		currentToken = scanner.getToken();		
		node.setLeft(bool());
		//need TRPAR token
		if(!checkToken(Token.TRPAR, errMsg+"Not found ')'")) return node;
		currentToken = scanner.getToken();		
		node.setRight(stats());		

		return ifstatb(node);;
		
	}
	//NIFTH NIFTE 
	//<ifstatb> ::= end | else <stats> end
	private TreeNode ifstatb(TreeNode node){
		//NIFTH or NIFTE
		if(currentToken.value() == Token.TELSE){
			currentToken = scanner.getToken();				
			node.setMiddle(node.getRight());
			node.setRight(stats());
			//need TEND token
			if(!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'")) return node;	
			node.setValue(TreeNode.NIFTE);
			currentToken = scanner.getToken();
		}else{
			//need TEND token
			if(!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'")) return node;
			node.setValue(TreeNode.NIFTH);
			currentToken = scanner.getToken();
			
		}
		return node

	}

	// NINPUT  NPRINT  NPRLN
	private TreeNode iostat(){
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		if(currentToken.value() == Token.TINPT){
			currentToken = scanner.getToken();			
			node.setValue(TreeNode.NINPUT);
			node.setLeft(vlist());
		}else if(currentToken.value() == Token.TPRIN){
			currentToken = scanner.getToken();			
			node.setValue(TreeNode.NPRINT);
			node.setLeft(prlist());
		}else if(currentToken.value() == Token.TPRLN){
			currentToken = scanner.getToken();			
			node.setValue(TreeNode.NPRLN);
			node.setLeft(prlist());
		}else{
			checkToken(Token.TINPT, "Invalid I/O statement.");
		}
		return node;
	}
	// NCALL
	//<callstat> ::= ( <callstatb> ) 
	//<callstatb> ::= <elist> | ε
	private TreeNode callstat(TreeNode node){
		String errMsg = "Invalid call statement: ";
		//need TLPAR token
		if(!checkToken(Token.TLPAR, errMsg+"Not found '('.")) return node;
		currentToken = scanner.getToken();		
		if(currentToken.value() == Token.TRPAR){
			//get next token
			currentToken = scanner.getToken();			
		}else{
			node.setLeft(elist());
			//need right paranthesis
			if(!checkToken(Token.TRPAR, errMsg+"Not found ')'.")) return node;
			currentToken = scanner.getToken();			
		}
		node.setValue(TreeNode.NCALL);
		return node;
	}
	
	// NRETN
	//<returnstat> ::= return <returnstatb>	
	//<returnstatb> ::= <expr> | ε 
	private TreeNode returnstat(){
		TreeNode node = new TreeNode(TreeNode.NRETN);
		//need TRETN token
		if(!checkToken(Token.TRETN, "Invalid return statement.")) return node;
		currentToken = scanner.getToken();	
		if(currentToken.value() == Token.TIDEN 
			|| currentToken.value() == Token.TILIT
			|| currentToken.value() == Token.TFLIT
			|| currentToken.value() == Token.TTRUE
			|| currentToken.value() == Token.TFALS
			|| currentToken.value() == Token.TLPAR){
			node.setLeft(expr());
		}
		return node;

	}

	//???
	// Special				
	// <vlist> ::= <id> <var> <vlistb>
	private TreeNode vlist(){
		if(!checkToken(Token.TIDEN, )) return node;
		StRec stRec = new StRec();			
		stRec.setName(currentToken.getStr());
		//node.setType(stRec);
		symbolTable.put(stRec.getName(), stRec);
		if (currentToken.value() == Token.TIDEN)
		TreeNode node = var();
		node.setRight(vlistb());
		return node;
	}
????????	
	//???
	// NVLIST				
	// <vlistb> ::= , <vlist> | ε
	private TreeNode vlistb(TreeNode node){
		if(currentToken.value() != Token.TCOMA)	return null;
		//get next Token
		currentToken = scanner.getToken();		
		return vlist();
	}
	
	// NSIMV  NARRV
	//<var> ::= ε | [<expr>] . <id>
	private TreeNode var(StRec stRec){
		String errMsg = "Invalid variable declaration: ";

		// NSIMV or NARRV
		if(currentToken.value() == Token.TLBRK){
			TreeNode node = new TreeNode(TreeNode.NARRV);
			//get next token
			currentToken = scanner.getToken();
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			node.setLeft(expr());
			//need TRBRK token 
			if(!checkToken(Token.TRBRK, errMsg+"Not found ']'.")) return node;
			currentToken = scanner.getToken();
			//need TDOT token
			if(!checkToken(Token.TDOT, errMsg+"Not found '.'.")) return node;
			currentToken = scanner.getToken();
			//need TIDEN token 
			if(!checkToken(Token.TIDEN, errMsg+".")) return node;
			StRec stRec2 = new StRec(currentToken.getStr());
			currentToken = scanner.getToken();
			//????
			node.setType(stRec2);//???????????
			symbolTable.put(stRec.getName(), stRec2);
			return node;
		}else{
			TreeNode node = new TreeNode(TreeNode.NSIMV);
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}
	}
	
	//<elist> ::= <bool> <elistb>
	private TreeNode elist(){
		TreeNode node = bool();
		return elistb(node);
	}
	
	// NEXPL	
	// <elistb> ::= , <elist> | ε 
	private TreeNode elistb(TreeNode node){
		if(currentToken.value() == Token.TCOMA){
			//get next token
			currentToken = scanner.getToken();	
			return new TreeNode(TreeNode.NEXPL, node, elist());
		}else{
			return node;
		}
	}
//????
	//<bool> ::=  <rel> <boolb> 
	private TreeNode bool(){
		TreeNode node = new TreeNode(TreeNode.NBOOL);
		node.setLeft(rel());
		node.setRight(boolb());		
		return node;
	}
	// NBOOL
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
//???????	
	// NNOT
	//<rel> ::= not <expr> <relop> <expr> | <expr> <relb> 
	private TreeNode rel(){
		TreeNode node = new TreeNode(TreeNode.NBOOL);
		if(currentToken.value() == Token.TNOT){
			node.setValue(TreeNode.NNOT);
			//get next Token
			currentToken = scanner.getToken();
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
			currentToken = scanner.getToken();			
			node.setValue(TreeNode.NAND);
		}else if(currentToken.value() == Token.TOR){
			currentToken = scanner.getToken();			
			node.setValue(TreeNode.NOR);
		}else if(currentToken.value() == Token.TXOR){
			currentToken = scanner.getToken();			
			node.setValue(TreeNode.NXOR);
		}else{
			checkToken(Token.TAND, "Invalid logic operation.");
			return node;
		}
		return node;
	}
	
	// NEQL, NNEQ, NGRT, NLEQ, NLSS, NGEQ
	// <relop> ::= == | != | > | <= | < | >=
	private TreeNode relop(){
		TreeNode node  = new TreeNode(TreeNode.NUNDEF);
		if(currentToken.value() == Token.TEQEQ){
			currentToken = scanner.getToken();
			node.setValue(TreeNode.NEQL);
		}
		else if(currentToken.value() == Token.TNEQL){
			currentToken = scanner.getToken();
			node.setValue(TreeNode.NNEQ);
		}
		else if(currentToken.value() == Token.TGRTR){
			currentToken = scanner.getToken();
			node.setValue(TreeNode.NGRT);
		}
		else if(currentToken.value() == Token.TLEQL){
			currentToken = scanner.getToken();
			node.setValue(TreeNode.NLEQ);
		}
		else if(currentToken.value() == Token.TLESS){
			currentToken = scanner.getToken();
			node.setValue(TreeNode.NLSS);
		}
		else if(currentToken.value() == Token.TGEQL){
			currentToken = scanner.getToken();
			node.setValue(TreeNode.NGEQ);
		}
		else{
			checkToken(Token.TEQEQ, "Invalid logic operation.");
			return node;
		}
		return node;
	}
	// plus-minus method
	// <expr> ::= <term> <exprb>
	private TreeNode expr(){
		TreeNode node = term();
		return exprb(node);
	}
	
	// NADD  NSUB
	//<exprb> ::= + <term> <exprb> | - <term> <exprb> | ε
	private TreeNode exprb(TreeNode node){
		TreeNode parent;
		if(currentToken.value() == Token.TPLUS){
			currentToken = scanner.getToken();
			parent = new TreeNode(TreeNode.NADD,node,term());
			return(exprb(parent));
		}else if(currentToken.value() == Token.TMINS){
			currentToken = scanner.getToken();
			parent = new TreeNode(TreeNode.NSUB,node,term());
			return(exprb(parent));
		}else{
			return node;
		}
		
	}
	
	// multiply operation
	//<term> ::= <fact> <termb>
	private TreeNode term(){
		TreeNode node = fact();
		return termb(node);
	}
	
	// NMUL, NDIV, NMOD
	//<termb> ::= * <fact> <termb> | / <fact> <termb> | % <fact> <termb> | ε
	private TreeNode termb(TreeNode node){
		TreeNode parent;
		if(currentToken.value() == Token.TSTAR){
			currentToken = scanner.getToken();
			parent = new TreeNode(TreeNode.NMUL);
			parent.setLeft(node);
			parent.setRight(fact());
			return(termb(parent));
		}else if(currentToken.value() == Token.TDIVD){
			currentToken = scanner.getToken();
			parent = new TreeNode(TreeNode.NDIV);
			parent.setLeft(node);
			parent.setRight(fact());
			return(termb(parent));
		}else if(currentToken.value() == Token.TPERC){
			currentToken = scanner.getToken();
			parent = new TreeNode(TreeNode.NMOD);
			parent.setLeft(node);
			parent.setRight(fact());
			return(termb(parent));
		}else{
			return node;
		}
	}
	
	// exponential operation
	//<fact> ::=  <exponent> <factb> 
	private TreeNode fact(){
		TreeNode node = exponent();
		return factb(node);
	}
	
	// NPOW 
	//<factb> ::=  ^ <exponent> <factb> | ε
	private TreeNode factb(TreeNode node){
		TreeNode parent;
		if(currentToken.value() == Token.TCART){
			currentToken = scanner.getToken();
			parent = new TreeNode(TreeNode.NPOW);
			parent.setLeft(node);
			parent.setRight(exponent());
			return(factb(parent));
		}else{
			return node;
		}
	}

	// NILIT,  NFLIT,  NTRUE,  NFALS
	//<exponent> ::= <intlit> | <reallit> | true | false | <id> <exponentb> | ( <bool> )  

	private TreeNode exponent(){
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		StRec stRec = new StRec();
		if(currentToken.value() == Token.TILIT){
			node.setValue(TreeNode.NILIT);
			stRec.setName(currentToken.getStr());
			//get next token
			currentToken = scanner.getToken();
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}else if(currentToken.value() == Token.TFLIT){
			node.setValue(TreeNode.NFLIT);
			stRec.setName(currentToken.getStr());
			//get next token
			currentToken = scanner.getToken();
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}else if(currentToken.value() == Token.TTRUE){
			node.setValue(TreeNode.NTRUE);
			stRec.setName(currentToken.getStr());
			//get next token
			currentToken = scanner.getToken();
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}else if(currentToken.value() == Token.TFALS){
			node.setValue(TreeNode.NFALS);
			stRec.setName(currentToken.getStr());
			//get next token
			currentToken = scanner.getToken();
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}else if(currentToken.value() == Token.TIDEN){
			return exponentb();	
		}else{
			//need TLPAR token
			if(!checkToken(Token.TLPAR, "Invalid exponent operation: Not found '('. ")) return node;
			currentToken = scanner.getToken();
			TreeNode tempNode = bool();
			//need TRPAR token
			if(!checkToken(Token.TRPAR, "Invalid exponent operation: Not found ')'. ")) return node;
			currentToken = scanner.getToken();
			return tempNode;
		}
	}
	//<exponentb> ::= <var> | <fncall> 
	private TreeNode exponentb(){
		
		//ID token
		StRec stRec = new StRec();
		stRec.setName(currentToken.getStr());
		currentToken = scanner.getToken();	
		if (currentToken.value() == Token.TLPAR){
			return fncall(stRec);
		}else{
			return var(stRec);
		}

		
	}

	// NFCALL
	//<fncall> ::= ( <fncallb> 
	private TreeNode fncall(StRec stRec){
		String errMsg = "Invalid function call: ";
		TreeNode node = new TreeNode(TreeNode.NFCALL);
		currentToken = scanner.getToken();		
		
		
		node.setLeft(fncallb());
		//Check for right paranthesis token
		if(!checkToken(Token.TRPAR, errMsg+"Not found ')'.")) return node;
		currentToken = scanner.getToken();		
		
		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);
		return node;
	}
	
	// <fncallb> ::= <elist> ) | )
	private TreeNode fncallb(){
		
		if(currentToken.value() != Token.TRPAR){
			return elist();
		}else{
			return null;
		}
		
	}
	
	//<prlist> ::= <printitem> <prlistb>
	private TreeNode prlist(){
		TreeNode node = printitem();
		return prlistb(node);
	}
	
	// NPRLST	
	// <prlistb> ::= , <prlist> | ε
	private TreeNode prlistb(TreeNode node){
		if(currentToken.value() == Token.TCOMA){
			//get next token
			currentToken = scanner.getToken();	
			return new TreeNode(TreeNode.NPRLST, node, prlist());
		}else{
			return node;
		}
	}
	
	// NSTRG
	//<printitem> ::= <expr> | <string>
	private TreeNode printitem(){
		if(currentToken.value() == Token.TSTRG){
			TreeNode node = new TreeNode(TreeNode.NSTRG);
			StRec stRec = new StRec(currentToken.getStr());
			//get next token
			currentToken = scanner.getToken();
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}
		return expr();
		
	}

}