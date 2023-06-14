Feature: Deploys

  Scenario: Put Transfer Deploy
    Given that user-1 initiates a transfer to user-2
    And the transfer amount is 2500000000
    And the transfer gas price is 1
    And the deploy is given a ttl of 30m
    When the deploy is put on chain "casper-net-1"
    Then the deploy response contains a valid deploy hash of length 64 and an API version "1.0.0"
    Then wait for a block added event with a timeout of 300 seconds

  Scenario: Get Deploy
    Given that a Transfer has been successfully deployed
    When a deploy is requested via the info_get_deploy RCP method
    Then the deploy data has an API version of "1.0.0"
    And the deploy execution result has "lastBlockAdded" block hash
    And the deploy execution has a cost of 100000000 motes
    And the deploy has a payment amount of 100000000
    And the deploy has a valid hash
    And the deploy has a valid timestamp
    And the deploy has a valid body hash
    And the deploy has a session type of "Transfer"
    And the deploy is approved by user-1
    And the deploy has a gas price of 1
    And the deploy has a ttl of 30m
    And the deploy session has a "amount" argument value of type "U512"
    And the deploy session has a "amount" argument with a numeric value of 2500000000
    And the deploy session has a "target" argument value of type "PublicKey"
    And the deploy session has a "target" argument with the public key of user-2
    And the deploy session has a "id" argument value of type "Option"
