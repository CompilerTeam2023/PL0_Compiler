import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 语法分析器。这是PL/0分析器中最重要的部分，在语法分析的过程中嵌入了语法错误检查和目标代码生成。
 */
public class Parser {
    private Scanner lex; // 对词法分析器的引用
    private Table table; // 对符号表的引用
    //	private Interpreter interp; // 对目标代码生成器的引用
    private Intermediater intermediater; // 中间代码生成器

    public Intermediater getIntermediater() {
        return intermediater;
    }

    /**
     * 当前符号的符号码，由nextsym()读入
     *
     * @see #nextsym()
     */
    private int symtype;

    /**
     * 当前符号，由nextsym()读入
     *
     * @see #nextsym()
     */
    private Symbol sym;

    /**
     * 构造并初始化语法分析器，这里包含了C语言版本中init()函数的一部分代码
     *
     * @param l 编译器的词法分析器
     * @param t 编译器的符号表
     * @param i 编译器的目标代码生成器
     */

    private BitSet subprogram_first;
    private BitSet statement_first;

    /**
     * 关系运算符map（= <> > < >= <=)
     */
    private Map<Integer, String> operationMap = new HashMap<>();

    public Parser(Scanner l, Table t, Intermediater i) {
        lex = l;
        table = t;
        intermediater = i;

        // subprogram的first集合
        subprogram_first = new BitSet(Symbol.symnum);
        subprogram_first.set(Symbol.constsym);
        subprogram_first.set(Symbol.varsym);
        subprogram_first.set(Symbol.ident);
        subprogram_first.set(Symbol.ifsym);
        subprogram_first.set(Symbol.whilesym);
        subprogram_first.set(Symbol.beginsym);
        subprogram_first.set(Symbol.nul);

        // statemennt的first集合
        statement_first = new BitSet(Symbol.symnum);
        statement_first.set(Symbol.ident);
        statement_first.set(Symbol.ifsym);
        statement_first.set(Symbol.whilesym);
        statement_first.set(Symbol.beginsym);

        // 关系运算符
        operationMap.put(Symbol.eql, "=");
        operationMap.put(Symbol.neq, "<>");
        operationMap.put(Symbol.gtr, ">");
        operationMap.put(Symbol.geq, ">=");
        operationMap.put(Symbol.lss, "<");
        operationMap.put(Symbol.leq, "<=");

    }

    /**
     * 启动语法分析过程，此前必须先调用一次nextsym()
     *
     * @see #nextsym()
     */
    public void parse() {
        program();
//		table.printTable();
    }

    /**
     * 获得下一个语法符号，这里只是简单调用一下getsym()
     */
    public void nextsym() {
        lex.getsym();
        sym = lex.sym;
        symtype = sym.symtype;
    }

    /**
     * 分析<程序>
     */
    public void program() {
        // P (programHeader)
        if (symtype == Symbol.progsym) {
            programHeader();
        } else {
            System.out.println("error: program-programHeader");
        }

        // P (subprogram)
        if (subprogram_first.get(symtype)) {
            subprogram();
        } else {
            System.out.println("error: program-subprogram");
        }

    }

    /**
     * 分析<程序首部>
     */
    public void programHeader() {
        // P (PROGRAM)
        if (symtype == Symbol.progsym) {
            nextsym();
        } else {
            System.out.println("error: program_header-PROGRAM");
        }

        // P (identifier)
        if (symtype == Symbol.ident) {
            nextsym();
        } else {
            System.out.println("error: program_header-identifier");
        }
    }

    /**
     * 分析<分程序>
     * <分程序>→[<常量说明>][<变量说明>]<语句>
     */
    public void subprogram() {
        // 为产生式右部的语句statement创建一个s_nextList
        ArrayList<Integer> s_nextList = intermediater.makeList();

        // P (constdeclaration)
        if (symtype == Symbol.constsym) {
            constDeclaration();
        }
        // P (varDeclaration)
        if (symtype == Symbol.varsym) {
            varDeclaration();
        }
        // P (statement)
        if (statement_first.get(symtype)) {
            statement(s_nextList);
        } else {
            // TODO: 这里不太确定，这个源程序结束后他到底读了个啥？
            if (symtype == Symbol.eof) {
                return;
            }
            System.out.println("error: subprogram");
        }
    }

