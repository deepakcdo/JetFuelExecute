package headfront.jetfuel.execute;

/**
 * Created by Deepak on 01/02/2018.
 */
public enum FunctionState {

    RequestNew("RequestNew", false),
    SubActive("SubActive", false),
    SubUpdate("SubUpdate", false),
    SubCancelled("SubCancelled", true),
    RequestCancelSub("RequestCancelSub", false),
    Completed("Completed", true),
    Error("Error", true),
    Timeout("Timeout", false); // For now Timeout is not a End state.

    private String text;
    private boolean isFinalState;

    FunctionState(String text, boolean isFinalState) {
        this.text = text;
        this.isFinalState = isFinalState;
    }

    public String getText() {
        return text;
    }

    public boolean isFinalState() {
        return isFinalState;
    }
}
