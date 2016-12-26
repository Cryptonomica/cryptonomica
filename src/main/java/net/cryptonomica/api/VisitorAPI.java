package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.Notary;
import net.cryptonomica.returns.NotaryGeneralView;
import net.cryptonomica.returns.UserProfileGeneralView;
import net.cryptonomica.service.UserTools;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries


/**
 * API for searching for Notaries, Lawyers and Arbitrators.
 * explore on: cryptonomica-{test || server}.appspot.com/_ah/api/explorer
 * ! - API should be registered in  web.xml (<param-name>services</param-name>)
 * ! - API should be loaded in app.js - app.run()
 * * in this API:
 * 1) searchForNotaries
 * 2) ...
 */
@Api(
        name = "visitorAPI", // The api name must match '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {
                Constants.WEB_CLIENT_ID,
                Constants.API_EXPLORER_CLIENT_ID},
        description = "visitorAPI"
)
@SuppressWarnings("unused")
public class VisitorAPI {
    /* ---- Logger: */
    private static final Logger LOG = Logger.getLogger(NotaryAPI.class.getName());

    @ApiMethod(
            name = "searchForNotaries",
            path = "searchForNotaries",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public ArrayList<NotaryGeneralView> showAllNotaries(final User googleUser) throws Exception {

        // ensure authorization
        UserTools.ensureCryptonomicaRegisteredUser(googleUser);
        //

        List<Notary> notaryList = ofy()
                .load()
                .type(Notary.class)
                .list();
        //
        ArrayList<NotaryGeneralView> notaryGeneralViewArrayList =
                new ArrayList<>();

        if (notaryList != null) {
            for (Notary notary : notaryList) {
                Key<CryptonomicaUser> notaryCUkey = Key.create(CryptonomicaUser.class, notary.getId());
                //
                CryptonomicaUser notaryCryptonomicaUser = ofy()
                        .load()
                        .key(notaryCUkey)
                        .now();
                //
                UserProfileGeneralView notaryUserProfileGeneralView =
                        new UserProfileGeneralView(notaryCryptonomicaUser);

                //
                notaryGeneralViewArrayList.add(
                        new NotaryGeneralView(
                                notaryUserProfileGeneralView,
                                notary
                        )
                );
            }
        }
        //
        LOG.warning("notaryGeneralViewArrayList: " + new Gson().toJson(notaryGeneralViewArrayList));
        //
        return notaryGeneralViewArrayList;

    } // end of showAllNotaries()

}
