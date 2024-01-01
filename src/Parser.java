import java.util.ArrayList;
import java.util.BitSet;

/**
 * 【PL/0编译器的语法分析器Parser】
 * 采用自上而下的递归子程序法；
 * 并在语法分析的过程中嵌入了语法错误检查和中间代码生成
 */
public class Parser {
    private final Lexer lex; // 对词法分析器的引用
    private final Table table; // 对符号表的引用
    private final Intermediater intermediater; // 对中间代码生成器的引用

    // 当前识别到的文法符号，由nextsym()读入
    private Symbol sym;

    // 位图存储一些复杂的first集合
    private final BitSet subprogram_first;
    private final BitSet statement_first;

    // 构造函数
    public Parser(Lexer l, Table t, Intermediater i) {
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
    }

    // 启动语法分析过程，必须先调用一次nextsym()
    public void parse() {
        nextsym(); // 前瞻分析需要预先读入一个符号
        program();
    }

    // 获得下一个语法符号
    public void nextsym() {
        lex.getsym();
        sym = lex.getSym();
    }

    /**
     * 分析<程序>
     */
    public void program() {
        // P (programHeader)
        if (sym.getSymtype() == Symbol.progsym) {
            programHeader();
        } else {
            Err.handleError("Expected 'PROGRAM' keyword.", lex.getCurrentLineNumber());
        }

        // P (subprogram)
        if (subprogram_first.get(sym.getSymtype())) {
            subprogram();
        } else {
            Err.handleError("Exist subprogram error.", lex.getCurrentLineNumber());
        }

    }

