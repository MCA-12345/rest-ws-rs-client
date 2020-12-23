package se.mc.rig.restclient;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static org.springframework.http.HttpStatus.CREATED;

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
import se.mc.rig.restclient.api.WriteResultDto;

@Component
@Slf4j
public class EsbRestClient {

    private Client client;
    private WebTarget webTarget = null;
    //private static final String ESB_HOST = "http://localhost:8090";
    private static final String ESB_HOST = "https://esbst.goteborg.se";
    private static final String ESB_URL_FETCH = "/evryapi/api/registry/getcustomer";
    private static final String ESB_URL_WRITE = "/evryapi/api/registry/contactinfo";

    @Value("${esb.user}")
    String userName;

    @Value("${esb.pwd}")
    String password;

    @PostConstruct
    private void init() {
        if (webTarget == null) {
            client = ClientBuilder.newClient().register(
                    (ClientRequestFilter) clientRequestContext -> clientRequestContext.getHeaders().add(
                            "Authorization",
                            format("Basic %s", printBase64Binary(format("%s:%s", userName, password).getBytes()))));
            webTarget = client.target(ESB_HOST);
        }
    }

    Response fetch(CustomerDto customerDto) {
        log.debug(ESB_HOST + ESB_URL_FETCH + "  " + userName + "=" + password);
        log.debug(customerDto.toString());
        Response response = webTarget.path(ESB_URL_FETCH).request(APPLICATION_JSON)
                .post(entity(customerDto, APPLICATION_JSON));
        log.debug("Response: {}", response.getStatus());
        return response;
    }

    WriteResultDto create(CustomerDto customerDto) {
        log.debug(ESB_HOST + ESB_URL_WRITE + "  " + userName + "=" + password);
        log.debug(customerDto.toString());
        Response response = webTarget.path(ESB_URL_WRITE).request(APPLICATION_JSON)
                .post(entity(customerDto, APPLICATION_JSON));
        if (response.getStatus() == CREATED.value()) {
            return response.readEntity(WriteResultDto.class);
        } else {
            System.out.println(response.readEntity(String.class));
            throw new RuntimeException(format(
                    "Error when creating in notification service. Host: %s endpoint: %s. customerDto: %s Status code: %s",
                    ESB_HOST,
                    ESB_URL_WRITE,
                    customerDto,
                    response.getStatus()));
        }
    }
}
