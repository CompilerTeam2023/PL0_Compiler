/**
 *　　C语言版本中采用全局变量sym来存储符号码，并用全局变量id和num来传递语义值。
 *本版本中把这三者封装起来，同时也把符号码以常数的形式封装起来。
 *
 */
public class Symbol {
	// 各类符号码
	public static final int nul 		= 0;
	public static final int ident 		= 1;
	public static final int number 		= 2;
	public static final int plus 		= 3;
	public static final int minus 		= 4;
	public static final int times 		= 5;
	public static final int slash 		= 6;
	public static final int oddsym 		= 7;
	public static final int eql 		= 8;
	public static final int neq 		= 9;
	public static final int lss 		= 10;
	public static final int leq 		= 11;
	public static final int gtr 		= 12;
	public static final int geq 		= 13;
	public static final int lparen 		= 14;
	public static final int rparen 		= 15;
	public static final int comma 		= 16;
	public static final int semicolon 	= 17;
	public static final int period 		= 18;
	public static final int becomes 	= 19;
	public static final int beginsym 	= 20;
	public static final int endsym 		= 21;
	public static final int ifsym 		= 22;
	public static final int thensym 	= 23;
	public static final int whilesym 	= 24;
	public static final int writesym 	= 25;
	public static final int readsym 	= 26;
	public static final int dosym 		= 27;
	public static final int callsym 	= 28;
	public static final int constsym 	= 29;
	public static final int varsym 		= 30;
	public static final int procsym 	= 31;

	// 符号码的个数
	public static final int symnum = 32;
	
	/**
	 * 符号码
	 */
	public int symtype;

	/**
	 * 标识符名字（如果这个符号是标识符的话）
	 */
	public String id;

	/**
	 * 数值大小（如果这个符号是数字的话）
	 */
	public int num;

	/**
	 * 构造具有特定符号码的符号
	 * @param stype 符号码
	 */
	Symbol(int stype) {
		symtype = stype;
		id = "";
		num = 0;
	}
}
