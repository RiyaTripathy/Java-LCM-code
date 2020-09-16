package okta.unit.testing;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.UserActivationToken;
import com.okta.sdk.resource.user.factor.Factor;

public class MemberJoinOnline {
	// Variables to store client and created user
	private static Client client = null;

	// Variables to get the sequence no. from properties file to fetch
	int login;
	String password = "";

	private static final Logger log = LogManager.getLogger(MemberJoinOnline.class);
		/**************************************************************************************************
	 * Executes the sequence of steps for the user to<h1>join member portal</h1> online.
	 * @param client {@code com.okta.sdk.client.Client} for authentication
	 * @param prospectUserProfiles {@code Map} user details in map which needs to be updated while prospect user creation.
	 * @param memberIdentityProfile {@code Map} member identity details in map needs to be updated while member user update.
	 *************************************************************************************************/
	public void test(Client client, Map prospectUserProfiles,Map memberIdentityProfile) {
		String method = "MemberJoinOnline.test: ";
		try {
						
			//Create prospect user in Okta 
			User user = OktaFunctions.createUser(client, prospectUserProfiles); 
			log.info(method+ "User is created in Okta: " + user); // user is created in Okta in 'Staged' status.

			//Enroll for SMS factor to the registered mobile number
			Factor factor = null;
			factor = OktaFunctions.enrollSmsFactor(client, user, user.getProfile().getMobilePhone());
			log.info(method+ "User is enrolled for SMS factor. " + factor);		
            
			//update the prospect user to member user 	
			User updatedUser = OktaFunctions.updateUser(user, memberIdentityProfile);
			log.info(method+ "Member account details are updated for the user: " + updatedUser);
		
			//Activate the user by sending activation email to the user.
			UserActivationToken activateResult = OktaFunctions.activateUserSendEmail(client, user);
			log.info(method+ "User is activated in Okta. " + activateResult); // user status is changed to 'Active'.
		} 
		
		catch (Exception exception) {
			log.error(method+ "Error while user joins the member portal online: " + exception.getMessage());
		}
	}
}
