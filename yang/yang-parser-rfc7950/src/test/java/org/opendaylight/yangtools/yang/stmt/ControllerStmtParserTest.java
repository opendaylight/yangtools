/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

public class ControllerStmtParserTest {

    @Test
    public void test() throws Exception {
        final EffectiveModelContext context = StmtTestUtils.parseYangSources("/sal-broker-impl");
        assertNotNull(context);

        salDomBrokerImplModuleTest(context);
        configModuleTest(context);
    }

    private static void salDomBrokerImplModuleTest(final EffectiveModelContext context) {
        final Module module = context.findModule("opendaylight-sal-dom-broker-impl", Revision.of("2013-10-28")).get();

        boolean checked = false;

        for (final AugmentationSchemaNode augmentationSchema : module.getAugmentations()) {
            final QName domBrokerImplQName = QName.create(module.getQNameModule(), "dom-broker-impl");
            final DataSchemaNode dataNode = augmentationSchema.dataChildByName(domBrokerImplQName);
            if (dataNode instanceof CaseSchemaNode) {
                final CaseSchemaNode caseNode = (CaseSchemaNode) dataNode;
                final QName asyncDataBrokerQName = QName.create(module.getQNameModule(), "async-data-broker");
                final DataSchemaNode dataNode2 = caseNode.dataChildByName(asyncDataBrokerQName);
                if (dataNode2 instanceof ContainerSchemaNode) {
                    final SchemaInferenceStack stack = new SchemaInferenceStack(context);
                    for (final QName qName : augmentationSchema.getTargetPath().getNodeIdentifiers()) {
                        stack.enterSchemaTree(qName);
                    }
                    final ContainerSchemaNode containerNode = (ContainerSchemaNode) dataNode2;
                    final QName typeQName = QName.create(module.getQNameModule(), "type");
                    final DataSchemaNode leaf = containerNode.getDataChildByName(typeQName);
                    stack.enterSchemaTree(domBrokerImplQName, asyncDataBrokerQName, typeQName);
                    final Collection<? extends UnknownSchemaNode> unknownSchemaNodes = leaf.getUnknownSchemaNodes();
                    assertEquals(1, unknownSchemaNodes.size());

                    final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.iterator().next();
                    assertEquals("dom-async-data-broker", unknownSchemaNode.getQName().getLocalName());
                    final QName domAsyncDataBrokerQName = ((SchemaNode) stack.currentStatement()
                            .findFirstEffectiveSubstatement(UnrecognizedEffectiveStatement.class).get()).getQName();
                    stack.clear();
                    assertEquals(unknownSchemaNode.getQName(), domAsyncDataBrokerQName);

                    checked = true;
                }
            }
        }
        assertTrue(checked);
    }

