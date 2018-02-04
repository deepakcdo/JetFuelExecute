package headfront.jetfuel.execute.functions;

/**
 * Created by Deepak on 09/04/2017.
 */
public class FunctionParameter {

    private final String parameterName;
    private final Class parameterType;
    private String description;

    public FunctionParameter(String parameterName, Class parameterType, String description) {
        this.parameterName = parameterName;
        this.parameterType = parameterType;
        this.description = description;
    }

    public String getParameterName() {
        return parameterName;
    }

    public Class getParameterType() {
        return parameterType;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return parameterType.getSimpleName() + " " + parameterName;
    }

    public String toStringWithDescription() {
        return parameterType.getSimpleName() + " " + parameterName + ", // " + description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FunctionParameter that = (FunctionParameter) o;

        if (parameterName != null ? !parameterName.equals(that.parameterName) : that.parameterName != null)
            return false;
        if (parameterType != null ? !parameterType.equals(that.parameterType) : that.parameterType != null)
            return false;
        return description != null ? description.equals(that.description) : that.description == null;
    }

    @Override
    public int hashCode() {
        int result = parameterName != null ? parameterName.hashCode() : 0;
        result = 31 * result + (parameterType != null ? parameterType.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
