# JetFuelExecute
Remote Procedure Call library over a super fast journaled messaging bus

JetFuel allows a developer to publish a function on a bus and any other client connected to the bus to call. The bus itself is journaled so you can easily look at fields of the request and response. 

Publishing a function on the bus is easy 
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

When you publish a function on the bus you also need to tell it how to process the function call. This is done easily by extending AbstractFunctionExecutor and passing it as a parameter to the publishFunction method

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
    
