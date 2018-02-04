package headfront.jetfuel.execute;

/**
 * Created by Deepak on 01/02/2018.
 */
public enum FunctionExecutionType {

    RequestResponse("Request / Response"),
    Subscription("Subscription");

    private String text;

    FunctionExecutionType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
