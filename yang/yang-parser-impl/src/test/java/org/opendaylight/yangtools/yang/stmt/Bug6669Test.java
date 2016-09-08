/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug6669Test {
    private static final String REV = "2016-09-08";
    private static final String FOO_NS = "foo";
    private static final String BAR_NS = "bar";
    private static final QName ROOT = QName.create(FOO_NS, REV, "root");
    private static final QName BAR = QName.create(BAR_NS, REV, "bar");
    private static final QName BAR_1 = QName.create(BAR_NS, REV, "bar1");
    private static final QName BAR_2 = QName.create(BAR_NS, REV, "bar2");
    private static final QName M = QName.create(BAR_NS, REV, "m");
    private static final QName L = QName.create(BAR_NS, REV, "l");

    @Test
    public void testInvalidAugment() throws SourceException, ReactorException, FileNotFoundException,
            URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6669/invalid/test1");
        assertNotNull(context);

        final SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, ROOT, BAR, BAR_1, M));
        assertNull(findDataSchemaNode);
    }

    @Test
    public void testInvalidAugment2() throws SourceException, ReactorException, FileNotFoundException,
            URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6669/invalid/test2");
        assertNotNull(context);

        final SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, ROOT, BAR, BAR_1, BAR_2, M));
        assertNull(findDataSchemaNode);
    }

    @Test
    public void testInvalidAugment3() throws SourceException, ReactorException, FileNotFoundException,
            URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6669/invalid/test3");
        assertNotNull(context);

        final SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, ROOT, BAR, BAR_1, BAR_2, L));
        assertNull(findDataSchemaNode);
    }

    @Test
    public void testValidAugment() throws SourceException, ReactorException, FileNotFoundException, URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6669/valid/test1");
        assertNotNull(context);

        final SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, ROOT, BAR, BAR_1, M));
        assertTrue(findDataSchemaNode instanceof LeafSchemaNode);
    }

    @Test
    public void testValidAugment2() throws SourceException, ReactorException, FileNotFoundException, URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6669/valid/test2");
        assertNotNull(context);

        final SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, ROOT, BAR, BAR_1, BAR_2, M));
        assertTrue(findDataSchemaNode instanceof LeafSchemaNode);
    }

    @Test
    public void testValidAugment3() throws SourceException, ReactorException, FileNotFoundException, URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6669/valid/test3");
        assertNotNull(context);

        final SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, ROOT, BAR, BAR_1, BAR_2, L));
        assertTrue(findDataSchemaNode instanceof ListSchemaNode);
    }
}
