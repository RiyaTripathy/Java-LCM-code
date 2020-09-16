package okta.unit.testing;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.okta.sdk.authc.credentials.TokenClientCredentials;
import com.okta.sdk.client.Client;
import com.okta.sdk.client.Clients;
import com.okta.sdk.resource.ExtensibleResource;
import com.okta.sdk.resource.user.ChangePasswordRequest;
import com.okta.sdk.resource.user.PasswordCredential;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.UserActivationToken;
import com.okta.sdk.resource.user.UserBuilder;
import com.okta.sdk.resource.user.UserCredentials;
import com.okta.sdk.resource.user.UserProfile;
import com.okta.sdk.resource.user.factor.Factor;
import com.okta.sdk.resource.user.factor.FactorList;
import com.okta.sdk.resource.user.factor.SmsFactor;
import com.okta.sdk.resource.user.factor.SmsFactorProfile;
import com.okta.sdk.resource.user.factor.VerifyFactorRequest;
import com.okta.sdk.resource.user.factor.VerifyFactorResponse;

public class OktaFunctions {
	private final static Logger log = LogManager.getLogger(OktaFunctions.class);
	static String className = "OktaFunctions.";
	/*****************************************************************************************************
	 * This function creates the client object to authenticate the Okta Domain
	 * with the authorization token.
	 * @param oktaDomain {@code String} oktaDomain is the url for Okta domain.
	 * @param oktaAuthToken {@code String} oktaAuthToken is the authorization token of the Okta domain.
	 * @return Client for authentication.
	 * @throws Exception
	 ******************************************************************************************************/
	protected static Client createClientObject(String oktaDomain, String oktaAuthToken) throws Exception {
		// String method = "createClientObject: ";
		log.debug("Entry of createClientObject in OktaFunctions class");
		Client client = null;
		try {
			client = Clients.builder().setOrgUrl(oktaDomain)
					.setClientCredentials(new TokenClientCredentials(oktaAuthToken)).build();
			log.debug(" createClientObject : The client object is created for the okta domain " + oktaDomain);
		} catch (Exception e) {
			
			log.error("createClient-Error in creating client object for the okta Domain" + oktaDomain + ": "
					+ e.getStackTrace());
			throw e;
		}
		return client;
	}

	/*****************************************************************************************************
	 * This function creates user in Okta in staged status.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param profiles {@code Map} user profile details in map which is updated.
	 * @return User contains user details like user id, status as:'STAGED' etc.
	 ******************************************************************************************************/
	public static User createUser(Client client, Map profiles) {
		String method = className+"createUser: ";

		log.debug(method + "The user is created with the following details:\n" + profiles);
		User user = UserBuilder.instance().setActive(false).setProfileProperties(profiles).buildAndCreate(client);
		
		log.debug(method + "The user created in Okta is: " + user);
		return user;
	}

	/*****************************************************************************************************
	 * This function updates the user details in Okta.
	 * @param user {@code com.okta.sdk.resource.user.User} user for whom details are updated.
	 * @param profiles {@code Map} profile details in map which would be updated.
	 * @return user contains the user details which are updated.
	 ******************************************************************************************************/
	protected static User updateUser(User user, Map profiles) {	
		String method = className+"updateUser: ";
		log.debug(method + "The user is updated with the following details:\n" + profiles);
		user.getProfile().putAll(profiles);
		user = user.update();

		log.debug(method+ "User details are successfully updated: " +user);
		return user;
	}

     /*****************************************************************************************************
	 * This function deletes the user in Okta.
	 * @param user {@code com.okta.sdk.resource.user.User} the user to get deleted from Okta.
	 ******************************************************************************************************/
	protected static void deleteUser(User user) {
		String method = className+"deleteUser: ";
		log.debug(method + "The user is deleted with the following details:\n" + "First Name: "
				+ user.getProfile().getFirstName() + "\nLast Name: " + user.getProfile().getLastName());
		user.deactivate(); //User gets de-activated in Okta and status of the user changes to 'Deactivated'.
		user.delete(); //User gets deleted in Okta.
		log.debug(method+ "User delete in Okta is successful.");
	}

