package net.cryptonomica.service;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import net.cryptonomica.entities.*;
import java.util.logging.Logger;

/**
 * Custom Objectify Service that this application should use.
 */
public class OfyService {

     private static final Logger LOG = Logger.getLogger(OfyService.class.getName());

    // This static block ensure the entity registration.
    // see:
    // https://github.com/objectify/objectify/wiki/BestPractices
    static {
        // see: http://stackoverflow.com/questions/28140884/503-error-objectifyservice-noclassdeffounderror
        try{factory().register(AppSettings.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(Arbitrator.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(ArbitratorLicence.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(AuthorityToRepresent.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(Company.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(CryptonomicaOfficer.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(CryptonomicaOfficerLicence.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(CryptonomicaUser.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(ImageData.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(Invitation.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(Lawyer.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(LawyerLicence.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(Licence.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(Login.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(Notary.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(NotaryLicence.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(PaymentData.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(PGPPublicKeyData.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(Verification.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(OnlineVerification.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(VideoUploadKey.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(VerificationVideo.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(VerificationDocumentsUploadKey.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(VerificationDocument.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(PhoneVerification.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(StripePaymentForKeyVerification.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(PromoCode.class);}catch (Exception e){LOG.warning(e.getMessage());}
        try{factory().register(ApiKey.class);}catch (Exception e){LOG.warning(e.getMessage());}
    }

    // Use this static method for getting the Objectify service factory.
    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }

    /**
     * Use this static method for getting the Objectify service object in order
     * to make sure the above static block is executed before using Objectify.
     *
     * @return Objectify service object.
     */
    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }
}
