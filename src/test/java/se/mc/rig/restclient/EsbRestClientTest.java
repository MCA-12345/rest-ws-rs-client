package se.mc.rig.restclient;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import se.mc.rig.restclient.api.CustomerDto;
import se.mc.rig.restclient.api.WriteResultDto;

@SpringBootTest
class EsbRestClientTest {

    private static final String SSN_CREATE = "199101017880";
    private static final String SSN_SWAGGER = "195712165512";

    @Autowired
    EsbRestClient esbRestClient;

    @Test
    void fetch() {
        Response response = esbRestClient.fetch(CustomerDto.builder().ssn(SSN_SWAGGER).guid(randomUUID().toString()).build());
        System.out.println(response.readEntity(String.class));
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void create() {
        WriteResultDto writeResultDto = esbRestClient.create(
                CustomerDto.builder().guid("123").ssn(SSN_CREATE).email("test@test.com").msisdn("460101223344")
                        .custid("").build());
        assertThat(writeResultDto.getGuid()).isNotEmpty();
    }
}