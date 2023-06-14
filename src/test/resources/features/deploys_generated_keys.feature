Feature: deploys_generated_keys

  Scenario: Generate "Ed25519" Account
    Given that a "Ed25519" sender key is generated
    Then fund the account from the faucet user
    Then wait for a block added event with a timeout of 300 seconds
    Given that a "Ed25519" receiver key is generated
    Then transfer to the receiver account
    Then wait for a block added event with a timeout of 300 seconds
    And the returned block contains the "Ed25519" algo

  Scenario: Generate "Secp256k1" Account
    Given that a "Secp256k1" sender key is generated
    Then fund the account from the faucet user
    Then wait for a block added event with a timeout of 300 seconds
    Given that a "Secp256k1" receiver key is generated
    Then transfer to the receiver account
    Then wait for a block added event with a timeout of 300 seconds
    And the returned block contains the "Secp256k1" algo