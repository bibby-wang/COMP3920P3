// The University of Newcastle
// School of Electrical Engineering and Computing
// COMP3290 Compiler Design
// Semester 2, 2019
// Project Part 3 A Recursive-Descent Parser for CD19 (15%)
// Due: September 27th
// Binbin.Wang C3214157

// Syntax Error : line 27 - invalid if-statement (missing Boolean experession)
// Semantic Error : line 54 - no return statement for function (int myFunction (int, int))

public class CompilerErrors{
	private String errorMessage;  

	CompilerErrors(){
		errorMessage="";
	}
	
	CompilerErrors(String type,int line,int column,String message){
		this.setTLC(type,line,column);
		this.setMessage(message);
	}
	private void setTLC(String type,int line,int column){
		errorMessage=type+" Error : line["+line+"], column["+column+"]- ";
	}
	public String getMessage(){
		return errorMessage;
	}
	public void setMessage(String message){
		errorMessage+=message;
	}
	
}