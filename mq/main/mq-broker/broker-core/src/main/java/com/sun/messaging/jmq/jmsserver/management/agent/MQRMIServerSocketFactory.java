/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * @(#)MQRMIServerSocketFactory.java	1.9 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.agent;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;

import java.net.ServerSocket;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;

import java.rmi.server.RMISocketFactory;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;

import javax.rmi.ssl.SslRMIServerSocketFactory;

import com.sun.messaging.jmq.jmsserver.Broker;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.config.BrokerConfig;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.util.StringUtil;
import com.sun.messaging.jmq.util.Password;
import com.sun.messaging.jmq.util.log.Logger;

import com.sun.messaging.jmq.jmsserver.net.tls.DefaultTrustManager;
import com.sun.messaging.jmq.jmsserver.tlsutil.KeystoreUtil;

public class MQRMIServerSocketFactory extends SslRMIServerSocketFactory {
    private static SSLServerSocketFactory ssfactory = null;
    private static final Object classlock = new Object();

    private String jmxHostname = null;
    private int backlog = 0;
    private boolean useSSL = false;

    protected static final Logger logger = Globals.getLogger();
    protected static final BrokerResources br = Globals.getBrokerResources();

    public MQRMIServerSocketFactory(String jmxHostname, int backlog, boolean useSSL)  {
        this.jmxHostname = jmxHostname;
        this.backlog = backlog;
        this.useSSL = useSSL;
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        ServerSocket serversocket = null;      

	if (useSSL)  {
            SSLServerSocketFactory ssf =
                (SSLServerSocketFactory)getSSLServerSocketFactory();

            InetSocketAddress endpoint;
	    if (jmxHostname != null && !jmxHostname.equals(Globals.HOSTNAME_ALL)) {
	        /*
	         * Scenario: SSL + multihome
	         */
                InetAddress bindAddr = Globals.getJMXInetAddress();
		endpoint = new InetSocketAddress(bindAddr, port);
                serversocket = (SSLServerSocket)ssf.createServerSocket();

		serversocket.setReuseAddress(true);

	    } else  {
	        /*
	         * Scenario: SSL
	         */
		endpoint = new InetSocketAddress(port);
                serversocket = (SSLServerSocket)ssf.createServerSocket();

		serversocket.setReuseAddress(true);
	    }
            if (Globals.getPoodleFixEnabled()) {
                Globals.applyPoodleFix(serversocket, "MQRMIServerSocketFactory");
            }
            serversocket.bind(endpoint, backlog);
	} else  {
	    if (jmxHostname != null && !jmxHostname.equals(Globals.HOSTNAME_ALL)) {
	        /*
	         * Scenario: multihome
	         */
                InetAddress bindAddr = Globals.getJMXInetAddress();
		InetSocketAddress endpoint = new InetSocketAddress(bindAddr, port);

		serversocket = new ServerSocket();

		serversocket.setReuseAddress(true);

		serversocket.bind(endpoint, backlog);
	    } else  {
		/*
		 * We shouldn't really get here since this socket factory should only be used
		 * for ssl and/or multihome support. For the normal case (no ssl, no multihome),
		 * no special socket factory should be needed.
		 */
		InetSocketAddress endpoint = new InetSocketAddress(port);
		serversocket = new ServerSocket();

		serversocket.setReuseAddress(true);

		serversocket.bind(endpoint, backlog);
	    }
	}

	return (serversocket);
    }

    public String toString()  {
	return ("jmxHostname="
		+ jmxHostname
		+ ",backlog="
		+ backlog
		+ ",useSSL="
		+ useSSL);
    }

    public boolean equals(Object obj)  {
	if (!(obj instanceof MQRMIServerSocketFactory))  {
	    return (false);
	}

	MQRMIServerSocketFactory that = (MQRMIServerSocketFactory)obj;

	if (this.jmxHostname != null)  {
	    if ((that.jmxHostname == null) || !that.jmxHostname.equals(this.jmxHostname))  {
	        return (false);
	    }
	} else  {
	    if (that.jmxHostname != null)  {
	        return (false);
	    }
	}

	if (this.backlog != that.backlog)  {
	    return (false);
	}

	if (this.useSSL != that.useSSL)  {
	    return (false);
	}

	return (true);
    }

    public int hashCode()  {
	return toString().hashCode();
    }

    private static ServerSocketFactory getSSLServerSocketFactory()
	throws IOException {

        synchronized (classlock) {

	  if (ssfactory == null) {

	    // need to get a SSLServerSocketFactory
	    try {
	    
		// set up key manager to do server authentication
		// Don't i18n Strings here.  They are key words
		SSLContext ctx;
		KeyManagerFactory kmf;
		KeyStore ks;		

		String keystore_location = KeystoreUtil.getKeystoreLocation();

		// Got Keystore full filename 

		// Check if the keystore exists.  If not throw exception.
		// This is done first as if the keystore does not exist, then
		// there is no point in going further.
	    	   
		File kf = new File(keystore_location);	
		if (kf.exists()) {
		    // nothing to do for now.		
		} else {
		    throw new IOException(
			br.getKString(BrokerResources.E_KEYSTORE_NOT_EXIST,
					keystore_location));
		}	

		/*
		 *     check if password is in the property file
		 *        if not present, 
		 *            then prompt for password.
		 *
		 * If password is not set by any of the above 2 methods, then
		 * keystore cannot be opened and the program exists by 
		 * throwing an exception stating:
		 * "Keystore was tampered with, or password was incorrect"
		 *
		 */

		// Get Passphrase from property setting 

		String pass_phrase = KeystoreUtil.getKeystorePassword();
	    
		// Got Passphrase. 
 	    
		if (pass_phrase == null) {
		    // In reality we should never reach this stage, but, 
		    // just in case, a check		
		    pass_phrase = "";
		    logger.log(Logger.ERROR, br.getKString(
					BrokerResources.E_PASS_PHRASE_NULL));
		}
	    
		char[] passphrase = pass_phrase.toCharArray();


		// Magic key to select the TLS protocol needed by JSSE
		// do not i18n these key strings.
		ctx = SSLContext.getInstance("TLS");
        try {
            kmf = KeyManagerFactory.getInstance("SunX509");  // Cert type
        } catch (NoSuchAlgorithmException e) {
            String defaultAlg = KeyManagerFactory.getDefaultAlgorithm();
            logger.log(logger.INFO, 
                   br.getKString(br.I_KEYMGRFACTORY_USE_DEFAULT_ALG, 
                                 e.getMessage(),defaultAlg));
            kmf = KeyManagerFactory.getInstance(defaultAlg);
        }

		ks = KeyStore.getInstance("JKS");  // Keystore type

                FileInputStream is =  new FileInputStream(keystore_location);
                try {
		    ks.load(is, passphrase);
                } finally {
                    try {              
                    is.close();
                    } catch (Exception e) {
                    /* ignore */
                    }
                }
		kmf.init(ks, passphrase);
	    
		TrustManager[] tm = new TrustManager[1];
		tm[0] = new DefaultTrustManager();
	    
		// SHA1 random number generator
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");  
	    
		ctx.init(kmf.getKeyManagers(), null, random);

		ssfactory = ctx.getServerSocketFactory();
	    } catch (IOException e) {
        	throw e;
	    } catch (Exception e)  {
		throw new IOException(e.toString());
            }
	  } // if (ssfactory == null)

	  return ssfactory;
	}
    }
}
