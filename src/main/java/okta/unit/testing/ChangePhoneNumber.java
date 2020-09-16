package okta.unit.testing;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.factor.Factor;
import com.okta.sdk.resource.user.factor.FactorList;

public class ChangePhoneNumber {


	private static final Logger log = LogManager.getLogger(ChangePhoneNumber.class);
	/**************************************************************************************************
	 * Executes the sequence of steps to <h1>change phone number</h1> of the user during the Member Join Online(MJOL) process.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param userName {@code String} username of the user whose phone number needs to be updated. 
	 * @param newPhoneNumber {@code String} newPhoneNumber of the user which needs to be updated.
	 * @param updateIdentityProfile {key,value}->e.g. {mobilePhone,numberValue} user phone number needs to be updated while member user update.
	 *************************************************************************************************/ 
	public void test(Client client, String userName,String newPhoneNumber,Map updateIdentityProfile) {
		String method = "ChangePhoneNumber.test";
		try {
			
			
			//Get the user object
			User user = client.getUser(userName);
			log.debug(method+"The user to update phone number is: "+user);
			
			//Get the registered SMS factor of the user to send the OTP
			FactorList factorList = user.listFactors();
			Factor factor = null;
			for (Factor smsfactor : factorList) {
				if (("sms").equalsIgnoreCase(smsfactor.getFactorType().toString())) {
				factor = smsfactor; // sms factor is retrieved from the list of enrolled factors.
				}
			}
			log.debug(method+"The sms factor of the user used to send OTP is "+factor);

			 //delete the user's existing sms factor
			OktaFunctions.deleteExistingFactor(client, user, factor);
			log.info(method+ "Existing sms factor of the user is deleted");
			
			//Update user phone number
			User updatedUser = OktaFunctions.updateUser(user, updateIdentityProfile);
			log.info(method+ "Phone number is updated for the user: " + updatedUser);
			
			 //enroll for sms factor for new phone number
			OktaFunctions.enrollSmsFactorForNewPhoneNumber(client, user, newPhoneNumber );
			log.info(method+ "sms factor is enrolled for new phone number.");
			
		} catch (Exception exception) {
			log.error(method+ "Error while user changing the phone number during Member Join Online(MJOL) process: " + exception.getMessage());
		}
	}
}
