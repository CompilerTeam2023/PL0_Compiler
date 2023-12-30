import java.util.BitSet;

/**
 * 语法分析器。这是PL/0分析器中最重要的部分，在语法分析的过程中嵌入了语法错误检查和目标代码生成。
 */
public class Parser {
    private Scanner lex; // 对词法分析器的引用
    private Table table; // 对符号表的引用
    private Interpreter interp; // 对目标代码生成器的引用


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
    private BitSet operator;

    public Parser(Scanner l, Table t, Interpreter i) {
        lex = l;
        table = t;
        interp = i;

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

        //关系运算符
        operator = new BitSet(Symbol.symnum);
        operator.set(Symbol.eql);
        operator.set(Symbol.neq);
        operator.set(Symbol.lss);
        operator.set(Symbol.leq);
        operator.set(Symbol.gtr);
        operator.set(Symbol.geq);

    }

    /**
     * 启动语法分析过程，此前必须先调用一次nextsym()
     *
     * @see #nextsym()
     */
    public void parse() {
        program();
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
     */
    public void subprogram() {
        // P (constdeclaration)
        if (symtype == Symbol.constsym) {
            constDeclaration();
        }
        // P (varDeclaration)
        if(symtype==Symbol.varsym) {
            varDeclaration();
        }
        // P (statement)
        if (statement_first.get(symtype)) {
            statement();
        } else {
            // TODO: 这里不太确定，这个源程序结束后他到底读了个啥？
            if(symtype==Symbol.eof){
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
        }
        else{
            System.out.println("error: constDeclaration-CONST");
        }

        // P (constDefinition)
        if(symtype==Symbol.ident){
            constDefinition();
        }
        else{
            System.out.println("error: constDeclaration-constDefinition");
        }

        while (symtype == Symbol.comma) {
            nextsym();
            // P (constDefinition)
            if(symtype==Symbol.ident){
                constDefinition();
            }
            else{
                System.out.println("error: constDeclaration-constDefinition");
            }
        }

        // P (semicolon)
        if(symtype==Symbol.semicolon){
            nextsym();
        }
        else{
            System.out.println("error: constDeclaration-semicolon");
        }
    }

    /**
     * 分析<常量定义>
     */
    public void constDefinition() {
        // P (identifier)
        if (symtype == Symbol.ident) {
            nextsym();
        } else {
            System.out.println("error: constDefinition-identifier");
        }

        // P (assign)
        if (symtype == Symbol.assign) {
            nextsym();
        } else {
            System.out.println("error: constDefinition-assign");
        }

        // P (number)
        if (symtype == Symbol.number) {
            nextsym();
        } else {
            System.out.println("error: constDefinition-number");
        }
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
            nextsym();
        } else {
            System.out.println("error: varDeclaration-identifier");
        }

        while (symtype == Symbol.comma) {
            nextsym();
            // P (identifier)
            if(symtype==Symbol.ident){
                nextsym();
            }
            else{
                System.out.println("error: varDeclaration-identifier");
            }
        }

        // P (semicolon)
        if(symtype==Symbol.semicolon){
            nextsym();
        }
        else{
            System.out.println("error: varDeclaration-semicolon");
        }
    }

    /**
     * 分析<复合语句>
     */
    public void compoundStatement(){
        // P(BEGIN)
        if(symtype==Symbol.beginsym){
            nextsym();
        }
        else{
            System.out.println("error: compoundStatement-BEGIN");
        }

        //  P(statement)
        if(statement_first.get(symtype)){
            statement();
        }
        else{
            System.out.println("error: compoundStatement-statement");
        }

        while (symtype == Symbol.semicolon) {
            nextsym();
            // P (statement)
            if(statement_first.get(symtype)){
                statement();
            }
            else{
                System.out.println("error: compoundStatement-statement");
            }
        }

        // P (END)
        if(symtype==Symbol.endsym){
            nextsym();
        }
        else{
            System.out.println("error: compoundStatement-END");
        }
    }

    /**
     * 分析<语句>
     */
    void statement() {
        switch (symtype) {
            case Symbol.ident:
                assignStatement();
                break;
            case Symbol.ifsym:
                ifStatement();
                break;
            case Symbol.beginsym:
                compoundStatement();
                break;
            case Symbol.whilesym:
                whileStatement();
                break;
            default:
                if (symtype == Symbol.semicolon || symtype == Symbol.endsym || symtype == Symbol.eof){
                    return;
                }
                else{
                    System.out.println("error: statement");
                }
                break;
        }
    }


    /**
     * 分析<赋值语句>
     */
    private void assignStatement() {
        //P(ident)
        if (symtype == Symbol.ident){
            nextsym();
        }
        else{
            System.out.println("error: assignStatement-ident");
        }

        //P(assign)
        if (symtype == Symbol.assign){
            nextsym();
        }
        else{
            System.out.println("error: assignStatement-assign");
        }

        //P(expression)
        if (symtype == Symbol.plus || symtype == Symbol.minus){
            expression();
        }
        else{
            System.out.println("error: assignStatement-expression");
        }

    }


    /**
     * 分析<表达式>
     */
    private void expression() {
        //P(zhegnfu)分析[+|-]<项>
        if (symtype == Symbol.plus || symtype == Symbol.minus){
            nextsym();
        }

        //P(term)
        if (symtype == Symbol.ident || symtype == Symbol.number || symtype == Symbol.lparen){
            term();
        }
        else{
            System.out.println("error: expression-term");
        }

        // 分析{<加法运算符><项>}
        while (symtype == Symbol.plus || symtype == Symbol.minus) {
            nextsym();

            //P(term)
            if (symtype == Symbol.ident || symtype == Symbol.number || symtype == Symbol.lparen){
                term();
            }
            else{
                System.out.println("error: expression-term");
            }
        }
    }


    /**
     * 分析<表达式>
     */
    private void term() {
        //P(factor)
        if (symtype == Symbol.ident || symtype == Symbol.number || symtype == Symbol.lparen){
            factor();
        }
        else{
            System.out.println("error: term-factor");
        }

        // 分析{<乘除运算符><因子>}
        while (symtype == Symbol.times || symtype == Symbol.slash) {
            nextsym();

            //P(factor)
            if (symtype == Symbol.ident || symtype == Symbol.number || symtype == Symbol.lparen){
                factor();
            }
            else{
                System.out.println("error: term-factor");
            }
        }
    }


    /**
     * 分析<因子>
     */
    private void factor() {

        //P(ident)
        if(symtype == Symbol.ident){
            nextsym();
        }

        //P(number)
        else if(symtype == Symbol.number){
            nextsym();
        }

        //P( LPAREN expression RPAREN)
        else if(symtype == Symbol.lparen){
            nextsym();
            expression();
            if(symtype == Symbol.rparen){
                nextsym();
            }
            else{
                System.out.println("error: factor-rparen");
            }
        }
        else{
            System.out.println("error: factor");
        }
    }

    /**
     * 分析<条件语句>
     */
    private void ifStatement() {
        //P(if)
        if(symtype == Symbol.ifsym){
            nextsym();
        }
        else{
            System.out.println("error: ifStatement-IF");
        }

        //P(+/-)
        if(symtype == Symbol.plus || symtype == Symbol.minus){
            condition();
        }
        else{
            System.out.println("error: ifStatement-condition");
        }

        //P(then)
        if(symtype == Symbol.thensym){
            nextsym();
        }
        else{
            System.out.println("error: ifStatement-then");
        }

        //P(statement)
        if(statement_first.get(symtype)){
            statement();
        }
        else {
            System.out.println("error: ifStatement-statement");
        }

    }

    /**
     * 分析<条件>
     */
    private void condition() {
        //P(expression)
        if (symtype == Symbol.plus || symtype == Symbol.minus){
            expression();
        }
        else{
            System.out.println("error: condition-expression");
        }

        //P(operator)
        if(operator.get(symtype)){
            nextsym();
        }
        else{
            System.out.println("error: condition-operator");
        }

        //P(expression)
        if (symtype == Symbol.plus || symtype == Symbol.minus){
            expression();
        }
        else{
            System.out.println("error: condition-expression");
        }
    }


    /**
     * 分析<循环语句>
     */
    private void whileStatement() {
        //P(while)
        if(symtype == Symbol.whilesym){
            nextsym();
        }
        else{
            System.out.println("error: whileStatement-WHILE");
        }

        //P(+/-)
        if(symtype == Symbol.plus || symtype == Symbol.minus){
            condition();
        }
        else{
            System.out.println("error: whileStatement-condition");
        }

        //P(do)
        if(symtype == Symbol.dosym){
            nextsym();
        }
        else{
            System.out.println("error: whileStatement-do");
        }

        //P(statement)
        if(statement_first.get(symtype)){
            statement();
        }
        else {
            System.out.println("error: whileStatement-statement");
        }
    }


}
