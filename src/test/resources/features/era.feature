Feature: Era Integration Tests
  Scenario: Evaluate Era Methods

    Given that era information is requested
    Then a valid era number is returned

    Given that era height is requested
    Then a valid era height is returned