	/*****************************************************************************************************
	 * This function enrolls the user for SMS factor to the provided mobile number(MFA through SMS).
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param user {@code com.okta.sdk.resource.user.User} user for whom sms factor needs to be enrolled.
	 * @param mobileNumber {@code String} mobileNumber of the user is required to enroll for sms factor.
	 * @return Factor factor object contains details like factorid,factor type,status of the factor etc.
	 * @throws Exception
	 ******************************************************************************************************/
	static Factor enrollSmsFactor(Client client, User user, String mobileNumber) throws Exception {
		String method = className+"enrollSmsFactor: ";
		log.debug(method + "Enrolling sms factor the user " + user + " with mobile number " + mobileNumber);
		try {
			SmsFactor smsFactor = client.instantiate(SmsFactor.class);
			smsFactor.getProfile().setPhoneNumber(mobileNumber);
			Factor factor = user.addFactor(null, null, null, true, smsFactor);
			log.debug(method+ "User is enrolled for SMS factor: " +factor );
			return factor;
			
		} catch (Exception exception) {
			log.error(method+ "Exception in enrolling the user for SMS Factor. " + exception.getMessage());
			throw exception;
		}
	}

	/**********************************************************************************************************
	 * This function updates the user's password after validating the old password.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param user {@code com.okta.sdk.resource.user.User} user whose password needs to be updated.
	 * @param oldPassword {@code String} oldPassword of the user which needs to be updated with new password.
	 * @param newPassword {@code String} newPassword of the user which needs to be updated to the user account.
	 * @return UserCredentials which contains status of the change password function.
	 * @throws Exception
	 **********************************************************************************************************/
	static UserCredentials changePassword(Client client, User user, String oldPassword, String newPassword) throws Exception {
		String method = className+"changePassword: ";
		UserCredentials changeResult = null;
		PasswordCredential passwordCredential = client.instantiate(PasswordCredential.class);
		passwordCredential.setValue(newPassword.toCharArray());
		PasswordCredential oldPasswordCredential = client.instantiate(PasswordCredential.class);
		oldPasswordCredential.setValue(oldPassword.toCharArray());

		ChangePasswordRequest changePasswordRequest = client.instantiate(ChangePasswordRequest.class);
		try {
			changePasswordRequest.setNewPassword(passwordCredential);
			changePasswordRequest.setOldPassword(oldPasswordCredential);
			changeResult = user.changePassword(changePasswordRequest);
			log.debug(method+ "Change password result for the user "+user.getProfile().getLogin()+" is "+changeResult);
		} catch (Exception exception) {
			log.error(method+ "Exception in Changing the user's password " + exception);
			throw exception;
		}
		
		log.debug(method+ "User password change is successful: " +changeResult);
		return changeResult;
	}
	/****************************************************************************************************************
	 * This function changes the user status to ACTIVE from STAGED status and sends an activation email to the user.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param user {@code com.okta.sdk.resource.user.User} user who needs to be activated.
	 * @return UserActivationToken which contains activation result.
	 * @throws Exception
	 ***************************************************************************************************************/
	static UserActivationToken activateUserSendEmail(Client client, User user) throws Exception {
		String method = className+"activateUserSendEmail: ";
		UserActivationToken activateResult = null;
		try {
			activateResult = user.activate(true); 
			log.debug(method + "The user is activated in okta: " + activateResult);
			
		} catch (Exception exception) {
			log.error(method + "Exception in activating the user from okta- " + exception);
			throw exception;
		}
		return activateResult;
	}

	/***************************************************************************************************************
	 * This function is used to get the list of enrolled factors of the user.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param user {@code com.okta.sdk.resource.user.User} user whose list of enrolled factors needs to be fetched.
	 * @return FactorList which contains details about the enrolled factors of the user .
	 * @throws Exception
	 **************************************************************************************************************/
	static FactorList listOfFactors(Client client, User user) throws Exception {
		String method = className+"listOfFactors: ";
		FactorList factorList = null;
		try {
			factorList = user.listFactors();

		} catch (Exception exception) {
			log.error(method + "Exception in fetching list of factors " + exception);
			throw exception;
		}
		log.debug(method+ "List of factors enrolled for the user are: " +factorList);
		return factorList;
	}

