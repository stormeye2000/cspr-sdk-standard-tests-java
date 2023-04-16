Feature: query_global_state

  Scenario: query_global_state by block hash
    Given that a valid block hash is known
    When the query_global_state RCP method is invoked with the block hash as the query identifier
    Then a valid query_global_state_result is returned
    And the query_global_state_result contains a valid deploy info stored value
    And the query_global_state_result's stored value from is the user-1 account hash
    And the query_global_state_result's stored value contains a gas price of 100000000
    And the query_global_state_result stored value contains the transfer hash
    And the query_global_state_result stored value contains the transfer source uref


  Scenario: query_global_state by state root hash with invalid key
    Given that the state root hash is known
    When the query_global_state RCP method is invoked with the state root hash as the query identifier and an invalid key
    Then an error code of -32002 is returned
    And an error message of "failed to parse key: uref-key from string error: no access rights as suffix" is returned


  Scenario: query_global_state by state with invalid block hash
    Given the query_global_state RCP method is invoked with an invalid block hash as the query identifier
    Then an error code of -32602 is returned
    And an error message of "Invalid params" is returned

