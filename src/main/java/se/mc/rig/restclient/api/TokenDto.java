package se.mc.rig.restclient.api;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class TokenDto {
    private String access_token;
    private String token_type;
    private String expires_in;
}
