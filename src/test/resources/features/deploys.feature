Feature: Deploys

  Scenario: Put Transfer Deploy
    Given that user-1 initiates a transfer to user-2
    And the transfer amount is 2500000000
    And the transfer gas price is 1
    And the deploy has a ttl of 30m
    When the deploy is put on chain "casper-net-1"
    Then the deploy response contains a valid deploy hash of length 64 and an API version "1.0.0"
    Then wait for a block added event with a timout of 650 seconds
