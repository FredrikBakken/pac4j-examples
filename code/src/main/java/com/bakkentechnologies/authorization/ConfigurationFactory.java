package com.bakkentechnologies.authorization;

import com.bakkentechnologies.authorization.Authorizer;
import com.bakkentechnologies.authorization.HttpActionAdapter;

import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;

import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;

import org.pac4j.kerberos.client.direct.DirectKerberosClient;
import org.pac4j.kerberos.client.indirect.IndirectKerberosClient;

import org.pac4j.kerberos.credentials.authenticator.KerberosAuthenticator;
import org.pac4j.kerberos.credentials.authenticator.KerberosTicketValidation;
import org.pac4j.kerberos.credentials.authenticator.KerberosTicketValidator;

import static org.mockito.Mockito.*;

import spark.TemplateEngine;


public class ConfigurationFactory implements ConfigFactory {
    private final TemplateEngine templateEngine;

    public ConfigurationFactory(final TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    private KerberosTicketValidator krbValidator = mock(KerberosTicketValidator.class);

    @Override
    public Config build(final Object... parameters) {
        // Define Kerberos validator
        // TODO: Add validator here...
        // https://www.pac4j.org/docs/clients/kerberos.html

        // Temporary validator (TODO: Remove)
        when(krbValidator.validateTicket(any())).thenReturn(new KerberosTicketValidation("fredrik", null, null, null));

        // Initialize DirectKerberosClient
        final DirectKerberosClient directClient = new DirectKerberosClient(new KerberosAuthenticator(krbValidator));

        // Initialize IndirectKerberosClient
        final IndirectKerberosClient indirectClient = new IndirectKerberosClient(new KerberosAuthenticator(krbValidator));
        indirectClient.setCallbackUrl("/indirect");

        // Initialize FormClient
        final FormClient formClient = new FormClient("/loginForm", new SimpleTestUsernamePasswordAuthenticator());

        // Contain all clients
        final Clients clients = new Clients("/callback", directClient, indirectClient, formClient);

        // Configure clients
        final Config config = new Config(clients);
        config.addAuthorizer("custom", new Authorizer());
        config.setHttpActionAdapter(new HttpActionAdapter(templateEngine));
        return config;
    }
}
