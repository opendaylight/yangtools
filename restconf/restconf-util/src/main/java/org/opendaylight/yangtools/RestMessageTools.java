/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools;

import com.sun.jersey.api.client.ClientResponse;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.commons.io.IOUtils;
import org.opendaylight.yangtools.draft.Draft01;
import org.opendaylight.yangtools.draft.Draft02;
import org.opendaylight.yangtools.restconf.client.api.RestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestMessageTools {

    public static final String XML = "+xml";
    public static final String JSON = "+json";
    private static final Logger logger = LoggerFactory.getLogger(RestMessageTools.class.toString());

    public RestMessage extractMessage(ClientResponse response){
        RestMessage restMessage = null;
        if (response.getType().equals(Draft01.MediaTypes.API+JSON) ||
                response.getType().equals(Draft02.MediaTypes.API+JSON)){
            //to be implemented
        }
        if (response.getType().equals(Draft01.MediaTypes.API+XML) ||
                response.getType().equals(Draft02.MediaTypes.API+XML)){
            StringWriter writer = new StringWriter();
            try {
                IOUtils.copy(response.getEntityInputStream(), writer, "UTF-8");
            } catch (IOException e) {
                logger.info("Error parsing XML file from response.");
            }
        }
        return restMessage;
    }
}
