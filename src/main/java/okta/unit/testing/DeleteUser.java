package okta.unit.testing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.user.User;

public class DeleteUser {

	private static final Logger log = LogManager.getLogger(DeleteUser.class);
	/**************************************************************************************************
	 * Executes the sequence of steps to <h1>delete user</h1>in Okta.
	 * @param client {@code com.okta.sdk.client.Client} for authentication
	 * @param userName {@code String} username of the user which needs to be deleted from Okta.
	 *************************************************************************************************/
	public void test(Client client,String userName) {
		String method = "DeleteUser.test";
		try {
			
			//Get the user object
			User user = client.getUser(userName);
			log.debug(method+"The user to be deleted is: "+user);

			//delete user in okta
			OktaFunctions.deleteUser(user);
			log.info(method+ "User deleted in Okta is: "+userName);

		} catch (Exception exception) {
			log.error(method+ "Error while deleting the user in Okta." + exception.getMessage());
		}

	}
}
