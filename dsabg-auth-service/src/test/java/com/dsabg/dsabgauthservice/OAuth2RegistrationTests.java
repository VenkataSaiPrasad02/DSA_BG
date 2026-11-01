package com.dsabg.dsabgauthservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OAuth2RegistrationTests {

    @Autowired
    private ClientRegistrationRepository repository;

    private String authorizationUrl(ClientRegistration registration) {
        return UriComponentsBuilder
                .fromUriString(registration.getProviderDetails().getAuthorizationUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", registration.getClientId())
                .queryParam("scope", String.join(" ", registration.getScopes()))
                .queryParam("redirect_uri",
                        "http://localhost:8081/login/oauth2/code/"
                                + registration.getRegistrationId())
                .build()
                .toUriString();
    }

    @Test
    void facebookRegistrationUsesSupportedGraphVersionAndScopes() {
        ClientRegistration facebook =
                repository.findByRegistrationId("facebook");

        assertThat(facebook).isNotNull();

        String authorizationUri =
                facebook.getProviderDetails().getAuthorizationUri();

        assertThat(authorizationUri)
                .as("Facebook authorization endpoint must not use the "
                        + "retired v2.8 Graph API")
                .doesNotContain("v2.8")
                .startsWith("https://www.facebook.com/v");

        assertThat(facebook.getScopes())
                .containsExactlyInAnyOrder("public_profile", "email");

        String url = authorizationUrl(facebook);

        assertThat(url)
                .contains("client_id=" + facebook.getClientId())
                .contains("scope=public_profile")
                .contains("redirect_uri=http://localhost:8081/login/oauth2/code/facebook");

        System.out.println("[oauth-test] facebook authorization URI: "
                + authorizationUri);
        System.out.println("[oauth-test] facebook scopes: "
                + facebook.getScopes());
        System.out.println("[oauth-test] facebook callback: "
                + "http://localhost:8081/login/oauth2/code/facebook");
    }

    @Test
    void googleAndGithubRegistrationsStillPresent() {
        assertThat(repository.findByRegistrationId("google")).isNotNull();
        assertThat(repository.findByRegistrationId("github")).isNotNull();
        assertThat(repository.findByRegistrationId("google").getScopes())
                .contains("openid", "profile", "email");
        assertThat(repository.findByRegistrationId("github").getScopes())
                .contains("read:user", "user:email");
    }
}
