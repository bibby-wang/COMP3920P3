// The University of Newcastle
// School of Electrical Engineering and Computing
// COMP3290 Compiler Design
// Semester 2, 2019
// Project Part 1 A Scanner for CD19 (15%) 
// Due: August 30th 23:99
// Binbin.Wang C3214157
// 
// fixed error from feedback 17/09/2019
// 


import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Scanner{
	private ArrayList<String> strArrayLine;// Separated String for each row
	private boolean fileEOF;
	private String fileName;
	private int printCount;
	private int lineNum; // working on line number
	private int columnNum; // working on column number

	private char nextChar;
	private int nextType;	// next char type 
	private char[] tempLineChar; 
	private String tempLineStr;
	
	// Constructor
	public Scanner(){
		
	}

	// Constructor Initialization input String
	public Scanner(String name){
		// Initialization data
		fileEOF=false;
		fileName=name;
		strArrayLine= new ArrayList<String>();
		lineNum=0;

		// try open file get all data
		try {				
			BufferedReader sourceFile = new BufferedReader(new FileReader(fileName));
			String strLine;

			// Separated character by strArrayLine
			while ((strLine = sourceFile.readLine()) != null) {
				// get String for each row
				strArrayLine.add(this.delComment(strLine));
			}
			strArrayLine.add("\r\n");// add an empty line at the end
			sourceFile.close();

		} catch (IOException e) {
			// Error of read file
			System.out.println("Reading File Error!");
			fileEOF=true;
		}
	}
	
	// end of file
	public boolean eof(){
		return this.fileEOF;
	}
	// Locate Lexeme Column number
	private void nextLexemeColumn(){
		// find next lexeme starting line and column number
		while(lineNum < strArrayLine.size()){

			tempLineStr=strArrayLine.get(lineNum);
			int columnL=tempLineStr.length();
			tempLineChar=tempLineStr.toCharArray();
			while(columnNum<columnL){
				int currentType=getType(tempLineChar[columnNum]);
				if (currentType!=0) return;
				columnNum++;
			}
			columnNum=0;
			lineNum++;
		}
		if (lineNum>strArrayLine.size()){
			fileEOF=true;
		}
	}

	// get token
	public Token getToken(){
		String lexeme="";// lexeme String
		int tokenMark=0;
			
		// Locate the Column number 
		nextLexemeColumn();	
		int tempColumnNum=columnNum;		
		char currentChar=tempLineChar[columnNum]; // current Char 
		int currentType=getType(currentChar); // type of current Char		
		nextChar=tempLineChar[columnNum+1]; // next Char
		nextType=getType(nextChar);		// type of next Char		
	

		if (!fileEOF){
		
			// type of the Char
			// -1 Undefined
			// 0 (white space eg. space TAB)
			// 1 Delimiters or Operators
			// 2 number
			// 3 alphabet
			
			if (currentType != 0 ){
				if (currentType == 3){
					tokenMark=extractIdentWords();
				}else if(currentType == 2){
					tokenMark=extractNumber();
				}else if(currentType == 1){
					// string or other token
					if (tempLineChar[columnNum]=='"'){
						tokenMark=extractString();
					}else{
						tokenMark=extractOperators();
						// find the single "!" token will is undefined
						if (tokenMark==62) {
							columnNum--;
							tokenMark=extractUndefined();
						}
					}
				}else if(currentType == -1){
					tokenMark=extractUndefined();
				}
				
			}
			// GET lexeme 
			for(int i=tempColumnNum;i<columnNum;i++){
				lexeme += tempLineChar[i];
			}
		}
		// check Delimiters or Operators token 
		if (tokenMark<58 && tokenMark>31) lexeme=null;
		// tack off the "" in String
		if(tokenMark==61){
			String tempLexeme=lexeme.substring(1,(lexeme.length()-1));
			lexeme=tempLexeme;
			
		}
		Token token=new Token(tokenMark, lineNum+1, tempColumnNum+1, lexeme);
		if (tokenMark==0) fileEOF=true;
		return token;
	}
	
	// delete comments
	private String delComment(String strLine){		
		int commFlg=strLine.indexOf("/--");
		int strFlg=strLine.indexOf('"');
		if (strFlg>=0 && commFlg>=0){
			// have both marks, find the position of comments
			commFlg = findCommIndex(strLine,commFlg);
		}
		if(commFlg>=0){
			strLine=strLine.substring(0,commFlg);
		}
		strLine+=" "; // add an speace at end
		return strLine;
		
	}
	
	// find the comments index number
	private int findCommIndex(String pressStr, int endIndex){
		int commFlg=pressStr.indexOf("/--", endIndex);
		int strFlg=pressStr.indexOf('"');
		int count=0;
		// Count the quotation marks before comment symbol 
		while(strFlg < endIndex && strFlg >=0 ){
			strFlg=pressStr.indexOf('"' , strFlg+1);
			count++;
		}
		// even number of quotation marks, means The definition of the string is correct
		if (count%2 == 0){
			// return the index, after that are the comments
			return commFlg;
		}else{
			// if there more quotation marks behind 
			if(strFlg>0){
				
				commFlg=pressStr.indexOf("/--", strFlg);
				// get next comments marks 
				if (commFlg>0){
					// start next searches
					return findCommIndex(pressStr,commFlg);
				}
			}
			// not find comments 
			return -1;
		}	
	}
	
	// TIDEN = 58 
	// extract the Identifiers and Reserved Keywords
	private int extractIdentWords(){
		int tokenNmu=58;
		// check the end of the Identifiers and Reserved Keywords
		while((nextType>=2) || (tempLineChar[columnNum+1]=='_')){
			columnNum++;
			nextType=getType(tempLineChar[columnNum+1]);
		}
		columnNum++;
		return tokenNmu;  // TSTRG 

	}

	// extract the number 
	// Undefined type does not appear, at least one number
	private int extractNumber(){
		int tokenNmu=59;  // TILIT
		while(nextType==2){
			columnNum++;
			nextType=getType(tempLineChar[columnNum+1]);
		}
		// Determine if it is a decimal
		if ((tempLineChar[columnNum+1]=='.') && (getType(tempLineChar[columnNum+2])==2)){
			columnNum+=2;
			nextType=getType(tempLineChar[columnNum+1]);
			while(nextType==2){
				columnNum++;
				nextType=getType(tempLineChar[columnNum+1]);
			}
			tokenNmu=60; // TFLIT
		}			
		columnNum++;
		return tokenNmu;
	}
	
	
	// extract the string "..." 
	private int extractString(){
		// check the next " 
		columnNum++;
		char C=tempLineChar[columnNum];
		// util end of String or end of line 
		while((int)C != 34 && columnNum < tempLineChar.length -1){
			columnNum++;
			C=tempLineChar[columnNum];
		}
		if ((int)C == 34){
			columnNum++;
			return 61;  // TSTRG String
		}else{
			return 62;  // TUNDF Undefined
		}
	}
	
	// extract the Delimiters and Operators
	private int extractOperators(){
		char C=tempLineChar[columnNum+1];
		String SS="";
		int num=0;
		if (C=='='){
			SS=tempLineChar[columnNum]+"=";
			num=getCoupleSymbolMark(SS);
		}

		// single or couple Symbol
		if (num<=0){
			
			SS=String.valueOf(tempLineChar[columnNum]);
			columnNum++;
			return getSingleSymbolMark(SS);
		}else{
			columnNum+=2;
			return getCoupleSymbolMark(SS);
		}	
		
	}
		
	// extract the undefined // TUNDF Undefined
	private int extractUndefined(){

		nextType=getType(tempLineChar[columnNum+1]);
		// check the "!" type "!=" or just "!" 
		if (tempLineChar[columnNum] == '!'){
			
			if (columnNum+1 < tempLineStr.length()){
				
				if(tempLineChar[columnNum+1] == '!'){
					nextType = -1;
					if (columnNum+1 < tempLineStr.length()){
						if (tempLineChar[columnNum+2] == '=')nextType=1;
					}
				}
			}

			
		}
		// check the "!" type "!=" or just "!"
		if (nextType==1 && tempLineChar[columnNum+1] == '!'&& getType(tempLineChar[columnNum])==-1){nextType = -1;}
		
		while(nextType == -1){
			
			columnNum++;
			nextType=getType(tempLineChar[columnNum+1]);
			if (tempLineChar[columnNum+1] == '!'){
				
				nextType=-1;

				
				if (columnNum+1 < tempLineStr.length()){
					if (tempLineChar[columnNum+2] == '=')nextType=1;
				}
			}
		}
		
		columnNum++;
		return 62; // TUNDF Undefined
	}
	
	
	// output to screen
	public void printToken(Token tempToken){
		Token sToken;
		if (tempToken.value()==62){
			
			System.out.println("");
			System.out.println("TUNDF ");
			System.out.println("lexical error "+tempToken.getStr());
			printCount=0;
		}else{
			
			if(tempToken.value()==61){
				// add "" in string
				String tokenStr='"'+tempToken.getStr()+'"';
				sToken= new Token(61,tempToken.getLn(),tempToken.getPos(),tokenStr);

			}else{
				
				sToken=tempToken;
			}

			// System.out.println("==one="+sToken.shortString().length());
			// System.out.println("==size="+printCount);
			
			if (printCount>60){
				System.out.println("");
				// System.out.println("-----1-----2-----3-----4-----5-----6-----7-----8-----9-----10");
				printCount=0;				
			}
			
			System.out.print(sToken.shortString());
			printCount+=sToken.shortString().length();
		}
	}

	// token nark of the Delimiters and Operators
	// , [ ] ( ) = + - * /% ^ < > : ; .
	private int getSingleSymbolMark(String symbol) {
		switch(symbol) {

			case ",": return 32; // TCOMA
			case "[": return 33; // TLBRK
			case "]": return 34; // TRBRK
			case "(": return 35; // TLPAR
			case ")": return 36; // TRPAR	
			case "=": return 37; // TEQUL
			case "+": return 38; // TPLUS
			case "-": return 39; // TMINS
			case "*": return 40; // TSTAR
			case "/": return 41; // TDIVD
			case "%": return 42; // TPERC
			case "^": return 43; // TCART
			case "<": return 44; // TLESS
			case ">": return 45; // TGRTR	
			case ":": return 46; // TCOLN
								 // 47 - 55 couple Symbol
			case ";": return 56; // TSEMI
			case ".": return 57; // TDOT		
			
		}
		return 62; // TUNDF Undefined
	}	
	// token nark of the Delimiters and Operators
	// <= >= != == += -= *= /=
	private int getCoupleSymbolMark(String symbol) {
		switch(symbol) {

			case "<=": return 47; // TLEQL
			case ">=": return 48; // TGEQL
			case "!=": return 49; // TNEQL
			case "==": return 50; // TEQEQ
			case "+=": return 51; // TPLEQ
			case "-=": return 52; // TMNEQ
			case "*=": return 53; // TSTEQ
			case "/=": return 54; // TDVEQ
			// case "%=": return 55; // TPCEQ // useless
			
		}
		return -1;
	}	
	
	// check chat type 
	private int getType(char C){
		// check the ASCII CODE
		// return number as below:
		// -2 the end of text
		// -1 Undefined
		// 0 (white space eg. space TAB)
		// 1 Delimiters or Operators
		// 2 number
		// 3 alphabet

		int num = (int)C;

		// end of text
		if (num==3){
			this.fileEOF=true;
			return -2;
		}

		switch(num){
			// all used ASCII code 
			case 9  : return 0;	  // tab
			case 32 : return 0;   // (space)
			case 33 : return 1;   // !
			case 34 : return 1;   // "
			case 35 : return -1;  // #
			case 36 : return -1;  // $
			case 37 : return 1;   // %
			case 38 : return -1;  // &
			case 39 : return -1;  // '
			case 40 : return 1;  // (
			case 41 : return 1;  // )
			case 42 : return 1;  // *
			case 43 : return 1;  // +
			case 44 : return 1;  // ,
			case 45 : return 1;  // -
			case 46 : return 1;  // .
			case 47 : return 1;  // /
			case 48 : return 2;  // 0
			case 49 : return 2;  // 1
			case 50 : return 2;  // 2
			case 51 : return 2;  // 3
			case 52 : return 2;  // 4
			case 53 : return 2;  // 5
			case 54 : return 2;  // 6
			case 55 : return 2;  // 7
			case 56 : return 2;  // 8
			case 57 : return 2;  // 9
			case 58 : return 1;  // :
			case 59 : return 1;  // ;
			case 60 : return 1;  // <
			case 61 : return 1;  // =
			case 62 : return 1;  // >
			case 63 : return -1;  // ?
			case 64 : return -1;  // @
			case 65 : return 3;  // A
			case 66 : return 3;  // B
			case 67 : return 3;  // C
			case 68 : return 3;  // D
			case 69 : return 3;  // E
			case 70 : return 3;  // F
			case 71 : return 3;  // G
			case 72 : return 3;  // H
			case 73 : return 3;  // I
			case 74 : return 3;  // J
			case 75 : return 3;  // K
			case 76 : return 3;  // L
			case 77 : return 3;  // M
			case 78 : return 3;  // N
			case 79 : return 3;  // O
			case 80 : return 3;  // P
			case 81 : return 3;  // Q
			case 82 : return 3;  // R
			case 83 : return 3;  // S
			case 84 : return 3;  // T
			case 85 : return 3;  // U
			case 86 : return 3;  // V
			case 87 : return 3;  // W
			case 88 : return 3;  // X
			case 89 : return 3;  // Y
			case 90 : return 3;  // Z
			case 91 : return 1;  // [
			case 92 : return -1;  // "\" 
			case 93 : return 1;  // ]
			case 94 : return 1;  // ^
			case 95 : return -1;  // _
			case 96 : return -1;  // `
			case 97 : return 3;  // a
			case 98 : return 3;  // b
			case 99 : return 3;  // c
			case 100 : return 3;  // d
			case 101 : return 3;  // e
			case 102 : return 3;  // f
			case 103 : return 3;  // g
			case 104 : return 3;  // h
			case 105 : return 3;  // i
			case 106 : return 3;  // j
			case 107 : return 3;  // k
			case 108 : return 3;  // l
			case 109 : return 3;  // m
			case 110 : return 3;  // n
			case 111 : return 3;  // o
			case 112 : return 3;  // p
			case 113 : return 3;  // q
			case 114 : return 3;  // r
			case 115 : return 3;  // s
			case 116 : return 3;  // t
			case 117 : return 3;  // u
			case 118 : return 3;  // v
			case 119 : return 3;  // w
			case 120 : return 3;  // x
			case 121 : return 3;  // y
			case 122 : return 3;  // z
			case 123 : return -1;  // {
			case 124 : return -1;  // |
			case 125 : return -1;  // }
			case 126 : return -1;  // ~
		default : return 0;
		}
	}
	
	

}