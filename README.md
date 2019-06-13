# Money Transfer Service

---

Programmed by <praz.jain@gmail.com>  

This guide show to interact with the REST api using tools like Postman. You will find the steps below to setup test data using REST API request and the corresponding response.

## Objective

To write a money transfer service that will:

  * Transfer money between different accounts.
  * Generate statement for account for given date range. It should show correct account balance, and statement balance as of applicable date range.

## Technology
Technology / Library / Framework used:

  * Java 8
  * Spring Boot
  * REST API
  * Maven
  * Junit 5 (TDD)
  * Mockito
  * Cucumber, Gherkin (BDD)
  * Spring Boot JPA
  * JQL
  * H2 (In memory database)
  * Postman (testing)
  * Lombok
  * ModelMapper


## Assumptions

  
  * **Security (Authentication and Authorization)** : This is a big item in itself, ideally in a real life application like this would need https enabled and again 
admin login's to create user accounts, and then user logins with username/password to allow them to transfer money and request statement from other their accounts.
Using Authentication in plain text is not good, best to store in database and storing hashed and salted password. Also we need to have certificate associated with site and trust it on client, so we can work on ssl.
  
  * A **single account number** is used to identify bank account. 
  
  * **Concept of currency** with bank account. We are not storing whether this bank account is for UK Pound/USD/EUR. So this avoid unneccessary 
complication when transferring money when you are transferring from accounts that hold money in different currency.
  
  * **Not storing additional user details** apart from a name associated with bank account number. It was not needed to store additional user details like User's firstname, lastname, date of birth, email, phone number, home address etc.
  
## Notes

  * Unit tests

  There are 35 unit test cases for the application, that test every minute detail. These unit tests isolate the object under test by mocking dependencies using Mockito.

  * End to End tests

	There are 4 End to End test cases written in Gherkin BDD style, and allows further tests to be written in natural language (a lot more BDD tests can be written). These end to end tests do not use mocking and load the actual application in real tomcat container and run tests on those.

  * Race conditions

	This is handled using If-Match http construct, Optimistic Locking mechanism and versioning of resources. As using synchronized is not an option here :), it will bring down the performance of the system. There is unit testing around these as well.

### Run the application

Open the application in eclipse, and right click on the project and Run As -> Spring Boot application
And then follow the step by step guide (below) to interact with the system.

### To run tests

Open the application in eclipse, and right click on src/test/java directory in project explorer, and right click -> Run As -> JUnit test
  
## Cucumber Tests (BDD)

For those not familiar with it, this is how Cucumber tests written in Gherkin look like. As you can see, non-techies can also write tests cases. BDD was meant for business users to write tests in natural language to confirm the system to their requirement. This does not disappoint, instead it makes me want to write more tests!

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
      
      
## Use Cases

[Create Account - Prashant](#create-account-prashant)

[Create Account - Nishant](#create-account-nishant)

[Get Account - Prashant](#get-account-prashant)

[Get Account - Nishant](#get-citizen-nishant)

[Get Statement - Prashant](#get-statement-prashant)

[Post Transfer from Prashant to Nishant's account](#post-transfer-from-prashant-to-nishant)

[Get Account - Nishant After Transfer](#get-account-nishant-after-transfer)

[Get Statement - Prashant After Transfer](#get-statement-prashant-after-transfer)

## Steps

#### <a name="create-account-prashant">Create Account Prashant</a>

*Request*

POST	`http://localhost:8080/accounts`

Body

	{
		"name" : "Prashant",
		"balance" : 1000
	}

*Response*
	
	{
	    "id": 2,
	    "name": "Prashant",
	    "openDate": "04-05-2019",
	    "closeDate": "",
	    "balance": "1000.00",
	    "version": 1
	}

#### <a name="create-account-nishant">Create Account Nishant</a>

*Request*

POST	`http://localhost:8080/accounts`

Body

	{
		"name" : "Nishant",
		"balance" : 1000
	}

*Response*
	
	{
	    "id": 4,
	    "name": "Nishant",
	    "openDate": "04-05-2019",
	    "closeDate": "",
	    "balance": "1000.00",
	    "version": 1
	}
	
#### <a name="get-account-prashant">Get Account Prashant</a>



*Request*

GET	`http://localhost:8080/account/2`

*Response*
	
	{
	    "id": 2,
	    "name": "Prashant",
	    "openDate": "04-05-2019",
	    "closeDate": "",
	    "balance": "1000.00",
	    "version": 1
	}


#### <a name="get-account-nishant">Get Account Nishant</a>


*Request*

GET	`http://localhost:8080/account/4`

*Response*
	
	{
	    "id": 4,
	    "name": "Nishant",
	    "openDate": "04-05-2019",
	    "closeDate": "",
	    "balance": "1000.00",
	    "version": 1
	}

#### <a name="get-statement-prashant">Get Statement Prashant</a>

Get statement for Prashant immediately after his account is created.

*Request*

GET	`http://localhost:8080/account/2/statement?fromDate=31012019&toDate=04052019`

*Response*
	
	{
	    "fromDate": "31-01-2019",
	    "toDate": "04-05-2019",
	    "transactions": [
	        {
	            "id": 1,
	            "typeCode": "IN",
	            "transactionDate": "2019-05-04 18:37:55.614",
	            "transactionAmount": "1000.00",
	            "balance": "1000.00",
	            "counterparty": "INITIAL DEPOSIT"
	        }
	    ],
	    "balance": "1000.00"
	}

#### <a name="post-transfer-from-prashant-to-nishant">Post Transfer from Prashant to Nishant's account</a>

This request below will transfer 500 amount from Prashant to Nishant's account.
Please bear in mind, we need to give Prashant's account resource object's version in If-Match request header, so server can check we are requesting transfer on latest version of that resource.

If object version does not match with If-Match header value, then this POST request returns 412 PRECONDITION_FAILURE status.

This also helps in case user clicks on transfer button twice :), only first request will succeed, as for second request the object version will have incremented and it will not match if-match value. (Try this case, by sending this exact request more than once)

*Request*

POST	`http://localhost:8080/account/2/transfers`

Request Header

	If-Match : "1"

Body

	{
		"toAccountId" : 4,
		"amount" : 500
	}
	
*Response*

Response Header

	Status: 200 OK

Body

	Transfer successful

#### <a name="get-account-nishant-after-transfer">Get Account Nishant After Transfer</a>

Lets look at Nishant's account now after the transfer. Version number has gone up too.

*Request*

GET	`http://localhost:8080/account/4`

*Response*
	
	{
	    "id": 4,
	    "name": "Nishant",
	    "openDate": "04-05-2019",
	    "closeDate": "",
	    "balance": "1500.00",
	    "version": 2
	}

#### <a name="get-statement-prashant-after-transfer">Get Statement - Prashant After Transfer</a>

Get statement for Prashant after he has done some transactions.

*Request*

GET	`http://localhost:8080/account/2/statement?fromDate=31012019&toDate=04052019`

*Response*
	
	{
	    "fromDate": "31-01-2019",
	    "toDate": "04-05-2019",
	    "transactions": [
	        {
	            "id": 1,
	            "typeCode": "IN",
	            "transactionDate": "2019-05-04 18:37:55.614",
	            "transactionAmount": "1000.00",
	            "balance": "1000.00",
	            "counterparty": "INITIAL DEPOSIT"
	        },
	        {
	            "id": 5,
	            "typeCode": "OUT",
	            "transactionDate": "2019-05-04 18:46:03.151",
	            "transactionAmount": "500.00",
	            "balance": "500.00",
	            "counterparty": "4"
	        }
	    ],
	    "balance": "500.00"
	}