    private static void configModuleTest(final EffectiveModelContext context) {
        final Module configModule = context.findModule("config", Revision.of("2013-04-05")).get();
        final Module module = context.findModule("opendaylight-sal-dom-broker-impl", Revision.of("2013-10-28")).get();
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        final QName modulesQName = QName.create(configModule.getQNameModule(), "modules");
        stack.enterSchemaTree(modulesQName);
        final DataSchemaNode dataNode = configModule.getDataChildByName(modulesQName);
        assertTrue(dataNode instanceof ContainerSchemaNode);

        final ContainerSchemaNode moduleContainer = (ContainerSchemaNode) dataNode;
        final QName moduleQName = QName.create(configModule.getQNameModule(), "module");
        stack.enterSchemaTree(moduleQName);
        final DataSchemaNode dataChildList = moduleContainer.getDataChildByName(moduleQName);

        assertTrue(dataChildList instanceof ListSchemaNode);

        final ListSchemaNode listModule = (ListSchemaNode) dataChildList;
        final QName configurationQName = QName.create(configModule.getQNameModule(), "configuration");
        stack.enterSchemaTree(configurationQName);
        final DataSchemaNode dataChildChoice = listModule.getDataChildByName(configurationQName);

        assertTrue(dataChildChoice instanceof ChoiceSchemaNode);

        final ChoiceSchemaNode confChoice = (ChoiceSchemaNode) dataChildChoice;
        final CaseSchemaNode caseNodeByName = confChoice.findCaseNodes("dom-broker-impl").iterator().next();
        stack.enterSchemaTree(QName.create(caseNodeByName.getQName().getModule(), "dom-broker-impl"));

        assertNotNull(caseNodeByName);
        final QName asyncDataBrokerQName = QName.create(module.getQNameModule(), "async-data-broker");
        stack.enterSchemaTree(asyncDataBrokerQName);
        final DataSchemaNode dataNode2 = caseNodeByName.getDataChildByName(asyncDataBrokerQName);
        assertTrue(dataNode2 instanceof ContainerSchemaNode);

        final ContainerSchemaNode containerNode = (ContainerSchemaNode) dataNode2;
        final QName typeQName = QName.create(module.getQNameModule(), "type");
        stack.enterSchemaTree(typeQName);
        final DataSchemaNode leaf = containerNode.getDataChildByName(typeQName);
        final Collection<? extends UnknownSchemaNode> unknownSchemaNodes = leaf.getUnknownSchemaNodes();

        assertEquals(1, unknownSchemaNodes.size());

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.iterator().next();

        final QName domAsyncDataBrokerQName = ((SchemaNode) stack.currentStatement()
                .findFirstEffectiveSubstatement(UnrecognizedEffectiveStatement.class).get()).getQName();
        stack.exit(3);
        assertEquals(unknownSchemaNode.getQName(), domAsyncDataBrokerQName);
        assertEquals("dom-async-data-broker", unknownSchemaNode.getQName().getLocalName());

        final CaseSchemaNode domInmemoryDataBroker = confChoice.findCaseNodes("dom-inmemory-data-broker").iterator()
                .next();
        stack.enterSchemaTree(QName.create(domInmemoryDataBroker.getQName().getModule(), "dom-inmemory-data-broker"));

        assertNotNull(domInmemoryDataBroker);
        final QName schemaServiceQName = QName.create(module.getQNameModule(), "schema-service");
        stack.enterSchemaTree(schemaServiceQName);
        final DataSchemaNode schemaService = domInmemoryDataBroker.getDataChildByName(schemaServiceQName);
        assertTrue(schemaService instanceof ContainerSchemaNode);

        final ContainerSchemaNode schemaServiceContainer = (ContainerSchemaNode) schemaService;

        assertEquals(1, schemaServiceContainer.getUses().size());
        final UsesNode uses = schemaServiceContainer.getUses().iterator().next();
        final QName groupingQName = QName.create("urn:opendaylight:params:xml:ns:yang:controller:config", "2013-04-05",
            "service-ref");
        assertEquals(groupingQName, uses.getSourceGrouping().getQName());
        assertEquals(0, getChildNodeSizeWithoutUses(schemaServiceContainer));

        stack.enterSchemaTree(typeQName);
        final DataSchemaNode type = schemaServiceContainer.getDataChildByName(typeQName);
        final Collection<? extends UnknownSchemaNode> typeUnknownSchemaNodes = type.getUnknownSchemaNodes();
        assertEquals(1, typeUnknownSchemaNodes.size());

        final UnknownSchemaNode typeUnknownSchemaNode = typeUnknownSchemaNodes.iterator().next();
        final QNameModule qNameModule = QNameModule.create(
            URI.create("urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom"), Revision.of("2013-10-28"));
        final QName qName = QName.create(qNameModule, "schema-service");

        assertEquals(qName, typeUnknownSchemaNode.getQName());
        final QName typeUnknownQname = ((SchemaNode) stack.currentStatement()
                .findFirstEffectiveSubstatement(UnrecognizedEffectiveStatement.class).get()).getQName();
        assertEquals(typeUnknownSchemaNode.getQName(), typeUnknownQname);
    }

    private static int getChildNodeSizeWithoutUses(final DataNodeContainer csn) {
        int result = 0;
        for (final DataSchemaNode dsn : csn.getChildNodes()) {
            if (!dsn.isAddedByUses()) {
                result++;
            }
        }
        return result;
    }
}