    /**
     * 分析<常量说明>
     */
    public void constDeclaration() {
        // P (CONST)
        if (symtype == Symbol.constsym) {
            nextsym();
        } else {
            System.out.println("error: constDeclaration-CONST");
        }

        // P (constDefinition)
        if (symtype == Symbol.ident) {
            table.addItem(sym.id, Table.constant);
            constDefinition();
        } else {
            System.out.println("error: constDeclaration-constDefinition");
        }

        while (symtype == Symbol.comma) {
            nextsym();
            // P (constDefinition)
            if (symtype == Symbol.ident) {
                table.addItem(sym.id, Table.constant);
                constDefinition();
            } else {
                System.out.println("error: constDeclaration-constDefinition");
            }
        }

        // P (semicolon)
        if (symtype == Symbol.semicolon) {
            nextsym();
        } else {
            System.out.println("error: constDeclaration-semicolon");
        }
    }

    /**
     * 分析<常量定义>
     */
    public void constDefinition() {
        String id = "", op = "", num = "";

        // P (identifier)
        if (symtype == Symbol.ident) {
            id = sym.id;
            nextsym();
        } else {
            System.out.println("error: constDefinition-identifier");
        }

        // P (assign)
        if (symtype == Symbol.assign) {
            op = ":=";
            nextsym();
        } else {
            System.out.println("error: constDefinition-assign");
        }

        // P (number)
        if (symtype == Symbol.number) {
            num = Integer.toString(sym.num);
            nextsym();
        } else {
            System.out.println("error: constDefinition-number");
        }
        String code = Integer.toString(intermediater.nextStat) + ":	" + id + op + num;
        intermediater.emit(code);
        intermediater.nextStat++;
    }

    /**
     * 分析<变量说明>
     */
    public void varDeclaration() {
        // P (var)
        if (symtype == Symbol.varsym) {
            nextsym();
        } else {
            System.out.println("error: varDeclaration-var");
        }

        // P (identifier)
        if (symtype == Symbol.ident) {
            table.addItem(sym.id, Table.variable);
            nextsym();
        } else {
            System.out.println("error: varDeclaration-identifier");
        }

        while (symtype == Symbol.comma) {
            nextsym();
            // P (identifier)
            if (symtype == Symbol.ident) {
                table.addItem(sym.id, Table.variable);
                nextsym();
            } else {
                System.out.println("error: varDeclaration-identifier");
            }
        }

        // P (semicolon)
        if (symtype == Symbol.semicolon) {
            nextsym();
        } else {
            System.out.println("error: varDeclaration-semicolon");
        }
    }

    /**
     * 分析<复合语句>
     */
    public void compoundStatement(ArrayList<Integer> nextList) {
        // 为产生式右部的语句statement创建一个s_nextList
        ArrayList<Integer> s_nextList = intermediater.makeList();

        // P(BEGIN)
        if (symtype == Symbol.beginsym) {
            nextsym();
        } else {
            System.out.println("error: compoundStatement-BEGIN");
        }

        // P(statement)
        if (statement_first.get(symtype)) {
            statement(s_nextList);
        } else {
            System.out.println("error: compoundStatement-statement");
        }

        while (symtype == Symbol.semicolon) {
            nextsym();
            // P (statement)
            if (statement_first.get(symtype)) {
                statement(s_nextList);
            } else {
                System.out.println("error: compoundStatement-statement");
            }
        }

        // P (END)
        if (symtype == Symbol.endsym) {
            nextsym();
        } else {
            System.out.println("error: compoundStatement-END");
        }
    }

    /**
     * 分析<语句>
     */
    public void statement(ArrayList<Integer> nextList) {

        switch (symtype) {
            case Symbol.ident:
                assignStatement(nextList);
                break;
            case Symbol.ifsym:
                ifStatement(nextList);
                break;
            case Symbol.beginsym:
                compoundStatement(nextList);
                break;
            case Symbol.whilesym:
                whileStatement(nextList);
                break;
            default:
                if (symtype == Symbol.semicolon || symtype == Symbol.endsym || symtype == Symbol.eof) {
                    return;
                } else {
                    System.out.println("error: statement");
                }
                break;
        }
    }

