Feature: query_global_state

  Scenario: query_global_state by block hash
    Given that a valid block hash is known
    When the query_global_state RCP method is invoked with the block hash as the query identifier
    Then a valid query_global_state_result is returned
    And the query_global_state_result contains a valid deploy info stored value
    And the query_global_state_result's stored value from is the user-1 account hash
    And the query_global_state_result's stored value contains a gas price of 100000000
    And the query_global_state_result stored value contains the transfer hash


  Scenario: query_global_state by state root hash
    Given that the state root hash is known
    When the query_global_state RCP method is invoked with the state root hash as the query identifier
    Then a valid query_global_state_result is returned
