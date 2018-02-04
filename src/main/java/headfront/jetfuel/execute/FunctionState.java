package headfront.jetfuel.execute;

/**
 * Created by Deepak on 01/02/2018.
 */
public enum FunctionState {

    StateNew("StateNew"),
    StateDone("StateDone"),
    StateError("StateError"),
    StateTimeout("StateTimeout");

    private String text;

    FunctionState(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

}
