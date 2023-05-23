## CSPR SDK Standard Tests: Java

This repo holds a set of tests to be run against the Casper Java SDK.

Points to note are:

- The tests are run via a GitHub action, standard-tests.yml
- The action is well documented with the steps clearly labelled
- A dockerised image of NCTL with it's generated assets is used to run the tests against
- Tests will run automatically on a push to main within the SDK repo
- Tests can be run manually within this repos action tab
- The tests are built using Cucumber features

To run locally, check out the repo and copy the SDK's jar files in the /libs folder. Gradle will then import it into the project.

To execute the cucumber tests perform a standard gradle build:
```
./gradlew build
```

The cucumber test reports will be written to the _reports_ folder off the project root.

The following system parameters (-D) are supported to specify the nctl host and ports. If not provided the defaults are used
| Parameter  | Description  | Default   | 
|---|---|---|
| cspr.hostname | The host name | _localhost_  | 
| cspr.port.rcp  | The RCP port number | _11101_ |
| cspr.port.rest | The SSE port number | _18101_ |
| cspr.docker.name | The docker container name | _storm-nctl_ |



