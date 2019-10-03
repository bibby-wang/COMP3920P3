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
	public Parser(Scanner scanner){
		this.scanner = scanner;
		symbolTable = new SymbolTable(null);
	}
	
	// check each token for errors and return the appropriate information.
	private boolean checkToken(int expected, String message){
		//test
		System.out.println("===cT: " + currentToken.value());
		
		if (currentToken.value() != expected){	
			if (currentToken.value() == Token.TUNDF){
				//the undefined token TUNDF
				System.out.println("=Error(Lexical): " + currentToken.getStr());
			}else{
				System.out.println("=Error(Syntax): " + message);
			}
			return false;
		}
		return true;
	}

	// NPROG
	// <program> ::= CD19 <id> <globals> <funcs> <mainbody>
	public TreeNode program(){
		
		
		TreeNode node = new TreeNode(TreeNode.NPROG);
		StRec stRec = new StRec();
		//need CD19 token
		currentToken = scanner.getToken();
		followToken = scanner.getToken();
		if (!checkToken(Token.TCD19, "Not found the Keyword 'CD19'")) return null;
		
		//need TIDEN token
		currentToken = followToken;
		followToken = scanner.getToken();
		if (!checkToken(Token.TIDEN, "Invalid initialisation: Not found ID name.")) return null;
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
	
	// constants
	// <consts> ::= constants <initlist> | ε
	private TreeNode consts(){
		if (currentToken.value() != Token.TCONS) return null;
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
		if (currentToken.value() != Token.TCOMA) return null;
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();

		return initlist();
	}
	
	// NINIT
	//<init> ::= <id> = <expr>
	private TreeNode init(){
		TreeNode node = new TreeNode(TreeNode.NINIT);
		StRec stRec = new StRec();

		//need TIDEN token
		if (!checkToken(Token.TIDEN, "Invalid initialisation: Not found ID name.")) return null;
		stRec.setName(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TEQUL token
		if (!checkToken(Token.TEQUL, "Invalid initialisation: Not found '=' .")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setLeft(expr());
		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);

		return node;
	}
	
	// <types> ::= types <typelist> | ε
	private TreeNode types(){
		if (currentToken.value() != Token.TTYPS)return null;

		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();

		return typelist();
	}
	
	// <arrays> ::= arrays <arrdecls> | ε
	private TreeNode arrays(){
		if (currentToken.value() != Token.TARRS)return null;

		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();

		return arrdecls();
	}
	
	// NFUNCS
	//<funcs> ::= <func> <funcs> | ε
	private TreeNode funcs(){
		TreeNode node = new TreeNode(TreeNode.NFUNCS);
		if (currentToken.value() != Token.TFUNC)return null;

		node.setLeft(func());
		node.setRight(funcs());

		return node;
	}
	
	// NMAIN
	//<mainbody> ::= main <slist> begin <stats> end CD19 <id>
	private TreeNode mainbody(){
		String errMsg="Invalid Mainbody: ";
		TreeNode node = new TreeNode(TreeNode.NMAIN);
		//need TMAIN token
		if (!checkToken(Token.TMAIN, errMsg+: Keyword missing: Not found 'MAIN'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setLeft(slist());

		//need TBEGN token
		if (!checkToken(Token.TBEGN, errMsg+"Keyword missing: Not found 'BEGIN'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setRight(stats());

		//need TEND token
		if (!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TCD19 token
		if (!checkToken(Token.TCD19, errMsg+"Keyword missing: Not found 'CD19'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TIDEN token
		if (!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return null;
		
		//need TEOF token
		currentToken = followToken;
		if (!checkToken(Token.TEOF, errMsg+"Terminator not found.")) return null;

		return node;
	}
	
	

	// NSDLST
	//<slist> ::= <id> : <stype> <slistb>
	private TreeNode slist(){
		TreeNode node = new TreeNode(TreeNode.NSDLST);
		String errMsg="Invalid Mainbody: ";
		StRec stRec = new StRec();
		//need TIDEN token
		if (!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return null;
		stRec.setName(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setSymbol(stRec);
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
	
		//need TCOLN token
		if (!checkToken(Token.TCOLN, errMsg+"Not found ':'."))return null;

		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setLeft(stype());
		node.setRight(slistb());

		return node;
	}
	
	// <slistb> ::= , <slist> | ε
	private TreeNode slistb(){
		
		if (currentToken.value() != Token.TCOMA)return null;
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
		if (currentToken.value() != Token.TIDEN)return null;
		return typelist();
	}

	// NRTYPE  NATYPE
	//<type> ::= <structid> is <fields> end | <typeid> is array [ <expr> ] of <structid>
	private TreeNode type(){
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		StRec stRec = new StRec();//first id
		String errMsg="Invalid struct or array declaration: ";
		
		//need TIDEN token
		if (!checkToken(Token.TIDEN, errMsg+"Not found StructID or TypeID name.")) return null;
		stRec.setName(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setSymbol(stRec);

		// need TIS token
		if (!checkToken(Token.TIS, errMsg+"Keyword missing: Not found 'IS'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		//NRTYPE node or NATYPE node
		if (currentToken.value() != Token.TARAY){
			//NRTYPE
			node.setValue(TreeNode.NRTYPE);
			node.setLeft(fields());
			//need TEND token
			if (!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'.")) return null;
			currentToken = followToken;
			followToken = scanner.getToken();
			stRec.setType("Struct");
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}
		//NATYPE
		node.setValue(TreeNode.NATYPE);
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TLBRK token
		if (!checkToken(Token.TLBRK, errMsg+"Not found '['.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setLeft(expr());

		//need TRBRK token
		if (!checkToken(Token.TRBRK, errMsg+"Not found ']'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		
		stRec.setType("Type");
		symbolTable.put(stRec.getName(), stRec);
		
		//need TOF token
		if (!checkToken(Token.TOF, errMsg+"Keyword missing: Not found 'OF'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TIDEN token
		if (!checkToken(Token.TIDEN, errMsg+"Not found StructID name.")) return null;
		
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

	// NFLIST
	//<fields> ::= <id> : <stype> <fieldsb>
	private TreeNode fields(){

		TreeNode node = new TreeNode(TreeNode.NFLIST);
		StRec stRec = new StRec();
		String errMsg="Invalid struct declaration: ";
		
		//need TIDEN token
		if (!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return null;
	
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
	
		//need TCOLN token
		if (!checkToken(Token.TCOLN, errMsg+"Not found ':'."))return null;

		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
???
		node.setLeft(id());
		node.setLeft(stype());
		node.setRight(fieldsb());

		return node;		
		
	}
	
	// <fieldsb> ::= , <fields> | ε
	private TreeNode fieldsb(){
		

		if (currentToken.value() != Token.TCOMA)return null;
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
		
		return fields();
	}
	
	// NSDECL
	//<sdecl> ::= <id> : <stype>
	private TreeNode sdecl(){
		String errMsg = "Invalid variable declaration: ";
		TreeNode node = new TreeNode(TreeNode.NSDECL);
		StRec stRec = new StRec();
		
		//need TIDEN token
		if (!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return null;
		stRec.setName(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TCOLN token
		if (!checkToken(Token.TCOLN, errMsg+"Not found ':'")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		//Resolution type TINTG or TREAL or TBOOL token
		//<stype> ::= integer | real | boolean
		if (currentToken.value() == Token.TINTG){
			stRec.setType("integer");
		}else if (currentToken.value() == Token.TREAL){
			stRec.setType("real");
		}else if (currentToken.value() == Token.TBOOL){
			stRec.setType("boolean");
		}else{
			if (!checkToken(Token.TINTG, errMsg+"Not found integer or real or boolen type")) return null;
		}
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);
		return node;		

	}
	
	// NALIST
	//<arrdecls> ::= <id> : <typeid> <arrdeclsb>
	private TreeNode arrdecls(){
		TreeNode node = new TreeNode(TreeNode.NALIST);
		String errMsg="Invalid struct declaration: ";
		
		//need TIDEN token
		if (!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return null;
	
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
		
		//need TCOLN token
		if (!checkToken(Token.TCOLN, errMsg+"Not found ':'")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TIDEN token
		if (!checkToken(Token.TIDEN, errMsg+"Not found TypeID name.")) return null;
		
		return node;
	}
	
	// <arrdeclsb> ::= , <arrdecls> | ε
	private TreeNode arrdeclsb(){
		if (currentToken.value() != Token.TCOMA) return null;	
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
		return arrdecls()
	}
	
	// NARRD
	//<arrdecl> ::= <id> : <typeid>
	private TreeNode arrdecl(){
		String errMsg = "Invalid array declaration: ";
		TreeNode node = new TreeNode(TreeNode.NARRD);
		StRec stRec = new StRec();

		//need TIDEN token
		if (!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return null;
		stRec.setName(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TCOLN token
		if (!checkToken(Token.TCOLN, errMsg+"Not found ':'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();
		
		//need TIDEN token
		if (!checkToken(Token.TIDEN, errMsg+"Not found TypeID name.")) return null;
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
		if (!checkToken(Token.TFUNC, errMsg+"Keyword missing: Not found 'FUNCTION'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		//neeed TIDEN token
		if (!checkToken(Token.TIDEN, errMsg+"Not found ID name.")) return null;
		stRec.setName(currentToken.getStr());
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TLPAR token
		if (!checkToken(Token.TLPAR, errMsg+"Not found '('.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setLeft(plist());

		//need TRPAR token
		if (!checkToken(Token.TRPAR, errMsg+"Not found ')'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TCOLN token
		if (!checkToken(Token.TCOLN, errMsg+"Not found ':'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		//Resolution type TINTG or TREAL or TBOOL token
		//<rtype> ::= integer | real | boolean | void
		if (currentToken.value() == Token.TINTG){
			stRec.setType("integer");
		}else if (currentToken.value() == Token.TREAL){
			stRec.setType("real");
		}else if (currentToken.value() == Token.TBOOL){
			stRec.setType("boolean");
		}else if (currentToken.value() == Token.TVOID){
			stRec.setType("void");
		}else{
			if (!checkToken(Token.TINTG, errMsg+"Not found integer or real or boolen or void type.")) return null;
		}
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setMiddle(locals());

		//need TBEGN token
		if (!checkToken(Token.TBEGN, errMsg+"Keyword missing: Not found 'BEGIN'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setRight(stats());

		//need TEND token
		if (!checkToken(Token.TEND, errMsg+"Keyword missing: Not found 'END'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);

		return node;
	}
	

	// <plist> ::= <params> | ε
	private TreeNode plist(){
		if (currentToken.value() == Token.TIDEN || currentToken.value() == Token.TCONS){
			return params();
		}
		return null;
	}
	
	//  NPLIST
	//<params> ::= <param> <paramsb> 
	private TreeNode params(){
		TreeNode node = new TreeNode(TreeNode.NPLIST);
		node.setLeft(params());
		node.setRight(paramsb());
		return node;
	}
	
	//  <paramsb> ::= , <params> | ε
	private TreeNode paramsb(){
		if (currentToken.value() != Token.TCOMA)return null;
		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();
		
		return params();
	}
	
	// NSIMP NARRP NARRC
	//<param> ::= const <id> : <typeid> | <decl>
	private TreeNode param(){
		??
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		if (currentToken.value() == Token.TCONS){
			//get next token
			currentToken = followToken;
			followToken = scanner.getToken();

			node.setValue(TreeNode.NARRC);
			node.setLeft(arrdecl());
			return node;
		}

		TreeNode check = decl();
		if (check.getValue() == TreeNode.NARRD){
			node.setValue(TreeNode.NARRP);
		}else if (check.getValue() == TreeNode.NSDECL){
			node.setValue(TreeNode.NSIMP);
		}else{
			return null;
		}
		node.setLeft(check);
		return node;
	}
	
	
	//
	private TreeNode locals(){
		
		return null;
	}
	
	// NDLIST
	private TreeNode dlist(){
		
		return null;
	}
	
	// 
	private TreeNode dlistb(){
		
		return null;
	}
	
	// <decl> ::= <id> : <declb>
	private TreeNode decl(){
		
		return null;
	
	}
	
	//<declb> ::= <stype> | <typeid>
	private TreeNode declb(){
		
		return null;
	}
	
	// 
	private TreeNode stype(){
		
		return null;
	}
	
	// NSTATS
	private TreeNode stats(){
		
		return null;
	}
	
	// 
	private TreeNode statsb(){
		
		return null;
	}
	
	// 
	private TreeNode strstat(){
		
		return null;
	}
	
	// 
	private TreeNode stat(){
		
		return null;
	}
	
	// NFOR
	private TreeNode forstat(){
		
		return null;
	}
	
	// NREPT
	private TreeNode repstat(){
		
		return null;
	}
	
	// 
	private TreeNode asgnlist(){
		
		return null;
	}
	
	// NASGNS
	private TreeNode alist(){
		
		return null;
	}
	
	// 
	private TreeNode alistb(){
		
		return null;
	}
	
	// 
	private TreeNode asgnstat(){
		
		return null;
	}
	
	// 
	private TreeNode asgnop(){
		
		return null;
	}
		
	// NIFTH 
	private TreeNode ifstat(){
		
		return null;
	}
	
	// NIFTE
	private TreeNode ifstatb(){
		
		return null;
	}
	
	// NINPUT  NPRINT  NPRLN
	private TreeNode iostat(){
		
		return null;
	}
	
	// NCALL
	private TreeNode callstat(){
		
		return null;
	}
	
	// 
	private TreeNode callstatb(){
		
		return null;
	}
	
	// NRETN
	private TreeNode returnstat(){
		
		return null;
	}
	
	// 
	private TreeNode returnstatb(){
		
		return null;
	}
	
	// NVLIST
	private TreeNode vlist(){
		
		return null;
	}
	
	// 
	private TreeNode vlistb(){
		
		return null;
	}
	
	// NSIMV  NARRV
	private TreeNode var(){
		
		return null;
	}
	
	// 
	private TreeNode varb(){
		
		return null;
	}
	
	// NEXPL
	private TreeNode elist(){
		
		return null;
	}
	
	// 
	private TreeNode elistb(){
		
		return null;
	}
	

////////////////////////////////////////////////////
////////////////////////////////////////////////////
////////////////////////////////////////////////////
	// NBOOL
	private TreeNode bool(){
		TreeNode node = new TreeNode(TreeNode.NBOOL);
		node.setLeft(rel());
		node.setRight(boolb());		
		return node;
	}
	
	// 
	private TreeNode boolb(){
//仿照 termb()
		
		TreeNode parent;
		if (currentToken.value() == Token.TAND 
			|| currentToken.value() == Token.TOR 
			|| currentToken.value() == Token.TXOR){
			parent = ;
			parent.setLeft(logop());
			parent.setRight(rel());
			return boolb(parent);
		}else{
			return leftNode;
		}
	}
	
	// NNOT
	private TreeNode rel(){
		TreeNode node = new TreeNode(TreeNode.NNOT);
		TreeNode temp, temp2;

		if (currentToken.value() == Token.TNOT){
			//Consume Token
			currentToken = followToken;
			followToken = scanner.getToken();

			node.setLeft(expr());
			node.setMiddle(relop());
			node.setRight(expr());

			return node;
		}

		temp = expr();
		if (currentToken.value() == Token.TEQEQ || currentToken.value() == Token.TNEQL || currentToken.value() == Token.TGEQL ||  currentToken.value() == Token.TLEQL || currentToken.value() == Token.TGRTR || currentToken.value() == Token.TLESS){
			temp2 = relop();
			temp2.setLeft(temp);
			temp2.setRight(expr());
			return temp2;
		}
		else{
			return temp;
		}
	}
	
	// ?????
	private TreeNode relb(){
		
		return null;
	}
	
	// 
	private TreeNode logop(){

		TreeNode node  = new TreeNode(TreeNode.NUNDEF);

		if (currentToken.value() == Token.TAND){
			currentToken = followToken;
			followToken = scanner.getToken();
			node.setValue(TreeNode.NAND);
		}
		else if (currentToken.value() == Token.TOR){
			currentToken = followToken;
			followToken = scanner.getToken();
			node.setValue(TreeNode.NOR);
		}
		else if (currentToken.value() == Token.TXOR){
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
	
	// 
	private TreeNode relop(){

		TreeNode node  = new TreeNode(TreeNode.NUNDEF);

		if (currentToken.value() == Token.TEQEQ){
			currentToken = followToken;
			followToken = scanner.getToken();
			node.setValue(TreeNode.NEQL);
		}
		else if (currentToken.value() == Token.TNEQL){
			currentToken = followToken;
			followToken = scanner.getToken();
			node.setValue(TreeNode.NNEQ);
		}
		else if (currentToken.value() == Token.TGRTR){
			currentToken = followToken;
			followToken = scanner.getToken();
			node.setValue(TreeNode.NGRT);
		}
		else if (currentToken.value() == Token.TLEQL){
			currentToken = followToken;
			followToken = scanner.getToken();
			node.setValue(TreeNode.NLEQ);
		}
		else if (currentToken.value() == Token.TLESS){
			currentToken = followToken;
			followToken = scanner.getToken();
			node.setValue(TreeNode.NLSS);
		}
		else if (currentToken.value() == Token.TGEQL){
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
	
////////////////////////////////////////////////////
////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////
////////////////////////////////////////////////////
	
	// plus-minus method
	private TreeNode expr(){
		TreeNode tempNode;
		tempNode = term();
		return exprb(tempNode);
	}
	
	// NADD  NSUB
	private TreeNode exprb(TreeNode leftNode){
		TreeNode parent;
		if (currentToken.value() == Token.TPLUS){
			currentToken = followToken;
			followToken = scanner.getToken();
			parent = new TreeNode(TreeNode.NADD);
			parent.setLeft(leftNode);
			parent.setRight(term());
			return(exprb(parent));
		}else if (currentToken.value() == Token.TMINS){
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
	private TreeNode term(){
		TreeNode tempNode;
		tempNode = fact();
		return termb(tempNode);
	}
	
	// NMUL NDIV NMOD
	private TreeNode termb(TreeNode leftNode){
		TreeNode parent;
		if (currentToken.value() == Token.TSTAR){
			currentToken = followToken;
			followToken = scanner.getToken();
			parent = new TreeNode(TreeNode.NMUL);
			parent.setLeft(leftNode);
			parent.setRight(fact());
			return(termb(parent));
		}else if (currentToken.value() == Token.TDIVD){
			currentToken = followToken;
			followToken = scanner.getToken();
			parent = new TreeNode(TreeNode.NDIV);
			parent.setLeft(leftNode);
			parent.setRight(fact());
			return(termb(parent));
		}else if (currentToken.value() == Token.TPERC){
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
	
	// power
	private TreeNode fact(){
		TreeNode tempNode;
		tempNode = exponent();
		return factb(tempNode);
	}
	
	// NPOW 
	private TreeNode factb(TreeNode leftNode){
		TreeNode parent;
		if (currentToken.value() == Token.TCART){
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
	
	// NILIT  NFLIT  NTRUE  NFALS
	private TreeNode exponent(){

		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		StRec stRec = new StRec();
		if (currentToken.value() == Token.TILIT){
			node.setValue(TreeNode.NILIT);
			stRec.setName(currentToken.getStr());
			//get next token
			currentToken = followToken;
			followToken = scanner.getToken();
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}else if (currentToken.value() == Token.TFLIT){
			node.setValue(TreeNode.NFLIT);
			stRec.setName(currentToken.getStr());
			//get next token
			currentToken = followToken;
			followToken = scanner.getToken();
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}else if (currentToken.value() == Token.TIDEN){
			//check for fncall
			if (followToken.value() == Token.TLPAR){
				return fncall();
			}else{
				return var();
			}
		}else if (currentToken.value() == Token.TTRUE){
			node.setValue(TreeNode.NTRUE);
			stRec.setName(currentToken.getStr());
			//get next token
			currentToken = followToken;
			followToken = scanner.getToken();
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}else if (currentToken.value() == Token.TFALS){
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
			if (!checkToken(Token.TLPAR, "Invalid exponent operation: Not found '('. ")) return null;
			currentToken = followToken;
			followToken = scanner.getToken();

			TreeNode tempNode;
			tempNode = bool();

			//need TRPAR token
			if (!checkToken(Token.TRPAR, "Invalid exponent operation: Not found ')'. ")) return null;
			currentToken = followToken;
			followToken = scanner.getToken();

			return tempNode;
		}
	}
	
	// NFCALL
	private TreeNode fncall(){
		
		return null;
	}
	
	// 
	private TreeNode fncallb(){
		
		return null;
	}
	
	// NPRLST
	private TreeNode prlist(){
		
		return null;
	}
	
	// 
	private TreeNode prlistb(){
		
		return null;
	}
	
	// NSTRG
	private TreeNode printitem(){
		
		return null;
	}
	// private TreeNode id(){
		
		// return null;
	// }
	// private TreeNode structid(){
		
		// return null;
	// }
	// private TreeNode typeid(){
		
		// return null;
	// }
	// private TreeNode intlit(){
		
		// return null;
	// }
	// private TreeNode reallit(){
		
		// return null;
	// }
	// private TreeNode string(){
		
		// return null;
	// }

}