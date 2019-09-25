// The University of Newcastle
// School of Electrical Engineering and Computing
// COMP3290 Compiler Design
// Semester 2, 2019
// Project Part 3 A Recursive-Descent Parser for CD19 (15%) 
// Due: September 27th 
// Binbin.Wang C3214157

// while not scanner.eof( ) do {
	// token = scanner . gettoken( );
	// scanner . printToken(token);
// }

public class A3{
	
	public static void main(String[] args)  {
		// check file name
		
		if (args.length>0){
			// Simultaneous scanning of multiple files
			for(int i=0; i<args.length; i++){
				if (i>0)System.out.println("");
				System.out.println("=====File: "+args[i]+"=====\r\n");
				//sacnner
				Scanner scanner=new Scanner(args[i]);
				//parser
				//Parser parser =new Parser();
				
				Token tempToken;
				int countToken=0;
				while (!scanner.eof()){
					countToken++;
					tempToken = scanner.getToken();
					// System.out.println(tempToken.toString());
					scanner.printToken(tempToken);
					
					
				}
				// System.out.println("\r\n Tokens count is: "+countToken);
			}
		}else{
			// erro of file name
			System.out.println("Empty File name args[] is Null");
		}	
	}
	
}
