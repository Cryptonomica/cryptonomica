package net.cryptonomica.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.util.logging.Logger;

/**
 * see:
 * https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/appengine/twilio/src/main/java/com/example/appengine/twilio/SendSmsServlet.java
 * <p>
 * https://www.twilio.com/docs/libraries/java#testing-your-installation
 * JavaDoc: https://twilio.github.io/twilio-java/7.5.0/
 */
public class TwillioUtils {

    /* ---- Logger: */
    private static final Logger LOG = Logger.getLogger(ApiKeysUtils.class.getName());

    public static Message sendSms(final String phoneNumber, final String smsMessage)
            throws NumberParseException, IllegalArgumentException {

        checkPhoneNumber(phoneNumber);
        checkSmsMessage(smsMessage);

        final String accountSid = ApiKeysUtils.getApiKey("twilioAccountSid");
        final String authToken = ApiKeysUtils.getApiKey("twilioAuthToken");
        final String twilioPhoneNumber = ApiKeysUtils.getApiKey("twilioPhoneNumber");

        Twilio.init(accountSid, authToken);

        Message message = Message.creator(
                new PhoneNumber(phoneNumber),        // To number
                new PhoneNumber(twilioPhoneNumber),  // From number
                smsMessage                           // SMS body
        ).create();

        LOG.warning(message.toString());

        return message;

    }

    static void checkPhoneNumber(String phoneNumberStr)
            throws NumberParseException, IllegalArgumentException {

        if (phoneNumberStr == null || phoneNumberStr.length() == 0) {
            throw new IllegalArgumentException("Phone number is empty");
        } else if (phoneNumberStr.charAt(0) != '+') {
            throw new IllegalArgumentException("Phone number should start with '+'");
        }

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(phoneNumberStr, null);
        // throws NumberParseException

        if (!phoneUtil.isValidNumber(phoneNumber)) {
            throw new IllegalArgumentException(phoneNumberStr + " is not valid phone number");
        }
    } // end of checkPhoneNumber

    static void checkSmsMessage(String smsMessageStr) throws IllegalArgumentException {

        if (smsMessageStr == null || smsMessageStr.length() == 0) {
            throw new IllegalArgumentException("sms message is empty");
        } else if (smsMessageStr.getBytes().length > 140) {
            throw new IllegalArgumentException("SMS message can contain up to 140 bytes");
        }

    } // end of checkSmsMessage

}
