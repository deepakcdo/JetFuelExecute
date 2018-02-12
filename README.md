# JetFuelExecute
A Flexible and Dynamic RPC Framework over a Highly Performant Journaled Message Bus.

JetFuelExecute allows a process to publish a function on AMPS which can be called by any other process connected to the AMPS. This forms the basis of a excellent request / response design pattern. This is bread and butter task for most applications. When one process has to tell another process to do something and report back.

JetFuelExecute takes care of all message and type conversion. JetFuelExecute uses AMPS as the message bus so you can easily look at fields of the original request and its response. This should simplify the life of developers and support staff who constantly have to debug production issues.

# Why use JetFuelExecute?
Apart from the awesome flexibility of just publishing and calling remote function with full audit JetFuelExecute also provides these extra features :-
* Simple and easy to use API
* No code dependency between function publisher and caller
* Clear error code from function calls where ever it was generated
* Developers create live documentation when they publish a function.
* Simple migration to new version of published functions. Just publish a new function so both are available then migrate clients to new function.
* FunctionExecutor that process the function call is cleanly encapsulated so can be tested independently.
* View available functions along with their documentation and test them easily
* Extremely flexible as a component can be publisher of function and at the same time call other functions.
* Automatic timeout response generated if the server publishing a function goes down,  so your client is not waiting for ever.
* Removal of functions if the publisher that published it disconnects. So client always knows what functions are really available before making the call
* High performance and High availability provided by AMPS along with various authentication options.

# JetFuelExecute demo using JetFuelExplorer 

1) View available functions that are published by other servers
![screenshot](http://headfront.co.uk/JetFuelExecuteAvailableFunctions.png)

2) Test a function - this is so useful
![screenshot](http://headfront.co.uk/JetFuelExecuteTestFunction.png)

3) View the full Request/Response message - your support team will love you
![screenshot](http://headfront.co.uk/JetFuelExecuteAudit.png)

Note :- JetFuelExplorer (http://headfront.co.uk/JetFuelExplorer.html) is an independent tool. JetFuelExecute can be used without JetFuelExplorer

# JetFuelExecute code example.

Using JetFuelExecute do the following :-

    1) Publish a function
    2) Call the function

Lets look at step 1 first - Publishing a function.
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

Now lets look at step 2 - Calling a function. This is even easier than publishing the function. You simply call the executeFunction with the function name, parameters and FunctionResponse listener

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

This request/response is now fully journaled so any audit or support staff can investigate this any time.

# Features coming very soon.
* The Ability to handle client disconnects. e.g. if a client calls function like QuoteOn and then disconnects after a few minutes, the function publisher that processed the QuoteOn request will realise the client disconnected and execute a clean-up action. Here a clean- up action could pull all the quotes from the market for the disconnected user.
* Ability to make a function call which is executed by several publishers. E.g. if you have BankOff function published by 5 different function publishers, you can call this once and each of the 5 publisher will execute this. This is very powerful feature and needs to be understood and used very carefully.
* Subscriptions requests. This will allow you to create a function which can stream you continuous updates till you unsubscribe. Very useful if you want to get a stream of custom price calculations.
* Api’s in different languages e.g. Javascript and C. This means JetFuelExecute functions can be published and called from multiple languages.
