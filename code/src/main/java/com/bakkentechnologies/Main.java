package com.bakkentechnologies;

import com.bakkentechnologies.authorization.ConfigurationFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.kerberos.client.direct.DirectKerberosClient;
import org.pac4j.kerberos.client.indirect.IndirectKerberosClient;

import org.pac4j.sparkjava.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.mustache.MustacheTemplateEngine;

import static spark.Spark.*;


public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);
    private final static MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();

    public static void main(String[] args) {
		// Initialize port and HTTPS-encryption
		port(4567);
		secure("deploy/keystore.jks", "secretkey", null, null);

		// Load the authentication configuration factory
        final Config config = new ConfigurationFactory(templateEngine).build();

        get("/", Main::index, templateEngine);
        
		final CallbackRoute callback = new CallbackRoute(config, null, true);
		//callback.setRenewSession(false);
		get("/callback", callback);
		post("/callback", callback);
        
		before("/form", new SecurityFilter(config, "FormClient"));
		before("/direct", new SecurityFilter(config, "DirectKerberosClient"));
        before("/indirect", new SecurityFilter(config, "IndirectKerberosClient"));
		before("/protected", new SecurityFilter(config, null));

		get("/form", Main::protectedIndex, templateEngine);
		get("/direct", Main::protectedIndex, templateEngine);
        get("/indirect", Main::protectedIndex, templateEngine);
        get("/protected", Main::protectedIndex, templateEngine);
        
        get("/loginForm", (rq, rs) -> form(config), templateEngine);

        final LogoutRoute localLogout = new LogoutRoute(config, "/?defaulturlafterlogout");
		localLogout.setDestroySession(true);
		get("/logout", localLogout);

        final LogoutRoute centralLogout = new LogoutRoute(config);
		centralLogout.setDefaultUrl("/?defaulturlafterlogoutafteridp");
		centralLogout.setLogoutUrlPattern("/.*");
		centralLogout.setLocalLogout(false);
		centralLogout.setCentralLogout(true);
		centralLogout.setDestroySession(true);
		get("/centralLogout", centralLogout);

        before("/body", new SecurityFilter(config, "HeaderClient"));
		post("/body", (request, response) -> {
			logger.debug("Body: " + request.body());
			return "done: " + getProfiles(request, response);
		});

		exception(Exception.class, (e, request, response) -> {
			logger.error("Unexpected exception", e);
			response.body(templateEngine.render(new ModelAndView(new HashMap<>(), "error500.mustache")));
		});
    }

    private static ModelAndView index(final Request request, final Response response) {
		final Map map = new HashMap();
		map.put("profiles", getProfiles(request, response));
		final SparkWebContext ctx = new SparkWebContext(request, response);
		map.put("sessionId", ctx.getSessionStore().getOrCreateSessionId(ctx));
		return new ModelAndView(map, "index.mustache");
	}

	private static ModelAndView form(final Config config) {
		final Map map = new HashMap();
		final FormClient formClient = config.getClients().findClient(FormClient.class).get();
		map.put("callbackUrl", formClient.getCallbackUrl());
		return new ModelAndView(map, "loginForm.mustache");
    }

	private static ModelAndView protectedIndex(final Request request, final Response response) {
		final Map map = new HashMap();
		map.put("profiles", getProfiles(request, response));
		return new ModelAndView(map, "protectedIndex.mustache");
	}

	private static List<CommonProfile> getProfiles(final Request request, final Response response) {
		final SparkWebContext context = new SparkWebContext(request, response);
		final ProfileManager manager = new ProfileManager(context);
		return manager.getAll(true);
	}
}
