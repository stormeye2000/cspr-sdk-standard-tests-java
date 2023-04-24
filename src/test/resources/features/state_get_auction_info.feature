Feature: state_get_auction_info RCP method

  Scenario: state_get_auction_info by hash block identifier
    Given that the state_get_auction_info RPC method is invoked by hash block identifier
    Then a valid state_get_auction_info_result is returned
    And the state_get_auction_info_result has and api version of "1.0.0"
    And the state_get_auction_info_result action_state has a valid state root hash
    And the state_get_auction_info_result action_state has a valid height
    And the state_get_auction_info_result action_state has valid bids
    And the state_get_auction_info_result action_state has valid era validators

  Scenario: state_get_auction_info by height block identifier
    Given that the state_get_auction_info RPC method is invoked by height block identifier
    Then a valid state_get_auction_info_result is returned
    And the state_get_auction_info_result has and api version of "1.0.0"
    And the state_get_auction_info_result action_state has a valid state root hash
    And the state_get_auction_info_result action_state has a valid height
    And the state_get_auction_info_result action_state has valid bids
    And the state_get_auction_info_result action_state has valid era validators

  Scenario: state_get_auction_info by invalid block hash identifier
    Given that the state_get_auction_info RPC method is invoked by an invalid block hash identifier
    Then an error code of -32001 is returned
    And an error message of "get-auction-info failed to get specified block" is returned
