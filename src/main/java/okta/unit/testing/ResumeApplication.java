package okta.unit.testing;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.factor.Factor;
import com.okta.sdk.resource.user.factor.FactorList;
import com.okta.sdk.resource.user.factor.VerifyFactorResponse;

public class ResumeApplication {

	private static final Logger log = LogManager.getLogger(ResumeApplication.class);
	/**************************************************************************************************
	 * Executes the sequence of steps to<h1>resume the application</h1> in the last saved state.
	 * @param client {@code com.okta.sdk.client.Client} for authentication
	 * @param userName {@code String} username of the user whose application form has to be retrieved from the last saved state.
	 *************************************************************************************************/
	public void test(Client client,String userName) throws Exception {
		String method = "ResumeApplication.test: ";
		Scanner in = new Scanner(System.in);
		
		//Get the user object
		try {					
			User user = client.getUser(userName);
			log.debug(method+"The user whose application form is to be retrieved is: "+user);
			
			//Get the registered SMS factor of the user to send the OTP
			FactorList factorList = user.listFactors();
			Factor factor = null;
			for (Factor smsfactor : factorList) {
				if (("sms").equalsIgnoreCase(smsfactor.getFactorType().toString())) {
					factor = smsfactor; // sms factor is retrieved from the list of factors.
				}
			}
			log.debug(method+"The sms factor of the user used to send OTP is "+factor);
			
			//Send SMS to the user with OTP to the registerd mobile number.
			VerifyFactorResponse smsResult = OktaFunctions.sendSmsFactor(client, user, factor);
			log.info(method+ "SMS sent to user successfully" + smsResult);
			
			//Ask the user to enter the OTP in console
			System.out.println("Enter the OTP: "); //This sysout is needed
			String OTP = in.nextLine();
			log.info(method+"The entered value is-"+OTP);
			
			//Verify OTP entered by the user
			VerifyFactorResponse smsVerifyResult = OktaFunctions.verifySmsFactor(client, user, factor,OTP);
			log.info(method+ "The sms verification result is: "+smsVerifyResult);

		} catch (Exception exception) {
			log.error(method+ "Error while resuming the user's MJOL form in the last saved state. " + exception.getMessage());	
		}	
		finally{
			in.close();
		}

	}
}
