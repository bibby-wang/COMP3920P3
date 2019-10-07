// The University of Newcastle
// School of Electrical Engineering and Computing
// COMP3290 Compiler Design
// Semester 2, 2019
// Project Part 3 A Recursive-Descent Parser for CD19 (15%) 
// Due: September 27th 
// Binbin.Wang C3214157

import java.util.*;

public class SymbolTable{
	
	private HashMap<String, StRec> table;
	protected SymbolTable prev;

	public SymbolTable(SymbolTable prev){
		table = new HashMap<>();
		this.prev = prev;
	}

	public void put(String str, StRec symbol){
		table.put(str, symbol);
	}

	public StRec get(String str){
		for (SymbolTable elem = this; elem != null; elem = elem.prev){
			StRec found = elem.table.get(str);
			if (found != null){
				return found;
			}
		}
		return null;
	}
}