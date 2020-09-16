package okta.unit.testing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.ExtensibleResource;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.factor.Factor;
import com.okta.sdk.resource.user.factor.FactorList;

public class LoginFlow {

	private static final Logger log = LogManager.getLogger(LoginFlow.class);
	/**************************************************************************************************
	 * Executes the sequence of steps for <h1>user login</h1>to the member portal.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param userName {@code String} username of the user for authentication.
	 * @param password {@code String} password of the user for authentication.
	 *************************************************************************************************/
	public void test(Client client,String userName, String password) {
		String method = "LoginFlow.test: ";	
		try {
			  
			//Get the user object			
			User user = client.getUser(userName);
			log.debug(method+"The user to login is: "+user);
			
			//Get the registered SMS factor of the user to send the OTP
			FactorList factorList = user.listFactors();
			Factor factor = null;

			for (Factor smsfactor : factorList) {
				if (("sms").equalsIgnoreCase(smsfactor.getFactorType().toString())) {
					factor = smsfactor; // sms factor is retrieved from the list of factors.
				}
			}
			log.debug(method+"The sms factor of the user used to send OTP is "+factor);

			//Authenticate the user credentials
			ExtensibleResource result = OktaFunctions.authenticateUser(client, userName, password);
			log.debug(method+ "User authentication is successful. " + result);

			//Send SMS to the user with OTP to the registered mobile number
			if(result.getString("status").equalsIgnoreCase("MFA_REQUIRED")) {
				String auth = result.getString("stateToken");
				log.info(method+ "State Token in the authentication result is :" + auth);
	
				String smsChallengeResult = OktaFunctions.sendSmsChallenge(client, auth, factor);
				log.info(method+ "SMS is sent successfully to the user " + smsChallengeResult);
	
				//Verify OTP entered by the user
				String verifyMfaResult = OktaFunctions.verifyMfa(client, user, smsChallengeResult, factor);
				log.info(method+ "SMS verification result is: " + verifyMfaResult);
			}
			
		} catch (Exception exception) {
			log.error(method+ "Error while user attempts to login to member portal: " + exception.getMessage());
		}

	}
}