	/**********************************************************************************************************
	 * This function is used to delete the existing sms factor of the user.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param user {@code com.okta.sdk.resource.user.User} user whose existing smsFactor needs to be deleted.
	 * @param factor {@code com.okta.sdk.resource.user.factor.Factor} smsFactor of the user needs to be deleted.
	 * @throws Exception
	 ***********************************************************************************************************/
	static void deleteExistingFactor(Client client, User user, Factor factor) throws Exception {
		String method = className+"deleteExistingFactor: ";
		try {
			//Get the registered sms factor of the user
			FactorList factorList = user.listFactors();
			for (Factor smsfactor : factorList) {
				if (("sms").equalsIgnoreCase(smsfactor.getFactorType().toString())) {
					factor = smsfactor;
				}
			}
			factor.delete();
			log.debug(method+ "Existing sms factor of the user is deleted: ");

		} catch (Exception exception) {
			log.error(method + "Exception in deleting the existing factor of the user. " + exception);
			throw exception;
		}

	}

	/**********************************************************************************************************
	 * This function is used to send sms to the user with passcode(OTP).
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param user {@code com.okta.sdk.resource.user.User} user for whom sms needs to be sent sent with OTP.
	 * @param factor {@code com.okta.sdk.resource.user.factor.Factor} smsFactor of the user.
	 * @return VerifyFactorResponse contains the details like factorResult,phoneNumber, etc.
	 * @throws Exception
	 ***********************************************************************************************************/
	static VerifyFactorResponse sendSmsFactor(Client client, User user,Factor factor) throws Exception {
		String method = className+"sendSmsFactor: ";
		try {
			VerifyFactorRequest verifyFactorRequest = client.instantiate(VerifyFactorRequest.class);
			factor = factor.setVerify(verifyFactorRequest);

			VerifyFactorResponse verifyFactorResponse = factor.verify(verifyFactorRequest);
			log.debug(method+ "User receives SMS with OTP: " +verifyFactorResponse);
			return verifyFactorResponse;
			
		} catch (Exception exception) {
			log.error(method + "Exception in sending SMS to the user: " + exception);
			throw exception;
		}
		
	}

	/*******************************************************************************************************
	 * This function is used to verify the OTP(Sent via function sendSmsFactor) entered by the user.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param user {@code com.okta.sdk.resource.user.User} user for whom otp entered needs to be validated.
	 * @param OTP {@code String} OTP entered  by the user is needed for verification.
	 * @return VerifyFactorResponse contains details like factorResult,etc.
	 * @throws Exception
	 *******************************************************************************************************/

	static VerifyFactorResponse verifySmsFactor(Client client, User user, Factor factor,String OTP) throws Exception {
		String method = className+"verifySmsFactor: ";
		try {
			String factorId = factor.getId();
			factor = user.getFactor(factorId);
			VerifyFactorRequest verifyFactorRequest = client.instantiate(VerifyFactorRequest.class);
			verifyFactorRequest.setPassCode(OTP);
			System.out.println("verify otp token" + verifyFactorRequest.getPassCode());
			VerifyFactorResponse response = factor.verify(verifyFactorRequest);
			log.debug(method + "otp validation result: " + response);
			
			return response;
		} catch (Exception exception) {
			log.error(method + "Exception in verifying the OTP received " + exception);
			throw exception;
		}

	}

	/*****************************************************************************************************
	 * This function is used to authenticate the user credentials with userName and password.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param userName {@code String} userName of the user for authentication.
	 * @param password {@code String} password of the user for authentication
	 * @return ExtensibleResource contains details like stateToken,factorResult,status etc.
	 * @throws Exception
	 ******************************************************************************************************/
	static ExtensibleResource authenticateUser(Client client, String userName, String password) throws Exception {
		String method = className+"authenticateUser: ";
		ExtensibleResource auth = null;
		try {
			ExtensibleResource resource = client.instantiate(ExtensibleResource.class);
			ExtensibleResource protocolNode = client.instantiate(ExtensibleResource.class);
			protocolNode.put("type", "OAUTH");
			resource.put("protocol", protocolNode);
			resource.put("username", userName);
			resource.put("password", password);
			Map<String, Boolean> optionsMap = new HashMap<String, Boolean>();
			optionsMap.put("multiOptionalFactorEnroll", true);
			optionsMap.put("warnBeforePasswordExpired", true);
			resource.put("options", optionsMap);
			
			ExtensibleResource result = client.http().setBody(resource).post("/api/v1/authn", ExtensibleResource.class);
			log.debug(method+ "Authentication result: " +result);

			return result;

		} catch (Exception exception) {
			log.error(method+ "Exception in authenticating the user " + exception);
			throw exception;
		}

	}

