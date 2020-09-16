package okta.unit.testing;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.UserActivationToken;
import com.okta.sdk.resource.user.factor.Factor;
import com.okta.sdk.resource.user.factor.FactorList;

public class OnlineAccountRegistration {

	private static final Logger log = LogManager.getLogger(OnlineAccountRegistration.class);
	/**************************************************************************************************
	 * Executes the sequence of steps when existing member user registers for <h1>online account registration</h1> for online account use.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param profiles {@code Map} user details in map which needs to be updated.
	 *************************************************************************************************/	
	public void test(Client client, Map profiles) throws Exception {
		String method = "OnlineAccountRegistration.test: ";
		try {
			//Get the user object
			User user = null;
			try {
				user = client.getUser(profiles.get("login").toString());
			}
			catch(Exception exception) {
				log.error(method+ "Error in fetching the user"+exception.getMessage());
			}
			
			if(user !=null) {
				log.info(method+ "User "+profiles.get("login")+" already exists in the system."); //user account is already existing in Okta.
			}	
					
			else {
				 //Create user if the user does not exist in Okta.	
				user = OktaFunctions.createUser(client, profiles);
				log.info(method+ "User is created in Okta" + user); // user created is in 'Staged' status.
	
				//Enroll for SMS factor to the registered mobile number.	
				Factor factor = OktaFunctions.enrollSmsFactor(client, user, user.getProfile().getMobilePhone());
				log.info(method+ "User enrollment for SMS factor is successful." + factor);
				
				//Activate the user by sending activation email to the user.
				UserActivationToken activateResult = OktaFunctions.activateUserSendEmail(client, user);
				log.info(method+ "user is successfully activated." + activateResult); // user status is changed to 'Active'.
			}

		} catch (Exception exception) {
			log.error(method+ "Error while registering online account :" + exception.getMessage());
		}

	}
}
