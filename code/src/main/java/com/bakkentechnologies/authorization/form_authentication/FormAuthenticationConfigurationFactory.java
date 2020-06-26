package com.bakkentechnologies.authorization.form_authentication;

import com.bakkentechnologies.authorization.Authorizer;
import com.bakkentechnologies.authorization.HttpActionAdapter;

import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;

import spark.TemplateEngine;


public class FormAuthenticationConfigurationFactory implements ConfigFactory {
    private final TemplateEngine templateEngine;

    public FormAuthenticationConfigurationFactory(final TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public Config build(final Object... parameters) {
        final FormClient formClient = new FormClient("/loginForm", new SimpleTestUsernamePasswordAuthenticator());

        final HeaderClient headerClient = new HeaderClient("Authorization", (credentials, ctx) -> {
            final String token = ((TokenCredentials) credentials).getToken();

            if (CommonHelper.isNotBlank(token)) {
                final CommonProfile profile = new CommonProfile();
                profile.setId(token);
                credentials.setUserProfile(profile);
            }
        });

        final Clients clients = new Clients("/callback", formClient, headerClient);

        final Config config = new Config(clients);
        config.addAuthorizer("custom", new Authorizer());
        config.setHttpActionAdapter(new HttpActionAdapter(templateEngine));

        return config;
    }
}
