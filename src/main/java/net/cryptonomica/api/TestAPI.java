package net.cryptonomica.api;

import com.google.api.server.spi.auth.EspAuthenticator;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiIssuer;
import com.google.api.server.spi.config.ApiIssuerAudience;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.UnauthorizedException;


/**
 * used example from
 * https://github.com/GoogleCloudPlatform/java-docs-samples/appengine/endpoints-frameworks-v2/backend
 * see:
 * https://cloud.google.com/endpoints/docs/frameworks/java/get-started-frameworks-java
 * <p>
 * [+] - API should be registered in  web.xml (<param-name>services</param-name>)
 * [ ] - API should be loaded in app.js - app.run()
 */
// [START echo_api_annotation]
@Api(
        name = "testAPI", // The api name must match '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        namespace =
        @ApiNamespace(
                ownerDomain = "cryptonomica-server.appspot.com",
                ownerName = "cryptonomica-server.appspot.com",
                packagePath = ""
        ),
        // [START_EXCLUDE]
        issuers = {
                @ApiIssuer(
                        name = "firebase", // TODO: ?
                        issuer = "https://securetoken.google.com/cryptonomica-server",
                        jwksUri = "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com")
        }
        // [END_EXCLUDE]
)
// [END echo_api_annotation]
public class TestAPI {
    /**
     * Echoes the received message back. If n is a non-negative integer, the message is copied that
     * many times in the returned message.
     * <p>
     * Note that name is specified and will override the default name of "{class name}.{method
     * name}". For example, the default is "echo.echo".
     * <p>
     * Note that httpMethod is not specified. This will default to a reasonable HTTP method
     * depending on the API method name. In this case, the HTTP method will default to POST.
     */
    // [START echo_method]
    @ApiMethod(name = "echo")
    public Message echo(Message message, @Named("n") @Nullable Integer n) {
        return doEcho(message, n);
    }
    // [END echo_method]

    /**
     * Echoes the received message back. If n is a non-negative integer, the message is copied that
     * many times in the returned message.
     * <p>
     * Note that name is specified and will override the default name of "{class name}.{method
     * name}". For example, the default is "echo.echo".
     * <p>
     * Note that httpMethod is not specified. This will default to a reasonable HTTP method
     * depending on the API method name. In this case, the HTTP method will default to POST.
     */
    // [START echo_path]
    @ApiMethod(name = "echo_path_parameter", path = "echo/{n}")
    public Message echoPathParameter(Message message, @Named("n") int n) {
        return doEcho(message, n);
    }
    // [END echo_path]

    /**
     * Echoes the received message back. If n is a non-negative integer, the message is copied that
     * many times in the returned message.
     * <p>
     * Note that name is specified and will override the default name of "{class name}.{method
     * name}". For example, the default is "echo.echo".
     * <p>
     * Note that httpMethod is not specified. This will default to a reasonable HTTP method
     * depending on the API method name. In this case, the HTTP method will default to POST.
     */
    // [START echo_api_key]
    @ApiMethod(name = "echo_api_key", path = "echo_api_key", apiKeyRequired = AnnotationBoolean.TRUE)
    public Message echoApiKey(Message message, @Named("n") @Nullable Integer n) {
        return doEcho(message, n);
    }
    // [END echo_api_key]

    private Message doEcho(Message message, Integer n) {
        if (n != null && n >= 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(message.getMessage());
            }
            message.setMessage(sb.toString());
        }
        return message;
    }

    /**
     * Gets the authenticated user's email. If the user is not authenticated, this will return an HTTP
     * 401.
     * <p>
     * Note that name is not specified. This will default to "{class name}.{method name}". For
     * example, the default is "echo.getUserEmail".
     * <p>
     * Note that httpMethod is not required here. Without httpMethod, this will default to GET due
     * to the API method name. httpMethod is added here for example purposes.
     */
    // [START google_id_token_auth]
    @ApiMethod(
            httpMethod = ApiMethod.HttpMethod.GET,
            authenticators = {EspAuthenticator.class},
            audiences = {"YOUR_OAUTH_CLIENT_ID"},
            clientIds = {"YOUR_OAUTH_CLIENT_ID"}
    )
    public Email getUserEmail(User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Email response = new Email();
        response.setEmail(user.getEmail());
        return response;
    }
    // [END google_id_token_auth]

    /**
     * Gets the authenticated user's email. If the user is not authenticated, this will return an HTTP
     * 401.
     * <p>
     * Note that name is not specified. This will default to "{class name}.{method name}". For
     * example, the default is "echo.getUserEmail".
     * <p>
     * Note that httpMethod is not required here. Without httpMethod, this will default to GET due
     * to the API method name. httpMethod is added here for example purposes.
     */
    // [START firebase_auth]
    @ApiMethod(
            path = "firebase_user",
            httpMethod = ApiMethod.HttpMethod.GET,
            authenticators = {EspAuthenticator.class},
            issuerAudiences = {
                    @ApiIssuerAudience(
                            name = "firebase",
//                            audiences = {"YOUR-PROJECT-ID"}
                            audiences = {"cryptonomica-server"}
                    )
            }
    )


    public Email getUserEmailFirebase(User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Email response = new Email();
        response.setEmail(user.getEmail());
        return response;
    }
    // [END firebase_auth]
}

/**
 * The email bean that will be used in the getUserEmail response.
 */
class Email {
    private String email;

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

/**
 * The message bean that will be used in the echo request and response.
 */
class Message {

    private String message;

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

