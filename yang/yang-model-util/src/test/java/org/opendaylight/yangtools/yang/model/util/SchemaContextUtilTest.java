/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class SchemaContextUtilTest {
    @Mock private SchemaContext mockSchemaContext;
    @Mock private Module mockModule;

    @Test
    public void testFindDummyData() {
        MockitoAnnotations.initMocks(this);

        QName qName = QName.create("TestQName");
        SchemaPath schemaPath = SchemaPath.create(Collections.singletonList(qName), true);
        assertEquals("Should be null. Module TestQName not found", null,
                SchemaContextUtil.findDataSchemaNode(mockSchemaContext, schemaPath));

        RevisionAwareXPath xPath = new RevisionAwareXPathImpl("/bookstore/book/title", true);
        assertEquals("Should be null. Module bookstore not found", null,
                SchemaContextUtil.findDataSchemaNode(mockSchemaContext, mockModule, xPath));

        SchemaNode schemaNode = Int32.getInstance();
        RevisionAwareXPath xPathRelative = new RevisionAwareXPathImpl("../prefix", false);
        assertEquals("Should be null, Module prefix not found", null,
                SchemaContextUtil.findDataSchemaNodeForRelativeXPath(
                        mockSchemaContext, mockModule, schemaNode, xPathRelative));

        assertEquals("Should be null. Module TestQName not found", null,
                SchemaContextUtil.findNodeInSchemaContext(mockSchemaContext, Collections.singleton(qName)));

        assertEquals("Should be null.", null, SchemaContextUtil.findParentModule(mockSchemaContext, schemaNode));
    }
}