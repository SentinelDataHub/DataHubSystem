package fr.gael.dhus.server.http.webapp.stub.controller;

import java.security.MessageDigest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import fr.gael.dhus.service.exception.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions.UserPasswordConfirmationException;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.User.PasswordEncryption;
import fr.gael.dhus.service.UserService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

@RestController
public class StubUserController {
	private static Log logger = LogFactory.getLog(StubUserController.class);


	@Autowired
	private UserService userService;

	private User getUserFromPrincipal(Principal principal) {
		User user = ((User) ((UsernamePasswordAuthenticationToken) principal)
				.getPrincipal());
		return userService.resolveUser(user);
	}

	@PreAuthorize("isAuthenticated ()")
	@RequestMapping(value = "/users/{userid}", method = RequestMethod.GET)
	public User getUserProfile(Principal principal,
			@PathVariable(value = "userid") String userid) {
		return getUserFromPrincipal(principal);
	}

	@PreAuthorize("isAuthenticated ()")
	@RequestMapping(value = "/users/{userid}", method = RequestMethod.PUT)
	public int updateUserProfile(Principal principal,
			@RequestBody UserRequestBody body,
			@PathVariable(value = "userid") String userid) throws RequiredFieldMissingException, RootNotModifiableException {
		logger.info("******** updateUserProfile()");
		int responseCode = 0;
		User user = body.getUser();
		logger.info("******** called body.getUser");
		PasswordModel passwordModel = body.getPasswordModel();
		User u = getUserFromPrincipal(principal);

		// check user fields. set only not empty fields
		if (user.getEmail() != null && !user.getEmail().isEmpty())
			u.setEmail(user.getEmail());
		if (user.getFirstname() != null && !user.getFirstname().isEmpty())
			u.setFirstname(user.getFirstname());
		if (user.getLastname() != null && !user.getLastname().isEmpty())
			u.setLastname(user.getLastname());
		if (user.getAddress() != null)
			u.setAddress(user.getAddress());
		if (user.getPhone() != null)
			u.setPhone(user.getPhone());
		if (user.getCountry() != null && !user.getCountry().isEmpty()
				&& !user.getCountry().equals("unknown"))
			u.setCountry(user.getCountry());
		if (user.getUsage() != null && !user.getUsage().isEmpty()
				&& !user.getUsage().equals("unknown"))
			u.setUsage(user.getUsage());
		if (user.getSubUsage() != null && !user.getSubUsage().isEmpty()
				&& !user.getSubUsage().equals("unknown"))
			u.setSubUsage(user.getSubUsage());
		if (user.getDomain() != null && !user.getDomain().isEmpty()
				&& !user.getDomain().equals("unknown"))
			u.setDomain(user.getDomain());
		if (user.getSubDomain() != null && !user.getSubDomain().isEmpty()
				&& !user.getSubDomain().equals("unknown"))
			u.setSubDomain(user.getSubDomain());

		if (user.getPassword() != null && passwordModel != null) {
			logger.info("******** update user password");
			// encrypt old password to compare
			PasswordEncryption encryption = u.getPasswordEncryption();
			String oldpwd = passwordModel.getOldPassword();
			if (encryption != PasswordEncryption.NONE) // when configurable
			{
				try {
					MessageDigest md = MessageDigest.getInstance(encryption
							.getAlgorithmKey());
					oldpwd = new String(Hex.encode(md.digest(passwordModel
							.getOldPassword().getBytes("UTF-8"))));
				} catch (Exception e) {
					responseCode = 1002;
					throw new UserBadEncryptionException(
							"There was an error while encrypting password of user "
									+ u.getUsername(), e);
				}
			}

			if (!u.getPassword().equals(oldpwd)) {
				responseCode = 1003;
				throw new UserBadOldPasswordException(
						"Old password is not correct.");
			}

			if (!user.getPassword().equals(passwordModel.getConfirmPassword())) {
				responseCode = 1004;
				throw new UserPasswordConfirmationException(
						"Confirmation password value doesn't match.");
			}
			userService.selfChangePassword(u.getUUID(),passwordModel.getOldPassword(),passwordModel.getConfirmPassword());
		}
		logger.info("******** update user");

		userService.selfUpdateUser(u);
		return responseCode;
	}



