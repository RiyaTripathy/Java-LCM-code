**Identity Unit Testing**

This project helps to test all the Okta unit test cases of CBus Member Portal design. Test data needs to be updated in the configuration file before executing the program. This would test both positive and negative scenarios based on the user data provided. The input from the user is accepted during the executing of the program through the console.

## Process to set up project in eclipse
This process would create the create the clone of the respostory in the Eclipse and would be execute the program throug eclipse.
### Install git
* Install the eclipse and open the eclipse in new workspace
* Go to Help->Install new software
* Enter the value "egit - http://download.eclipse.org/egit/updates" 
* Select all the Names and click on Finish
### Import the project
* Click File->Import
* Select Git-> Projects from Git
* Select "Clone URI"
* Click on "Clone" on the bit bucket repository and copy the URL.
* Paste the copied URL in eclipse and provide the authentication credentials
* Click on Finish

## Process to execute the program
* Update the test data in the **unitTesting.properties** file
* Configure the logging method in log4j.xml. The loggers will be written to different files based on the logging level
* Execute the program **UnitTest.java**

## Process to update the configuration file 
* Update the unit test cases to be tested in the property file **TestFunctions**. Multiple values are comma-separated and are selected from the given set of value.
* The unit test cases that can be tested are 
    * Member Join Online
    * Change Phone number during MJOL process
    * Resume Application
    * Delete User (Unsuccessful Member User creation from prospect user)
    * Login to Member Portal
    * Change Contact Details
    * Change Password
    * Forgot Password
    * Online Account Registration
* The number of users to be tested for each use cases are updated in **NoOfTestUser** and each test users details are pre-concatenated with the numerical value counting till NoOfTestUsers.
* Each test data are pre-concatenated  with the function name

**Refer the configuration file to update the test users data**

## Design
* The design document for the project is **https://cbussuper-uat.atlassian.net/wiki/spaces/ID/pages/533496258/MJOL+and+Member+Portal+Detailed+Design**
* The unit test cases which can be tested are **https://cbussuper-uat.atlassian.net/browse/AI-119**

## Execution
* The detailed steps on the execution can be tracked through the log statements
* The input which needs to be provided during the execution of the program is entered through the console

## Updates to the design/Code
* Each test functionality is executed through the different class file which has the sequence of steps to execute for that particular unit test case. 
* All the Okta functionality are stored in the single the class file OktaFunctions.java

