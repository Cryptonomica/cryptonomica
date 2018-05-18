package net.cryptonomica.service;

import com.google.api.server.spi.response.UnauthorizedException;
import com.googlecode.objectify.Key;
import net.cryptonomica.entities.ApiKey;

import javax.servlet.http.HttpServletRequest;

import static net.cryptonomica.service.OfyService.ofy;

public class ApiKeysService {

    public static ApiKey checkApiKey(
            final HttpServletRequest httpServletRequest,
            String serviceName,
            String apiKeyString
    ) throws UnauthorizedException {

        /* --- check service name */
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = httpServletRequest.getHeader("serviceName");
        }
        if (serviceName == null || serviceName.isEmpty()) {
            throw new UnauthorizedException("serviceName not provided");
        }

        /* --- check apiKeyString */
        if (apiKeyString == null || apiKeyString.isEmpty()) {
            apiKeyString = httpServletRequest.getHeader("apiKeyString");
        }
        if (apiKeyString == null || apiKeyString.isEmpty()) {
            throw new UnauthorizedException("apiKeyString not provided");
        }

        /* --- check authorization */
        ApiKey apiKey = ofy()
                .load()
                .key(Key.create(ApiKey.class, serviceName))
                .now();

        if (apiKey == null || apiKey.getApiKey() == null || apiKey.getApiKey().isEmpty()) {
            throw new UnauthorizedException("No API key registered for this user");
        }

        // The equals() method is case-sensitive
        if (!apiKey.getApiKey().equals(apiKeyString)) {
            throw new UnauthorizedException("invalid API key");
        }

        return apiKey;
    }
}
