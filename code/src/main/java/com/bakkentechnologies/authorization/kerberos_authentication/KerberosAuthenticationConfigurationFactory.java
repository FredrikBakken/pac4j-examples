package com.bakkentechnologies.authorization.kerberos_authentication;

import com.bakkentechnologies.authorization.Authorizer;
import com.bakkentechnologies.authorization.HttpActionAdapter;

import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.kerberos.client.direct.DirectKerberosClient;
import org.pac4j.kerberos.client.indirect.IndirectKerberosClient;
import org.pac4j.kerberos.credentials.KerberosCredentials;
import org.pac4j.kerberos.credentials.authenticator.KerberosAuthenticator;
import org.pac4j.kerberos.credentials.authenticator.SunJaasKerberosTicketValidator;
import org.pac4j.kerberos.profile.KerberosProfile;
import org.springframework.core.io.FileSystemResource;

import spark.TemplateEngine;


public class KerberosAuthenticationConfigurationFactory implements ConfigFactory {
    private final TemplateEngine templateEngine;

    public KerberosAuthenticationConfigurationFactory(final TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public Config build(final Object... parameters) {
        SunJaasKerberosTicketValidator validator = new SunJaasKerberosTicketValidator();
        validator.setServicePrincipal("principal");
        validator.setKeyTabLocation(new FileSystemResource("path"));
        validator.setDebug(true);

        final IndirectKerberosClient kerberosClient = new IndirectKerberosClient(new KerberosAuthenticator(validator));
        
        final Clients clients = new Clients("/callback", kerberosClient);

        final Config config = new Config(clients);
        config.addAuthorizer("custom", new Authorizer());
        config.setHttpActionAdapter(new HttpActionAdapter(templateEngine));

        return config;
    }
}