    /**
     * 分析<赋值语句>
     */
    public void assignStatement(ArrayList<Integer> nextList) {
        String code = "", left = "", op = "", right = "";
        // P(ident)
        if (symtype == Symbol.ident) {
            if (table.lookup(sym.id)) {
                left = sym.id;
                nextsym();
            } else {
                System.out.println("error: identifier [" + sym.id + "] undefined!");
            }

        } else {
            System.out.println("error: assignStatement-ident");
        }

        // P(assign)
        if (symtype == Symbol.assign) {
            op = ":=";
            nextsym();
        } else {
            System.out.println("error: assignStatement-assign");
        }

        // P(expression)
        if (symtype == Symbol.plus || symtype == Symbol.minus || symtype == Symbol.ident || symtype == Symbol.number
                || symtype == Symbol.lparen) {
            right = expression();
        } else {
            System.out.println("error: assignStatement-expression");
        }
        code = Integer.toString(intermediater.nextStat) + ":	" + left + op + right;
        intermediater.emit(code);
        intermediater.nextStat++;
    }

    /**
     * 分析<表达式>
     */
    public String expression() {
        String code = "";
        String value = "";
        String temp1 = "", temp2 = "", temp = "";
        String op = "";
        String prefix = "";

        // P(zhegnfu)分析[+|-]<项>
        if (symtype == Symbol.plus || symtype == Symbol.minus) {
            if (symtype == Symbol.minus)
                prefix = "-";
            else if (symtype == Symbol.plus)
                prefix = "+";
            nextsym();
            if (symtype == Symbol.ident || symtype == Symbol.number || symtype == Symbol.lparen) {
                value = term();
                temp1 = intermediater.newTempVar();
                code = Integer.toString(intermediater.nextStat) + ":	" + temp1 + ":=" + prefix + value;
                intermediater.emit(code);
                intermediater.nextStat++;
                return temp1;
            } else {
                System.out.println("error: expression-zhengfu");
            }
        }

        // P(term)
        if (symtype == Symbol.ident || symtype == Symbol.number || symtype == Symbol.lparen) {
            temp1 = term();
        } else {
            System.out.println("error: expression-term");
        }


        // 分析{<加法运算符><项>}
        while (symtype == Symbol.plus || symtype == Symbol.minus) {
            if (symtype == Symbol.plus)
                op = "+";
            else
                op = "-";
            nextsym();

            // P(term)
            if (symtype == Symbol.ident || symtype == Symbol.number || symtype == Symbol.lparen) {
                temp2 = term();
            } else {
                System.out.println("error: expression-term");
            }
            temp = intermediater.newTempVar();
            code = Integer.toString(intermediater.nextStat) + ":	" + temp + ":=" + temp1 + op + temp2;
            intermediater.emit(code);
            intermediater.nextStat++;
            temp1 = temp;
        }

        return temp1;
    }

    /**
     * 分析<项>
     */
    public String term() {
        String op = "";
        String temp1 = "", temp2 = "", temp = "";
        String code = "";

        // P(factor)
        if (symtype == Symbol.ident || symtype == Symbol.number || symtype == Symbol.lparen) {
            temp1 = factor();
        } else {
            System.out.println("error: term-factor");
        }

        // 分析{<乘除运算符><因子>}
        while (symtype == Symbol.times || symtype == Symbol.slash) {
            if (symtype == Symbol.times)
                op = "*";
            else
                op = "/";
            nextsym();

            // P(factor)
            if (symtype == Symbol.ident || symtype == Symbol.number || symtype == Symbol.lparen) {
                temp2 = factor();
            } else {
                System.out.println("error: term-factor");
            }

            temp = intermediater.newTempVar();
            code = Integer.toString(intermediater.nextStat) + ":	" + temp + ":=" + temp1 + op + temp2;
            intermediater.emit(code);
            intermediater.nextStat++;
            temp1 = temp;
        }
        return temp1;
    }

    /**
     * 分析<因子>
     */
    public String factor() {
        String result = "";
        // P(ident)
        if (symtype == Symbol.ident) {
            result = sym.id;
            nextsym();
        }

        // P(number)
        else if (symtype == Symbol.number) {
            result = Integer.toString(sym.num);
            nextsym();
        }

        // P( LPAREN expression RPAREN)
        else if (symtype == Symbol.lparen) {
            nextsym();
            result = expression();
            if (symtype == Symbol.rparen) {
                nextsym();
            } else {
                System.out.println("error: factor-rparen");
            }
        } else {
            System.out.println("error: factor");
        }
        return result;
    }