	/*****************************************************************************************************************
	 * This function is used to send sms with OTP to the user when login credentials are successfully authenticated
	 * when multifator authentication is required for login
	 * by taking stateToken as input from the function authenticateUser().
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param auth {@code String}  contains stateToken from the authenticateUser() output.
	 * @param factorId {@code com.okta.sdk.resource.user.factor.Factor} smsFactor id of the user.
	 * @return ExtensibleResource contains details like stateToken,status,etc.
	 * @throws Exception
	 ******************************************************************************************************************/

	static String sendSmsChallenge(Client client, String auth, Factor factorId) throws Exception {
		String method = className+"sendSmsChallenge";
		ExtensibleResource verify = null;
		try {
			ExtensibleResource resource = client.instantiate(ExtensibleResource.class);
			ExtensibleResource protocolNode = client.instantiate(ExtensibleResource.class);
			protocolNode.put("type", "OAUTH");
			resource.put("protocol", protocolNode);
			resource.put("stateToken", auth);
			ExtensibleResource smsResult = client.http().setBody(resource)
					.post("/api/v1/authn/factors/" + factorId.getId() + "/verify", ExtensibleResource.class);
			//System.out.println("Result" + smsResult.toString());
			log.debug(method+ "SMS is sent to the user: " + smsResult.toString());
			return smsResult.getString("stateToken");
		} catch (Exception exception) {
			log.error(method+ "Exception in sending SMS with OTP to the user " + exception);
			throw exception;
		}

	}

	/**************************************************************************************************************
	 * This function is used to verify the OTP(from function sendSmsChallenge) entered by the user during MFA 
	 * by taking stateToken as input from the function sendSmsChallenge().
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param user {@code com.okta.sdk.resource.user.User} user for whom otp entered needs to be validated.
	 * @param smsResult {@code String} contains stateToken from the sendSmsChallenge()function output.
	 * @param factorId {@code com.okta.sdk.resource.user.factor.Factor} smsFactor id of the user
	 * @return ExtensibleResource contains details like sessionToken ,status: "SUCCESS",etc.
	 * @throws Exception
	 **************************************************************************************************************/
	static String verifyMfa(Client client, User user, String smsResult, Factor factorId) throws Exception {
		String method = className+"verifyMfa: ";
		try {
			ExtensibleResource resource = client.instantiate(ExtensibleResource.class);
			ExtensibleResource protocolNode = client.instantiate(ExtensibleResource.class);
			protocolNode.put("type", "OAUTH");
			resource.put("protocol", protocolNode);
			resource.put("stateToken", smsResult);
			resource.put("passCode", "057704");
			ExtensibleResource mfaResult = client.http().setBody(resource)
					.post("/api/v1/authn/factors/" + factorId.getId() + "/verify", ExtensibleResource.class);
			//System.out.println("**Result**" + mfaResult.toString());
			log.debug(method+ "OTP verification result:  " + mfaResult.toString());
			return (String) mfaResult.get("sessionToken");
		} catch (Exception exception) {
			log.error(method+"Exception in validating the OTP entered by user " + exception);
			throw exception;
		}

	}
	/*********************************************************************************************************
	 * This function is used to send sms with otp to the user for authentication in case of forgot password
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param userName {@code String} userName of the user for whom SMS needs to be sent for forgot password.
	 * @return ExtensibleResource contains details like stateToken etc.
	 * @throws Exception
	 *********************************************************************************************************/

	static String forgotPassword(Client client, String userName) throws Exception {
		String method = className+"forgotPassword: ";
		ExtensibleResource verify = null;
		try {
			ExtensibleResource resource = client.instantiate(ExtensibleResource.class);
			ExtensibleResource protocolNode = client.instantiate(ExtensibleResource.class);
			protocolNode.put("type", "OAUTH");
			resource.put("protocol", protocolNode);
			resource.put("username", userName);
			resource.put("factorType", "SMS");
			ExtensibleResource result = client.http().setBody(resource)
					.post("/api/v1/authn/recovery/password", ExtensibleResource.class);
			log.debug(method+"Forgot Password OTP verify status is " + result.getString("status"));
			return result.getString("stateToken");
		} 
		catch (Exception exception) {
			log.error(method+"Exception in sending OTP to the user: " + exception);
			throw exception;
		}

	}
	/*****************************************************************************************************
	 * This function is used to verify the otp entered by the user in forgot password scenario.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param stateToken {@code String} stateToken which is required for OTP verification.
	 * @param passCode {@code String} passCode is the OTP entered by the user for verification.
	 * @return ExtensibleResource contains details like stateToken etc.
	 * @throws Exception
	 ******************************************************************************************************/

