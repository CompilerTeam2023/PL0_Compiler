/**
 * @Description PL/0词法规则
 **/
public class Symbol {

    public static final int nul = 0;
    // 标识符
    public static final int ident = 1;
    // 整数
    public static final int number = 2;
    // 算符、界符
    public static final int plus = 3;
    public static final int minus = 4;
    public static final int times = 5;
    public static final int slash = 6;
    public static final int assign = 7;
    public static final int eql = 8;
    public static final int neq = 9;
    public static final int lss = 10;
    public static final int leq = 11;
    public static final int gtr = 12;
    public static final int geq = 13;
    public static final int lparen = 14;
    public static final int rparen = 15;
    public static final int comma = 16;
    public static final int semicolon = 17;
    // 关键字
    public static final int progsym = 18;
    public static final int beginsym = 19;
    public static final int endsym = 20;
    public static final int ifsym = 21;
    public static final int thensym = 22;
    public static final int whilesym = 23;
    public static final int dosym = 24;
    public static final int constsym = 25;
    public static final int varsym = 26;
    public static final int eof = 27;// 井号 作为源程序结束符

    // 符号的个数
    public static final int symnum = 28;

    // 符号的类型
    private int symtype;
    // 具体字符的值（标识符名字/关键字/算符界符）
    private String value;
    // 数值大小（如果这个符号是数字的话）
    private int num;

    public Symbol() {
    }

    public Symbol(int symtype, String value) {
        this.symtype = symtype;
        this.value = value;
    }

    public Symbol(int symtype, int num) {
        this.symtype = symtype;
        this.num = num;
    }

    public int getSymtype() {
        return symtype;
    }

    public String getValue() {
        return value;
    }

    public int getNum() {
        return num;
    }
}
