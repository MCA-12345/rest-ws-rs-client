package se.mc.rig.restclient;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.springframework.stereotype.Component;
import se.mc.rig.restclient.api.CustomerDto;
import se.mc.rig.restclient.api.CustomerResultDto;
import se.mc.rig.restclient.api.WriteResultDto;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

/**
 * Denna klassen kör mot: C:\git-repos\autogiro-notification-mock-service
 */

@Component
public class RestClient {

    private Client client;
    private WebTarget webTarget = null;

    private static final String HOST = "http://localhost:8082";
    private static final String URL_FETCH = "/api/registry/getcustomer/";
    private static final String URL_WRITE = "/api/registry/contactinfo/";

    @PostConstruct
    private void init() {
        if (webTarget == null) {
            client = ClientBuilder.newClient();
            //client.property("jersey.config.client.httpUrlConnection.setMethodWorkaround", true);
            client.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
            webTarget = client.target(HOST);
        }
    }

    CustomerResultDto fetch(CustomerDto customerDto) {
        Response response = webTarget.path(URL_FETCH).request(APPLICATION_JSON)
                .post(entity(CustomerDto.builder().ssn(customerDto.getSsn()).build(), APPLICATION_JSON));
        if (response.getStatus() == OK.value()) {
            return response.readEntity(CustomerResultDto.class);
        } else if (response.getStatus() == NOT_FOUND.value()) {
            return CustomerResultDto.builder().response_code("404").customers(singletonList(CustomerDto.builder().build())).build();
        } else {
            throw new RuntimeException(format(
                    "Error when fetching from notification service. endpoint: %s  Status code: %s",
                    URL_FETCH,
                    response.getStatus()));
        }
    }


    public WriteResultDto updatePATCH(CustomerDto customerDto) {
            Response response = webTarget.path(URL_WRITE).request(APPLICATION_JSON)
                    .build("PATCH", entity(customerDto, APPLICATION_JSON)).invoke();
            if (response.getStatus() == OK.value()) {
                return response.readEntity(WriteResultDto.class);
            } else {
                throw new RuntimeException(format(
                        "Error when fetching from notification service. endpoint: %s  Status code: %s",
                        URL_WRITE,
                        response.getStatus()));
            }

    }


}
