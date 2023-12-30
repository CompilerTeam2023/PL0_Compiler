import java.util.HashMap;
import java.util.Map;

/**
 * 　　这个类封装了PL/0编译器的符号表，C语言版本中关键的全局变量tx和table[]就在这里。
 */
public class Table {

    public static final int constant = 0;
    public static final int variable = 1;

    /**
     * 标识符map
     */
    Map<String, Integer> indentifier_map = new HashMap<>();

    /**
     * lookup函数：检查标识符是否存在
     */
    public boolean lookup(String id) {
        return indentifier_map.containsKey(id);
    }

    /**
     * addItem:添加表项
     */
    public void addItem(String id, Integer type) {
        if (!indentifier_map.containsKey(id))
            indentifier_map.put(id, type);
        else
            System.out.println("error: 已经存在标识" + id);
    }

    /**
     * printTable:打印符号表内容
     */
    public void printTable() {
        for (Map.Entry<String, Integer> entry : indentifier_map.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println(key + ":" + value);
        }
    }


    /**
     * 打印符号表内容，摘自C语言版本的 block() 函数。
     *
     * @param start 当前作用域符号表区间的左端
     */
//    public void debugTable(int start) {
//        if (!PL0.tableswitch)
//            return;
//        System.out.println("TABLE:");
//        if (start >= tx)
//            System.out.println("    NULL");
//        for (int i = start + 1; i <= tx; i++) {
//            String msg = "OOPS! UNKNOWN TABLE ITEM!";
//            switch (table[i].kind) {
//                case Item.constant:
//                    msg = "    " + i + " const " + table[i].name + " val=" + table[i].val;
//                    break;
//                case Item.variable:
//                    msg = "    " + i + " var   " + table[i].name + " lev=" + table[i].level + " addr=" + table[i].adr;
//                    break;
//                case Item.procedur:
//                    msg = "    " + i + " proc  " + table[i].name + " lev=" + table[i].level + " addr=" + table[i].adr + " size=" + table[i].size;
//                    break;
//            }
//            System.out.println(msg);
//            PL0.fas.println(msg);
//        }
//        System.out.println();
//    }

}
