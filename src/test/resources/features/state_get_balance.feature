Feature: state_get_balance
  Scenario: state_get_balance
    Given that the state_get_balance RPC method is invoked against nclt user-1 purse
    Then a valid state_get_balance_result is returned
    And the state_get_balance_result contains the purse amount
    And the state_get_balance_result contains api version "1.0.0"
    And the state_get_balance_result contains a valid merkle proof
