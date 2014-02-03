/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yangtools.restconf.rev140114.TestModuleService;
import org.opendaylight.yangtools.restconf.client.api.RestconfClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestconfClientImplTest {

    private static final Logger logger = LoggerFactory.getLogger(RestconfClientImplTest.class);
    private static final String restconfUrl = "http://localhost:8080";
    public static final String JSON = "+json";
    public static final String XML = "+xml";
    private  RestconfClientContext restconfClientContext;




    @Test
    public void testGetRpcServiceContext() throws ExecutionException, InterruptedException {
        URI uri = null;
        try {
            uri = new URL(restconfUrl).toURI();
        } catch (URISyntaxException e) {
            logger.trace("Error in URI syntax {}",e.getMessage(),e);
        } catch (MalformedURLException e) {
            logger.trace("Malformed URL :",restconfUrl,e);
        }
        TestModuleService proxiedService = BindingToRestRpc.getProxy(TestModuleService.class,uri);
    }

}
