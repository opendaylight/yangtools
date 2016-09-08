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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug5335 {
    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String REV = "2016-03-04";

    private static final QName ROOT = QName.create(FOO, REV, "root");
    private static final QName PRESENCE_CONTAINER_F = QName.create(FOO, REV, "presence-container");
    private static final QName NON_PRESENCE_CONTAINER_F = QName.create(FOO, REV, "non-presence-container");
    private static final QName MANDATORY_LEAF_F = QName.create(FOO, REV, "mandatory-leaf");
    private static final QName PRESENCE_CONTAINER_B = QName.create(BAR, REV, "presence-container");
    private static final QName NON_PRESENCE_CONTAINER_B = QName.create(BAR, REV, "non-presence-container");
    private static final QName MANDATORY_LEAF_B = QName.create(BAR, REV, "mandatory-leaf");

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    @Before
    public void setUp() throws UnsupportedEncodingException {
        System.setOut(new PrintStream(output, true, "UTF-8"));
    }

    @After
    public void cleanUp() {
        System.setOut(System.out);
    }

    @Test
    public void incorrectTest1() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/incorrect/case-1");
        assertNotNull(context);

        final SchemaPath schemaPath = SchemaPath.create(true, ROOT, NON_PRESENCE_CONTAINER_B, MANDATORY_LEAF_B);
        final SchemaNode mandatoryLeaf = SchemaContextUtil.findDataSchemaNode(context, schemaPath);
        assertNull(mandatoryLeaf);

        final String testLog = output.toString();
        assertTrue(testLog
                .contains("An augment cannot add node 'mandatory-leaf' because it is mandatory and in module different than target"));
    }

    @Test
    public void incorrectTest2() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/incorrect/case-2");
        assertNotNull(context);

        final SchemaPath schemaPath = SchemaPath.create(true, ROOT, PRESENCE_CONTAINER_F, MANDATORY_LEAF_B);
        final SchemaNode mandatoryLeaf = SchemaContextUtil.findDataSchemaNode(context, schemaPath);
        assertNull(mandatoryLeaf);

        final String testLog = output.toString();
        assertTrue(testLog
                .contains("An augment cannot add node 'mandatory-leaf' because it is mandatory and in module different than target"));
    }

    @Test
    public void incorrectTest3() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/incorrect/case-2");
        assertNotNull(context);

        final SchemaPath schemaPath = SchemaPath.create(true, ROOT, PRESENCE_CONTAINER_F, NON_PRESENCE_CONTAINER_B,
                MANDATORY_LEAF_B);
        final SchemaNode mandatoryLeaf = SchemaContextUtil.findDataSchemaNode(context, schemaPath);
        assertNull(mandatoryLeaf);

        final String testLog = output.toString();
        assertTrue(testLog
                .contains("An augment cannot add node 'mandatory-leaf' because it is mandatory and in module different than target"));
    }

    @Test
    public void correctTest1() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/correct/case-1");
        assertNotNull(context);

        final SchemaPath schemaPath = SchemaPath.create(true, ROOT, PRESENCE_CONTAINER_B, MANDATORY_LEAF_B);
        final SchemaNode mandatoryLeaf = SchemaContextUtil.findDataSchemaNode(context, schemaPath);
        assertTrue(mandatoryLeaf instanceof LeafSchemaNode);
    }

    @Test
    public void correctTest2() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/correct/case-2");
        assertNotNull(context);

        final SchemaPath schemaPath = SchemaPath.create(true, ROOT, PRESENCE_CONTAINER_B, NON_PRESENCE_CONTAINER_B,
                MANDATORY_LEAF_B);
        final SchemaNode mandatoryLeaf = SchemaContextUtil.findDataSchemaNode(context, schemaPath);
        assertTrue(mandatoryLeaf instanceof LeafSchemaNode);
    }

    @Test
    public void correctTest3() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/correct/case-3");
        assertNotNull(context);

        final SchemaPath schemaPath = SchemaPath.create(true, ROOT, PRESENCE_CONTAINER_B, NON_PRESENCE_CONTAINER_B,
                MANDATORY_LEAF_B);
        final SchemaNode mandatoryLeaf = SchemaContextUtil.findDataSchemaNode(context, schemaPath);
        assertTrue(mandatoryLeaf instanceof LeafSchemaNode);
    }

    @Test
    public void correctTest4() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/correct/case-4");
        assertNotNull(context);

        final SchemaPath schemaPath = SchemaPath.create(true, ROOT, NON_PRESENCE_CONTAINER_F, MANDATORY_LEAF_F);
        final SchemaNode mandatoryLeaf = SchemaContextUtil.findDataSchemaNode(context, schemaPath);
        assertTrue(mandatoryLeaf instanceof LeafSchemaNode);
    }
}
