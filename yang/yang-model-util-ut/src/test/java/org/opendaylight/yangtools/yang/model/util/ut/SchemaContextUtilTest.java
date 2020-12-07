/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.ut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URI;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.PathExpressionImpl;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class SchemaContextUtilTest {
    private static SchemaContext context;
    private static Module myModule;

    @BeforeClass
    public static void beforeClass() {
        context = YangParserTestUtils.parseYangResourceDirectory("/schema-context-util");
        myModule = context.findModule(URI.create("uri:my-module"), Revision.of("2014-10-07")).get();
    }

    @Test
    public void findNodeInSchemaContextTest() {
        SchemaNode testNode = ((ContainerSchemaNode) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-container"));

        SchemaPath path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-leaf-in-container"));
        SchemaNode foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertEquals(testNode, foundNode);

        RpcDefinition rpc = getRpcByName(myModule, "my-rpc");
        testNode = rpc.getInput().getDataChildByName(QName.create(myModule.getQNameModule(), "my-input-leaf"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "input"),
                QName.create(myModule.getQNameModule(), "my-input-leaf"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertEquals(testNode, foundNode);

        rpc = getRpcByName(myModule, "my-rpc");
        testNode = rpc.getOutput().getDataChildByName(QName.create(myModule.getQNameModule(), "my-output-leaf"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "output"),
                QName.create(myModule.getQNameModule(), "my-output-leaf"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertEquals(testNode, foundNode);

        final NotificationDefinition notification = myModule.getNotifications().iterator().next();
        testNode = notification.getDataChildByName(QName.create(myModule.getQNameModule(), "my-notification-leaf"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-notification"),
                QName.create(myModule.getQNameModule(), "my-notification-leaf"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertEquals(testNode, foundNode);

        final GroupingDefinition grouping = getGroupingByName(myModule, "my-grouping");
        testNode = ((ContainerSchemaNode) grouping.getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-container-in-grouping"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-grouping"),
                QName.create(myModule.getQNameModule(), "my-container-in-grouping"),
                QName.create(myModule.getQNameModule(), "my-leaf-in-grouping"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertEquals(testNode, foundNode);

        testNode = ((ChoiceSchemaNode) myModule
                .getDataChildByName(QName.create(myModule.getQNameModule(), "my-choice")))
                .findCaseNodes("one").iterator().next()
                .getDataChildByName(QName.create(myModule.getQNameModule(), "my-choice-leaf-one"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-choice"),
                QName.create(myModule.getQNameModule(), "one"),
                QName.create(myModule.getQNameModule(), "my-choice-leaf-one"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertEquals(testNode, foundNode);

        ListSchemaNode listNode = (ListSchemaNode) ((ContainerSchemaNode) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-list"));

        testNode = listNode.getDataChildByName(QName.create(myModule.getQNameModule(), "my-leaf-in-list"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-list"),
                QName.create(myModule.getQNameModule(), "my-leaf-in-list"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertEquals(testNode, foundNode);

        listNode = (ListSchemaNode) ((ContainerSchemaNode) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-list"));

        testNode = listNode.getDataChildByName(QName.create(myModule.getQNameModule(), "my-leaf-list-in-list"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-list"),
                QName.create(myModule.getQNameModule(), "my-leaf-list-in-list"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertEquals(testNode, foundNode);
    }

    @Test
    public void findNodeInSchemaContextTest2() {
        SchemaNode testNode = ((ContainerSchemaNode) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).dataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-not-in-container"));
        assertNull(testNode);

        SchemaPath path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-leaf-not-in-container"));
        SchemaNode foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNull(foundNode);

        final RpcDefinition rpc = getRpcByName(myModule, "my-rpc");
        testNode = rpc.getInput().dataChildByName(QName.create(myModule.getQNameModule(), "no-input-leaf"));
        assertNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "input"),
                QName.create(myModule.getQNameModule(), "no-input-leaf"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNull(foundNode);

        final NotificationDefinition notification = myModule.getNotifications().iterator().next();
        testNode = notification.dataChildByName(QName.create(myModule.getQNameModule(), "no-notification-leaf"));
        assertNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-notification"),
                QName.create(myModule.getQNameModule(), "no-notification-leaf"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNull(foundNode);

        final GroupingDefinition grouping = getGroupingByName(myModule, "my-grouping");
        testNode = ((ContainerSchemaNode) grouping.getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-container-in-grouping"))).dataChildByName(QName.create(myModule.getQNameModule(),
                "no-leaf-in-grouping"));
        assertNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-grouping"),
                QName.create(myModule.getQNameModule(), "my-container-in-grouping"),
                QName.create(myModule.getQNameModule(), "no-leaf-in-grouping"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNull(foundNode);

        testNode = ((ChoiceSchemaNode) myModule
                .getDataChildByName(QName.create(myModule.getQNameModule(), "my-choice")))
                .findCaseNodes("one").iterator().next()
                .dataChildByName(QName.create(myModule.getQNameModule(), "no-choice-leaf"));
        assertNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-choice"),
                QName.create(myModule.getQNameModule(), "one"),
                QName.create(myModule.getQNameModule(), "no-choice-leaf"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNull(foundNode);

        ListSchemaNode listNode = (ListSchemaNode) ((ContainerSchemaNode) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-list"));

        testNode = listNode.dataChildByName(QName.create(myModule.getQNameModule(), "no-leaf-in-list"));
        assertNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-list"),
                QName.create(myModule.getQNameModule(), "no-leaf-in-list"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNull(foundNode);

        listNode = (ListSchemaNode) ((ContainerSchemaNode) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-list"));

        testNode = listNode.dataChildByName(QName.create(myModule.getQNameModule(), "no-leaf-list-in-list"));
        assertNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-list"),
                QName.create(myModule.getQNameModule(), "no-leaf-list-in-list"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNull(foundNode);
    }

    @Test
    public void findNodeInSchemaContextTest3() {
        SchemaNode testNode = myModule.getDataChildByName(QName.create(myModule.getQNameModule(), "my-container"));

        SchemaPath path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"));
        SchemaNode foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = getRpcByName(myModule, "my-rpc");
        assertNotNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = myModule.getNotifications().iterator().next();

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-notification"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = getGroupingByName(myModule, "my-grouping");
        assertNotNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-grouping"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = myModule.getDataChildByName(QName.create(myModule.getQNameModule(), "my-choice"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-choice"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((ContainerSchemaNode) myModule.getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(), "my-list"));

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-list"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);
    }

    @Test
    public void findParentModuleTest() {
        final DataSchemaNode node = myModule.getDataChildByName(QName.create(myModule.getQNameModule(),
            "my-container"));

        final Module foundModule = SchemaContextUtil.findParentModule(context, node);

        assertEquals(myModule, foundModule);
    }

    @Test
    public void findDataSchemaNodeTest() {
        final Module importedModule = context.findModule(URI.create("uri:imported-module"),
            Revision.of("2014-10-07")).get();

        final SchemaNode testNode = ((ContainerSchemaNode) importedModule.getDataChildByName(QName.create(
                importedModule.getQNameModule(), "my-imported-container"))).getDataChildByName(QName.create(
                importedModule.getQNameModule(), "my-imported-leaf"));

        final PathExpression xpath = new PathExpressionImpl("imp:my-imported-container/imp:my-imported-leaf", true);

        final SchemaNode foundNode = SchemaContextUtil.findDataSchemaNode(context, myModule, xpath);
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);
    }

    @Test
    public void findDataSchemaNodeTest2() {
        final GroupingDefinition grouping = getGroupingByName(myModule, "my-grouping");
        final SchemaNode testNode = grouping.getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-gouping2"));

        final PathExpression xpath = new PathExpressionImpl("my:my-grouping/my:my-leaf-in-gouping2", true);

        final SchemaNode foundNode = SchemaContextUtil.findDataSchemaNode(context, myModule, xpath);

        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);
    }

    @Test
    public void findNodeInSchemaContextGroupingsTest() {
        // find grouping in container
        DataNodeContainer dataContainer = (DataNodeContainer) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"));
        SchemaNode testNode = getGroupingByName(dataContainer, "my-grouping-in-container");
        assertNotNull(testNode);

        SchemaPath path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-container"));
        SchemaNode foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());

        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-container"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-container"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        // find grouping in list
        dataContainer = (DataNodeContainer) ((DataNodeContainer) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-list"));
        testNode = getGroupingByName(dataContainer, "my-grouping-in-list");
        assertNotNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-list"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-list"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-list"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-list"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        // find grouping in grouping
        dataContainer = getGroupingByName(myModule, "my-grouping");
        testNode = getGroupingByName(dataContainer, "my-grouping-in-grouping");
        assertNotNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-grouping"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-grouping"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-grouping"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-grouping"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        // find grouping in rpc
        final RpcDefinition rpc = getRpcByName(myModule, "my-rpc");
        for (final GroupingDefinition grouping : rpc.getGroupings()) {
            if (grouping.getQName().getLocalName().equals("my-grouping-in-rpc")) {
                testNode = grouping;
            }
        }
        assertNotNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-rpc"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-rpc"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-rpc"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        // find grouping in output
        dataContainer = getRpcByName(myModule, "my-rpc").getOutput();
        testNode = getGroupingByName(dataContainer, "my-grouping-in-output");
        assertNotNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "output"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-output"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-output"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-output"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        // find grouping in input
        dataContainer = getRpcByName(myModule, "my-rpc").getInput();
        testNode = getGroupingByName(dataContainer, "my-grouping-in-input");
        assertNotNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "input"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-input"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-input"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-input"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        // find grouping in notification
        dataContainer = getNotificationByName(myModule, "my-notification");
        testNode = getGroupingByName(dataContainer, "my-grouping-in-notification");
        assertNotNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-notification"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-notification"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-notification"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-notification"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        // find grouping in case
        dataContainer = (DataNodeContainer) ((ChoiceSchemaNode) myModule.getDataChildByName(
            QName.create(myModule.getQNameModule(), "my-choice")))
                .findCaseNodes("one").iterator().next()
                .getDataChildByName(QName.create(myModule.getQNameModule(), "my-container-in-case"));
        testNode = getGroupingByName(dataContainer, "my-grouping-in-case");
        assertNotNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-choice"),
                QName.create(myModule.getQNameModule(), "one"),
                QName.create(myModule.getQNameModule(), "my-container-in-case"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-case"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);

        testNode = ((GroupingDefinition) testNode).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-leaf-in-grouping-in-case"));
        path = path.createChild(QName.create(myModule.getQNameModule(), "my-leaf-in-grouping-in-case"));

        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);
    }

    @Test
    public void findNodeInSchemaContextGroupingsTest2() {
        // find grouping in container
        DataNodeContainer dataContainer = (DataNodeContainer) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"));
        SchemaNode testNode = getGroupingByName(dataContainer, "my-grouping-in-container2");
        assertNull(testNode);

        SchemaPath path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-container2"));
        SchemaNode foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNull(foundNode);

        // find grouping in list
        dataContainer = (DataNodeContainer) ((DataNodeContainer) myModule.getDataChildByName(QName.create(
                myModule.getQNameModule(), "my-container"))).getDataChildByName(QName.create(myModule.getQNameModule(),
                "my-list"));
        testNode = getGroupingByName(dataContainer, "my-grouping-in-list2");
        assertNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-container"),
                QName.create(myModule.getQNameModule(), "my-list"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-list2"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNull(foundNode);

        // find grouping in grouping
        dataContainer = getGroupingByName(myModule, "my-grouping");
        testNode = getGroupingByName(dataContainer, "my-grouping-in-grouping2");
        assertNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-grouping"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-grouping2"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNull(foundNode);

        // find grouping in rpc
        final RpcDefinition rpc = getRpcByName(myModule, "my-rpc");
        for (final GroupingDefinition grouping : rpc.getGroupings()) {
            if (grouping.getQName().getLocalName().equals("my-grouping-in-rpc2")) {
                testNode = grouping;
            }
        }
        assertNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-rpc2"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNull(foundNode);

        // find grouping in output
        dataContainer = getRpcByName(myModule, "my-rpc").getOutput();
        testNode = getGroupingByName(dataContainer, "my-grouping-in-output2");
        assertNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "output"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-output2"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNull(foundNode);

        // find grouping in input
        dataContainer = getRpcByName(myModule, "my-rpc").getInput();
        testNode = getGroupingByName(dataContainer, "my-grouping-in-input2");
        assertNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-rpc"),
                QName.create(myModule.getQNameModule(), "input"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-input2"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNull(foundNode);

        // find grouping in notification
        dataContainer = getNotificationByName(myModule, "my-notification");
        testNode = getGroupingByName(dataContainer, "my-grouping-in-notification2");
        assertNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-notification"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-notification2"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNull(foundNode);

        // find grouping in case
        dataContainer = (DataNodeContainer) ((ChoiceSchemaNode) myModule.getDataChildByName(
            QName.create(myModule.getQNameModule(), "my-choice")))
                .findCaseNodes("one").iterator().next()
                .getDataChildByName(QName.create(myModule.getQNameModule(), "my-container-in-case"));
        testNode = getGroupingByName(dataContainer, "my-grouping-in-case2");
        assertNull(testNode);

        path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-choice"),
                QName.create(myModule.getQNameModule(), "one"),
                QName.create(myModule.getQNameModule(), "my-container-in-case"),
                QName.create(myModule.getQNameModule(), "my-grouping-in-case2"));
        foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
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
    public void findNodeInSchemaContextTheSameNameOfSiblingsTest() {
        final ChoiceSchemaNode choice = (ChoiceSchemaNode) getRpcByName(myModule, "my-name").getInput()
                .getDataChildByName(QName.create(myModule.getQNameModule(), "my-choice"));
        final SchemaNode testNode = choice.findCaseNodes("case-two").iterator().next()
                .getDataChildByName(QName.create(myModule.getQNameModule(), "two"));

        final SchemaPath path = SchemaPath.create(true, QName.create(myModule.getQNameModule(), "my-name"),
                QName.create(myModule.getQNameModule(), "input"), QName.create(myModule.getQNameModule(), "my-choice"),
                QName.create(myModule.getQNameModule(), "case-two"), QName.create(myModule.getQNameModule(), "two"));
        final SchemaNode foundNode = SchemaContextUtil.findNodeInSchemaContext(context, path.getPathFromRoot());
        assertNotNull(foundNode);
        assertEquals(testNode, foundNode);
    }
}