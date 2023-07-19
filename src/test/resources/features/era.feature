Feature: Era Integration Tests

  Scenario: Evaluate Era Methods

    Given that the era summary is requested via the sdk
    Then request the era summary via the node
    And the block hash of the returned era summary is equal to the block hash of the test node era summary
    And the era of the returned era summary is equal to the era of the returned test node era summary
    And the merkle proof of the returned era summary is equal to the merkle proof of the returned test node era summary
    And the state root hash of the returned era summary is equal to the state root hash of the returned test node era summary
    And the delegators data of the returned era summary is equal to the delegators data of the returned test node era summary
    And the validators data of the returned era summary is equal to the validators data of the returned test node era summary