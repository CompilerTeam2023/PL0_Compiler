import java.util.HashMap;

/**
 * 　　这个类封装了PL/0编译器的符号表
 */
public class Table {

    // 标识符map: <key=标识符的名称, value=标识符的类型>
    private final HashMap<String, String> tokenTable = new HashMap<>();

    // lookup函数：检查标识符是否存在
    public boolean lookup(String id) {
        return tokenTable.containsKey(id);
    }

    // addItem函数: 向符号表中添加表项
    public boolean addItem(String id, String type) {
        if (!tokenTable.containsKey(id)) {
            tokenTable.put(id, type);
            return true;
        } else
            return false;
    }

    // printTable函数: 打印符号表内容
    public void printTable() {
        for (HashMap.Entry<String, String> entry : tokenTable.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("name: " + key + ", type: " + value);
        }
    }
}
