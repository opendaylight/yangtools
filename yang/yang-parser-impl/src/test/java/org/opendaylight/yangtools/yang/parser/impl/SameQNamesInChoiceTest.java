/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URI;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public class SameQNamesInChoiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSameQNameInChoice() throws Exception {
        thrown.expect(YangParseException.class);
        thrown.expectMessage("Choice has two choice case with same qnames");

        SchemaContext context;
        File yangFile = new File(getClass().getResource("/bugs/qnameDuplicity/qname-duplicity.yang").toURI());
        File dependenciesDir = new File(getClass().getResource("/bugs/qnameDuplicity").toURI());
        YangContextParser parser = new YangParserImpl();
        context = parser.parseFile(yangFile, dependenciesDir);

        Module testModule = context.findModuleByNamespace(URI.create("urn:test:qname:duplicity")).iterator().next();
        assertNotNull(testModule);
    }
}
