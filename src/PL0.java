import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.io.BufferedReader;

/**
 * @Description PL0编译器 主函数入口
 * @Author fjy
 * @Date 2024-01-02
 **/
public class PL0 {
    // 一个典型的编译器的组成部分
    public static Lexer lex; // 词法分析器
    public static Parser parser; // 语法分析器
    public static Intermediater intermediater; // 中间代码生成工具
    public static Table table; // 符号表

    public static final int id_max = 15; // 标识符的最大长度
    public static final int num_max = 8; // number的最大位数

    public PL0(BufferedReader input) {
        lex = new Lexer(input);
        table = new Table();
        intermediater = new Intermediater();
        parser = new Parser(lex, table, intermediater);
    }

    public static void main(String[] args) {
        boolean tableDisplay = false; // 显示符号表与否

        System.out.println("=============================== PL0 Compiler Start ===============================");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.print("请输入pl0源程序的文件名(或者输入 exit 退出程序): ");
                String userInput = scanner.nextLine();

                if (userInput.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting PL0 Compiler.");
                    break;
                }
                String filePath = userInput;
                BufferedReader input = new BufferedReader(new FileReader(filePath));

                // 构造编译器并初始化
                PL0 pl0 = new PL0(input);
                parser.parse(); // 开始语法分析过程（连同词法分析、语法检查、目标代码生成）

                // 是否输出符号表
                System.out.print("是否展示符号表呢？(Y/N)");
                // 读取用户输入
                String option = scanner.nextLine().trim().toUpperCase();
                // 检查用户输入
                if (option.equals("Y")) {
                    tableDisplay = true;
                }
                if (tableDisplay) {
                    // 输出符号表的代码
                    System.out.println("\n-----------------Printing symbol table---------------------");
                    table.printTable();
                    System.out.println("-----------------------------------------------------------");
                } else {
                    System.out.println("Symbol table will not be printed.");
                }

                System.out.println("\n----------------Printing intermediater code----------------");
                intermediater.ouputCode();// 输出中间代码
                System.out.println("-----------------------------------------------------------");
                System.out.println();

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        scanner.close();
        System.out.println("=============================== PL0 Compiler End ===============================");
    }
}
