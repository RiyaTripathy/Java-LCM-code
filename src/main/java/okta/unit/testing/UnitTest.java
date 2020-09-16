package okta.unit.testing;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.user.User;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitTest {
	private static final Logger log = LogManager.getLogger(UnitTest.class);
	/**************************************************************************************************
	 * Executes the sequence of steps for <h1>test cases</h1> for the unitTest functions in Okta.
	 * @param args {@code String} args is the argument for the main method.
	 * @throws Exception
	 *************************************************************************************************/
	public static void main(String[] args) throws Exception {
		// Date startTime = new Date();
		String method = "UnitTest.test: ";
		
		//Reading  the properties file
		String propertiesFileName = "unitTesting.properties";
		Properties config = new Properties();
		try {
			InputStream fis = new FileInputStream(propertiesFileName);	
			config.load(fis);
		} catch (IOException e) {
			log.error(method+"Error in reading the configurations file-" + e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error(method+"Error in getting values from config.properties file -" + e.getMessage());
			e.printStackTrace();
		}
		    //creates the client object to authenticate the Okta Domain with the authorization token.
			Client client = OktaFunctions.createClientObject(config.getProperty("oktaDomain").trim(),config.getProperty("oktaAuthToken").trim());
			log.debug(method+"The properties are " + config);
			
			//get the list of use cases for unit testing from the properties file.
		
			List<String> unitTestsFunctions=Arrays.asList(config.getProperty("TestFunctions").split(","));
			log.debug(method+"The cases for unit test are:  "+unitTestsFunctions);
			
			int noOfTestUsers =0;
			
			 for(String unitTest: unitTestsFunctions) {
				 log.info(method+ "Testing the case: "+unitTest);
				 
				 //get the number of users to test from the properties file.
				 noOfTestUsers = Integer.parseInt(config.getProperty(unitTest+"-NoOfTestUsers").trim());
				 log.info(method+ "No of users for the test case: "+noOfTestUsers);
				 
				 //get the usecase to test from the list of UnitTest functions
				 
				 if(unitTest.equalsIgnoreCase("MJOL")) {
					 for(int i=1; i <=noOfTestUsers;i++) {
						 Map prospectUserProfiles = getProfiles(i+".prospectUser",config);
						 Map memberIdentityProfile = getProfiles(i+".memberIdentity",config);
						 log.info(method+ "The Prospect User is created with following profile: "+prospectUserProfiles);
						 log.info(method+ "The member User is converted to profile: "+prospectUserProfiles);
						 new MemberJoinOnline().test(client,prospectUserProfiles,memberIdentityProfile);
					 }
				 }
				 else if(unitTest.equalsIgnoreCase("ResumeApplication")) {
					 for(int i=1; i <=noOfTestUsers;i++) {
						 log.info(method+ "The Resume Application is initiatated for the user: "+config.getProperty(i+".ResumeApplication-user"));
						 new ResumeApplication().test(client,config.getProperty(i+".ResumeApplication-user"));
					 }
				 }
				 else if(unitTest.equalsIgnoreCase("DeleteUser")) {
					 for(int i=1; i <=noOfTestUsers;i++) {
						 log.info(method+ "The delete process is initiatated for the user: "+config.getProperty(i+".DeleteUser-user"));
						 new DeleteUser().test(client,config.getProperty(i+".DeleteUser-user"));
					 }
				 }
				 else if(unitTest.equalsIgnoreCase("ChangeContactDetails")) {
					 for(int i=1; i <=noOfTestUsers;i++) {
						 Map profiles = getProfiles(i+".ChangeContactDetails",config);
						 log.info(method+"The update contact details of the user "+config.getProperty(i+".ChangeContactDetails-login")+" is initiated with following details "+profiles);
						 new ChangeContactDetails().test(client,config.getProperty(i+".ChangeContactDetails-login"),profiles);
					 }
				 }
				 else if(unitTest.equalsIgnoreCase("ChangePassword")) {
					 for(int i=1; i <=noOfTestUsers;i++) {
						 log.info(method+"The Change password is initiated for the user "+config.getProperty(i+".ChangePassword-login"));
						 new ChangePassword().test(client,config.getProperty(i+".ChangePassword-login"),config.getProperty(i+".ChangePassword-oldPassword"),config.getProperty(i+".ChangePassword-newPassword"));
					 }
				 }
				 else if(unitTest.equalsIgnoreCase("OnlineAccountRegistration")) {
					 for(int i=1; i <=noOfTestUsers;i++) {
						 Map profiles = getProfiles(i+".OnlineAccountRegistration",config);
						 log.info(method+ "The Online Account Registration is initiated with following profile: "+profiles);
						 new OnlineAccountRegistration().test(client,profiles);
					 }
				 }
				 else if(unitTest.equalsIgnoreCase("LoginFlow")) {
					 for(int i=1; i <=noOfTestUsers;i++) {
						 log.info(method+ "The Login is initiated for: "+config.getProperty(i+".LoginFlow-login"));
						 new LoginFlow().test(client, config.getProperty(i+".LoginFlow-login"), config.getProperty(i+".LoginFlow-password"));
					 }
				 }
				 else if(unitTest.equalsIgnoreCase("ForgotPassword")) {
					 for(int i=1; i <=noOfTestUsers;i++) {
						 log.info(method+ "The Forgot Password is initiated for: "+config.getProperty(i+".ForgotPassword-login"));
						 new ForgotPassword().test(client, config.getProperty(i+".ForgotPassword-login"), config.getProperty(i+".ForgotPassword-newPassword"));
					 }
				 }
				 else if(unitTest.equalsIgnoreCase("ChangePhoneNumber")) {
					 for(int i=1; i <=noOfTestUsers;i++) {
						 Map updateIdentityProfile = getProfiles(i+".updateIdentity",config);
						 log.info(method+ "The Change Phone Number is initiated for prospect user "+config.getProperty(i+".ChangePhoneNumber-login")+" with new phone number "+config.getProperty(i+".ChangePhoneNumber-mobilePhone"));
					 new ChangePhoneNumber().test(client, config.getProperty(i+".ChangePhoneNumber-login"), config.getProperty(i+".ChangePhoneNumber-mobilePhone"), updateIdentityProfile);
					 }
				 }
			 }
	}
	/**************************************************************************************************************
	 * This function is used to get the value of the profiles mentioned array list
	 * deleting the previously enrolled SMS factor.
	 * @param profilesName {@code String} for which profile needs to be fetched from config.
	 * @param config {@code Properties} configuration containing all the profiles.
	 * @return Map profile for the user.
	 * @throws Exception
	 **************************************************************************************************************/
	public static Map getProfiles(String profilesName,Properties config ) {
		
		String method = "getProfiles.test: ";
		List dataTypeArray = Arrays.asList(config.getProperty("dataType-array").split(","));
		
		//gets the list of user profile details from the properties file.
		List<String> profileList = Arrays.asList(config.getProperty(profilesName+"-profiles").split(","));
		 Map profiles = new HashMap();
		 for(String profile: profileList) {
				if(dataTypeArray.contains(profile)) {
					List dataArray = new ArrayList<>();
					dataArray.add(config.getProperty(profilesName+"-"+profile));
					profiles.put(profile, dataArray);
				}
				else
					profiles.put(profile, config.getProperty(profilesName+"-"+profile));
			}
		 log.debug(method+ "Profiles for "+profilesName+": "+profiles);
		 return profiles;
		 
	}
}
