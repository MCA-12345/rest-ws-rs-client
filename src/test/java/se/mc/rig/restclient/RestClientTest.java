package se.mc.rig.restclient;

import static java.lang.String.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.mc.rig.restclient.api.CustomerDto;
import se.mc.rig.restclient.api.CustomerResultDto;
import se.mc.rig.restclient.api.WriteResultDto;

@SpringBootTest
class RestClientTest {

    private static final String SSN = "188512128899";

    @Autowired
    RestClient restClient;

    @Test
    void fetch() {
        CustomerResultDto customerResultDto = restClient.fetch(CustomerDto.builder().ssn(SSN).build());
        assertThat(customerResultDto.getResponse_code()).isIn(valueOf(OK.value()), valueOf(NOT_FOUND.value()));
    }

    @Test
    void update() {
        WriteResultDto writeResultDto =  restClient.updatePATCH(CustomerDto.builder().ssn(SSN).build());
        assertThat(writeResultDto.getResponse_code()).isIn(valueOf(OK.value()));
    }


}