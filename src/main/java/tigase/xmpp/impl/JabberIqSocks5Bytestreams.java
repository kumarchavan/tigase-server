/*
 * JabberIqSocks5Bytestreams.java
 *
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2013 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
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
 */



package tigase.xmpp.impl;

//~--- non-JDK imports --------------------------------------------------------

import tigase.server.Iq;

//~--- JDK imports ------------------------------------------------------------

import java.util.logging.Logger;

/**
 * XEP-0065: SOCKS5 Bytestreams
 * The class is not abstract in fact. Is has been made abstract artificially
 * to prevent from loading the class.
 *
 *
 * Created: Fri Jan 12 21:37:05 2007
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 * @deprecated This class has been deprecated and replaced with
 * <code>tigase.server.xmppsession.PacketFilter</code> code. The class is left
 * for educational purpose only and should not be used. It may be removed in
 * future releases.
 */
@Deprecated
public abstract class JabberIqSocks5Bytestreams
				extends SimpleForwarder {
	/**
	 * Private logger for class instancess.
	 */
	private static final Logger log = Logger.getLogger(JabberIqSocks5Bytestreams.class
			.getName());
	private static final String     XMLNS    = "http://jabber.org/protocol/bytestreams";
	private static final String     ID       = XMLNS;
	private static final String[][] ELEMENTS = {
		Iq.IQ_QUERY_PATH
	};
	private static final String[]   XMLNSS   = { XMLNS };

	//~--- methods --------------------------------------------------------------

	// Implementation of tigase.xmpp.XMPPImplIfc

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	@Override
	public String id() {
		return ID;
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	@Override
	public String[][] supElementNamePaths() {
		return ELEMENTS;
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	@Override
	public String[] supNamespaces() {
		return XMLNSS;
	}
}


//~ Formatted in Tigase Code Convention on 13/03/12
