Feature: wasm operations

  Scenario: Read wasm
    Given that a smart contract "erc20.wasm" is located in the "contracts" folder
    Then when the wasm is loaded as from the file system