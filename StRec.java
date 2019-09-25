// The University of Newcastle
// School of Electrical Engineering and Computing
// COMP3290 Compiler Design
// Semester 2, 2019
// Project Part 3 A Recursive-Descent Parser for CD19 (15%) 
// Due: September 27th 
// Binbin.Wang C3214157

public class StRec{
	
	private String name;
	private String type;

	public StRec(){
		name = null;
		type = null;
	}

	public StRec(String name){
		this();
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public String getType(){
		return type;
	}

	public void setName(String name){
		this.name = name;
	}

	public void setType(String type){
		this.type = type;
	}
}