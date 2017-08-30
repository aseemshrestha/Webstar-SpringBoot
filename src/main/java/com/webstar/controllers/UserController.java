package com.webstar.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.webstar.generated.models.Classfication;
import com.webstar.models.UserDetails;
import com.webstar.services.IEmailService;
import com.webstar.services.IUserService;
import com.webstar.util.Categories;
import com.webstar.util.Constants;
import com.webstar.util.Roles;
import com.webstar.util.Utils;
import com.webstar.util.Views;

@Controller
public class UserController
{
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    public static final String CHARSET = "ISO-8859-1";

    @Autowired
    private IUserService userService;

    @Autowired
    private IEmailService emailService;

    @RequestMapping( "/" )
    public String home()
    {
        return Views.HOME_PAGE;
    }

    @RequestMapping( "/about" )
    public String about()
    {

        return Views.ABOUT_PAGE;
    }

    @RequestMapping( value = "/myhome", method = { RequestMethod.GET, RequestMethod.POST } )
    public String myhome(String email, String password, Model model, HttpServletResponse response,
        HttpServletRequest request)
    {
        String nameEmail = userService.readNameEmailFromCookie(request);

        if (nameEmail == null || nameEmail.isEmpty()) {
            Optional<UserDetails> user = userService.isUserAuthenticated(email, password);
            if (!user.isPresent()) {
                model.addAttribute("loginError", Constants.LOGIN_FAIL_MSG);
                return Views.HOME_PAGE;
            } else {
                String cookieValue = email + "_" + user.get().getFirstName();
                String encodedCookie = "";
                try {
                    userService.updateLastLoggedTime(new Date(), email);
                    encodedCookie = new String(Base64.encodeBase64(cookieValue.getBytes(CHARSET)));
                } catch (UnsupportedEncodingException e) {}
                response.addCookie(new Cookie(Constants.WEBSTAR_COOKIE_AUTH, encodedCookie));
                // if cookie is disabled, hidden form to maintain session
                model.addAttribute("nameEmail", email + "_" + user.get().getFirstName());
            }
        } else {
            model.addAttribute("nameEmail", nameEmail);
        }
        return Views.MY_HOME_PAGE;

    }

    @RequestMapping( value = "/register", method = RequestMethod.GET )
    public ModelAndView register(ModelAndView modelAndView, @ModelAttribute( "userDetails" ) UserDetails userDetails,
        HttpServletRequest request)
    {

        modelAndView.setViewName(Views.REGISTRATION_PAGE);
        modelAndView.getModel().put("ipAddress", Utils.getClientIp(request));
        return modelAndView;
    }

    @RequestMapping( value = "/forgotpassword", method = RequestMethod.GET )
    public String forgotPassword()
    {

        return Views.FORGOT_PASSWORD;
    }

    @RequestMapping( value = "/forgotpassword", method = RequestMethod.POST )
    public String forgotPassword(String email, Model model, HttpServletRequest request)
    {

        Optional<UserDetails> user = userService.findUserbyEmail(email);
        if (!user.isPresent()) {
            model.addAttribute("noaccount", "Cannot find the user with email:" + email);
        } else {
            UserDetails userObj = user.get();
            userObj.setResetToken(UUID.randomUUID().toString());
            String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String msg = "To reset your password, click the link below:\n" + appUrl
                + "/reset?token=";
            try {
                userService.save(userObj);
                emailService.sendMail(userObj.getEmail(), "Password Reset Request",
                                      msg + userObj.getResetToken() + "\n\n Webstar Support Team ",
                                      "webstar@support.com");
                model.addAttribute("passwordResetLink",
                                   "Password reset link has been set to your email address:" + userObj.getEmail());
            } catch (Exception ex) {
                LOG.debug("[UserController][forgotPassword]Cannot save user to db:", ex);
            }
        }
        return Views.FORGOT_PASSWORD;
    }

    @RequestMapping( value = "/reset", method = RequestMethod.GET )
    public ModelAndView resetPasswordPage(ModelAndView modelAndView, @RequestParam( "token" ) String token)
    {

        Optional<UserDetails> user = userService.findUserbyToken(token);
        if (user.isPresent()) {
            modelAndView.addObject("resetToken", token);
        } else {
            modelAndView.addObject("resetTokenError", "Oops ! Password reset link is either expired or invalid !");
        }
        modelAndView.setViewName(Views.RESET_PASSWORD);
        return modelAndView;
    }

