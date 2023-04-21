Feature: info_get_status

  Scenario: info_get_status
    Given that the info_get_status is invoked against nctl
    Then an info_get_status_result is returned
    And the info_get_status_result api_version is "1.0.0"
    And the info_get_status_result chainspec_name is "casper-net-1"
    And the info_get_status_result has a valid last_added_block_info
    And the info_get_status_result has a valid our_public_signing_key
    And the info_get_status_result has a valid starting_state_root_hash
    And the info_get_status_result has a valid build_version
    And the info_get_status_result has a valid round_length
    And the info_get_status_result has a valid uptime
    And the info_get_status_result has a valid peers