	static String forgotPasswordVerifySms(Client client, String stateToken, String passCode) throws Exception {
		String method = className+"forgotPasswordVerifySms: ";
		ExtensibleResource verify = null;
		try {
			ExtensibleResource resource = client.instantiate(ExtensibleResource.class);
			ExtensibleResource protocolNode = client.instantiate(ExtensibleResource.class);
			protocolNode.put("type", "OAUTH");
			resource.put("protocol", protocolNode);
			resource.put("stateToken", stateToken);
			resource.put("passCode", passCode);
			ExtensibleResource result = client.http().setBody(resource)
					.post("/api/v1/authn/recovery/factors/sms/verify", ExtensibleResource.class);
			log.debug(method+"Forgot Password OTP verify status is " + result.getString("status"));
			return result.getString("stateToken");
		} catch (Exception exception) {
			log.error(method+"Exception in verifying the OTP entered by user" + exception);
			throw exception;
		}

	}
	/***********************************************************************************************************
	 * This function is used to reset user's password after successful validation of OTP in forgot password.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param stateToken {@code String} stateToken from the function forgotPasswordVerifySms() output.
	 * @param newPassword {@code String} newPassword of the user which needs to be updated to the user account.
	 * @return ExtensibleResource contains details like status,sessionToken,etc.
	 * @throws Exception
	 **********************************************************************************************************/

	static String forgotPasswordRestPassword(Client client, String stateToken, String newPassword) throws Exception {
		String method = className+"forgotPasswordRestPassword: ";
		ExtensibleResource verify = null;
		try {
			ExtensibleResource resource = client.instantiate(ExtensibleResource.class);
			ExtensibleResource protocolNode = client.instantiate(ExtensibleResource.class);
			protocolNode.put("type", "OAUTH");
			resource.put("protocol", protocolNode);
			resource.put("stateToken", stateToken);
			resource.put("newPassword", newPassword.toCharArray());
			ExtensibleResource result = client.http().setBody(resource)
					.post("/api/v1/authn/credentials/reset_password", ExtensibleResource.class);
			log.debug(method+"Password reset status is " + result.getString("status"));
			return result.getString("status");
		} catch (Exception exception) {
			log.error(method+"Exception in user password reset: " + exception.getMessage());
			throw exception;
		}
	}
	/**************************************************************************************************************
	 * This function is used to enroll the user for SMS factor for new phone number(MFA through SMS) after 
	 * deleting the previously enrolled SMS factor.
	 * @param client {@code com.okta.sdk.client.Client} for authentication.
	 * @param user {@code com.okta.sdk.resource.user.User} user for whom sms factor needs to be enrolled.
	 * @param mobileNumber {@code String} new mobileNumber of the user for which sms factor needs to be enrolled.
	 * @return Factor factor object contains details like factorid,factor type,status of the factor etc.
	 * @throws Exception
	 **************************************************************************************************************/
	static Factor enrollSmsFactorForNewPhoneNumber(Client client, User user, String mobileNumber) throws Exception {
		String method = className+"enrollSmsFactorForNewPhoneNumber: ";
		log.debug(method + "Enrolling sms factor the user " + user + " with mobile number " + mobileNumber);
		try {
			SmsFactor smsFactor = client.instantiate(SmsFactor.class);
			smsFactor.getProfile().setPhoneNumber(mobileNumber);
			Factor factor = user.addFactor(true, null, null, true, smsFactor);
			log.debug(method+ "user is enrolled for sms factor for new phone number: "+factor);
			return factor;
		} catch (Exception exception) {
			log.error(method+"Exception in enrolling for sms factor for new phone number " + exception.getMessage());
			throw exception;
		}
	}

}
