Feature: Blocks Integration Tests
  Scenario: Test Get Block

    Given that the latest block is requested
    Then a valid block is returned
    And with a valid hash
    And with a valid body
    And with valid headers
    And with valid proofs

    Given that a block is returned by hash
    Then a valid block is returned
    And with a valid hash
    And with a valid body
    And with valid headers
    And with valid proofs

    Given that a block is returned by height 2
    Then a valid block is returned
    And with a valid hash
    And with a valid body
    And with valid headers
    And with valid proofs

    Given that a block is returned by height 3
    Then a valid block is returned
    And with a valid hash
    And with a valid body
    And with valid headers
    And with valid proofs

    Given that a block is returned by height 4
    Then a valid block is returned
    And with a valid hash
    And with a valid body
    And with valid headers
    And with valid proofs

    Given that a transfer block is requested
    Then a valid block is returned
    And with a valid hash
    And with a valid body
    And with valid headers
    And with valid proofs

#    Given I have the block at era switch
#    Then block returns required data


