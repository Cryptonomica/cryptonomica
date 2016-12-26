package net.cryptonomica.payments;

import com.google.gson.Gson;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.ExternalAccount;
import com.stripe.net.RequestOptions;
import net.cryptonomica.service.ApiKeysUtils;
import net.cryptonomica.servlets.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * https://cryptonomica.net/StripeTestServlet
 */
public class StripeTestServlet extends HttpServlet {

    /* ---- Logger: */
    private static final Logger LOG = Logger.getLogger(StripeTestServlet.class.getName());

    /* ---- Gson */
    private static final Gson GSON = new Gson();

    // Set your secret key: remember to change this to your live secret key in production
    // See your keys here: https://dashboard.stripe.com/account/apikeys
    private static final String STRIPE_SECRET_KEY = ApiKeysUtils.getApiKey("StripeTestSecretKey");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        /* --- request*/
        String requestDataStr = ServletUtils.getAllRequestData(request);

        String responseStr = requestDataStr + ",";

        // Get the credit card details submitted by the form
        String token = request.getParameter("stripeToken");
        @SuppressWarnings("unused")
        String cardHolderName = request.getParameter("cardHolderName");

        /* -- see example on: https://github.com/stripe/stripe-java#usage */
        //
        RequestOptions requestOptions = (new RequestOptions.RequestOptionsBuilder())
                .setApiKey(STRIPE_SECRET_KEY).build();
        //
        // -- Card options:
        // ------------ // Map<String, Object> cardMap = new HashMap<>();
        // cardMap.put("name", cardHolderName);// - "Received both card and source parameters. Please pass in only one."
        /*
        cardMap.put("number", "4242424242424242");
        cardMap.put("exp_month", 12);
        cardMap.put("exp_year", 2020);
        */

        // -- Charge options:
        Map<String, Object> chargeMap = new HashMap<>();
        chargeMap.put("amount", 100); // -- this is ONE dollar
        chargeMap.put("currency", "usd");
        // see: https://stripe.com/docs/charges (Java)
        // chargeParams.put("source", token);
        // chargeParams.put("description", "Example charge");
        chargeMap.put("source", token);
        chargeMap.put("description", "Example charge: " + token);
        //
        // ------------ // chargeMap.put("card", cardMap);
        // -- make charge:
        responseStr += "\"charge\":";
        try {
            Charge charge = Charge.create(chargeMap, requestOptions);
            // System.out.println(charge);
            responseStr += GSON.toJson(charge);
            LOG.warning(GSON.toJson(charge));
            ExternalAccount source = charge.getSource();
            LOG.warning("source: " + GSON.toJson(source));
            charge.getStatus(); // -- "succeeded"
            charge.getPaid(); // - boolean: true or false
            //
            // String customerStr = source.getCustomer();
            // LOG.warning("customerStr: " + customerStr); //
            // responseStr += "," + "\"customerStr\":" + GSON.toJson(charge); // - same as source

        } catch (StripeException e) {
            String errorMessage = e.getMessage();
            responseStr += "\"";
            responseStr += errorMessage;
            responseStr += "\"";
            LOG.warning(errorMessage);
        }

        responseStr += "}";
        ServletUtils.sendJsonResponse(response, responseStr);
    } // end of doPost()

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }
}
