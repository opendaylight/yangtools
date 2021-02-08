/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.base.Splitter;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SchemaContextUtilTest {
    public static final Splitter SPACE_SPLITTER = Splitter.on(' ');
    public static final XMLNamespace NAMESPACE = XMLNamespace.of("abc");

    @Mock
    public SchemaContext mockSchemaContext;
    @Mock
    public Module mockModule;
    @Mock
    public SchemaNode schemaNode;

    @Test
    public void testFindDummyData() {
        doReturn(Optional.empty()).when(mockSchemaContext).findModule(any(QNameModule.class));
        doReturn(Optional.empty()).when(mockSchemaContext).findDataTreeChild(any(Iterable.class));

        doReturn("test").when(mockModule).getName();
        doReturn("test").when(mockModule).getPrefix();
        doReturn(QNameModule.create(NAMESPACE)).when(mockModule).getQNameModule();

        QName qname = QName.create("namespace", "localname");
        SchemaPath schemaPath = SchemaPath.create(Collections.singletonList(qname), true);
        assertNull("Should be null. Module TestQName not found",
                SchemaContextUtil.findDataSchemaNode(mockSchemaContext, schemaPath));

        assertNull("Should be null. Module TestQName not found",
                SchemaContextUtil.findNodeInSchemaContext(mockSchemaContext, Collections.singleton(qname)));
    }

    @Test
    public void findParentModuleIllegalArgumentTest() {
        assertThrows(NullPointerException.class,
            () -> SchemaContextUtil.findParentModule(mock(SchemaContext.class), null));
    }

    @Test
    public void findParentModuleIllegalArgumentTest2() {
        doReturn(QName.create("foo", "bar")).when(schemaNode).getQName();
        assertThrows(NullPointerException.class, () -> SchemaContextUtil.findParentModule(null, schemaNode));
    }

    @Test
    public void findDataSchemaNodeIllegalArgumentTest() {
        assertThrows(NullPointerException.class,
            () -> SchemaContextUtil.findDataSchemaNode(mock(SchemaContext.class), (SchemaPath) null));
    }

    @Test
    public void findDataSchemaNodeIllegalArgumentTest2() {
        assertThrows(NullPointerException.class, () -> SchemaContextUtil.findDataSchemaNode(null,
            SchemaPath.create(true, QName.create(XMLNamespace.of("uri:my-module"), Revision.of("2014-10-07"), "foo"))));
    }
}
