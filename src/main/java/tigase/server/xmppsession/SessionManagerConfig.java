/*
 * Tigase Jabber/XMPP Server
* Copyright (C) 2004-2012 "Artur Hefczyc" <artur.hefczyc@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 *
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */

package tigase.server.xmppsession;

//~--- non-JDK imports --------------------------------------------------------

import tigase.osgi.ModulesManagerImpl;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.util.DNSResolver;

import static tigase.conf.Configurable.*;
import tigase.db.RepositoryFactory;
import tigase.xmpp.*;

//~--- JDK imports ------------------------------------------------------------


//~--- classes ----------------------------------------------------------------

/**
 * Describe class SessionManagerConfig here.
 *
 *
 * Created: Tue Oct 24 23:07:57 2006
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public abstract class SessionManagerConfig {

	/** Field description */
	public static final String PLUGINS_PROP_KEY = "plugins";

	/** Field description */
	public static final String PLUGINS_CONF_PROP_KEY = "plugins-conf";

	/** Field description */
	public static final String PLUGINS_CONCURRENCY_PROP_KEY = "plugins-concurrency";

	/** Field description */
	public static final String sessionCloseProcId = "session-close";

	/** Field description */
	public static final String defaultHandlerProcId = "default-handler";

	/** Field description */
	public static final String sessionOpenProcId = "session-open";

	// public static final String ANONYMOUS_PEERS_PROP_KEY = "anonymous-peers";

	/**
	 * List of default plugins loaded by the server. It can be changed later
	 * in config file or at runtime.
	 */
	private static final String[] PLUGINS_NO_REG_PROP_VAL = {
		sessionCloseProcId, sessionOpenProcId, defaultHandlerProcId, "jabber:iq:auth",
		"urn:ietf:params:xml:ns:xmpp-sasl", "urn:ietf:params:xml:ns:xmpp-bind",
		"urn:ietf:params:xml:ns:xmpp-session", "jabber:iq:roster", "jabber:iq:privacy",
		"jabber:iq:version", "http://jabber.org/protocol/stats", "starttls", "vcard-temp",
		"http://jabber.org/protocol/commands", "jabber:iq:private", "urn:xmpp:ping", "presence",

		// "basic-filter",
                "disco", "domain-filter", "zlib", "amp"
	};

	/**
	 * List of plugins loaded when the server is loaded in test mode.
	 * Some plugins like off-line message storage is disabled during tests.
	 */
	private static final String[] PLUGINS_TEST_PROP_VAL = {
		sessionCloseProcId, sessionOpenProcId, defaultHandlerProcId, "jabber:iq:register",
		"jabber:iq:auth", "urn:ietf:params:xml:ns:xmpp-sasl", "urn:ietf:params:xml:ns:xmpp-bind",
		"urn:ietf:params:xml:ns:xmpp-session", "jabber:iq:roster", "jabber:iq:privacy",
		"jabber:iq:version", "http://jabber.org/protocol/stats", "starttls", "vcard-temp",
		"http://jabber.org/protocol/commands", "jabber:iq:private", "urn:xmpp:ping", "presence",

		// "basic-filter",
                "disco", "domain-filter", "zlib", "amp"
	};
	private static final String[] PLUGINS_FULL_PROP_VAL = {
		sessionCloseProcId, sessionOpenProcId, defaultHandlerProcId, "jabber:iq:register",
		"jabber:iq:auth", "urn:ietf:params:xml:ns:xmpp-sasl", "urn:ietf:params:xml:ns:xmpp-bind",
		"urn:ietf:params:xml:ns:xmpp-session", "jabber:iq:roster", "jabber:iq:privacy",
		"jabber:iq:version", "http://jabber.org/protocol/stats", "starttls", "vcard-temp",
		"http://jabber.org/protocol/commands", "jabber:iq:private", "urn:xmpp:ping", "presence",

		// "basic-filter",
                "disco", "domain-filter", "zlib", "amp"
	};
	private static String[] HOSTNAMES_PROP_VAL = { "localhost", "hostname" };
	private static String[] ANONYMOUS_DOMAINS_PROP_VAL = { "localhost", "hostname" };
	private static String[] ADMINS_PROP_VAL = { "admin@localhost", "admin@hostname" };
	private static String[] TRUSTED_PROP_VAL = { "admin@localhost", "admin@hostname" };
	private static String[] CLUSTER_NODES_PROP_VAL = { "sess-man@localhost" };
	protected static final String ADMIN_SCRIPTS_PROP_KEY = "admin-scripts-dir";
	protected static final String ADMIN_SCRIPTS_PROP_VAL = "scripts/admin/";
	protected static final String SKIP_PRIVACY_PROP_KEY = "skip-privacy";
	private static final boolean SKIP_PRIVACY_PROP_VAL = false;
	protected static final String AUTO_CREATE_OFFLINE_USER_PROP_KEY = "offline-user-autocreate";
	protected static final String AUTO_CREATE_OFFLINE_USER_PROP_VAL = "false";
	protected static final String SM_THREADS_POOL_PROP_KEY = "sm-threads-pool";
	protected static final String SM_THREADS_POOL_PROP_VAL = "default";
	protected static final String SM_THREADS_POOL_CUSTOM_PROP_VAL = "custom";

	//~--- get methods ----------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @param props
	 * @param params
	 */
	public static void getDefaults(Map<String, Object> props, Map<String, Object> params) {
		props.put(ADMIN_SCRIPTS_PROP_KEY, ADMIN_SCRIPTS_PROP_VAL);

		boolean full_comps = (params.get(RepositoryFactory.GEN_AUTH_DB) == null)
			|| params.get(RepositoryFactory.GEN_AUTH_DB).toString().equals("mysql")
			|| params.get(RepositoryFactory.GEN_AUTH_DB).toString().equals("pgsql")
			|| params.get(RepositoryFactory.GEN_AUTH_DB).toString().equals("derby")
			|| params.get(RepositoryFactory.GEN_AUTH_DB).toString().equals("tigase-auth");
		LinkedHashSet<String> plugins = new LinkedHashSet<String>(32);

		if ((Boolean) params.get(GEN_TEST)) {
			Collections.addAll(plugins, PLUGINS_TEST_PROP_VAL);
		} else {
			if (full_comps) {

				// Some plugins are not loaded during tests at least until proper
				// test cases are created for them. Sample case is off-line message
				// storage which may impact some test cases.
				Collections.addAll(plugins, PLUGINS_FULL_PROP_VAL);
			} else {
				Collections.addAll(plugins, PLUGINS_NO_REG_PROP_VAL);
			}
		}

		String str_plugins = (String) params.get(GEN_SM_PLUGINS);
		String plugin_concurrency = "";

		if (str_plugins != null) {
			String[] conf_plugins = str_plugins.split(",");

			for (String plugin : conf_plugins) {
				switch (plugin.charAt(0)) {
					case '+' :
						if (addPlugin(plugins, plugin.substring(1))) {
							plugin_concurrency += plugin.substring(1) + ",";
						}

						break;

					case '-' :
						plugins.remove(plugin.substring(1));

						break;

					default :
						if (addPlugin(plugins, plugin)) {
							plugin_concurrency += plugin + ",";
						}
				}
			}
		}

		props.put(PLUGINS_PROP_KEY, plugins.toArray(new String[plugins.size()]));
		props.put(PLUGINS_CONCURRENCY_PROP_KEY, plugin_concurrency);

		String skip_privacy = (String) params.get("--" + SKIP_PRIVACY_PROP_KEY);

		props.put(SKIP_PRIVACY_PROP_KEY, (skip_privacy != null) && skip_privacy.equals("true"));

		if (params.get(GEN_VIRT_HOSTS) != null) {
			HOSTNAMES_PROP_VAL = ((String) params.get(GEN_VIRT_HOSTS)).split(",");
			ANONYMOUS_DOMAINS_PROP_VAL = ((String) params.get(GEN_VIRT_HOSTS)).split(",");
		} else {
			HOSTNAMES_PROP_VAL = DNSResolver.getDefHostNames();
			ANONYMOUS_DOMAINS_PROP_VAL = DNSResolver.getDefHostNames();
		}

//  props.put(HOSTNAMES_PROP_KEY, HOSTNAMES_PROP_VAL);
//  props.put(ANONYMOUS_DOMAINS_PROP_KEY, ANONYMOUS_DOMAINS_PROP_VAL);
//  if (params.get(GEN_ADMINS) != null) {
//    ADMINS_PROP_VAL = ((String)params.get(GEN_ADMINS)).split(",");
//  } else {
//    ADMINS_PROP_VAL = new String[HOSTNAMES_PROP_VAL.length];
//    for (int i = 0; i < ADMINS_PROP_VAL.length; i++) {
//      ADMINS_PROP_VAL[i] = "admin@"+HOSTNAMES_PROP_VAL[i];
//    } // end of for (int i = 0; i < ADMINS_PROP_VAL.length; i++)
//  }
//  props.put(ADMINS_PROP_KEY, ADMINS_PROP_VAL);
		// props.put(ANONYMOUS_PEERS_PROP_KEY, ADMINS_PROP_VAL);
		if (params.get(GEN_TRUSTED) != null) {
			TRUSTED_PROP_VAL = ((String) params.get(GEN_TRUSTED)).split(",");
		} else {
			TRUSTED_PROP_VAL = new String[HOSTNAMES_PROP_VAL.length];

			for (int i = 0; i < TRUSTED_PROP_VAL.length; i++) {
				TRUSTED_PROP_VAL[i] = "admin@" + HOSTNAMES_PROP_VAL[i];
			}    // end of for (int i = 0; i < TRUSTED_PROP_VAL.length; i++)
		}

		props.put(TRUSTED_PROP_KEY, TRUSTED_PROP_VAL);
		props.put(AUTO_CREATE_OFFLINE_USER_PROP_KEY, AUTO_CREATE_OFFLINE_USER_PROP_VAL);

		String sm_threads_pool = SM_THREADS_POOL_PROP_VAL;

		if (params.get("--" + SM_THREADS_POOL_PROP_KEY) != null) {
			sm_threads_pool = (String) params.get("--" + SM_THREADS_POOL_PROP_KEY);
		}

		props.put(SM_THREADS_POOL_PROP_KEY, sm_threads_pool);
	}

        /**
         * Returns list of active plugins with implementation available
         * 
         * @param props
         * @return 
         */
        public static String[] getActivePlugins(Map<String, Object> props) {
                String[] allPlugins = (String[]) props.get(PLUGINS_PROP_KEY);
                List<String> plugins = new ArrayList<String>();
                for (String plugin_id : allPlugins) {
                        // this plugins are not in ModulesManager and not in ProcessorFactory
                        if (sessionCloseProcId.equals(plugin_id) || sessionOpenProcId.equals(plugin_id) 
                                || defaultHandlerProcId.equals(plugin_id)) {
                                plugins.add(plugin_id);
                        }
                        else if (ModulesManagerImpl.getInstance().hasPluginForId(plugin_id)
                                || ProcessorFactory.hasImplementation(plugin_id)) {
                                plugins.add(plugin_id);
                        }
                }
                return plugins.toArray(new String[plugins.size()]);
        }
        
	//~--- methods --------------------------------------------------------------

	private static boolean addPlugin(LinkedHashSet<String> plugins, String plugin) {
		String[] pla = plugin.split("=");

		plugins.add(pla[0]);

		return pla.length > 1;
	}

        /**
         * Get processor instance
         * 
         * @param plug_id
         * @return 
         */
        public static XMPPProcessorIfc getProcessor(String plug_id) {
                XMPPImplIfc proc = getXMPPImplIfc(plug_id);

                if (proc == null) {
                        return ProcessorFactory.getProcessor(plug_id);
                }

                if (proc instanceof XMPPProcessorIfc) {
                        return (XMPPProcessorIfc) proc;
                }

                return null;
        }

        /**
         * Get preprocessor instance
         * 
         * @param plug_id
         * @return 
         */
        public static XMPPPreprocessorIfc getPreprocessor(String plug_id) {
                XMPPImplIfc proc = getXMPPImplIfc(plug_id);

                if (proc == null) {
                        return ProcessorFactory.getPreprocessor(plug_id);
                }

                if (proc instanceof XMPPPreprocessorIfc) {
                        return (XMPPPreprocessorIfc) proc;
                }

                return null;
        }

        /**
         * Get postprocessor instance
         * 
         * @param plug_id
         * @return 
         */
        public static XMPPPostprocessorIfc getPostprocessor(String plug_id) {
                XMPPImplIfc proc = getXMPPImplIfc(plug_id);

                if (proc == null) {
                        return ProcessorFactory.getPostprocessor(plug_id);
                }

                if (proc instanceof XMPPPostprocessorIfc) {
                        return (XMPPPostprocessorIfc) proc;
                }

                return null;
        }

        /**
         * Get packetfilter instance
         * 
         * @param plug_id
         * @return 
         */
        public static XMPPPacketFilterIfc getPacketFilter(String plug_id) {
                XMPPImplIfc proc = getXMPPImplIfc(plug_id);

                if (proc == null) {
                        return ProcessorFactory.getPacketFilter(plug_id);
                }

                if (proc instanceof XMPPPacketFilterIfc) {
                        return (XMPPPacketFilterIfc) proc;
                }

                return null;
        }

        /**
         * Get stoplistener instance
         * 
         * @param plug_id
         * @return 
         */
        public static XMPPStopListenerIfc getStopListener(String plug_id) {
                XMPPImplIfc proc = getXMPPImplIfc(plug_id);

                if (proc == null) {
                        return ProcessorFactory.getStopListener(plug_id);
                }

                if (proc instanceof XMPPStopListenerIfc) {
                        return (XMPPStopListenerIfc) proc;
                }

                return null;
        }
        
        private static XMPPImplIfc getXMPPImplIfc(String plug_id) {
                XMPPImplIfc proc = null;
                
                try {
                        proc = ModulesManagerImpl.getInstance().getPlugin(plug_id);
                } catch (InstantiationException ex) {
                        Logger.getLogger(SessionManagerConfig.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                        Logger.getLogger(SessionManagerConfig.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                return proc;
        }
        
}    // SessionManagerConfig


//~ Formatted in Sun Code Convention


//~ Formatted by Jindent --- http://www.jindent.com