    /**
     * 分析<程序首部>
     */
    public void programHeader() {
        // P (PROGRAM)
        if (sym.getSymtype() == Symbol.progsym) {
            nextsym();
        } else {
            Err.handleError("Expected 'PROGRAM' keyword.", lex.getCurrentLineNumber());
        }

        // P (identifier)
        if (sym.getSymtype() == Symbol.ident) {
            nextsym();
        } else {
            Err.handleError("Invalid program_header(identifier).", lex.getCurrentLineNumber());
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
        if (sym.getSymtype() == Symbol.constsym) {
            constDeclaration();
        }
        // P (varDeclaration)
        if (sym.getSymtype() == Symbol.varsym) {
            varDeclaration();
        }
        // P (statement)
        if (statement_first.get(sym.getSymtype())) {
            statement(s_nextList);
        } else {
            if (sym.getSymtype() == Symbol.eof) {
                return;
            }
            Err.handleError("Missing subprogram.", lex.getCurrentLineNumber());

        }
    }

    /**
     * 分析<常量说明>
     */
    public void constDeclaration() {
        // P (CONST)
        if (sym.getSymtype() == Symbol.constsym) {
            nextsym();
        } else {
            Err.handleError("Expected 'CONST' keyword.", lex.getCurrentLineNumber());
        }

        // P (constDefinition)
        if (sym.getSymtype() == Symbol.ident) {
            if (!table.addItem(sym.getValue(), "Constant"))
                Err.handleError("Constant identifier repeat.", lex.getCurrentLineNumber());
            constDefinition();
        } else {
            Err.handleError("Expected identifier after CONST.", lex.getCurrentLineNumber());
        }

        while (sym.getSymtype() == Symbol.comma) {
            nextsym();
            // P (constDefinition)
            if (sym.getSymtype() == Symbol.ident) {
                if (!table.addItem(sym.getValue(), "Constant"))
                    Err.handleError("Constant identifier repeat.", lex.getCurrentLineNumber());
                constDefinition();
            } else {
                Err.handleError("Expected identifier after CONST.", lex.getCurrentLineNumber());
            }
        }

        // P (semicolon)
        if (sym.getSymtype() == Symbol.semicolon) {
            nextsym();
        } else {
            Err.handleError("Expected semicolon ';' after constant definition.", lex.getCurrentLineNumber());
        }
    }

    /**
     * 分析<常量定义>
     */
    public void constDefinition() {
        String id = "", op = "", num = "";

        // P (identifier)
        if (sym.getSymtype() == Symbol.ident) {
            id = sym.getValue();
            nextsym();
        } else {
            Err.handleError("Expected identifier first in the constant definition.", lex.getCurrentLineNumber());
        }

        // P (assign)
        if (sym.getSymtype() == Symbol.assign) {
            op = sym.getValue();
            nextsym();
        } else {
            Err.handleError("Expected assign ':=' in the constant definition.", lex.getCurrentLineNumber());
        }

        // P (number)
        if (sym.getSymtype() == Symbol.number) {
            num = Integer.toString(sym.getNum());
            nextsym();
        } else {
            Err.handleError("Expected an unsigned_int in the constant definition.", lex.getCurrentLineNumber());
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
        if (sym.getSymtype() == Symbol.varsym) {
            nextsym();
        } else {
            Err.handleError("Expected 'VAR' in the variable definition.", lex.getCurrentLineNumber());
        }

        // P (identifier)
        if (sym.getSymtype() == Symbol.ident) {
            if (!table.addItem(sym.getValue(), "Variable"))
                Err.handleError("Variable identifier repeat.", lex.getCurrentLineNumber());
            nextsym();
        } else {
            Err.handleError("Invalid identifier in the variable definition.", lex.getCurrentLineNumber());
        }

        while (sym.getSymtype() == Symbol.comma) {
            nextsym();
            // P (identifier)
            if (sym.getSymtype() == Symbol.ident) {
                if (!table.addItem(sym.getValue(), "Variable"))
                    Err.handleError("Variable identifier repeat.", lex.getCurrentLineNumber());
                nextsym();
            } else {
                Err.handleError("Invalid identifier in the variable definition.", lex.getCurrentLineNumber());
            }
        }

        // P (semicolon)
        if (sym.getSymtype() == Symbol.semicolon) {
            nextsym();
        } else {
            Err.handleError("Expected semicolon ';' in the variable definition.", lex.getCurrentLineNumber());
        }
    }

    /**
     * 分析<复合语句>
     */
    public void compoundStatement() {
        // 为产生式右部的语句statement创建一个s_nextList
        ArrayList<Integer> s_nextList = intermediater.makeList();

        // P(BEGIN)
        if (sym.getSymtype() == Symbol.beginsym) {
            nextsym();
        } else {
            Err.handleError("Expected 'BEGIN' keyword.", lex.getCurrentLineNumber());
        }

        // P(statement)
        if (statement_first.get(sym.getSymtype())) {
            statement(s_nextList);
        } else {
            Err.handleError("Error in compoundStatement: statement expected.", lex.getCurrentLineNumber());
        }

        while (sym.getSymtype() == Symbol.semicolon) {
            nextsym();
            // P (statement)
            if (statement_first.get(sym.getSymtype())) {
                statement(s_nextList);
            } else {
                Err.handleError("Error in compoundStatement: statement expected.", lex.getCurrentLineNumber());
            }
        }

        // P (END)
        if (sym.getSymtype() == Symbol.endsym) {
            nextsym();
        } else {
            Err.handleError("Error in compoundStatement: 'END' keyword expected.", lex.getCurrentLineNumber());
        }
    }

    /**
     * 分析<语句>
     */
    public void statement(ArrayList<Integer> nextList) {

        switch (sym.getSymtype()) {
            case Symbol.ident:
                assignStatement();
                break;
            case Symbol.ifsym:
                ifStatement(nextList);
                break;
            case Symbol.beginsym:
                compoundStatement();
                break;
            case Symbol.whilesym:
                whileStatement(nextList);
                break;
            default:
                if (sym.getSymtype() == Symbol.semicolon || sym.getSymtype() == Symbol.endsym || sym.getSymtype() == Symbol.eof) {
                    return;
                } else {
                    Err.handleError("Error in statement.", lex.getCurrentLineNumber());
                }
                break;
        }
    }

    /**
     * 分析<赋值语句>
     */
    public void assignStatement() {
        String code, left = "", op = "", right = "";
        // P(ident)
        if (sym.getSymtype() == Symbol.ident) {
            if (table.lookup(sym.getValue())) {
                left = sym.getValue();
                nextsym();
            } else {
                Err.handleError("Error in assignStatement: Identifier [" + sym.getValue() + "] undefined!", lex.getCurrentLineNumber());
            }

        } else {
            Err.handleError("Error in assignStatement: Identifier expected.", lex.getCurrentLineNumber());
        }

        // P(assign)
        if (sym.getSymtype() == Symbol.assign) {
            op = sym.getValue();
            nextsym();
        } else {
            Err.handleError("Error in assignStatement: ':=' expected.", lex.getCurrentLineNumber());
        }

        // P(expression)
        if (sym.getSymtype() == Symbol.plus || sym.getSymtype() == Symbol.minus || sym.getSymtype() == Symbol.ident || sym.getSymtype() == Symbol.number
                || sym.getSymtype() == Symbol.lparen) {
            right = expression();
        } else {
            Err.handleError("Error in assignStatement: Expression expected.", lex.getCurrentLineNumber());
        }
        code = Integer.toString(intermediater.nextStat) + ":	" + left + op + right;
        intermediater.emit(code);
        intermediater.nextStat++;
    }

    /**
     * 分析<表达式>
     */
    public String expression() {
        String code;
        String value;
        String temp1 = "", temp2 = "", temp;
        String op;
        String prefix;

        // P(zhegnfu)分析[+|-]<项>
        if (sym.getSymtype() == Symbol.plus || sym.getSymtype() == Symbol.minus) {
            prefix = sym.getValue();
            nextsym();
            if (sym.getSymtype() == Symbol.ident || sym.getSymtype() == Symbol.number || sym.getSymtype() == Symbol.lparen) {
                value = term();
                temp1 = intermediater.newTempVar();
                code = Integer.toString(intermediater.nextStat) + ":	" + temp1 + ":=" + prefix + value;
                intermediater.emit(code);
                intermediater.nextStat++;
                return temp1;
            } else {
                Err.handleError("Error in expression: Term expected.", lex.getCurrentLineNumber());
            }
        }

        // P(term)
        if (sym.getSymtype() == Symbol.ident || sym.getSymtype() == Symbol.number || sym.getSymtype() == Symbol.lparen) {
            temp1 = term();
        } else {
            Err.handleError("Error in expression: Term expected.", lex.getCurrentLineNumber());
        }


        // 分析{<加法运算符><项>}
        while (sym.getSymtype() == Symbol.plus || sym.getSymtype() == Symbol.minus) {
            op = sym.getValue();
            nextsym();

            // P(term)
            if (sym.getSymtype() == Symbol.ident || sym.getSymtype() == Symbol.number || sym.getSymtype() == Symbol.lparen) {
                temp2 = term();
            } else {
                Err.handleError("Error in expression: Term expected.", lex.getCurrentLineNumber());
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
        String op;
        String temp1 = "", temp2 = "", temp;
        String code;

        // P(factor)
        if (sym.getSymtype() == Symbol.ident || sym.getSymtype() == Symbol.number || sym.getSymtype() == Symbol.lparen) {
            temp1 = factor();
        } else {
            Err.handleError("Error in term: Factor expected.", lex.getCurrentLineNumber());
        }

        // 分析{<乘除运算符><因子>}
        while (sym.getSymtype() == Symbol.times || sym.getSymtype() == Symbol.slash) {
            op = sym.getValue();
            nextsym();

            // P(factor)
            if (sym.getSymtype() == Symbol.ident || sym.getSymtype() == Symbol.number || sym.getSymtype() == Symbol.lparen) {
                temp2 = factor();
            } else {
                Err.handleError("Error in term: Factor expected.", lex.getCurrentLineNumber());
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
        if (sym.getSymtype() == Symbol.ident) {
            result = sym.getValue();
            nextsym();
        }

        // P(number)
        else if (sym.getSymtype() == Symbol.number) {
            result = Integer.toString(sym.getNum());
            nextsym();
        }

        // P( LPAREN expression RPAREN)
        else if (sym.getSymtype() == Symbol.lparen) {
            nextsym();
            result = expression();
            if (sym.getSymtype() == Symbol.rparen) {
                nextsym();
            } else {
                System.out.println("error: factor-rparen");
                Err.handleError("Error in factor: right-paren expected.", lex.getCurrentLineNumber());
            }
        } else {
            Err.handleError("Error in factor.", lex.getCurrentLineNumber());
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
        if (sym.getSymtype() == Symbol.ifsym) {
            nextsym();
        } else {
            Err.handleError("Error in ifStatement: 'IF' keyword expected.", lex.getCurrentLineNumber());
        }

        // P(condition)
        if (sym.getSymtype() == Symbol.plus || sym.getSymtype() == Symbol.minus || sym.getSymtype() == Symbol.ident || sym.getSymtype() == Symbol.number
                || sym.getSymtype() == Symbol.lparen) {
            condition(trueList, falseList);
            M1 = intermediater.nextStat;
            intermediater.BackPatch(trueList, M1); // 回填trueList地址:此时nextStat一定为M1.quard
        } else {
            Err.handleError("Error in ifStatement: condition expected.", lex.getCurrentLineNumber());
        }

        // P(then)
        if (sym.getSymtype() == Symbol.thensym) {
            nextsym();
        } else {
            Err.handleError("Error in ifStatement: 'THEN' keyword expected.", lex.getCurrentLineNumber());
        }

        // P(statement)
        if (statement_first.get(sym.getSymtype())) {
            statement(s_nextList);
            M2 = intermediater.nextStat;
            intermediater.BackPatch(falseList, M2); // 回填E.falseList地址:此时nextStat一定为M2.quard
        } else {
            Err.handleError("Error in ifStatement: statement expected.", lex.getCurrentLineNumber());
        }

        nextList = intermediater.merge(falseList, s_nextList); // 合并链
    }

    /**
     * 分析<条件>
     */
    public void condition(ArrayList<Integer> trueList, ArrayList<Integer> falseList) {
        String code, left = "", op = "", right = "";

        trueList.add(intermediater.nextStat); // 写死，真链一定在nextStat+2
        falseList.add(intermediater.nextStat + 1);

        // P(expression)
        if (sym.getSymtype() == Symbol.plus || sym.getSymtype() == Symbol.minus || sym.getSymtype() == Symbol.ident || sym.getSymtype() == Symbol.number
                || sym.getSymtype() == Symbol.lparen) {
            left = expression();
        } else {
            Err.handleError("Error in condition: expression expected.", lex.getCurrentLineNumber());
        }

        // P(operator)
        if (sym.getSymtype() >= 8 && sym.getSymtype() <= 13) {
            op = sym.getValue();// 获取关系运算符具体的符号
            nextsym();
        } else {
            Err.handleError("Error in condition: relational operator expected.", lex.getCurrentLineNumber());
        }

        // P(expression)
        if (sym.getSymtype() == Symbol.plus || sym.getSymtype() == Symbol.minus || sym.getSymtype() == Symbol.ident || sym.getSymtype() == Symbol.number
                || sym.getSymtype() == Symbol.lparen) {
            right = expression();
        } else {
            Err.handleError("Error in condition: expression expected.", lex.getCurrentLineNumber());
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
        int M1 = 0, M2 = 0, M3;
        ArrayList<Integer> trueList = intermediater.makeList();
        ArrayList<Integer> falseList = intermediater.makeList();
        // 为产生式右部的语句statement创建一个s_nextList
        ArrayList<Integer> s_nextList = intermediater.makeList();

        // P(while)
        if (sym.getSymtype() == Symbol.whilesym) {
            nextsym();
        } else {
            Err.handleError("Error in whileStatement: 'WHILE' keyword expected.", lex.getCurrentLineNumber());
        }

        // P(condition)
        if (sym.getSymtype() == Symbol.plus || sym.getSymtype() == Symbol.minus || sym.getSymtype() == Symbol.ident || sym.getSymtype() == Symbol.number
                || sym.getSymtype() == Symbol.lparen) {
            M1 = intermediater.nextStat;
            condition(trueList, falseList);
        } else {
            Err.handleError("Error in whileStatement: condition expected.", lex.getCurrentLineNumber());
        }

        // P(do)
        if (sym.getSymtype() == Symbol.dosym) {
            nextsym();
        } else {
            Err.handleError("Error in whileStatement: 'DO' keyword expected.", lex.getCurrentLineNumber());
        }

        // P(statement)
        if (statement_first.get(sym.getSymtype())) {
            M2 = intermediater.nextStat;
            statement(s_nextList);
        } else {
            Err.handleError("Error in whileStatement: statement expected.", lex.getCurrentLineNumber());
        }

        M3 = intermediater.nextStat + 1;
        intermediater.BackPatch(nextList, M1);
        intermediater.BackPatch(trueList, M2);
        intermediater.BackPatch(falseList, M3); // 回填E.falseList地址:此时nextStat一定为M3.quard
        nextList = falseList;

        String code = Integer.toString(intermediater.nextStat) + ":    goto " + Integer.toString(M1);
        intermediater.emit(code);
        intermediater.nextStat++;
    }
}
