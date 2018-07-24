package headfront.jetfuel.execute;

/**
 * Created by Deepak on 12/04/2017.
 */
public interface JetFuelExecuteConstants {

    //Topics
    String CLIENT_STATUS_TOPIC = "/AMPS/ClientStatus";

    // Publish Fields
    String FUNCTION_NAME = "FunctionName";
    String PUBLISH_FUNCTION_ID = "ID";
    String FUNCTION_TO_CALL = "FunctionToCall";
    String FUNCTION_DESCRIPTION = "FunctionDescription";
    String PARAMETERS = "FunctionParameters";
    String PARAMETERS_NAMES = "ParametersNames";
    String PARAMETERS_TYPES = "ParametersTypes";
    String PARAMETERS_DESC = "ParametersDesc";
    String FUNCTION_PUBLISHER_NAME = "FunctionPublisherName";
    String FUNCTION_PUBLISHER_HOSTNAME = "FunctionPublisherHostName";
    String FUNCTION_PUBLISH_TIME = "FunctionPublisherTime";
    String RETURN_DESCRIPTION = "ReturnDescription";
    String RETURN_TYPE = "ReturnType";
    String RETURN_VALUE = "ReturnValue";
    String FUNCTION_ACCESS_TYPE = "FunctionAccessType";
    String FUNCTION_EXECUTION_TYPE = "FunctionExecutionType";
    String FUNCTION_UPDATE_MESSAGE = "FunctionUpdateMsg";
    String FUNCTION_AMPS_INSTANCE_OWNER = "AmpsInstanceOwner";
    String ALLOW_MULTI_EXECUTE = "AllowMultiExecute";


    String FUNCTION_CALL_ID = "ID";
    String FUNCTION_CALLER_HOSTNAME = "FunctionCallerHostName";
    String MSG_CREATION_TIME = "MsgCreationTime";
    String MSG_CREATION_NAME = "MsgCreationName";
    String FUNCTION_INITIATOR_NAME = "FunctionInitiatorName";
    String FUNCTION_RECEIVED_BY = "FunctionReceivedBy";


    String CURRENT_STATE = "CurrentState";
    String CURRENT_STATE_MSG = "CurrentStateMsg";
    String EXCEPTION_MESSAGE = "ExceptionMessage";

    String CANCEL_REQ_MESSAGE = "Cancel request from client";
}
