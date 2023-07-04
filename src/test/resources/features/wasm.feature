Feature: wasm operations

  Scenario: Read wasm
    Given that a smart contract "erc20.wasm" is located in the "contracts" folder
    When the wasm is loaded as from the file system
    And the wasm has been successfully deployed
    Then the account named keys contain the "erc20" name