	@RequestMapping(value = "/forgotpwd", method = RequestMethod.POST)
	public ResponseEntity<?>  forgotPassword(@RequestBody User user)
			throws RootNotModifiableException {
		int responseCode = 0;

		try {
			userService.forgotPassword ( user, "#/home/r=");
		}
		catch(EmailNotSentException ex){
			return new ResponseEntity<>("{\"code\":\"error-sending-email\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		catch (UserNotExistingException ex){
			return new ResponseEntity<>("{\"code\":\"user-not-found\"}", HttpStatus.BAD_REQUEST);
		}
		catch (Exception e) {
			return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\",\"code\":\"generic-error\"}", HttpStatus.BAD_REQUEST);
		}


		return new ResponseEntity<>("{\"code\":\""+responseCode+"\"}", HttpStatus.OK);
	}

	/**
	 * Uploaded products ids list of logged user
	 *
	 * @return ArrayList String ids
	 */
	@RequestMapping(value = "/admin/users/{userid}/uploadedproducts", method = RequestMethod.GET)
	public ResponseEntity<?> uploadedProducts(Principal principal,
			@RequestParam(value = "offset", defaultValue = "0") int start,
			@RequestParam(value = "limit", defaultValue = "") int count) {
		fr.gael.dhus.service.UserService userService = ApplicationContextProvider
				.getBean(fr.gael.dhus.service.UserService.class);

		try {
			User user = (User) ((UsernamePasswordAuthenticationToken) principal)
					.getPrincipal();

			ArrayList<String> productsIdentifiers = new ArrayList<String>();
			Set<String> products = userService
					.getUploadedProductsIdentifiers(user.getUUID());
			if (products == null)
				return new ResponseEntity<>(productsIdentifiers, HttpStatus.OK);
			int i = 0;
			Iterator<String> iter = products.iterator();

			while (products != null && iter.hasNext() && i < count) {
				String product = iter.next();
				if (i < start) {
					i++;
					continue;
				}
				i++;
				if (product == null) {
					break;
				}
				productsIdentifiers.add(product);
			}

			return new ResponseEntity<>(productsIdentifiers, HttpStatus.OK);
		} catch (org.springframework.security.access.AccessDeniedException e) {
			e.printStackTrace();
			return new ResponseEntity<>("{\"code\":\"unauthorized\"}",
					HttpStatus.FORBIDDEN);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Count uploaded products ids list of logged user
	 *
	 * @return Long count of ids
	 */
	@RequestMapping(value = "/admin/users/{userid}/uploadedproducts/count", method = RequestMethod.GET)
	public ResponseEntity<?> uploadedProductsCount(Principal principal) {
		fr.gael.dhus.service.UserService userService = ApplicationContextProvider
				.getBean(fr.gael.dhus.service.UserService.class);
		try {
			User user = (User) ((UsernamePasswordAuthenticationToken) principal)
					.getPrincipal();
			return new ResponseEntity<>("{\"count\":"
					+ userService.countUploadedProducts(user.getUUID()) + "}",
					HttpStatus.OK);
		} catch (org.springframework.security.access.AccessDeniedException e) {
			e.printStackTrace();
			return new ResponseEntity<>("{\"code\":\"unauthorized\"}",
					HttpStatus.FORBIDDEN);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Check User code in order to allow password reset
	 *
	 * @return user code or error message in json format
	 */
	@RequestMapping(value = "/resetpwd", method = RequestMethod.POST)
	public ResponseEntity<?> resetPassword(Principal principal,
			@RequestParam(value = "code", defaultValue = "") String code,
			@RequestParam(value = "password", defaultValue = "") String password) {
		fr.gael.dhus.service.UserService userService = ApplicationContextProvider
				.getBean(fr.gael.dhus.service.UserService.class);

		try {
			userService.resetPassword(code, password);
		} catch (org.springframework.security.access.AccessDeniedException e) {
			e.printStackTrace();
			return new ResponseEntity<>("{\"code\":\"unauthorized\"}",
					HttpStatus.FORBIDDEN);
		} catch (EmailNotSentException e) {
			e.printStackTrace();
			return new ResponseEntity<>("{\"code\":\"email_not_sent\"}",
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>("{\"code\":\"success\"}", HttpStatus.OK);

	}

}
