package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.Arbitrator;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.forms.AddArbitratorForm;
import net.cryptonomica.returns.ArbitratorGeneralView;
import net.cryptonomica.service.UserTools;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries

/**
 * explore on: {sandbox-cryptonomica || cryptonomica-server}.appspot.com/_ah/api/explorer
 * ! - API should be registered in  web.xml (<param-name>services</param-name>)
 * ! - on frontend API should be loaded in app.js - app.run()
 */
@Api(name = "arbitratorsAPI", // The api name must match '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
        description = "functions connected to arbitrators")
//
public class ArbitratorsAPI {

    /* --- Logger: */
    private static final Logger LOG = Logger.getLogger(ArbitratorsAPI.class.getName());

    @ApiMethod(
            name = "addArbitrator",
            path = "addArbitrator",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public ArbitratorGeneralView addArbitrator(
            final User googleUser,
            final AddArbitratorForm addArbitratorForm)
            throws Exception {

        /* --- Ensure cryptonomica officer: */
        UserTools.ensureCryptonomicaOfficer(googleUser);

        /* --- Check form: */
        if (addArbitratorForm.getId() == null){
            throw new IllegalArgumentException("User ID is missing");
        }


        // create arbitrator from form:
        Arbitrator arbitrator = new Arbitrator(addArbitratorForm);
        // save arbitrator in DS:
        Key<Arbitrator> arbitratorKey = ofy().save()
                .entity(arbitrator).now();
        // add info to CryptonomicaUser
        CryptonomicaUser arbitratorCryptonomicaUser = ofy().load()
                .key(
                        Key.create(CryptonomicaUser.class, arbitrator.getId())
                )
                .now();
        arbitratorCryptonomicaUser.setArbitrator(Boolean.TRUE);
        ofy().save().entity(arbitratorCryptonomicaUser).now();
        // create ArbitratorGeneralView
        ArbitratorGeneralView arbitratorGeneralView = new ArbitratorGeneralView(arbitratorCryptonomicaUser, arbitrator);

        LOG.warning("new arbitrator created: " + new Gson().toJson(arbitratorGeneralView));

        return arbitratorGeneralView;
    }

    @ApiMethod(
            name = "showAllArbitrators",
            path = "showAllArbitrators",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public ArrayList<ArbitratorGeneralView> showAllArbitrators(final User googleUser) throws Exception {

        /* --- Ensure authorization: */
        UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        /* --- Load list of all arbitrators: */
        List<Arbitrator> arbitratorsList = ofy().load()
                .type(Arbitrator.class)
                .list();

        /* --- Create an empty list to store arbitrators data  */
        ArrayList<ArbitratorGeneralView> arbitratorGeneralViewArrayList =
                new ArrayList<>();

        /* ---  fill the arbitratorGeneralViewArrayList */
        if (arbitratorsList != null) {
            for (Arbitrator arbitrator : arbitratorsList) {
                // load CryptonomicaUser data for notary:
                CryptonomicaUser arbitratorCryptonomicaUser = ofy()
                        .load()
                        .key(
                                Key.create(CryptonomicaUser.class, arbitrator.getId())
                        )
                        .now();
                // make ArbitratorGeneralView:
                ArbitratorGeneralView arbitratorGeneralView = new ArbitratorGeneralView(
                        arbitratorCryptonomicaUser, arbitrator
                );
                // add to ArrayList:
                arbitratorGeneralViewArrayList.add(arbitratorGeneralView);
            }
        } // end: if loop

        LOG.warning("arbitratorGeneralViewArrayList: " + new Gson().toJson(arbitratorGeneralViewArrayList));

        return arbitratorGeneralViewArrayList;

    } // end of showAllArbitrators

}
