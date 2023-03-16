Feature: Deploys

  Scenario: Put Deploy
    Given that user-1 initiates a transfer to user-2
    And the transfer amount is 2500000000
    And the transfer gas price is 1
    And the deploy has a ttl of 30m
    When the deploy is put
    Then the valid deploy hash is returned
    


