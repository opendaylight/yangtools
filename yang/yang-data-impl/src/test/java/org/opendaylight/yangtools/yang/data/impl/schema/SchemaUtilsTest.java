/*
 * Copyright (c) 2016 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class SchemaUtilsTest {

    @Test
    public void testFindSchemaNode() throws Exception
    {
        SchemaContext schemaContext = getSchemaContext("/bug7246/foo.yang");
        QName qName1 = createQName("foo","test");
        QName qName2 = createQName("foo","output");
        SchemaPath path = SchemaPath.create(true,qName1,qName2);
        SchemaNode node = SchemaUtils.findParentSchemaOnPath(schemaContext,path);
        Assert.assertTrue(node instanceof DataNodeContainer);
    }

    private SchemaContext getSchemaContext(final String filePath) throws URISyntaxException, ReactorException, FileNotFoundException {
        final InputStream resourceStream = getClass().getResourceAsStream(filePath);
        final YangStatementSourceImpl source = new YangStatementSourceImpl(resourceStream);
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        reactor.addSources(source);
        return reactor.buildEffective();
    }

    private static YangInstanceIdentifier.NodeIdentifier getNodeIdentifier(final String ns, final String name) {
        return YangInstanceIdentifier.NodeIdentifier.create(createQName(ns, name));
    }

    private static QName createQName(final String ns, final String name) {
        return QName.create(ns, "2016-11-30", name);
    }
}
