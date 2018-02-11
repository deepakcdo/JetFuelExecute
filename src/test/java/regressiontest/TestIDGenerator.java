package regressiontest;

import headfront.jetfuel.execute.utils.FunctionUtils;

import java.util.function.Function;

/**
 * Created by Deepak on 10/02/2018.
 */
public class TestIDGenerator implements Function<String, String> {

    private int count = 1000;

    @Override
    public String apply(String s) {
        return s + FunctionUtils.NAME_SEPARATOR + (count++);
    }
}
