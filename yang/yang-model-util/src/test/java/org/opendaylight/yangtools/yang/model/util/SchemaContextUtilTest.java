/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.google.common.base.Splitter;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SchemaContextUtilTest {
    private static final Splitter SPACE_SPLITTER = Splitter.on(' ');
    private static final URI NAMESPACE = URI.create("abc");

    // The idea is:
    // container baz {
    //     leaf xyzzy {
    //         type leafref;
    //     }
    //     leaf foo {
    //         type string;
    //     }
    //     leaf bar {
    //         type string;
    //     }
    // }
    private static final QName FOO = QName.create(NAMESPACE, "foo");
    private static final QName BAR = QName.create(NAMESPACE, "bar");
    private static final QName BAZ = QName.create(NAMESPACE, "baz");
    private static final QName XYZZY = QName.create(NAMESPACE, "xyzzy");

    @Mock
    private SchemaContext mockSchemaContext;
    @Mock
    private Module mockModule;

    @Mock
    private SchemaNode schemaNode;

    @Before
    public void before() {
        doReturn(Optional.empty()).when(mockSchemaContext).findModule(any(QNameModule.class));
        doReturn(Optional.empty()).when(mockSchemaContext).findDataTreeChild(any(Iterable.class));

        doReturn("test").when(mockModule).getName();
        doReturn("test").when(mockModule).getPrefix();
        doReturn(NAMESPACE).when(mockModule).getNamespace();
        doReturn(QNameModule.create(NAMESPACE)).when(mockModule).getQNameModule();
        doReturn(Optional.empty()).when(mockModule).getRevision();

        doReturn(SchemaPath.create(true, BAZ, XYZZY)).when(schemaNode).getPath();
    }

    @Test
    public void testFindDummyData() {

        QName qname = QName.create("namespace", "localname");
        SchemaPath schemaPath = SchemaPath.create(Collections.singletonList(qname), true);
        assertNull("Should be null. Module TestQName not found",
                SchemaContextUtil.findDataSchemaNode(mockSchemaContext, schemaPath));

        RevisionAwareXPath xpath = new RevisionAwareXPathImpl("/test:bookstore/test:book/test:title", true);
        assertNull("Should be null. Module bookstore not found",
                SchemaContextUtil.findDataSchemaNode(mockSchemaContext, mockModule, xpath));

        SchemaNode int32node = BaseTypes.int32Type();
        RevisionAwareXPath xpathRelative = new RevisionAwareXPathImpl("../prefix", false);
        assertNull("Should be null, Module prefix not found",
                SchemaContextUtil.findDataSchemaNodeForRelativeXPath(
                        mockSchemaContext, mockModule, int32node, xpathRelative));

        assertNull("Should be null. Module TestQName not found",
                SchemaContextUtil.findNodeInSchemaContext(mockSchemaContext, Collections.singleton(qname)));

        assertNull("Should be null.", SchemaContextUtil.findParentModule(mockSchemaContext, int32node));
    }

    @Test
    public void testDeref() {
        RevisionAwareXPath xpath = new RevisionAwareXPathImpl("deref(../foo)/../bar", false);
        assertNull(SchemaContextUtil.findDataSchemaNodeForRelativeXPath(mockSchemaContext, mockModule, schemaNode,
            xpath));
    }
}
