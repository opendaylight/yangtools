/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.TestUtils;

@RunWith(MockitoJUnitRunner.class)
public class SchemaContextUtilTest {
    @Mock
    private SchemaContext mockSchemaContext;
    @Mock
    private Module mockModule;

    @Test
    public void testFindDummyData() {
        doReturn(Optional.empty()).when(mockSchemaContext).findModule(any(QNameModule.class));
        doReturn(Optional.empty()).when(mockSchemaContext).findDataTreeChild(any(Iterable.class));
        doReturn(URI.create("dummy")).when(mockModule).getNamespace();
        doReturn(Optional.empty()).when(mockModule).getRevision();

        final QName qName = QName.create("dummy", "TestQName");
        final SchemaPath schemaPath = SchemaPath.create(Collections.singletonList(qName), true);
        assertEquals("Should be null. Module TestQName not found", null,
                SchemaContextUtil.findDataSchemaNode(mockSchemaContext, schemaPath));

        final RevisionAwareXPath xPath = new RevisionAwareXPathImpl("/bookstore/book/title", true);
        assertEquals("Should be null. Module bookstore not found", null,
                SchemaContextUtil.findDataSchemaNode(mockSchemaContext, mockModule, xPath));

        final SchemaNode schemaNode = BaseTypes.int32Type();
        final RevisionAwareXPath xPathRelative = new RevisionAwareXPathImpl("../prefix", false);
        assertEquals("Should be null, Module prefix not found", null,
                SchemaContextUtil.findDataSchemaNodeForRelativeXPath(mockSchemaContext, mockModule, schemaNode,
                        xPathRelative));

        assertEquals("Should be null. Module TestQName not found", null,
                SchemaContextUtil.findNodeInSchemaContext(mockSchemaContext, Collections.singleton(qName)));

        assertEquals("Should be null.", null, SchemaContextUtil.findParentModule(mockSchemaContext, schemaNode));
    }

