//	COMP3290 CD19 Compiler
//		Syntax Tree Node Class - Builds a syntax tree node
//
//	By COMP3290 Staff - 2019	
//

import java.io.*;

public class TreeNode {

	// SYNTAX TREE NODE VALUES
	// ***********************
	public static final int NUNDEF = 0,
				NPROG = 1,		NGLOB = 2,		NILIST = 3,		NINIT = 4,		NFUNCS = 5,
				NMAIN = 6,		NSDLST = 7,		NTYPEL = 8,		NRTYPE = 9,		NATYPE = 10,
				NFLIST = 11,	NSDECL = 12,	NALIST = 13,	NARRD = 14,		NFUND = 15,
				NPLIST = 16,	NSIMP = 17,		NARRP = 18,		NARRC = 19,		NDLIST = 20,
				NSTATS = 21,	NFOR = 22,		NREPT = 23,		NASGNS = 24,	NIFTH = 25,
				NIFTE = 26,		NASGN = 27,		NPLEQ = 28,		NMNEQ = 29,		NSTEQ = 30,
				NDVEQ = 31,		NINPUT = 32,	NPRINT = 33,	NPRLN = 34,		NCALL = 35,
				NRETN = 36,		NVLIST = 37,	NSIMV = 38,		NARRV = 39,		NEXPL = 40,
				NBOOL = 41,		NNOT = 42,		NAND = 43,		NOR = 44,		NXOR = 45,
				NEQL = 46,		NNEQ = 47,		NGRT = 48,		NLSS = 49,		NLEQ = 50,
				NADD = 51,		NSUB = 52,		NMUL = 53,		NDIV = 54,		NMOD = 55,
				NPOW = 56,		NILIT = 57,		NFLIT = 58,		NTRUE = 59,		NFALS = 60,
				NFCALL = 61,	NPRLST = 62,	NSTRG = 63,		NGEQ = 64;

	private static final String PRINTNODE[] = {  	//  PRINTNODE[TreeNode Value] will produce the associated String
						  							//  e.g. PRINTNODE[NPROG] will be the String "NPROG".
				"NUNDEF",
				"NPROG ",	"NGLOB ",	"NILIST",	"NINIT ",	"NFUNCS ",
				"NMAIN",	"NSDLST",	"NTYPEL",	"NRTYPE",	"NATYPE",
				"NFLIST",	"NSDECL",	"NALIST",	"NARRD ",	"NFUND ",
				"NPLIST",	"NSIMP ",	"NARRP ",	"NARRC ",	"NDLIST",
				"NSTATS",	"NFOR  ",	"NREPT ",	"NASGNS",	"NIFTH ",
				"NIFTE ",	"NASGN ",	"NPLEQ ",	"NMNEQ ",	"NSTEQ ",
				"NDVEQ ",	"NINPUT",	"NPRINT",	"NPRLN ",	"NCALL ",
				"NRETN ",	"NVLIST",	"NSIMV ",	"NARRV ",	"NEXPL ",
				"NBOOL ",	"NNOT  ",	"NAND  ",	"NOR   ",	"NXOR  ",
				"NEQL  ",	"NNEQ  ",	"NGRT  ",	"NLSS  ",	"NLEQ  ",
				"NADD  ",	"NSUB  ",	"NMUL  ",	"NDIV  ",	"NMOD  ",
				"NPOW  ",	"NILIT ",	"NFLIT ",	"NTRUE ",	"NFALS ",
				"NFCALL",	"NPRLST",	"NSTRG ",	"NGEQ  " };


	private static int count = 0;

	private int nodeValue;
	private TreeNode left,middle,right;
	private StRec symbol, type;

	public TreeNode (int value) {
		nodeValue = value;
		left = null;
		middle = null;
		right = null;
		symbol = null;
		type = null;
	}

	public TreeNode (int value, StRec st) {
		this(value);
		symbol = st;
	}

	public TreeNode (int value, TreeNode l, TreeNode r) {
		this(value);
		left = l;
		right = r;
	}

	public TreeNode (int value, TreeNode l, TreeNode m, TreeNode r) {
		this(value,l,r);
		middle = m;
	}

	public int getValue() { return nodeValue; }

	public TreeNode getLeft() { return left; }

	public TreeNode getMiddle() { return middle; }

	public TreeNode getRight() { return right; }

	public StRec getSymbol() { return symbol; }

	public StRec getType() { return type; }

	public void setValue(int value) { nodeValue = value; }

	public void setLeft(TreeNode l) { left = l; }

	public void setMiddle(TreeNode m) { middle = m; }

	public void setRight(TreeNode r) { right = r; }

	public void setSymbol(StRec st) { symbol = st; }

	public void setType(StRec st) { type = st; }


  //
  // Call is: TreeNode.printTree(outfile, rootOfTree);
  //	-> prints tree pre-order as a flat 7 values per line
  //
  //   I am used to this type of print - if you cannot use
  //	it then you are free to implement your own XML or
  //	whatever you like tree output routine.
  //

	public static void printTree(PrintWriter out, TreeNode tr) {
		if (tr.nodeValue == NPROG) count = 0;
		out.print(PRINTNODE[tr.nodeValue]+" ");
		count++;
		if (count%7 == 0) out.println();
		if (tr.symbol != null) {
			out.print(tr.symbol.getName() + " ");
			count++;
			if (count%7 == 0) out.println();
		}
		if (tr.type   != null) {
			out.print(  tr.type.getName() + " ");
			count++;
			if (count%7 == 0) out.println();
		}
		if (tr.left   != null) { printTree(out,tr.left);   }
		if (tr.middle != null) { printTree(out,tr.middle); }
		if (tr.right  != null) { printTree(out,tr.right);  }
		if (tr.nodeValue == NPROG && count%7 != 0) out.println();
	}
}
