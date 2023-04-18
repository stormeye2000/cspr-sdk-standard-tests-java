Feature: chain_get_state_root_hash
  Scenario: chain_get_state_root_hash
    Given that the chain_get_state_root_hash RCP method is invoked
    Then a valid chain_get_state_root_hash_result is returned