    @Test
    public void findNodeInSchemaContextTest() throws URISyntaxException, IOException, YangSyntaxErrorException,
            ReactorException {

        final SchemaContext context = TestUtils.parseYangSources("/schema-context-util-test");

        final Module myModule = context.findModule(new URI("uri:my-module"),Revision.of("2014-10-07")).get();

        SchemaNode testNode = ((ContainerSchemaNode) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-container"));

        SchemaPath path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-leaf-in-container"));
        SchemaNode foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        RpcDefinition rpc = getRpcByName(myModule, "my-rpc");
        testNode = rpc.getInput().getDataChildByName(QName.create(myModule.getQNameModule(), "my-input-leaf"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "input"),
                QName.create(myModule.getQNameModule(), "my-input-leaf"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        rpc = getRpcByName(myModule, "my-rpc");
        testNode = rpc.getOutput().getDataChildByName(QName.create(myModule.getQNameModule(), "my-output-leaf"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "output"),
                QName.create(myModule.getQNameModule(), "my-output-leaf"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        final NotificationDefinition notification = myModule.getNotifications().iterator().next();
        testNode = notification.getDataChildByName(QName.create(myModule.getQNameModule(), "my-notification-leaf"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-notification"),
                QName.create(myModule.getQNameModule(), "my-notification-leaf"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        final GroupingDefinition grouping = getGroupingByName(myModule, "my-grouping");
        testNode = ((ContainerSchemaNode) grouping.getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-container-in-grouping"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-grouping"),
                QName.create(myModule.getQNameModule(), "my-container-in-grouping"),
                QName.create(myModule.getQNameModule(), "my-leaf-in-grouping"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((ChoiceSchemaNode) myModule
                .getDataChildByName(QName.create(myModule.getQNameModule(), "my-choice")))
                .findCaseNodes("one").iterator().next()
                .getDataChildByName(QName.create(myModule.getQNameModule(), "my-choice-leaf-one"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-choice"),
                QName.create(myModule.getQNameModule(), "one"),
                QName.create(myModule.getQNameModule(), "my-choice-leaf-one"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        ListSchemaNode listNode = (ListSchemaNode) ((ContainerSchemaNode) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-list"));

        testNode = listNode.getDataChildByName(QName.create(myModule.getQNameModule(), "my-leaf-in-list"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-list"),
                QName.create(myModule.getQNameModule(), "my-leaf-in-list"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        listNode = (ListSchemaNode) ((ContainerSchemaNode) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-list"));

        testNode = listNode.getDataChildByName(QName.create(myModule.getQNameModule(), "my-leaf-list-in-list"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-list"),
                QName.create(myModule.getQNameModule(), "my-leaf-list-in-list"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

    }

    @Test
    public void findNodeInSchemaContextTest2() throws URISyntaxException, IOException, YangSyntaxErrorException,
            ReactorException {

        final SchemaContext context = TestUtils.parseYangSources("/schema-context-util-test");

        final Module myModule = context.findModule(new URI("uri:my-module"), Revision.of("2014-10-07")).get();

        SchemaNode testNode = ((ContainerSchemaNode) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-not-in-container"));

        SchemaPath path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-leaf-not-in-container"));
        SchemaNode foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);

        final RpcDefinition rpc = getRpcByName(myModule, "my-rpc");
        testNode = rpc.getInput().getDataChildByName(QName.create(myModule.getQNameModule(), "no-input-leaf"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "input"),
                QName.create(myModule.getQNameModule(), "no-input-leaf"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);

        final NotificationDefinition notification = myModule.getNotifications().iterator().next();
        testNode = notification.getDataChildByName(QName.create(myModule.getQNameModule(), "no-notification-leaf"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-notification"),
                QName.create(myModule.getQNameModule(), "no-notification-leaf"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);

        final GroupingDefinition grouping = getGroupingByName(myModule, "my-grouping");
        testNode = ((ContainerSchemaNode) grouping.getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-container-in-grouping"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "no-leaf-in-grouping"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-grouping"),
                QName.create(myModule.getQNameModule(), "my-container-in-grouping"),
                QName.create(myModule.getQNameModule(), "no-leaf-in-grouping"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);

        testNode = ((ChoiceSchemaNode) myModule
                .getDataChildByName(QName.create(myModule.getQNameModule(), "my-choice")))
                .findCaseNodes("one").iterator().next()
                .getDataChildByName(QName.create(myModule.getQNameModule(), "no-choice-leaf"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-choice"),
                QName.create(myModule.getQNameModule(), "one"),
                QName.create(myModule.getQNameModule(), "no-choice-leaf"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);

        ListSchemaNode listNode = (ListSchemaNode) ((ContainerSchemaNode) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-list"));

        testNode = listNode.getDataChildByName(QName.create(myModule.getQNameModule(), "no-leaf-in-list"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-list"),
                QName.create(myModule.getQNameModule(), "no-leaf-in-list"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);

        listNode = (ListSchemaNode) ((ContainerSchemaNode) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-list"));

        testNode = listNode.getDataChildByName(QName.create(myModule.getQNameModule(), "no-leaf-list-in-list"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-list"),
                QName.create(myModule.getQNameModule(), "no-leaf-list-in-list"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);

    }

    @Test
    public void findNodeInSchemaContextTest3() throws URISyntaxException, IOException, YangSyntaxErrorException,
            ReactorException {

        final SchemaContext context = TestUtils.parseYangSources("/schema-context-util-test");

        final Module myModule = context.findModule(new URI("uri:my-module"), Revision.of("2014-10-07")).get();

        SchemaNode testNode = myModule.getDataChildByName(QName.create(myModule.getQNameModule(), "my-container"));

        SchemaPath path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"));
        SchemaNode foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = getRpcByName(myModule, "my-rpc");

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = myModule.getNotifications().iterator().next();

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-notification"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = getGroupingByName(myModule, "my-grouping");

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-grouping"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = myModule.getDataChildByName(QName.create(myModule.getQNameModule(), "my-choice"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-choice"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((ContainerSchemaNode) myModule.getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(), "my-list"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-list"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

    }

    @Test
    public void findParentModuleTest() throws URISyntaxException, IOException, YangSyntaxErrorException,
            ReactorException {

        final SchemaContext context = TestUtils.parseYangSources("/schema-context-util-test");

        final Module myModule = context.findModule(new URI("uri:my-module"), Revision.of("2014-10-07")).get();

        final DataSchemaNode node = myModule.getDataChildByName(QName.create(myModule.getQNameModule(),
            "my-container"));

        final Module foundModule = SchemaContextUtil.findParentModule(context, node);

        assertEquals(myModule, foundModule);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findParentModuleIllegalArgumentTest() {

        final SchemaContext mockContext = mock(SchemaContext.class);
        SchemaContextUtil.findParentModule(mockContext, null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void findParentModuleIllegalArgumentTest2() {

        final SchemaNode mockSchemaNode = mock(SchemaNode.class);
        SchemaContextUtil.findParentModule(null, mockSchemaNode);

    }

    @Test(expected = IllegalStateException.class)
    public void findParentModuleIllegalStateTest() {
        final SchemaContext mockContext = mock(SchemaContext.class);
        final SchemaNode mockSchemaNode = mock(SchemaNode.class);
        when(mockSchemaNode.getPath()).thenReturn(null);
        SchemaContextUtil.findParentModule(mockContext, mockSchemaNode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findDataSchemaNodeIllegalArgumentTest() {
        final SchemaContext mockContext = mock(SchemaContext.class);
        SchemaContextUtil.findDataSchemaNode(mockContext, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findDataSchemaNodeIllegalArgumentTest2() {
        final SchemaPath mockSchemaPath = mock(SchemaPath.class);
        SchemaContextUtil.findDataSchemaNode(null, mockSchemaPath);
    }

    @Test
    public void findDataSchemaNodeTest() throws URISyntaxException, IOException, YangSyntaxErrorException,
            ReactorException {
        final SchemaContext context = TestUtils.parseYangSources("/schema-context-util-test");
        final Module module = context.findModule(new URI("uri:my-module"), Revision.of("2014-10-07")).get();
        final Module importedModule = context.findModule(new URI("uri:imported-module"),
            Revision.of("2014-10-07")).get();

        final SchemaNode testNode = ((ContainerSchemaNode) importedModule.getDataChildByName(QName.create(
                importedModule.getQNameModule(), "my-imported-container"))).getDataChildByName(QName.create(
                importedModule.getQNameModule(), "my-imported-leaf"));

        final RevisionAwareXPath xpath = new RevisionAwareXPathImpl("imp:my-imported-container/imp:my-imported-leaf",
                true);

        final SchemaNode foundNode = SchemaContextUtil.findDataSchemaNode(context, module, xpath);

        assertNotNull(foundNode);
        assertNotNull(testNode);
        assertEquals(testNode, foundNode);
    }

    @Test
    public void findDataSchemaNodeTest2() throws URISyntaxException, IOException, YangSyntaxErrorException,
            ReactorException {
        // findDataSchemaNode(final SchemaContext context, final Module module,
        // final RevisionAwareXPath nonCondXPath) {

        final SchemaContext context = TestUtils.parseYangSources("/schema-context-util-test");
        final Module module = context.findModule(new URI("uri:my-module"), Revision.of("2014-10-07")).get();

        final GroupingDefinition grouping = getGroupingByName(module, "my-grouping");
        final SchemaNode testNode = grouping.getDataChildByName(QName.create(module.getQNameModule(),
                "my-leaf-in-gouping2"));

        final RevisionAwareXPath xpath = new RevisionAwareXPathImpl("my:my-grouping/my:my-leaf-in-gouping2", true);

        final SchemaNode foundNode = SchemaContextUtil.findDataSchemaNode(context, module, xpath);

        assertNotNull(foundNode);
        assertNotNull(testNode);
        assertEquals(testNode, foundNode);

    }

    @Test(expected = IllegalArgumentException.class)
    public void findDataSchemaNodeFromXPathIllegalArgumentTest() {
        final SchemaContext mockContext = mock(SchemaContext.class);
        final Module module = mock(Module.class);

        SchemaContextUtil.findDataSchemaNode(mockContext, module, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findDataSchemaNodeFromXPathIllegalArgumentTest2() {
        final SchemaContext mockContext = mock(SchemaContext.class);
        final RevisionAwareXPath xpath = new RevisionAwareXPathImpl("my:my-grouping/my:my-leaf-in-gouping2", true);

        SchemaContextUtil.findDataSchemaNode(mockContext, null, xpath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findDataSchemaNodeFromXPathIllegalArgumentTest3() {
        final Module module = mock(Module.class);
        final RevisionAwareXPath xpath = new RevisionAwareXPathImpl("my:my-grouping/my:my-leaf-in-gouping2", true);

        SchemaContextUtil.findDataSchemaNode(null, module, xpath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findDataSchemaNodeFromXPathIllegalArgumentTest4() {
        final SchemaContext mockContext = mock(SchemaContext.class);
        final Module module = mock(Module.class);
        final RevisionAwareXPath xpath = new RevisionAwareXPathImpl(
                "my:my-grouping[@con='NULL']/my:my-leaf-in-gouping2", true);

        SchemaContextUtil.findDataSchemaNode(mockContext, module, xpath);
    }

    @Test
    public void findDataSchemaNodeFromXPathNullTest() {
        final SchemaContext mockContext = mock(SchemaContext.class);
        final Module module = mock(Module.class);
        final RevisionAwareXPath xpath = mock(RevisionAwareXPath.class);

        when(xpath.toString()).thenReturn(null);
        assertNull(SchemaContextUtil.findDataSchemaNode(mockContext, module, xpath));
    }

    @Test
    public void findDataSchemaNodeFromXPathNullTest2() {
        final SchemaContext mockContext = mock(SchemaContext.class);
        final Module module = mock(Module.class);
        final RevisionAwareXPath xpath = new RevisionAwareXPathImpl("my:my-grouping/my:my-leaf-in-gouping2", false);

        assertNull(SchemaContextUtil.findDataSchemaNode(mockContext, module, xpath));
    }

    @Test
    public void findNodeInSchemaContextGroupingsTest() throws URISyntaxException, IOException,
            YangSyntaxErrorException, ReactorException {
        final SchemaContext context = TestUtils.parseYangSources("/schema-context-util-test");
        final Module myModule = context.findModule(URI.create("uri:my-module"), Revision.of("2014-10-07")).get();

        // find grouping in container
        DataNodeContainer dataContainer = (DataNodeContainer) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"));
        SchemaNode testNode = getGroupingByName(dataContainer, "my-grouping-in-container");

        SchemaPath path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-container"));
        SchemaNode foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-container"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-container"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        // find grouping in list
        dataContainer = (DataNodeContainer) ((DataNodeContainer) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-list"));
        testNode = getGroupingByName(dataContainer, "my-grouping-in-list");

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-list"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-list"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-list"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-list"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        // find grouping in grouping
        dataContainer = getGroupingByName(myModule, "my-grouping");
        testNode = getGroupingByName(dataContainer, "my-grouping-in-grouping");

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-grouping"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-grouping"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-grouping"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-grouping"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        // find grouping in rpc
        final RpcDefinition rpc = getRpcByName(myModule, "my-rpc");
        for (final GroupingDefinition grouping : rpc.getGroupings()) {
            if (grouping.getQName().getLocalName().equals("my-grouping-in-rpc")) {
                testNode = grouping;
            }
        }

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-rpc"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-rpc"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-rpc"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        // find grouping in output
        dataContainer = getRpcByName(myModule, "my-rpc").getOutput();
        testNode = getGroupingByName(dataContainer, "my-grouping-in-output");

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "output"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-output"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-output"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-output"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        // find grouping in input
        dataContainer = getRpcByName(myModule, "my-rpc").getInput();
        testNode = getGroupingByName(dataContainer, "my-grouping-in-input");

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "input"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-input"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-input"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-input"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        // find grouping in notification
        dataContainer = getNotificationByName(myModule, "my-notification");
        testNode = getGroupingByName(dataContainer, "my-grouping-in-notification");

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-notification"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-notification"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-notification"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-notification"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        // find grouping in case
        dataContainer = (DataNodeContainer) ((ChoiceSchemaNode) myModule.getDataChildByName(
            QName.create(myModule.getQNameModule(), "my-choice")))
                .findCaseNodes("one").iterator().next()
                .getDataChildByName(QName.create(myModule.getQNameModule(), "my-container-in-case"));
        testNode = getGroupingByName(dataContainer, "my-grouping-in-case");

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-choice"),
                QName.create(myModule.getQNameModule(), "one"),
                QName.create(myModule.getQNameModule(), "my-container-in-case"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-case"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-case"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-case"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);
    }

    @Test
    public void findNodeInSchemaContextGroupingsTest2() throws URISyntaxException, IOException,
            YangSyntaxErrorException, ReactorException {

        final SchemaContext context = TestUtils.parseYangSources("/schema-context-util-test");
        final Module myModule = context.findModule(new URI("uri:my-module"), Revision.of("2014-10-07")).get();

        // find grouping in container
        DataNodeContainer dataContainer = (DataNodeContainer) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"));
        SchemaNode testNode = getGroupingByName(dataContainer, "my-grouping-in-container2");

        SchemaPath path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-container2"));
        SchemaNode foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);

        // find grouping in list
        dataContainer = (DataNodeContainer) ((DataNodeContainer) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-list"));
        testNode = getGroupingByName(dataContainer, "my-grouping-in-list2");

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-list"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-list2"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);

        // find grouping in grouping
        dataContainer = getGroupingByName(myModule, "my-grouping");
        testNode = getGroupingByName(dataContainer, "my-grouping-in-grouping2");

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-grouping"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-grouping2"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);

        // find grouping in rpc
        final RpcDefinition rpc = getRpcByName(myModule, "my-rpc");
        for (final GroupingDefinition grouping : rpc.getGroupings()) {
            if (grouping.getQName().getLocalName().equals("my-grouping-in-rpc2")) {
                testNode = grouping;
            }
        }

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-rpc2"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);

        // find grouping in output
        dataContainer = getRpcByName(myModule, "my-rpc").getOutput();
        testNode = getGroupingByName(dataContainer, "my-grouping-in-output2");

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "output"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-output2"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);

        // find grouping in input
        dataContainer = getRpcByName(myModule, "my-rpc").getInput();
        testNode = getGroupingByName(dataContainer, "my-grouping-in-input2");

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "input"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-input2"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);

        // find grouping in notification
        dataContainer = getNotificationByName(myModule, "my-notification");
        testNode = getGroupingByName(dataContainer, "my-grouping-in-notification2");

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-notification"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-notification2"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);

        // find grouping in case
        dataContainer = (DataNodeContainer) ((ChoiceSchemaNode) myModule.getDataChildByName(
            QName.create(myModule.getQNameModule(), "my-choice")))
                .findCaseNodes("one").iterator().next()
                .getDataChildByName(QName.create(myModule.getQNameModule(), "my-container-in-case"));
        testNode = getGroupingByName(dataContainer, "my-grouping-in-case2");

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-choice"),
                QName.create(myModule.getQNameModule(), "one"),
                QName.create(myModule.getQNameModule(), "my-container-in-case"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-case2"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNull(testNode);
        assertNull(foundNode);
    }

    private static GroupingDefinition getGroupingByName(final DataNodeContainer dataNodeContainer, final String name) {
        for (final GroupingDefinition grouping : dataNodeContainer.getGroupings()) {
            if (grouping.getQName().getLocalName().equals(name)) {
                return grouping;
            }
        }
        return null;
    }

    private static RpcDefinition getRpcByName(final Module module, final String name) {
        for (final RpcDefinition rpc : module.getRpcs()) {
            if (rpc.getQName().getLocalName().equals(name)) {
                return rpc;
            }
        }
        return null;
    }

    private static NotificationDefinition getNotificationByName(final Module module, final String name) {
        for (final NotificationDefinition notification : module.getNotifications()) {
            if (notification.getQName().getLocalName().equals(name)) {
                return notification;
            }
        }
        return null;
    }

    @Test
    public void findNodeInSchemaContextTheSameNameOfSiblingsTest() throws URISyntaxException, IOException,
            YangSyntaxErrorException, ReactorException {

        final SchemaContext context = TestUtils.parseYangSources("/schema-context-util-test");

        final Module myModule = context.findModule(new URI("uri:my-module"), Revision.of("2014-10-07")).get();
        final ChoiceSchemaNode choice = (ChoiceSchemaNode) getRpcByName(myModule, "my-name").getInput()
                .getDataChildByName(QName.create(myModule.getQNameModule(), "my-choice"));
        final SchemaNode testNode = choice.findCaseNodes("case-two").iterator().next()
                .getDataChildByName(QName.create(myModule.getQNameModule(), "two"));

        final SchemaPath path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-name"),
                QName.create(myModule.getQNameModule(), "input"), QName.create(myModule.getQNameModule(), "my-choice"),
                QName.create(myModule.getQNameModule(), "case-two"), QName.create(myModule.getQNameModule(), "two"));
        final SchemaNode foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(testNode);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);
    }
}