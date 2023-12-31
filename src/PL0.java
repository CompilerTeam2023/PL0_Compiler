import java.io.*;

/**
 * PL0简单编译器
 */
public class PL0 {
	// 编译程序的常数
	public static final int al = 10; // 符号的最大长度
	public static final int cxmax = 500; // 最多的虚拟机代码数
	public static final int nmax = 14; // number的最大位数
	public static final int norw = 9; // 关键字个数

	public static PrintStream fa; // 输出虚拟机代码
	public static PrintStream fa1; // 输出源文件及其各行对应的首地址
	public static PrintStream fa2; // 输出结果
	public static PrintStream fas; // 输出名字表
	public static boolean listswitch; // 显示虚拟机代码与否
	public static boolean tableswitch; // 显示名字表与否

	// 一个典型的编译器的组成部分
	public static Scanner lex; // 词法分析器
	public static Parser parser; // 语法分析器
	public static Intermediater intermediater; // 中间代码生成工具
	public static Table table; // 名字表

	/**
	 * 构造函数，初始化编译器所有组成部分
	 * 
	 * @param fin PL/0 源文件的输入流
	 */
	public PL0(BufferedReader fin) {
		table = new Table();
		intermediater = new Intermediater();
		lex = new Scanner(fin);
		parser = new Parser(lex, table, intermediater);
	}

	/**
	 * 执行编译动作
	 * 
	 * @return 是否编译成功
	 */
	boolean compile() {
		boolean abort = false;

		try {
			PL0.fa = new PrintStream("fa.tmp");
			PL0.fas = new PrintStream("fas.tmp");
			parser.nextsym(); // 前瞻分析需要预先读入一个符号
			parser.parse(); // 开始语法分析过程（连同语法检查、目标代码生成）
			parser.getIntermediater().ouputCode();
		} catch (Error e) {
			// 如果是发生严重错误则直接中止
			abort = true;
		} catch (IOException e) {
		} finally {
			PL0.fa.close();
			PL0.fa1.close();
			PL0.fas.close();
		}
		if (abort)
			System.exit(0);

		// 编译成功是指完成编译过程并且没有错误
		return (Err.err == 0);
	}

	/**
	 * 主函数
	 */
	public static void main(String[] args) {
		// 原来 C 语言版的一些语句划分到compile()和Parser.parse()中
		String fname = "test.pl0";
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		BufferedReader fin;
		try {
			// 输入文件名
			// fname = "";
			// System.out.print("Input pl/0 file? ");
			// while (fname.equals(""))
			// fname = stdin.readLine();
			fin = new BufferedReader(new FileReader(fname));

			// 是否输出虚拟机代码
			fname = "n";
			// System.out.print("List object code?(Y/N)");
			// while (fname.equals(""))
			// fname = stdin.readLine();
			PL0.listswitch = (fname.charAt(0) == 'y' || fname.charAt(0) == 'Y');

			// 是否输出名字表
			fname = "y";
			// System.out.print("List symbol table?(Y/N)");
			// while (fname.equals(""))
			// fname = stdin.readLine();
			PL0.tableswitch = (fname.charAt(0) == 'y' || fname.charAt(0) == 'Y');

			PL0.fa1 = new PrintStream("fa1.tmp");
			PL0.fa1.println("Input pl/0 file?   " + fname);

			// 构造编译器并初始化
			PL0 pl0 = new PL0(fin);

			if (pl0.compile()) {
				// 如果成功编译则接着解释运行
				PL0.fa2 = new PrintStream("fa2.tmp");
//				interp.interpret();
				PL0.fa2.close();
			} else {
				System.out.print("Errors in pl/0 program");
			}

		} catch (IOException e) {
			System.out.println("Can't open file!");
		}

		System.out.println();
	}
}
