package headfront.jetfuel.execute;

/**
 * Created by Deepak on 01/02/2018.
 */
public enum FunctionAccessType {

    Read("Read Only"),
    Write("Write"),
    Refresh("Refresh / Replay");

    private String text;

    FunctionAccessType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
