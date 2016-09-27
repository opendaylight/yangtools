/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug6771Test {
    private static final String NS = "http://www.example.com/typedef-bug";
    private static final String REV = "1970-01-01";

    @Test
    public void augmentTest() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6771/augment");
        assertNotNull(context);

        final QName c = QName.create(NS, REV, "container-b");
        final QName l = QName.create(NS, REV, "leaf-container-b");
//        final SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(true, c, l));
//
//        assertTrue(findDataSchemaNode instanceof LeafSchemaNode);
//        final LeafSchemaNode leaf = (LeafSchemaNode) findDataSchemaNode;
//        System.out.println(leaf.getType());

    }

    @Test
    public void groupingTest() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6771/grouping");
        assertNotNull(context);

        final QName r = QName.create(NS, REV, "root");
        final QName c = QName.create(NS, REV, "container-b");
        final QName l = QName.create(NS, REV, "leaf-container-b");
        final SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(true, r, c, l));

        assertTrue(findDataSchemaNode instanceof LeafSchemaNode);
        final LeafSchemaNode leaf = (LeafSchemaNode) findDataSchemaNode;
        System.out.println(leaf.getType());

    }
}
