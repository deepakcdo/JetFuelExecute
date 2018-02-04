# JetFuelExecute
JetFuelExecute is a Remote Procedure Call (RPC) library over a super fast journaled and reliable messaging bus.

JetFuelExecute allows a developer to publish a function on a bus and any other client connected to the bus to call it. It takes care of all message and type conversion. The bus itself is journaled so you can easily look at fields of the request and response. This should simplify the life of developers and support staff who constantly have to debug production issues.

This library forms the basis of a very good command / response paradigm. This is bread and butter for most applications. When one process has to tell another process to do something and report back.


Apart from the awesome flexibility of just publishing and calling remote function with full audit JetFuelExecute also provides these extra features :-
1) Automatic timeout response if the publishing server goes down so you client is not waiting for ever.
2) Removal of published functions if the publisher that published it disconnects. So client always knows what functions are really available before making the call
3) Type safety of parameters. So when you send a integer its received as a integer
4) Coming soon - The Ability to handle client disconnects. e.g. if a client calls function like QuoteOn and then disconnects after a few minutes, the function publisher that processed the QuoteOn request will realise the client disconnected and execute a clean up action. Here a clean up action could pull all the quotes from the market for the disconnected user.
5) Coming soon - Ability to make a function call which is executed by several publishers. e.g. if you have BankOff function published by 5 different gateways, you can call this once and each of the 5 publisher will execute this. This is very powerful feature and needs to be understood and used very carefully.
6) Coming soon - Subscriptions requests. This will allow you to create a function which can stream you continuous updates till you unsubscribe. Very useful if you want to get a stream of custom price calculations.

Currently the only bus that is supported is http://www.crankuptheamps.com/ In future other buses will be supported.

This project currently has the java implementation, in the near future we will javascript and C implementation. This means JetFuelExecute functions can be published and called from multiple languages.

JetFuelExplorer is another tool set you can use with JetFuelExecute. With JetFuelExplorer you can do 3 important things

1) View available functions that are published by other servers
![screenshot](http://headfront.co.uk/JetFuelExecuteAvailableFunctions.png)

2) Test a function - this is so useful
![screenshot](http://headfront.co.uk/JetFuelExecuteTestFunction.png)

3) View the full Request/Response message - your support team will love you
![screenshot](http://headfront.co.uk/JetFuelExecuteAudit.png)


Let quickly look how you can use JetFuelExecute in two easy steps :-
1) Publish a function
2) Call the function

Let look at step 1 first. Publishing a function on the bus is easy.
```java
            // Create amps connection
            final HAClient haClient = new HaClientFactory().createHaClient("SampleJetFuelSever",
                    "tcp://192.168.56.101:8001/amps/json", false);
            ObjectMapper jsonMapper = new ObjectMapper();
            System.out.println("Connected to Amps");

            //Create JetFuelExecute
            JetFuelExecute jetFuelExecute = new AmpsJetFuelExecute(haClient, jsonMapper);
            jetFuelExecute.initialise();

            //Create JetFuelExecute Function

            // First Create Parameters
            FunctionParameter citizenParmeter = new FunctionParameter("isCitizen", Boolean.class,
                    "Set this to true of the person is a citizen of the country");
            FunctionParameter ageParmeter = new FunctionParameter("age", Integer.class,
                    "Set age of the person");
            List<FunctionParameter> functionParameters = new ArrayList<>();
            functionParameters.add(citizenParmeter);
            functionParameters.add(ageParmeter);

            // Then create the function
            JetFuelFunction ableToVoteFunction = new JetFuelFunction("CheckAbilityToVote",
                    "This function will check if a person is able to vote",
                    functionParameters,
                    Boolean.class, "Return true if person can vote else false",
                    new AbleToVoteExecutor(), FunctionAccessType.Read, FunctionExecutionType.RequestResponse);

            System.out.println("Publishing Function");
            // publish JetFuel Function on the bus
            jetFuelExecute.publishFunction(ableToVoteFunction);

            System.out.println("Now waiting for client calls");
```

When you publish a function on the bus you also need to tell it how to process the function call. This is done easily by extending AbstractFunctionExecutor and passing it as a parameter to the publishFunction method.

```java
    static class AbleToVoteExecutor extends AbstractFunctionExecutor {

        @Override
        protected void executeFunction(String id, List<Object> parameters, FunctionResponse result) {
            Boolean citizen = Boolean.parseBoolean(parameters.get(0).toString());
            Integer age = Integer.parseInt(parameters.get(1).toString());
            if (!citizen) {
                result.onCompleted(id, "You have to be a citizen to vote", false);
            } else if (age <= 20) {
                result.onCompleted(id, "You have to be 21 or over to vote", false);
            } else {
                result.onCompleted(id, "You can vote", true);
            }

        }
    }
```

Now lets look at step 2. Calling a function. This is even easier than publishing the function. You simply call the excuteFunction with the function name, parameters and FunctionResponse listener

```java
            // Create amps connection
            final HAClient haClient = new HaClientFactory().createHaClient("SampleJetFuelClient",
                    "tcp://192.168.56.101:8001/amps/json", false);
            ObjectMapper jsonMapper = new ObjectMapper();
            System.out.println("Connected to Amps");

            //Create JetFuelExecute
            JetFuelExecute jetFuelExecute = new AmpsJetFuelExecute(haClient, jsonMapper);
            jetFuelExecute.initialise();

            // calling function

            final String id1 = jetFuelExecute.executeFunction("SampleJetFuelSever.CheckAbilityToVote",
                    new Object[]{true, 22}, new ClientFunctionResponse());
```
And here is the code for ClientFunctionResponse.

```java
    class ClientFunctionResponse implements FunctionResponse {
        @Override
        public void onCompleted(String id, Object message, Object returnValue) {
            System.out.println("Got onCompleted for id '" + id + "' with message '" + message + 
            "' and returnValue '" + returnValue + "'");
        }

        @Override
        public void onError(String id, Object message, Object exception) {
            System.out.println("Got onError for id '" + id + "' with message '" + message + 
            "' and exception '" + exception + "'");
        }
    }
```

This request/response is now fully journaled so any audit or support staff can investigate this.