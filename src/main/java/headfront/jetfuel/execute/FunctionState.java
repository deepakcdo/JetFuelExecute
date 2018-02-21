package headfront.jetfuel.execute;

/**
 * Created by Deepak on 01/02/2018.
 */
public enum FunctionState {

    RequestNew("RequestNew"),
    SubActive("SubActive"),
    SubUpdate("SubUpdate"),
    SubCancelled("SubCancelled"),
    RequestCancelSub("RequestCancelSub"),
    Completed("Completed"),
    Error("Error"),
    Timeout("Timeout");

    private String text;

    FunctionState(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

}
