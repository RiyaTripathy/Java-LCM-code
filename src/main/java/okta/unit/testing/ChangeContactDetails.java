package okta.unit.testing;

import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.factor.Factor;
import com.okta.sdk.resource.user.factor.FactorList;
import com.okta.sdk.resource.user.factor.VerifyFactorResponse;

public class ChangeContactDetails {

	private static final Logger log = LogManager.getLogger(ChangeContactDetails.class);
	/**************************************************************************************************
	 * Executes the sequence of steps to update the <h1>contact detail</h1> of the user in Okta
	 * @param client {@code com.okta.sdk.client.Client} for authentication
	 * @param userName {@code String} userName of the user whose contact details needs to be updated. 
	 * @param profiles {@code Map} contact details in map which is updated
	 *************************************************************************************************/
	public void test(Client client,String userName,Map profiles) {
		String method = "ChangeContactDetails.test: ";
		Scanner in = new Scanner(System.in);  
		try {
			
			//Get the user object
			User user = client.getUser(userName);
			log.debug(method+"The user to update contact details is "+user);
			
			//Get the registered SMS factor of the user to send the OTP
			FactorList factorList = user.listFactors();
			Factor factor = null;
			for (Factor smsfactor : factorList) {
				if (("sms").equalsIgnoreCase(smsfactor.getFactorType().toString())) {
					factor = smsfactor; // sms factor is retrieved from the list of enrolled factors.
				}
			}
			log.debug(method+"The sms factor of the user used to send OTP is "+factor);
			
			// Send SMS to the user with OTP to the registered mobile number
			VerifyFactorResponse smsResult = OktaFunctions.sendSmsFactor(client, user, factor);
			log.info(method+"SMS sent successfully to the user " + smsResult);

			//Ask the user to enter the OTP in console
			System.out.println("Enter the OTP: "); //This sysout is needed
			String OTP = in.nextLine();
			log.info(method+"The entered value is-"+OTP);
			
			// Verify OTP entered by the user
			VerifyFactorResponse smsVerifyResult = OktaFunctions.verifySmsFactor(client, user, factor,OTP);
			log.info(method+ "The sms verification result is:  "+smsVerifyResult);
			
			//update contact details to the user account
			User updatedUser = OktaFunctions.updateUser(user, profiles);
			log.info(method+"The user contact details are successfully updated. " + updatedUser);
				
		} catch (Exception exception) {
			log.error(method+"Error while updating the contact details of the user :" + exception.getMessage());
		}
		finally {
			in.close();
		}
	}
}