    @RequestMapping( value = "/reset", method = RequestMethod.POST )
    public ModelAndView resetPasswordPage(ModelAndView modelAndView, String password, String confirmpassword,
        String token)
    {

        Optional<UserDetails> user = userService.findUserbyToken(token);
        if (!password.equals(confirmpassword)) {
            modelAndView.addObject("resetTokenError", "Password and Compare Password do not match.");
        } else {
            if (user.isPresent()) {
                UserDetails resetUser = user.get();
                resetUser.setPassword(password);
                resetUser.setPasswordConfirm(password);
                resetUser.setResetToken(null);
                try {
                    userService.save(resetUser);
                } catch (Exception ex) {
                    LOG.debug("Error logging password reset:", ex);
                }
                modelAndView.addObject("passwordResetSuccessful",
                                       "You have successfully reset your password. You may now login");
            } else {
                modelAndView.addObject("resetTokenError", "Oops! Password reset link is not valid.");
            }
        }
        modelAndView.setViewName(Views.RESET_PASSWORD);
        return modelAndView;
    }

    @RequestMapping( value = "/categories", method = RequestMethod.GET, produces = { "application/json" } )
    public @ResponseBody Map<String, String> getCategories()
    {
        return Categories.getCategories();
    }

    @RequestMapping( value = "/subcategories", method = RequestMethod.GET )
    public @ResponseBody String getSubCategoryByKey(@RequestParam( "category" ) String category)
    {
        return Categories.getSubCategoryByKey(category);
    }

    @RequestMapping( value = "/register", method = RequestMethod.POST )
    public ModelAndView register(ModelAndView modelAndView,
        @ModelAttribute( "userDetails" ) @Valid UserDetails userDetails, BindingResult result)
    {

        boolean isValidPassword = true;
        boolean isEmailValid = true;
        boolean isPhoneValid = true;
        modelAndView.setViewName(Views.REGISTRATION_PAGE);
        Optional<UserDetails> user = userService.findUserbyEmail(userDetails.getEmail());
        if (user.isPresent()) {
            modelAndView.getModel().put("emailExists", Constants.EMAIL_EXISTS);
        } else {
            userDetails.setFirstName(userDetails.getFirstName());
            userDetails.setLastName(userDetails.getLastName());
            userDetails.setEmail(userDetails.getEmail());
            userDetails.setPassword(userDetails.getPassword());
            userDetails.setPasswordConfirm(userDetails.getPasswordConfirm());
            userDetails.setPhone(userDetails.getPhone());
            userDetails.setIpAddress(userDetails.getIpAddress());
            userDetails.setUserStatus(Constants.ACTIVE);
            userDetails.setRegistrationDate(new Date());
            userDetails.setLastLoggedIn(new Date());
            userDetails.setRole(Roles.ROLE_USER);
            if (!userDetails.getPassword().equals(userDetails.getPasswordConfirm())) {
                modelAndView.getModel().put("passwordMismatch", "Password mismatch");
                isValidPassword = false;
            }
            if (!Utils.emailValidator(userDetails.getEmail())) {
                modelAndView.getModel().put("invalidEmail", "Invalid email");
                isEmailValid = false;
            }
            if (userDetails.getPhone().length() >= 1) {
                if (!Utils.validatePhoneNumber(userDetails.getPhone())) {
                    modelAndView.getModel().put("invalidPhone", "Invalid Phone number");
                    isPhoneValid = false;
                }
            }
            if (!result.hasErrors() && isValidPassword && isEmailValid && isPhoneValid) {
                try {
                    userService.save(userDetails);
                    modelAndView.getModel().put("userDetails", new UserDetails());
                    modelAndView.getModel().put("registrationSuccess", Constants.REGISTRATION_SUCCESS_MSG);
                    emailService.sendMail(userDetails.getEmail(), "Registration Confirmation with Webstar",
                                          "Dear " + Utils.upperCaseFirst(userDetails.getFirstName()) + " ,\n"
                                              + Constants.REGISTRATION_MESSAGE,
                                          "support@webstar.com");
                } catch (Exception ex) {
                    LOG.debug("[UserController-Register] Error while saving to db", ex);
                }
            }
        }
        return modelAndView;
    }
}
