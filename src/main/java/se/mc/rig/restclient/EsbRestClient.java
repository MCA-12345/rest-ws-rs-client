package se.mc.rig.restclient;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import se.mc.rig.restclient.api.CustomerDto;
import se.mc.rig.restclient.api.CustomerResultDto;

/**
 * Denna klass kör mot: C:\git-repos\autogiro-notification-mock-service Testar om det går att köra update mha av Http
 * PATCH via request.build metod och HttpUrlConnectorProvider.SET_METHOD_WORKAROUND Det funkar med spring boot
 * implementation av api:et men inte med portalens implementation.
 */

@Component
@Slf4j
public class EsbRestClient {

    private Client client;
    private WebTarget webTarget = null;
    private static final String ESB_HOST = "https://esbst.goteborg.se";
    private static final String ESB_URL_FETCH = "evryapi/api/registry/getcustomer/";

    @Value("esb.user")
    String userName;

    @Value("esb.pwd")
    String password;

    @PostConstruct
    private void init() {
        if (webTarget == null) {
            client = ClientBuilder.newClient().register(
                    (ClientRequestFilter) clientRequestContext -> clientRequestContext.getHeaders().add(
                            "Authorization",
                            format(
                                    "Basic %s",
                                    printBase64Binary(format("%s:%s", userName, password).getBytes()))));
            webTarget = client.target(ESB_HOST);
        }
    }

    CustomerResultDto fetch(CustomerDto customerDto) {
        Response response = webTarget.path(ESB_URL_FETCH).request(APPLICATION_JSON)
                .post(entity(CustomerDto.builder().ssn(customerDto.getSsn()).build(), APPLICATION_JSON));
        log.debug("Response: {}", response.getStatus());
        if (response.getStatus() == OK.value()) {
            return response.readEntity(CustomerResultDto.class);
        } else if (response.getStatus() == NOT_FOUND.value()) {
            return CustomerResultDto.builder().response_code("404")
                    .customers(singletonList(CustomerDto.builder().build())).build();
        } else {
            throw new RuntimeException(format(
                    "Error when fetching from notification service. endpoint: %s  Status code: %s",
                    ESB_URL_FETCH,
                    response.getStatus()));
        }
    }

}
