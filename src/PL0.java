import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.io.BufferedReader;

/**
 * @Description PL0������ ���������
 * @Author fjy
 * @Date 2024-01-02
 **/
public class PL0 {
    // һ�����͵ı���������ɲ���
    public static Lexer lex; // �ʷ�������
    public static Parser parser; // �﷨������
    public static Intermediater intermediater; // �м�������ɹ���
    public static Table table; // ���ű�

    public static final int id_max = 15; // ��ʶ������󳤶�
    public static final int num_max = 8; // number�����λ��

    public PL0(BufferedReader input) {
        lex = new Lexer(input);
        table = new Table();
        intermediater = new Intermediater();
        parser = new Parser(lex, table, intermediater);
    }

    public static void main(String[] args) {
        boolean tableDisplay = false; // ��ʾ���ű����

        System.out.println("=============================== PL0 Compiler Start ===============================");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.print("������pl0Դ������ļ���(�������� exit �˳�����): ");
                String userInput = scanner.nextLine();

                if (userInput.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting PL0 Compiler.");
                    break;
                }
                String filePath = userInput;
                BufferedReader input = new BufferedReader(new FileReader(filePath));

                // �������������ʼ��
                PL0 pl0 = new PL0(input);
                parser.parse(); // ��ʼ�﷨�������̣���ͬ�ʷ��������﷨��顢Ŀ��������ɣ�

                // �Ƿ�������ű�
                System.out.print("�Ƿ�չʾ���ű��أ�(Y/N)");
                // ��ȡ�û�����
                String option = scanner.nextLine().trim().toUpperCase();
                // ����û�����
                if (option.equals("Y")) {
                    tableDisplay = true;
                }
                if (tableDisplay) {
                    // ������ű�Ĵ���
                    System.out.println("\n-----------------Printing symbol table---------------------");
                    table.printTable();
                    System.out.println("-----------------------------------------------------------");
                } else {
                    System.out.println("Symbol table will not be printed.");
                }

                System.out.println("\n----------------Printing intermediater code----------------");
                intermediater.ouputCode();// ����м����
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
