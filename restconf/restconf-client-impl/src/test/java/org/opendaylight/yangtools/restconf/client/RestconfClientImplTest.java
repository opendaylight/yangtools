/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client;

import com.google.common.base.Charsets;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import org.opendaylight.yangtools.restconf.client.api.RestconfClientContext;
import org.opendaylight.yangtools.restconf.client.api.rpc.RpcServiceContext;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static junit.framework.Assert.assertNotNull;

public class RestconfClientImplTest {

    private static final Logger logger = LoggerFactory.getLogger(RestconfClientImplTest.class);
    private static final String restconfUrl = "http://localhost:8080";
    public static final String JSON = "+json";
    public static final String XML = "+xml";
    private  RestconfClientContext restconfClientContext;


//    @Before
    public void setup(){
        URL url = null;
        try {
            url = new URL(restconfUrl);
        } catch (MalformedURLException e) {
            logger.trace("Mallformed URL");
        }

        this.restconfClientContext = new RestconfClientImpl(url);

    }

//    @Test

    public void testGetRpcServices() throws ExecutionException, InterruptedException {
        ListenableFuture future  = restconfClientContext.getRpcServices();
        while (!future.isDone()){
            //noop
        }
        assertNotNull(future.get());
    }

//    @Test
    public void testGetRpcServiceContext(){
        RpcServiceContext ctx = restconfClientContext.getRpcServiceContext(TestRpcItf.class);
        assertNotNull(ctx);
        ((TestRpcItf)ctx.getRpcService()).noop();
    }

    public static String createUri(String prefix, String encodedPart) throws UnsupportedEncodingException {
        return URI.create(prefix + URLEncoder.encode(encodedPart, Charsets.US_ASCII.name()).toString()).toASCIIString();
    }


    public interface TestRpcItf extends RpcService {
        public void noop();
    }
    public class TstDataObject implements DataObject{

        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return null;
        }
    }
}
