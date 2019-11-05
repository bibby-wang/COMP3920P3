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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.util.ArrayList;
public class A3{
	
	public static void main(String[] args) throws IOException{
		// check file name
		if (args.length>0){
			// Simultaneous scanning of multiple files
			for(int i=0; i<args.length; i++){
				if (i>0)System.out.println("");
				System.out.println("=====File: "+args[i]+"=====\r\n");

				// sacnner
				Scanner scanner=new Scanner(args[i]);
			
				// Modify the output file name
				String fileN= args[i].substring(0,args[i].lastIndexOf('.'));
				
				// output file
				PrintWriter outFile= new PrintWriter(new FileWriter(fileN+".lst"));
				// parser
				Parser parser=new Parser(scanner);	
				// get the Syntax Tree and write to file: Source File name _tree.cdr)
				TreeNode tree=parser.getSyntaxTree();
				TreeNode.printTree(outFile, tree);
				
				outFile.close();
				
				if (parser.getErrorList().isEmpty()){
					// no errors
					// Output to the terminal when there is no error in the program
					
					System.out.println("No errors found.");
					System.out.println("Pre-Order Traversal:");
					BufferedReader br = new BufferedReader(new FileReader(fileN+".lst"));
					// print by line
					String printLine;
					while ((printLine = br.readLine()) != null) {
						System.out.println(printLine);
					}
				}else{
					// found error
					
					
					//??????
					// need output Listing file
					// read the cd19 code and add a line number then add errors then add the NODE TREE
					///////////////// *.lis
					// 1: code...
					// 2: code...
					// 3: code...
					//  ......
					// n: end code
					// error list 1 line:x col:y....
					// error list 2 line:x col:y....
					// error list 3 line:x col:y....
					///////////////////
					
					// dirty code but working 
					// need output all to file ".lst"
					///////////////////////////////////////////
					BufferedReader br = new BufferedReader(new FileReader(args[i]));
					String SSSS="";
					String printLine;
					int num=0;
					while ((printLine = br.readLine()) != null) {
						num++;
						SSSS+=num+": "+printLine+"\r\n";
					}
					System.out.println(SSSS);
					System.out.println("Found "+parser.getErrorList().size()+" Errors.");
					SSSS+="\r\n";
					for (CompilerErrors errorlist : parser.getErrorList()){
						SSSS+=errorlist.getMessage()+"\r\n";
						System.out.println(errorlist.getMessage());
					}
					System.out.println("");
					System.out.println("Pre-Order Traversal: ");
					SSSS+="\r\nPre-Order Traversal: \r\n";
					BufferedReader br1 = new BufferedReader(new FileReader(fileN+".lst"));
					// print by line
					String printLine1;
					while ((printLine1 = br1.readLine()) != null) {
						SSSS+=printLine1+"\r\n";
						System.out.println(printLine1);
					}
					/////////////////////////////////
					BufferedWriter outlis = new BufferedWriter(new FileWriter(fileN+".lst"));
					outlis.write(SSSS);
					outlis.close();
					////////////////////////////

				}
	
				//System.out.println("\r\n=*= Output File: "+fileN+"_tree.cdt");
				System.out.println("=====Finished the file: "+args[i]+"=====");				
			}
			
		}else{
			// erro of file name
			System.out.println("Empty File name args[] is Null");
		}	
	}
	
}
