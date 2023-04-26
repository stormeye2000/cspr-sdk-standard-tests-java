## CSPR SDK Standard Tests: Java

This repo holds a set of tests to be run against the Casper Java SDK.

Points to note are:

- The tests are run via a GitHub action, standard-tests.yml
- The action is well documented with the steps clearly labelled
- A dockerised image of NCTL with it's generated assets is used to run the tests against
- Tests will run automatically on a push to main within the SDK repo
- Tests can be run manually within this repos action tab
- The tests are built using Cucumber features





