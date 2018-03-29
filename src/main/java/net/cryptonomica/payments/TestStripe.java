package net.cryptonomica.payments;

import com.google.gson.Gson;
import com.stripe.*;
import com.stripe.exception.*;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import net.cryptonomica.api.NotaryAPI;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * see:
 */
public class TestStripe {

    /* ---- Logger: */
    private static final Logger LOG = Logger.getLogger(TestStripe.class.getName());

    public void makeCharge1(final HttpServletRequest request) throws APIException,
            AuthenticationException, InvalidRequestException, APIConnectionException {
        // see: https://stripe.com/docs/charges
        /* --- */
        // Set your secret key: remember to change this to your live secret key in production
        // See your keys here: https://dashboard.stripe.com/account/apikeys

        Stripe.apiKey = "YOUR-SECRET-KEY";

        // Get the credit card details submitted by the form
        String token = request.getParameter("stripeToken");

        // Create a charge: this will charge the user's card
        Charge charge = null;
        try {
            Map<String, Object> chargeParams = new HashMap<String, Object>();
            chargeParams.put("amount", 1000); // Amount in cents
            chargeParams.put("currency", "eur");
            chargeParams.put("source", token);
            chargeParams.put("description", "Example charge");

            charge = Charge.create(chargeParams);

            /* -- my tests: */
            charge.getId();
            BalanceTransaction balanceTransaction = charge.getBalanceTransactionObject();
            balanceTransaction.getDescription();
            Transfer transfer = charge.getSourceTransferObject();
            ExternalAccount source = charge.getSource();
            Customer customer = charge.getCustomerObject();
            ChargeOutcome chargeOutcome = charge.getOutcome();
            /* -- */

        } catch (CardException e) {
            // The card has been declined
        }
    } // end makeCharge1

    public void makeCharge2() {
        // see:
        // https://github.com/stripe/stripe-java#usage
        RequestOptions requestOptions = (new RequestOptions.RequestOptionsBuilder())
                .setApiKey("YOUR-SECRET-KEY").build();

        Map<String, Object> chargeMap = new HashMap<String, Object>();
        chargeMap.put("amount", 100);
        chargeMap.put("currency", "usd");

        Map<String, Object> cardMap = new HashMap<String, Object>();
        cardMap.put("number", "4242424242424242");
        cardMap.put("exp_month", 12);
        cardMap.put("exp_year", 2020);

        chargeMap.put("card", cardMap);

        try {
            Charge charge = Charge.create(chargeMap, requestOptions);

            System.out.println(charge);
        } catch (StripeException e) {
            e.printStackTrace();
        }
    } // end of makeCharge2
}
