Feature: deploys_generated_keys

  Scenario: Generate Ed25519 Account
    Given that a Ed25519 sender key is generated
    Then fund the account from the faucet user
    Then wait for a block added event with a timout of 300 seconds

  Scenario: Transfer to another Ed25519 account
    Given that a Ed25519 receiver key is generated
    Then transfer to the receiver account
    Then wait for a block added event with a timout of 300 seconds

  Scenario: Generate Secp256k1 Account
    Given that a Secp256k1 sender key is generated
    Then fund the account from the faucet user
    Then wait for a block added event with a timout of 300 seconds

  Scenario: Transfer to another Secp256k1 account
    Given that a Secp256k1 receiver key is generated
    Then transfer to the receiver account
    Then wait for a block added event with a timout of 300 seconds