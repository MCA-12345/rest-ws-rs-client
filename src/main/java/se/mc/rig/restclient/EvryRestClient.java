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
    private static final int THRESHOLD = 500;
    private Client tokenClient;
    private WebTarget tokenWebTarget = null;
    private Client client;
    private WebTarget webTarget = null;
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
        if (tokenWebTarget == null) {
            tokenClient = ClientBuilder.newClient()
                    .register(
                            (ClientRequestFilter) clientRequestContext -> clientRequestContext.getHeaders()
                                    .add(
                                            "Authorization",
                                            format(
                                                    "Basic %s",
                                                    printBase64Binary(
                                                            format("%s:%s", userName, password).getBytes()))));
            tokenWebTarget = tokenClient.target(EVRY_HOST_TOKEN);
        }
    }

    @PreDestroy
    private void close() {
        tokenClient.close();
        client.close();
    }

    private void initClient() {
        tokenDto = getToken();
        client = ClientBuilder.newClient()
                .register(
                        (ClientRequestFilter) clientRequestContext -> clientRequestContext.getHeaders()
                                .add("Authorization", " Bearer " + tokenDto.getAccess_token()));
        webTarget = client.target(EVRY_HOST);
    }

    private WebTarget getWebTarget() {
        if (webTarget == null || hasExpired()) {
            if (client != null) {
                client.close();
            }
            initClient();
            tokenTimestamp = currentTimeMillis();
        }
        return webTarget;
    }

    private boolean hasExpired() {
        log.debug(format("Millis counter: %d  Expires in: %d",currentTimeMillis() - tokenTimestamp + 500, parseInt(tokenDto.getExpires_in()) * 1000) );
        return (currentTimeMillis() - tokenTimestamp + THRESHOLD) > (parseInt(tokenDto.getExpires_in()) * 1000);
    }

    TokenDto getToken() {
        log.debug(EVRY_HOST_TOKEN + URL_TOKEN + "  " + userName + "=" + password);
        Response response = tokenWebTarget.path(URL_TOKEN)
                .request(APPLICATION_JSON)
                .post(entity(GRANT_TYPE_CLIENT_CREDENTIALS, APPLICATION_FORM_URLENCODED_TYPE));
        log.debug("Response: {}", response.getStatus());
        TokenDto newTokenDto = response.readEntity(TokenDto.class);
        // log.debug("Response: {}", response.readEntity(String.class));
        log.debug("Response: {}", newTokenDto.toString());
        return newTokenDto;
    }

    Response fetch(CustomerDto customerDto) {
        log.debug(EVRY_HOST + URL_FETCH + "  " + userName + "=" + password);
        log.debug(customerDto.toString());
        Response response = getWebTarget().path(URL_FETCH)
                .request(APPLICATION_JSON)
                .post(entity(customerDto, APPLICATION_JSON));
        log.debug("Response: {}", response.getStatus());
        return response;
    }

    Response create(CustomerDto customerDto) {
        log.debug(EVRY_HOST + URL_WRITE + "  " + userName + "=" + password);
        log.debug(customerDto.toString());
        Response response = getWebTarget().path(URL_WRITE)
                .request(APPLICATION_JSON)
                .post(entity(customerDto, APPLICATION_JSON));
        log.debug("Response: {}", response.getStatus());
        return response;
    }

    Response update(CustomerDto customerDto) {
        log.debug(EVRY_HOST + URL_WRITE + "  " + userName + "=" + password);
        log.debug(customerDto.toString());
        Response response = getWebTarget().path(URL_WRITE)
                .request(APPLICATION_JSON)
                .put(entity(customerDto, APPLICATION_JSON));
        log.debug("Response: {}", response.getStatus());
        return response;
    }

    Response delete(CustomerDto customerDto) {
        log.debug(EVRY_HOST + URL_DELETE + "  " + userName + "=" + password);
        log.debug(customerDto.toString());
        Response response = getWebTarget().path(URL_DELETE)
                .request(APPLICATION_JSON)
                .post(entity(customerDto, APPLICATION_JSON));
        log.debug("Response: {}", response.getStatus());
        return response;
    }
}