    /**
     * 分析<条件语句>
     * Condition_statement -> IF Condition THEN (M1) Statement (M2)
     */
    public void ifStatement(ArrayList<Integer> nextList) {
        int M1, M2;
        ArrayList<Integer> trueList = intermediater.makeList();
        ArrayList<Integer> falseList = intermediater.makeList();
        // 为产生式右部的语句statement创建一个s_nextList
        ArrayList<Integer> s_nextList = intermediater.makeList();

        // P(if)
        if (symtype == Symbol.ifsym) {
            nextsym();
        } else {
            System.out.println("error: ifStatement-IF");
        }

        // P(condition)
        if (symtype == Symbol.plus || symtype == Symbol.minus || symtype == Symbol.ident || symtype == Symbol.number
                || symtype == Symbol.lparen) {
            condition(trueList, falseList);
            M1 = intermediater.nextStat;
            intermediater.BackPatch(trueList, M1); // 回填trueList地址:此时nextStat一定为M1.quard
        } else {
            System.out.println("error: ifStatement-condition");
        }

        // P(then)
        if (symtype == Symbol.thensym) {
            nextsym();
        } else {
            System.out.println("error: ifStatement-then");
        }

        // P(statement)
        if (statement_first.get(symtype)) {
            statement(s_nextList);
            M2 = intermediater.nextStat;
            intermediater.BackPatch(falseList, M2); // 回填E.falseList地址:此时nextStat一定为M2.quard
        } else {
            System.out.println("error: ifStatement-statement");
        }

        nextList = intermediater.merge(falseList, s_nextList); // 合并链
    }

    /**
     * 分析<条件>
     */
    public void condition(ArrayList<Integer> trueList, ArrayList<Integer> falseList) {
        String code = "", left = "", op = "", right = "";

        trueList.add(intermediater.nextStat); // 写死，真链一定在nextStat+2
        falseList.add(intermediater.nextStat + 1);

        // P(expression)
        if (symtype == Symbol.plus || symtype == Symbol.minus || symtype == Symbol.ident || symtype == Symbol.number
                || symtype == Symbol.lparen) {
            left = expression();
        } else {
            System.out.println("error: condition-expression");
        }

        // P(operator)
        if (operationMap.containsKey(symtype)) {
            op = operationMap.get(symtype);// 获取关系运算符具体的符号
            nextsym();
        } else {
            System.out.println("error: condition-operator");
        }

        // P(expression)
        if (symtype == Symbol.plus || symtype == Symbol.minus || symtype == Symbol.ident || symtype == Symbol.number
                || symtype == Symbol.lparen) {
            right = expression();
        } else {
            System.out.println("error: condition-expression");
        }

        code = Integer.toString(intermediater.nextStat) + ":    if " + left + op + right + " goto "; // 待回填：true
        intermediater.emit(code);
        intermediater.nextStat++;
        intermediater.emit(Integer.toString(intermediater.nextStat) + ":    goto "); // 待回填：false
        intermediater.nextStat++;
    }

    /**
     * 分析<循环语句>
     * Loop_statement -> WHILE (M1)Condition DO (M2)Statement (M3)
     */
    public void whileStatement(ArrayList<Integer> nextList) {
        int M1 = 0, M2 = 0, M3 = 0;
        ArrayList<Integer> trueList = intermediater.makeList();
        ArrayList<Integer> falseList = intermediater.makeList();
        // 为产生式右部的语句statement创建一个s_nextList
        ArrayList<Integer> s_nextList = intermediater.makeList();

        // P(while)
        if (symtype == Symbol.whilesym) {
            nextsym();
        } else {
            System.out.println("error: whileStatement-WHILE");
        }

        // P(condition)
        if (symtype == Symbol.plus || symtype == Symbol.minus || symtype == Symbol.ident || symtype == Symbol.number
                || symtype == Symbol.lparen) {
            M1 = intermediater.nextStat;
            condition(trueList, falseList);
        } else {
            System.out.println("error: whileStatement-condition");
        }

        // P(do)
        if (symtype == Symbol.dosym) {
            nextsym();
        } else {
            System.out.println("error: whileStatement-do");
        }

        // P(statement)
        if (statement_first.get(symtype)) {
            M2 = intermediater.nextStat;
            statement(s_nextList);
        } else {
            System.out.println("error: whileStatement-statement");
        }

        M3 = intermediater.nextStat + 1;
        intermediater.BackPatch(nextList, M1);
        intermediater.BackPatch(trueList, M2);
        intermediater.BackPatch(falseList, M3); //回填E.falseList地址:此时nextStat一定为M3.quard
        nextList = falseList;

        String code = Integer.toString(intermediater.nextStat) + ":    goto " + Integer.toString(M1);
        intermediater.emit(code);
        intermediater.nextStat++;
    }

}
