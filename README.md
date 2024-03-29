## CSPR SDK Standard Tests: Java

This repo holds a set of tests to be run against the Casper Java SDK.

Points to note are:

- The tests are run via a GitHub action, standard-tests.yml
- The action is well documented with the steps clearly labelled
- A dockerised image of NCTL with it's generated assets is used to run the tests against
- Tests will run automatically on a push to main within the SDK repo
- Tests can be run manually within this repos action tab
- The tests are built using Cucumber features

### Obtaining Dependencies *(cspr-standard-test-resources submodule)*
The cucumber test features and their required test resources are maintained in their own repository. To obtain these resources execute the bootstrap script:
```
./script/bootstrap
```
### Starting Docker and Obtaining Assets From Docker
If running the test locally an nctl instance needs to be executing the following script will start a docker instance:
```
./script/docker-run.sh
```
The test require access to the assets generated by the nctl instance. Once the nctl has created all assets and stared 
its nodes, copy the required assets from the docker image use the following script:
```
./script/docker-copy-assets.sh
```

### Executing the Cucumber Tests
To execute the cucumber tests perform a standard gradle build execute the test script:
```
./script/test
```

The cucumber test reports will be written to the _reports_ folder off the project root.

The following system parameters (-D) are supported to specify the nctl host and ports. If not provided the defaults are used:

| Parameter  | Description  | Default     | 
|---|---|-------------|
| cspr.hostname | The host name | _localhost_ | 
| cspr.port.rcp  | The RCP port number | _11101_     |
| cspr.port.rest | The SSE port number | _18101_     |
| cspr.docker.name | The docker container name | _cspr-nctl_ |

e.g:
```
./script/test -Dcspr.hostname=somehost -Dcspr.port.rcp=1234 
```


