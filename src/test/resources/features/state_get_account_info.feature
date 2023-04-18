Feature: state_get_account_info
  Scenario: state_get_account_info
    Given that the state_get_account_info RCP method is invoked
    Then a valid state_get_account_info_result is returned
    And the state_get_account_info_result contain a valid account hash
    And the state_get_account_info_result contain a valid main purse uref
    And the state_get_account_info_result contain a valid merkelProof