package headfront.jetfuel.execute.utils;

import headfront.jetfuel.execute.JetFuelExecute;
import headfront.jetfuel.execute.functions.JetFuelFunction;
import headfront.jetfuel.execute.impl.AmpsJetFuelExecute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Deepak on 25/04/2018.
 */
public class FunctionPublisherHelper {

    private JetFuelExecute execute;
    private List<JetFuelFunction> functions;

    private static Logger LOG = LoggerFactory.getLogger(AmpsJetFuelExecute.class);


    public void setExecute(JetFuelExecute execute) {
        this.execute = execute;
    }

    public void setFunctions(List<JetFuelFunction> functions) {
        this.functions = functions;
    }

    public void init(){
        AssertChecks.notNull(execute);
        LOG.info("FunctionPublisherHelper will initalise " + functions.size() + " functions");
        functions.forEach(execute::publishFunction);
    }
}
