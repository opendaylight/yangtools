/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SchemaContextUtilTest {
    public static final Splitter SPACE_SPLITTER = Splitter.on(' ');
    public static final URI NAMESPACE = URI.create("abc");

    @Mock
    public SchemaContext mockSchemaContext;
    @Mock
    public Module mockModule;
    @Mock
    public SchemaNode schemaNode;

    @Before
    public void before() {
        doReturn(Optional.empty()).when(mockSchemaContext).findModule(any(QNameModule.class));
        doReturn(Optional.empty()).when(mockSchemaContext).findDataTreeChild(any(Iterable.class));

        doReturn("test").when(mockModule).getName();
        doReturn("test").when(mockModule).getPrefix();
        doReturn(NAMESPACE).when(mockModule).getNamespace();
        doReturn(QNameModule.create(NAMESPACE)).when(mockModule).getQNameModule();
        doReturn(Optional.empty()).when(mockModule).getRevision();
    }

    @Test
    public void testFindDummyData() {

        QName qname = QName.create("namespace", "localname");
        SchemaPath schemaPath = SchemaPath.create(Collections.singletonList(qname), true);
        assertNull("Should be null. Module TestQName not found",
                SchemaContextUtil.findDataSchemaNode(mockSchemaContext, schemaPath));

        PathExpression xpath = new PathExpressionImpl("/test:bookstore/test:book/test:title", true);
        assertNull("Should be null. Module bookstore not found",
                SchemaContextUtil.findDataSchemaNode(mockSchemaContext, mockModule, xpath));


        final PathExpression xPath = new PathExpressionImpl("/bookstore/book/title", true);
        assertEquals("Should be null. Module bookstore not found", null,
                SchemaContextUtil.findDataSchemaNode(mockSchemaContext, mockModule, xPath));

        SchemaNode int32node = BaseTypes.int32Type();
        PathExpression xpathRelative = new PathExpressionImpl("../prefix", false);
        assertNull("Should be null, Module prefix not found",
                SchemaContextUtil.findDataSchemaNodeForRelativeXPath(
                        mockSchemaContext, mockModule, int32node, xpathRelative));

        assertNull("Should be null. Module TestQName not found",
                SchemaContextUtil.findNodeInSchemaContext(mockSchemaContext, Collections.singleton(qname)));

        assertNull("Should be null.", SchemaContextUtil.findParentModule(mockSchemaContext, int32node));
    }

    @Test
    public void findDataSchemaNodeFromXPathIllegalArgumentTest() {
        assertThrows(NullPointerException.class,
            () -> SchemaContextUtil.findDataSchemaNode(mock(SchemaContext.class), mock(Module.class), null));
    }

    @Test
    public void findDataSchemaNodeFromXPathIllegalArgumentTest2() {
        final SchemaContext mockContext = mock(SchemaContext.class);
        final PathExpression xpath = new PathExpressionImpl("my:my-grouping/my:my-leaf-in-gouping2", true);

        assertThrows(NullPointerException.class, () -> SchemaContextUtil.findDataSchemaNode(mockContext, null, xpath));
    }

    @Test
    public void findDataSchemaNodeFromXPathIllegalArgumentTest3() {
        final Module module = mock(Module.class);
        final PathExpression xpath = new PathExpressionImpl("my:my-grouping/my:my-leaf-in-gouping2", true);

        assertThrows(NullPointerException.class, () -> SchemaContextUtil.findDataSchemaNode(null, module, xpath));
    }

    @Test
    public void findDataSchemaNodeFromXPathIllegalArgumentTest4() {
        final SchemaContext mockContext = mock(SchemaContext.class);
        final Module module = mock(Module.class);
        final PathExpression xpath = new PathExpressionImpl("my:my-grouping[@con='NULL']/my:my-leaf-in-gouping2", true);

        assertThrows(IllegalArgumentException.class,
            () -> SchemaContextUtil.findDataSchemaNode(mockContext, module, xpath));
    }

    @Test
    public void findParentModuleIllegalArgumentTest() {
        assertThrows(NullPointerException.class,
            () -> SchemaContextUtil.findParentModule(mock(SchemaContext.class), null));
    }

    @Test
    public void findParentModuleIllegalArgumentTest2() {
        doReturn(SchemaPath.create(true, QName.create("foo", "bar"))).when(schemaNode).getPath();
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
            SchemaPath.create(true, QName.create(URI.create("uri:my-module"), Revision.of("2014-10-07"), "foo"))));
    }

    @Test
    public void findDataSchemaNodeFromXPathNullTest2() {
        final SchemaContext mockContext = mock(SchemaContext.class);
        final Module module = mock(Module.class);
        final PathExpression xpath = new PathExpressionImpl("my:my-grouping/my:my-leaf-in-gouping2", false);

        assertNull(SchemaContextUtil.findDataSchemaNode(mockContext, module, xpath));
    }

    @Test
    public void testNormalizeXPath() {
        assertNormalizedPath(0, ImmutableList.of(""), "");
        assertNormalizedPath(0, ImmutableList.of("a"), "a");
        assertNormalizedPath(0, ImmutableList.of("a", "b"), "a b");
        assertNormalizedPath(1, ImmutableList.of("..", "b"), ".. b");
        assertNormalizedPath(0, ImmutableList.of(), "a ..");
        assertNormalizedPath(0, ImmutableList.of("b"), "a .. b");
        assertNormalizedPath(2, ImmutableList.of("..", "..", "a", "c"), ".. .. a b .. c");
        assertNormalizedPath(3, ImmutableList.of("..", "..", "..", "b"), ".. .. a .. .. b");
    }

    private static void assertNormalizedPath(final int expectedLead, final List<String> expectedList,
            final String input) {
        final List<String> list = new ArrayList<>(SPACE_SPLITTER.splitToList(input));
        assertEquals(expectedLead, SchemaContextUtil.normalizeXPath(list));
        assertEquals(expectedList, list);
    }
}
