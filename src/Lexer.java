import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * 【PL/0编译器的词法分析器Lexer】
 * 从源代码里面读取文法符号
 */
public class Lexer {
    // 输入流
    private final BufferedReader in;

    // 当前读入的字符
    private char ch = ' ';
    // 当前读入的行
    private String currentLine;
    // 当前行的长度
    private int lineLength = 0;
    // 当前字符在当前行中的位置
    private int charPosition = 0;
    // 当前行数
    private int currentLineNumber = 0;

    // 当前识别出的符号
    private Symbol sym = new Symbol();

    // getters:
    public Symbol getSym() {
        return sym;
    }

    public int getCurrentLineNumber() {
        return currentLineNumber;
    }

    // 单字符的符号
    private final HashMap<Character, Integer> singleSymbol;
    // 关键字
    private final HashMap<String, Integer> keywordMap;


    /**
     * 初始化词法分析器（构造函数）
     *
     * @param input PL/0 源文件输入流
     */
    public Lexer(BufferedReader input) {
        in = input;

        // 设置单字符符号
        singleSymbol = new HashMap<>();
        singleSymbol.put('+', Symbol.plus);
        singleSymbol.put('-', Symbol.minus);
        singleSymbol.put('*', Symbol.times);
        singleSymbol.put('/', Symbol.slash);
        singleSymbol.put('(', Symbol.lparen);
        singleSymbol.put(')', Symbol.rparen);
        singleSymbol.put('=', Symbol.eql);
        singleSymbol.put(',', Symbol.comma);
        singleSymbol.put(';', Symbol.semicolon);

        // 设置保留字（关键字）按照字母顺序，便于折半查找
        keywordMap = new HashMap<>();
        keywordMap.put("BEGIN", Symbol.beginsym);
        keywordMap.put("CONST", Symbol.constsym);
        keywordMap.put("DO", Symbol.dosym);
        keywordMap.put("END", Symbol.endsym);
        keywordMap.put("IF", Symbol.ifsym);
        keywordMap.put("PROGRAM", Symbol.progsym);
        keywordMap.put("THEN", Symbol.thensym);
        keywordMap.put("VAR", Symbol.varsym);
        keywordMap.put("WHILE", Symbol.whilesym);
    }

    /**
     * 读取一个字符，为减少磁盘I/O次数，每次读取一行
     */
    void getch() {
        try {
            while (charPosition == lineLength) {
                currentLine = in.readLine() + "\n";
                if (currentLine.equals("null\n")) {
                    // 文件已经结束
                    ch = '#';
                    return;
                } else {
                    lineLength = currentLine.length();
                    currentLineNumber++;
                    charPosition = 0;
                }
            }
        } catch (IOException e) {
            throw new Error("Program incomplete", e);
        }
        ch = currentLine.charAt(charPosition++);
    }

    /**
     * 词法分析，获取一个词法符号，是词法分析器的重点
     */
    public void getsym() {
        while (Character.isWhitespace(ch)) // 跳过所有空白字符
            getch();
        if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') {
            // 关键字或者一般标识符
            matchKeywordOrIdentifier();
        } else if (ch >= '0' && ch <= '9') {
            // 数字
            matchNumber();
        } else if (ch == '#') {
            // 文本结束符
            sym = new Symbol(Symbol.eof, '#');
        } else {
            // 操作符
            matchOperator();
        }
    }

    /**
     * 分析关键字或者一般标识符
     */
    void matchKeywordOrIdentifier() {
        StringBuilder sb = new StringBuilder(PL0.id_max);
        // 首先把整个单词读出来
        do {
            sb.append(ch);
            getch();
        } while (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9');
        String id = sb.toString();
        // 判断是不是保留字（使用hashmap的键值对搜索）最后形成符号信息
        if (keywordMap.containsKey(id)) {
            // 关键字
            sym = new Symbol(keywordMap.get(id), id);
        } else {
            // 一般标识符
            sym = new Symbol(Symbol.ident, id);
        }
    }

    /**
     * 分析数字
     */
    void matchNumber() {
        int count = 0;// 计数器，防止数字长度太大
        int num = 0;
        // 获取数字的值
        while (Character.isDigit(ch)) {
            num = 10 * num + Character.digit(ch, 10);
            count++;
            getch();
        }
        sym = new Symbol(Symbol.number, num);
        if (count > PL0.num_max)
            Err.handleError("数字长度过长！", currentLineNumber);
    }

    /**
     * 分析操作符
     */
    void matchOperator() {
        switch (ch) {
            case ':': // 赋值符号
                getch();
                if (ch == '=') {
                    sym = new Symbol(Symbol.assign, ":=");
                    getch();
                } else {
                    // 不能识别的符号
                    sym = new Symbol(Symbol.nul, "");
                }
                break;
            case '<': // 小于或者小于等于
                getch();
                if (ch == '=') {
                    sym = new Symbol(Symbol.leq, "<=");
                    getch();
                } else if (ch == '>') { // 不等于
                    sym = new Symbol(Symbol.neq, "<>");
                    getch();
                } else {
                    sym = new Symbol(Symbol.lss, "<");
                }
                break;
            case '>': // 大于或者大于等于
                getch();
                if (ch == '=') {
                    sym = new Symbol(Symbol.geq, ">=");
                    getch();
                } else {
                    sym = new Symbol(Symbol.gtr, ">");
                }
                break;
            default: // 其他为单字符操作符（如果符号非法则返回nul）
                sym = new Symbol(singleSymbol.get(ch), Character.toString(ch));
                getch();
                break;
        }
    }
}
