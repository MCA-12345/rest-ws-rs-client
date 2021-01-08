package se.mc.rig.restclient;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import se.mc.rig.restclient.api.CustomerDto;
import se.mc.rig.restclient.api.CustomerResultDto;
import se.mc.rig.restclient.api.WriteResultDto;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class EvryRestClientTest {

    public static final String SSN_SWAGGER = "199001011239";
    private static final String SSN_CREATE = "195712165512";

    @Autowired
    EvryRestClient evryRestClient;

    @Test
    @Order(1)
    void getToken() {
        assertThat(evryRestClient.getToken().getExpires_in()).isEqualTo("300");
    }

    @Test
    @Order(2)
    void create() {
        Response response = evryRestClient.create(
                CustomerDto.builder()
                        .ssn(SSN_CREATE)
                        .guid(randomUUID().toString())
                        .msisdn("46738787889")
                        .email("test@test.com")
                        .custid("")
                        .build());
        assertThat(response.getStatus()).isEqualTo(201);
        WriteResultDto writeResultDto = response.readEntity(WriteResultDto.class);
        assertThat(writeResultDto.getResponse_message()).isEqualTo("User Added");
        System.out.println(writeResultDto);
    }

    @Test
    @Order(3)
    void fetch() {
        Response response = evryRestClient
                .fetch(CustomerDto.builder().ssn(SSN_CREATE).guid(randomUUID().toString()).build());
        assertThat(response.getStatus()).isEqualTo(200);
        CustomerResultDto customerResultDto = response.readEntity(CustomerResultDto.class);
        assertThat(customerResultDto.getResponse_message()).isEqualTo("User Matched");
        System.out.println(customerResultDto);
    }

    @Test
    @Order(4)
    void update() {
        Response response = evryRestClient.update(
                CustomerDto.builder()
                        .ssn(SSN_CREATE)
                        .guid(randomUUID().toString())
                        .email("test3@test.com")
                        .msisdn("46738787889")
                        .custid("")
                        .build());
        assertThat(response.getStatus()).isEqualTo(200);
        WriteResultDto writeResultDto = response.readEntity(WriteResultDto.class);
        assertThat(writeResultDto.getResponse_message()).isEqualTo("User Modified");
        System.out.println(writeResultDto);
    }

    @Test
    @Order(5)
    void delete() {
        Response response = evryRestClient
                .delete(CustomerDto.builder().ssn(SSN_CREATE).guid(randomUUID().toString()).custid("").build());
        assertThat(response.getStatus()).isEqualTo(200);
        WriteResultDto writeResultDto = response.readEntity(WriteResultDto.class);
        assertThat(writeResultDto.getResponse_message()).isEqualTo("User Removed");
        System.out.println(writeResultDto);
    }
}