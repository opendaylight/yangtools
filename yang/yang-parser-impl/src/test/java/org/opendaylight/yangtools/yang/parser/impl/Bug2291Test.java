/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import java.io.File;
import java.util.Arrays;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;

public class Bug2291Test {

    @Test
    public void testRevisionWithExt() throws Exception {
        File extdef = new File(getClass().getResource("/bugs/bug2291/bug2291-ext.yang").toURI());
        File bug = new File(getClass().getResource("/bugs/bug2291/bug2291.yang").toURI());
        File inet = new File(getClass().getResource("/ietf/ietf-inet-types@2010-09-24.yang").toURI());
        YangContextParser parser = new YangParserImpl();
        parser.parseFiles(Arrays.asList(extdef, bug, inet));
    }

}
