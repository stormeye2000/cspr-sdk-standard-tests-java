Feature: Blocks Integration Tests
  Scenario: Evaluate Block Methods

    Given that the latest block is returned
    Then request the latest block via nctl
    Then the body is equal
    And the hash is equal
    And the header is equal
    And the proofs are equal

    Given that a block is returned by hash
    Then request a block by hash via nctl
    Then the body is equal
    And the hash is equal
    And the header is equal
    And the proofs are equal

    Given that a block is returned by height 2
    Then request the returned block from nctl via its hash
    Then the body is equal
    And the hash is equal
    And the header is equal
    And the proofs are equal

    Given that a block is returned by height 3
    Then request the returned block from nctl via its hash
    Then the body is equal
    And the hash is equal
    And the header is equal
    And the proofs are equal

    Given that a block is returned by height 4
    Then request the returned block from nctl via its hash
    Then the body is equal
    And the hash is equal
    And the header is equal
    And the proofs are equal

    Given that an invalid block hash is requested
    Then a valid error message is returned

    Given that an invalid block height is requested
    Then a valid error message is returned

    Given that a NCTL era switch block is requested
    Then wait for the NCTL era switch block
    Then request the corresponding era switch block
    And the switch block hashes are equal
    And the switch block eras are equal
    And the switch block merkle proofs are equal
    And the switch block state root hashes are equal
    And the delegator data is equal
    And the validator data is equal





