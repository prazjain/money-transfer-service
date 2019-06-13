#Author: your.email@your.domain.com
#Keywords Summary :
#Feature: List of scenarios.
#Scenario: Business rule through list of steps with arguments.
#Given: Some precondition step
#When: Some key actions
#Then: To observe outcomes or validation
#And,But: To enumerate more Given,When,Then steps
#Scenario Outline: List of steps for data-driven as an Examples and <placeholder>
#Examples: Container for s table
#Background: List of steps run before each of the scenarios
#""" (Doc Strings)
#| (Data Tables)
#@ (Tags/Labels):To group Scenarios
#<> (placeholder)
#""
## (Comments)
#Sample Feature Definition Template
@tag
Feature: This feature tests the system end to end, for the successful scenario
  
  @tag1
  Scenario: User creation check
    Given User one account "Prashant" is created with starting balance of "1000"
    When I get statement for user one
    Then I see transaction count for user one is "1"
    And I see last transaction amount for user one is "1000"
    And I see last transaction type code for user one is "IN"
    And I see last transaction balance for user one is "1000"
    And I see last transaction counterparty for user one is "INITIAL DEPOSIT"
    And I see statement balance  for user one is "1000"
    And I see account balance  for user one is "1000"


  Scenario: User money transfer from User one to user two
    Given User one account "Prashant" is created with starting balance of "1000"
    And User two account "Nishant" is created with starting balance of "1000"
    When User one transfers "600" to user two account
    And I get statement for user one
    And I get statement for user two
    Then I see transaction count for user one is "2"
    And I see last transaction amount for user one is "600"
    And I see last transaction type code for user one is "OUT"
    And I see last transaction balance for user one is "400"
    And I see statement balance  for user one is "400"
    And I see account balance  for user one is "400"
    And I see transaction count for user two is "2"
    And I see last transaction amount for user two is "600"
    And I see last transaction type code for user two is "IN"
    And I see last transaction balance for user two is "1600"
    And I see statement balance  for user two is "1600"
    And I see account balance  for user two is "1600"

  Scenario: User money transfer from User two to user one
    Given User one account "Prashant" is created with starting balance of "1000"
    And User two account "Nishant" is created with starting balance of "1000"
    When User two transfers "800" to user one account
    And I get statement for user one
    And I get statement for user two
    Then I see transaction count for user one is "2"
    And I see last transaction amount for user one is "800"
    And I see last transaction type code for user one is "IN"
    And I see last transaction balance for user one is "1800"
    And I see statement balance  for user one is "1800"
    And I see account balance  for user one is "1800"
    And I see transaction count for user two is "2"
    And I see last transaction amount for user two is "800"
    And I see last transaction type code for user two is "OUT"
    And I see last transaction balance for user two is "200"
    And I see statement balance  for user two is "200"
    And I see account balance  for user two is "200"
             
  Scenario: Multiple money transfer between User one and User two
    Given User one account "Prashant" is created with starting balance of "1000"
    And User two account "Nishant" is created with starting balance of "1000"
    When User two transfers "800" to user one account
    And User one transfers "300" to user two account
    And I get statement for user one
    And I get statement for user two
    Then I see transaction count for user one is "3"
    And I see last transaction amount for user one is "300"
    And I see last transaction type code for user one is "OUT"
    And I see last transaction balance for user one is "1500"
    And I see statement balance  for user one is "1500"
    And I see account balance  for user one is "1500"
    And I see transaction count for user two is "3"
    And I see last transaction amount for user two is "300"
    And I see last transaction type code for user two is "IN"
    And I see last transaction balance for user two is "500"
    And I see statement balance  for user two is "500"
    And I see account balance  for user two is "500"
             