Feature: info_get_validator_changes RCP method against nctl

  Scenario: info_get_validator_changes valid request
    Given that the info_get_validator_changes method is invoked against a node
    Then a valid info_get_validator_changes_result is returned
    And the info_get_validator_changes_result contains a valid API version

