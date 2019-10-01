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
	
	// 
	private TreeNode types(){
		if (currentToken.value() != Token.TTYPS)return null;

		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();

		return typelist();
	}
	
	// 
	private TreeNode arrays(){
		if (currentToken.value() != Token.TARRS)return null;

		//get next token
		currentToken = followToken;
		followToken = scanner.getToken();

		return arrdecls();
	}
	
	// NFUNCS
	private TreeNode funcs(){
		TreeNode node = new TreeNode(TreeNode.NFUNCS);
		if (currentToken.value() != Token.TFUNC)return null;

		node.setLeft(func());
		node.setRight(funcs());

		return node;
	}
	
	// NMAIN
	private TreeNode mainbody(){
		
		TreeNode node = new TreeNode(TreeNode.NMAIN);
		//need TMAIN token
		if (!checkToken(Token.TMAIN, "Invalid Mainbody: Keyword missing, Not found 'MAIN'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setLeft(slist());

		//need TBEGN token
		if (!checkToken(Token.TBEGN, "Invalid Mainbody: Keyword missing, Not found 'BEGIN'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setRight(stats());

		//need TEND token
		if (!checkToken(Token.TEND, "Invalid Mainbody: Keyword missing, Not found 'END'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TCD19 token
		if (!checkToken(Token.TCD19, "Invalid Mainbody: Keyword missing, Not found 'CD19'.")) return null;
		currentToken = followToken;
		followToken = scanner.getToken();

		//need TIDEN token
		if (!checkToken(Token.TIDEN, "Invalid Mainbody: Not found ID name.")) return null;
		
		//need TEOF token
		currentToken = followToken;
		if (!checkToken(Token.TEOF, "Invalid Mainbody: Terminator not found.")) return null;

		return node;
	}
	
	// NSDLST
	private TreeNode slist(){
		TreeNode node = new TreeNode(TreeNode.NSDLST);
		//Enter leftNode node
		TreeNode sdecimal = sdecl();

		if (currentToken.value() != Token.TCOMA){
			return sdecimal;
		}

		//consume token
		currentToken = followToken;
		followToken = scanner.getToken();

		node.setLeft(sdecimal);
		node.setRight(slist());

		return node;
	}
	
	// 
	private TreeNode slistb(){
		
		return null;
	}
	
	// NTYPEL
	private TreeNode typelist(){
		
		return null;
	}
	
	// 
	private TreeNode typelistb(){
		
		return null;
	}
	
	// NRTYPE  NATYPE
	private TreeNode type(){
		
		return null;
	}
	
	// NFLIST
	private TreeNode fields(){
		
		return null;
	}
	
	// 
	private TreeNode fieldsb(){
		
		return null;
	}
	
	// NSDECL
	private TreeNode sdecl(){
		
		return null;
	}
	
	// NALIST
	private TreeNode arrdecls(){
		
		return null;
	}
	
	// 
	private TreeNode arrdeclsb(){
		
		return null;
	}
	
	// NARRD
	private TreeNode arrdecl(){
		
		return null;
	}
	
	// NFUND
	private TreeNode func(){
		
		return null;
	}
	
	// 
	private TreeNode rtype(){
		
		return null;
	}
	
	// NPLIST
	private TreeNode plist(){
		
		return null;
	}
	
	// NSIMP
	private TreeNode params(){
		
		return null;
	}
	
	// 
	private TreeNode paramsb(){
		
		return null;
	}
	
	// NARRP
	private TreeNode param(){
		
		return null;
	}
	
	// 
	private TreeNode funcbody(){
		
		return null;
	}
	
	// NARRC
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
	
	// 
	private TreeNode decl(){
		
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
	
	// 
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