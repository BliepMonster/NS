package utils;

import java.util.HashSet;

public class VariableList extends HashSet<String> {
    public VariableList() {
        super();
    }
    public VariableList(HashSet<String> list) {
        super(list);
    }
    public VariableList(String item) {
        super();
        add(item);
    }
    public VariableList with(VariableList list) {
        VariableList newList = new VariableList(this);
        newList.addAll(list);
        return newList;
    }
}
