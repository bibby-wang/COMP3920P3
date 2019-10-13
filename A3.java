// The University of Newcastle
// School of Electrical Engineering and Computing
// COMP3290 Compiler Design
// Semester 2, 2019
// Project Part 3 A Recursive-Descent Parser for CD19 (15%) 
// Due: September 27th 
// Binbin.Wang C3214157


import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
public class A3{
	
	public static void main(String[] args) throws IOException{
		// check file name
		if (args.length>0){
			// Simultaneous scanning of multiple files
			for(int i=0; i<args.length; i++){
				if (i>0)System.out.println("");
				System.out.println("=====File: "+args[i]+"=====\r\n");

				//sacnner
				Scanner scanner=new Scanner(args[i]);
			
				// Modify the output file name
				String fileN= args[i].substring(0,args[i].lastIndexOf('.'));
				
				// output file
				PrintWriter outFile= new PrintWriter(new FileWriter(fileN+"_tree.cdr"));
				//parser
				Parser parser=new Parser(scanner);	
				// get the Syntax Tree and write to file: Source File name _tree.cdr)
				TreeNode tree=parser.getSyntaxTree();
				TreeNode.printTree(outFile, tree);
				System.out.println("Pre-Order Traversal:");
				outFile.close();

				if (parser.getErrorList()!=""){
					System.out.println("=***=Errors: =***=");
					System.out.println(parser.getErrorList());
				}else{
					// no errors
					// Output to the terminal when there is no error in the program
					BufferedReader br = new BufferedReader(new FileReader(fileN+"_tree.cdr"));
					String printLine;
					// print by line
					while ((printLine = br.readLine()) != null) {

						System.out.println(printLine);
					}
				}
	
				System.out.println("\r\n=*= Output File: "+fileN+"_tree.cdr");
				System.out.println("=====Finished: "+args[i]+"=====");				
			}
			
		}else{
			// erro of file name
			System.out.println("Empty File name args[] is Null");
		}	
	}
	
}
