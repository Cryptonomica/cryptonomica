package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.OnlineVerification;
import net.cryptonomica.entities.PGPPublicKeyData;
import net.cryptonomica.returns.StatsView;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries

/**
 * explore on: {sandbox-cryptonomica || cryptonomica-server}.appspot.com/_ah/api/explorer
 * ! - API should be registered in  web.xml (<param-name>services</param-name>)
 * ! - on frontend API should be loaded in app.js - app.run()
 */
@Api(name = "statisticsAPI", // The api name must match '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
        description = "Statistics API")

public class StatisticsAPI {

    /* --- Logger: */
    private static final Logger LOG = Logger.getLogger(StatisticsAPI.class.getName());
    /* --- Gson */
    // private static final Gson GSON = new Gson();


    /* ---------------- Statistics */

    @ApiMethod(
            name = "getStatsAllTime",
            path = "getStatsAllTime",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    public StatsView getStatsAllTime(
            // final User googleUser
    )
    // throws Exception
    {

        /* Check authorization: */
        // CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        StatsView result = new StatsView();

        Integer usersRegisteredTotal = ofy().load().type(CryptonomicaUser.class).count();
        Integer keysUploadedTotal = ofy().load().type(PGPPublicKeyData.class).count();

        Integer keysVerifiedOnlineTotal = ofy().load().type(PGPPublicKeyData.class).filter("verifiedOnline ==", Boolean.TRUE).count();
        Integer keysVerifiedOfflineTotal = ofy().load().type(PGPPublicKeyData.class).filter("verifiedOffline ==", Boolean.TRUE).count();

        result.setUsersRegistered(usersRegisteredTotal);
        result.setKeysUploaded(keysUploadedTotal);
        result.setKeysVerifiedOnline(keysVerifiedOnlineTotal);
        result.setKeysVerifiedOffline(keysVerifiedOfflineTotal);

        return result;

    } // end of: public StatsView getStatsAllTime(

    @ApiMethod(
            name = "getStatsByDate",
            path = "getStatsByDate",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    public StatsView getStatsByDate(
            // final User googleUser,
            final @Named("year") Integer year, // "2015"
            final @Named("month") Integer month, // "3"
            final @Named("day") Integer day // "1"
    )
    // throws Exception
    {

        /* Check authorization: */
        // CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        StatsView result = new StatsView();

        result.setDate(year.toString() + "-" + month.toString() + "-" + day.toString());

        Date start = new Date(year - 1900, month - 1, day, 0, 0, 0);
        Date end = new Date(year - 1900, month - 1, day, 23, 59, 59);

        // see:
        // https://stackoverflow.com/questions/36351108/how-to-filter-on-basis-of-jodatime-in-objectify

        result.setUsersRegistered(
                ofy().load().type(CryptonomicaUser.class)
                        .filter("entityCreatedOn >=", start)
                        .filter("entityCreatedOn <=", end)
                        .count()
        );

        result.setKeysUploaded(
                ofy().load().type(PGPPublicKeyData.class)
                        .filter("entityCreated >=", start)
                        .filter("entityCreated <=", end)
                        .count()
        );

/*
To avoid having to scan the entire index table, the query mechanism relies on
all of a query's potential results being adjacent to one another in the index.
To satisfy this constraint, a single query may not use inequality comparisons
(LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, NOT_EQUAL)
on more than one property across all of its filters
[Source : https://cloud.google.com/appengine/docs/standard/java/datastore/query-restrictions ]
https://stackoverflow.com/questions/21001371/why-gae-datastore-not-support-multiple-inequality-filter-on-different-properties
*/
//
//        result.setDocumentsUploaded(
//                ofy().load().type(OnlineVerification.class)
//                        .filter("entityCreated >=", start)
//                        .filter("entityCreated <=", end)
//                        .filter("verificationDocumentsArray !=", null)
//                        .count()
//        );
//
//        result.setVideosUploaded(
//                ofy().load().type(OnlineVerification.class)
//                        .filter("entityCreated >=", start)
//                        .filter("entityCreated <=", end)
//                        .filter("verificationVideoId !=", null)
//                        .count()
//        );

        result.setPaymentsMade(
                ofy().load().type(OnlineVerification.class)
                        .filter("entityCreated >=", start)
                        .filter("entityCreated <=", end)
                        .filter("paymentMade ==", true)
                        .count()
        );

        result.setKeysVerifiedOnline(ofy().load().type(PGPPublicKeyData.class)
                .filter("verifiedOnline ==", true)
                .filter("entityCreated >=", start)
                .filter("entityCreated <=", end)
                .count()
        );

        result.setKeysVerifiedOffline(
                ofy().load().type(PGPPublicKeyData.class)
                        .filter("verifiedOffline ==", true)
                        .filter("entityCreated >=", start)
                        .filter("entityCreated <=", end)
                        .count()
        );

        return result;

    } // end of:  public StatsView getStatsByDate(


    @ApiMethod(
            name = "getStatsByMonth",
            path = "getStatsByMonth",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    public StatsView getStatsByMonth(
//            final User googleUser,
            final @Named("year") Integer year, // "2015"
            final @Named("month") Integer month // "3"
    )
    //  throws Exception
    {

        /* Check authorization: */
        // CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        StatsView result = new StatsView();

        result.setDate(year.toString() + "-" + month.toString());

        Date start = new Date(year - 1900, month - 1, 1, 0, 0, 0);

        // see:
        // https://stackoverflow.com/questions/21242110/convert-java-util-date-to-java-time-localdate
        LocalDate firstDay = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Integer lastDayOfMonth = firstDay.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();

        Date end = new Date(year - 1900, month - 1, lastDayOfMonth, 23, 59, 59);

        // see:
        // https://stackoverflow.com/questions/36351108/how-to-filter-on-basis-of-jodatime-in-objectify

        result.setUsersRegistered(
                ofy().load().type(CryptonomicaUser.class)
                        .filter("entityCreatedOn >=", start)
                        .filter("entityCreatedOn <=", end)
                        .count()
        );

        result.setKeysUploaded(
                ofy().load().type(PGPPublicKeyData.class)
                        .filter("entityCreated >=", start)
                        .filter("entityCreated <=", end)
                        .count()
        );

/*
To avoid having to scan the entire index table, the query mechanism relies on
all of a query's potential results being adjacent to one another in the index.
To satisfy this constraint, a single query may not use inequality comparisons
(LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, NOT_EQUAL)
on more than one property across all of its filters
[Source : https://cloud.google.com/appengine/docs/standard/java/datastore/query-restrictions ]
https://stackoverflow.com/questions/21001371/why-gae-datastore-not-support-multiple-inequality-filter-on-different-properties
*/
//
//        result.setDocumentsUploaded(
//                ofy().load().type(OnlineVerification.class)
//                        .filter("entityCreated >=", start)
//                        .filter("entityCreated <=", end)
//                        .filter("verificationDocumentsArray !=", null)
//                        .count()
//        );
//
//        result.setVideosUploaded(
//                ofy().load().type(OnlineVerification.class)
//                        .filter("entityCreated >=", start)
//                        .filter("entityCreated <=", end)
//                        .filter("verificationVideoId !=", null)
//                        .count()
//        );

        result.setPaymentsMade(
                ofy().load().type(OnlineVerification.class)
                        .filter("entityCreated >=", start)
                        .filter("entityCreated <=", end)
                        .filter("paymentMade ==", true)
                        .count()
        );

        result.setKeysVerifiedOnline(ofy().load().type(PGPPublicKeyData.class)
                .filter("verifiedOnline ==", true)
                .filter("entityCreated >=", start)
                .filter("entityCreated <=", end)
                .count()
        );

        result.setKeysVerifiedOffline(
                ofy().load().type(PGPPublicKeyData.class)
                        .filter("verifiedOffline ==", true)
                        .filter("entityCreated >=", start)
                        .filter("entityCreated <=", end)
                        .count()
        );

        return result;

    } // end of:  public StatsView getStatsByDate(


    @ApiMethod(
            name = "onlineVerificationsByCountry",
            path = "onlineVerificationsByCountry",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    public HashMap<String, Integer> onlineVerificationsByCountry(
            // final User googleUser
    )
    // throws Exception
    {

        /* Check authorization: */
        // CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        List<String> countryCodes = Arrays.asList(Locale.getISOCountries());
        HashMap<String, Integer> result = new HashMap<>();
        for (int i = 0; i < countryCodes.size(); i++) {
            result.put(countryCodes.get(i).toLowerCase(), 0);
        }

        List<PGPPublicKeyData> keysVerifiedOnlineTotalList = ofy()
                .load().type(PGPPublicKeyData.class)
                .filter("verifiedOnline ==", Boolean.TRUE)
                .list();
        for (int i = 0; i < keysVerifiedOnlineTotalList.size(); i++) {
            String countryCode = null;
            try {
                countryCode = keysVerifiedOnlineTotalList.get(i).getNationality().toLowerCase();
                result.put(countryCode, result.get(countryCode) + 1);
            } catch (Exception e) {
                LOG.severe(e.getMessage());
            }
        }

        return result;

    } // end of: public HashMap<String, Integer> onlineVerificationsByCountry(..

}
