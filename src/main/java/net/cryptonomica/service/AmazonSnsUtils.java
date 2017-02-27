//package net.cryptonomica.service;
//
//
//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.services.sns.AmazonSNSClient;
//import com.amazonaws.services.sns.AmazonSNSClientBuilder;
//import com.amazonaws.services.sns.model.MessageAttributeValue;
//import com.amazonaws.services.sns.model.PublishRequest;
//import com.amazonaws.services.sns.model.PublishResult;
//import com.google.i18n.phonenumbers.NumberParseException;
//import com.google.i18n.phonenumbers.PhoneNumberUtil;
//import com.google.i18n.phonenumbers.Phonenumber;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.logging.Logger;
//
///**
// * see:
// * https://github.com/aws/aws-sdk-java
// */
//public class AmazonSnsUtils {
//
//    /* ---- Logger: */
//    private static final Logger LOG = Logger.getLogger(AmazonSnsUtils.class.getName());
//
//    /* Each SMS message can contain up to 140 bytes, and the character limit depends on the encoding scheme.
//    For example, an SMS message can contain:
//    160 GSM characters
//    140 ASCII characters
//    70 UCS-2 characters
//    */
//    // private static String message;
//    /* When you send an SMS message, specify the phone number using the E.164 format. E.164 is a standard for the phone
//    number structure used for international telecommunication. Phone numbers that follow this format can have a maximum
//    of 15 digits, and they are prefixed with the plus character (+) and the country code. For example, a U.S. phone
//    number in E.164 format would appear as +1XXX5550100.
//    */
//    // private static String phoneNumber;
//
//    public static PublishResult sendSMSMessage(final String phoneNumber, final String message)
//            throws NumberParseException, IllegalArgumentException {
//
//        checkPhoneNumber(phoneNumber);
//        checkSmsMessage(message);
//
//        final String amazonSnsAccessKey = ApiKeysUtils.getApiKey("AmazonSnsAccessKey");
//        final String amazonSnsSecretKey = ApiKeysUtils.getApiKey("AmazonSnsSecretKey");
//
//        // AmazonSNSClient snsClient = new AmazonSNSClient(); // depreciated
//        AWSCredentials awsCredentials = new BasicAWSCredentials(amazonSnsAccessKey, amazonSnsSecretKey);
//        AmazonSNSClient snsClient = (AmazonSNSClient) AmazonSNSClientBuilder
//                .standard()
//                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
//                .build();
//        //<set SMS attributes>
//        Map<String, MessageAttributeValue> smsAttributes =
//                new HashMap<String, MessageAttributeValue>();
//        smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue()
//                .withStringValue("IACC") //The sender ID shown on the device. !!! up to 10 chars
//                .withDataType("String"));
//        smsAttributes.put("AWS.SNS.SMS.MaxPrice", new MessageAttributeValue()
//                // .withStringValue("0.50") //Sets the max price to 0.50 USD.
//                .withStringValue("1.00")
//                .withDataType("Number"));
//        smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue()
//                // .withStringValue("Promotional") //Sets the type to promotional.
//                .withStringValue("Transactional") //Sets the type to promotional.
//                .withDataType("String"));
//
//        PublishResult result = snsClient.publish(
//                new PublishRequest()
//                        .withMessage(message)
//                        .withPhoneNumber(phoneNumber)
//                        .withMessageAttributes(smsAttributes));
//        System.out.println(result.getMessageId()); //
//        System.out.println(result.toString()); //
//        return result;
//    }
//
//    static void checkPhoneNumber(String phoneNumberStr)
//            throws NumberParseException, IllegalArgumentException {
//
//        if (phoneNumberStr == null || phoneNumberStr.length() == 0) {
//            throw new IllegalArgumentException("Phone number is empty");
//        } else if (phoneNumberStr.charAt(0) != '+') {
//            throw new IllegalArgumentException("Phone number should start with '+'");
//        }
//
//        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
//        Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(phoneNumberStr, null);
//        // throws NumberParseException
//
//        if (!phoneUtil.isValidNumber(phoneNumber)) {
//            throw new IllegalArgumentException(phoneNumberStr + " is not valid phone number");
//        }
//    } // end of checkPhoneNumber
//
//    static void checkSmsMessage(String smsMessageStr) throws IllegalArgumentException {
//
//        if (smsMessageStr == null || smsMessageStr.length() == 0) {
//            throw new IllegalArgumentException("sms message is empty");
//        } else if (smsMessageStr.getBytes().length > 140) {
//            throw new IllegalArgumentException("SMS message can contain up to 140 bytes");
//        }
//
//    } // end of checkSmsMessage
//
//}
