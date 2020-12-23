package se.mc.rig.restclient;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import se.mc.rig.restclient.api.CustomerDto;
import se.mc.rig.restclient.api.TokenDto;

//https://auth0.com/docs/flows/call-your-api-using-the-client-credentials-flow

@Component
@Slf4j
public class EvryRestClient {

    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "grant_type=client_credentials";
    private Client client;
    private WebTarget webTarget = null;
    private Client client2;
    private WebTarget webTarget2 = null;
    // private static final String EVRY_HOST = "http://localhost:8090";
    private static final String EVRY_HOST = "https://registryapi.exttest.ilnet.se";
    private static final String EVRY_HOST_TOKEN = "https://registryapi.exttest.ilnet.se";
    private static final String URL_FETCH = "/api/registry/getcustomer";
    private static final String URL_WRITE = "/api/registry/contactinfo";
    private static final String URL_DELETE = "/api/registry/deletecustomer";
    private static final String URL_TOKEN = "/api/auth/token";
    private static long tokenTimestamp;
    private static TokenDto tokenDto;

    @Value("${evry.user}")
    String userName;
    @Value("${evry.psw}")
    String password;

    @PostConstruct
    private void init() {
        if (webTarget == null) {
            client = ClientBuilder.newClient()
                    .register(
                            (ClientRequestFilter) clientRequestContext -> clientRequestContext.getHeaders()
                                    .add(
                                            "Authorization",
                                            format(
                                                    "Basic %s",
                                                    printBase64Binary(
                                                            format("%s:%s", userName, password).getBytes()))));
            webTarget = client.target(EVRY_HOST_TOKEN);
        }
    }

    @PreDestroy
    private void close() {
        client.close();
        client2.close();
    }

    private void initClient2() {
        tokenDto = getToken();
        client2 = ClientBuilder.newClient()
                .register(
                        (ClientRequestFilter) clientRequestContext -> clientRequestContext.getHeaders()
                                .add("Authorization", " Bearer " + tokenDto.getAccess_token()));
        webTarget2 = client2.target(EVRY_HOST);
    }

    private WebTarget getWebTarget2() {
        if (webTarget2 == null || hasExpired()) {
            if (client2 != null) {
                client2.close();
            }
            initClient2();
            tokenTimestamp = currentTimeMillis();
        }
        return webTarget2;
    }

    private boolean hasExpired() {
        return (currentTimeMillis() - tokenTimestamp) > (parseInt(tokenDto.getExpires_in()) * 1000);
    }

    TokenDto getToken() {
        log.debug(EVRY_HOST_TOKEN + URL_TOKEN + "  " + userName + "=" + password);
        Response response = webTarget.path(URL_TOKEN)
                .request(APPLICATION_JSON)
                .post(entity(GRANT_TYPE_CLIENT_CREDENTIALS, APPLICATION_FORM_URLENCODED_TYPE));
        log.debug("Response: {}", response.getStatus());
        TokenDto tokenDto = response.readEntity(TokenDto.class);
        // log.debug("Response: {}", response.readEntity(String.class));
        log.debug("Response: {}", tokenDto.toString());
        return tokenDto;
    }

    Response fetch(CustomerDto customerDto) {
        log.debug(EVRY_HOST + URL_FETCH + "  " + userName + "=" + password);
        log.debug(customerDto.toString());
        Response response = getWebTarget2().path(URL_FETCH)
                .request(APPLICATION_JSON)
                .post(entity(customerDto, APPLICATION_JSON));
        log.debug("Response: {}", response.getStatus());
        return response;
    }

    Response create(CustomerDto customerDto) {
        log.debug(EVRY_HOST + URL_WRITE + "  " + userName + "=" + password);
        log.debug(customerDto.toString());
        Response response = getWebTarget2().path(URL_WRITE)
                .request(APPLICATION_JSON)
                .post(entity(customerDto, APPLICATION_JSON));
        log.debug("Response: {}", response.getStatus());
        return response;
    }

    Response update(CustomerDto customerDto) {
        log.debug(EVRY_HOST + URL_WRITE + "  " + userName + "=" + password);
        log.debug(customerDto.toString());
        Response response = getWebTarget2().path(URL_WRITE)
                .request(APPLICATION_JSON)
                .put(entity(customerDto, APPLICATION_JSON));
        log.debug("Response: {}", response.getStatus());
        return response;
    }

    Response delete(CustomerDto customerDto) {
        log.debug(EVRY_HOST + URL_DELETE + "  " + userName + "=" + password);
        log.debug(customerDto.toString());
        Response response = getWebTarget2().path(URL_DELETE)
                .request(APPLICATION_JSON)
                .post(entity(customerDto, APPLICATION_JSON));
        log.debug("Response: {}", response.getStatus());
        return response;
    }
}
