Feature: read_deploy
  Scenario: read_deploy
    Given that the "transfer.json" JSON deploy is loaded
    Then a valid transfer deploy is created
    And the deploy hash is "d7a68bbe656a883d04bba9f26aa340dbe3f8ec99b2adb63b628f2bc920431998"
    And the account is "017f747b67bd3fe63c2a736739dfe40156d622347346e70f68f51c178a75ce5537"
    And the timestamp is "2021-05-04T14:20:35.104Z"
    And the ttl is 30m
    And the gas price is 2
    And the body_hash is "f2e0782bba4a0a9663cafc7d707fd4a74421bc5bfef4e368b7e8f38dfab87db8"
    And the chain name is  "mainnet"
    And dependency 0 is "0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f"
    And dependency 1 is "1010101010101010101010101010101010101010101010101010101010101010"
    And the payment amount is 1000000000
    And the session is a transfer
    And the session amount is 24500000000
    And the session target is "0101010101010101010101010101010101010101010101010101010101010101"
    And the session additional_info is "this is transfer"
    And the deploy has 1 approval
    And the approval signer is "017f747b67bd3fe63c2a736739dfe40156d622347346e70f68f51c178a75ce5537"
    And the approval signature is "0195a68b1a05731b7014e580b4c67a506e0339a7fffeaded9f24eb2e7f78b96bdd900b9be8ca33e4552a9a619dc4fc5e4e3a9f74a4b0537c14a5a8007d62a5dc06"