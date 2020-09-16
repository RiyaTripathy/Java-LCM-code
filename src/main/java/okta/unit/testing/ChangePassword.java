package okta.unit.testing;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.UserCredentials;
import com.okta.sdk.resource.user.factor.Factor;
import com.okta.sdk.resource.user.factor.FactorList;
import com.okta.sdk.resource.user.factor.VerifyFactorResponse;

public class ChangePassword {

	private static final Logger log = LogManager.getLogger(ChangePassword.class);
	
	/**************************************************************************************************
	 * Executes the sequence of steps to <h1>change user password</h1>in Okta.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param userName {@code String} username of the user whose password needs to be updated. 
	 * @param oldPassword {@code String} oldPassword of the user which needs to be updated with new password.
	 * @param newPassword {@code String} newPassword of the user which needs to be updated to the user account.
	 *************************************************************************************************/

	public void test(Client client,String userName,String oldPassword, String newPassword){
		String method = "ChangePassword.test: ";
		Scanner in = new Scanner(System.in);  // Reading from System.in
		try {
			//Get the user object
	
			User user = client.getUser(userName);
			log.debug(method+"The user to update password is: "+user);
			
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
			log.info(method+ "SMS sent successfully to the user " + smsResult);

			//Ask the user to enter the OTP in console
			System.out.println("Enter the OTP: ");  //This sysout is needed
			String OTP = in.nextLine();
			log.info("The entered value is-"+OTP);
			
			// Verify OTP entered by the user
			VerifyFactorResponse smsVerifyResult = OktaFunctions.verifySmsFactor(client, user, factor,OTP);
			log.info(method+ "The sms verification result is:  "+smsVerifyResult);
			
			//Change the user's existing password
			
			UserCredentials changeOutput = OktaFunctions.changePassword(client, user, oldPassword, newPassword);
			log.info(method+ "Change password is success for the user" + changeOutput.toString());
			
		} catch (Exception exception) {
			log.error(method+ "Error while changing the user password :" + exception.getMessage());
		}
		finally {
			in.close();
		}
	}
}
