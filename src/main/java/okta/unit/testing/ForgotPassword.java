package okta.unit.testing;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.ResourceException;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.UserActivationToken;


public class ForgotPassword {

	private static final Logger log = LogManager.getLogger(ForgotPassword.class);
	/**************************************************************************************************
	 * Executes the sequence of steps for <h1>forgot password</h1>in Okta.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param userName {@code String} username of the user whose password needs to be updated. 
	 * @param newPassword {@code String} newPassword of the user which needs to be updated for user acount.
	 *************************************************************************************************/

	public void test(Client client,String userName, String newPassword) {
		String method = "ForgotPassword.test: "; 
		Scanner in = new Scanner(System.in);
		try {
			
			//Send SMS to the user with OTP to the registered mobile number
			String stateToken;
			stateToken = OktaFunctions.forgotPassword(client, userName);	
			log.info(method+ "OTP is sent to the user and stateToken is-" + stateToken);
			
			//Ask the user to enter the OTP in console
			System.out.println("Enter the OTP: "); //This sysout is needed
			String OTP = in.nextLine();
			log.info(method+"The entered value is-"+OTP);
			
			//Verify OTP entered by the user
			stateToken = OktaFunctions.forgotPasswordVerifySms(client, stateToken,OTP);
			log.info(method+ "The OTP sent to the user is verified and stateToken is-" + stateToken);
			
			//Reset user's password
			stateToken = OktaFunctions.forgotPasswordRestPassword(client, stateToken,newPassword);
			log.info(method+ "user's password reset is successful.-" + stateToken);
			
		} 
		catch (ResourceException e) {
			log.error(method+ "Error while user password reset: " + e.getError());
			try {
				if(e.getCode().equalsIgnoreCase("E0000034") && e.getCauses().get(0).toString().contains("errorSummary: Forgot password is not allowed in the user's current status")) {
					//Get the user object
					User user = client.getUser(userName);
					log.debug(method+"The user to be activated is: "+user);
			
					//if the status user is not active in Okta, re-activate the user account
					UserActivationToken activateResult = OktaFunctions.activateUserSendEmail(client, user);
					log.info(method+"User is activated in Okta. " + activateResult);
				}
			}
			catch(Exception ex) {
				log.error(method+"Error while reactivating the user: " + ex);
			}
		}
		catch (Exception exception) {
			log.error(method+ "Error while updating the user's password: " + exception);
			
		}
		finally {
			in.close();
		}

	}
}
