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

    private JetFuelExecute jetFuelExecute;
    private List<JetFuelFunction> functions;

    private static Logger LOG = LoggerFactory.getLogger(AmpsJetFuelExecute.class);


    public void setJetFuelExecute(JetFuelExecute execute) {
        this.jetFuelExecute = execute;
    }

    public void setFunctionsToPublish(List<JetFuelFunction> functions) {
        this.functions = functions;
    }

    public void init() {
        AssertChecks.notNull(jetFuelExecute);
        LOG.info("FunctionPublisherHelper will initialise " + functions.size() + " functions");
        functions.forEach(jetFuelExecute::publishFunction);
    }
}
