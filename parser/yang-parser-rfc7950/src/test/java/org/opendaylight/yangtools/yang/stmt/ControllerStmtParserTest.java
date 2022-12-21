/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;

class ControllerStmtParserTest extends AbstractYangTest {
    @Test
    void test() {
        final var context = assertEffectiveModelDir("/sal-broker-impl");
        salDomBrokerImplModuleTest(context);
        configModuleTest(context);
    }

    private static void salDomBrokerImplModuleTest(final SchemaContext context) {
        final Module module = context.findModule("opendaylight-sal-dom-broker-impl", Revision.of("2013-10-28")).get();

        boolean checked = false;
        for (final AugmentationSchemaNode augmentationSchema : module.getAugmentations()) {
            final DataSchemaNode dataNode = augmentationSchema.dataChildByName(
                QName.create(module.getQNameModule(), "dom-broker-impl"));
            if (dataNode instanceof CaseSchemaNode caseNode) {
                final DataSchemaNode dataNode2 = caseNode.dataChildByName(
                    QName.create(module.getQNameModule(), "async-data-broker"));
                if (dataNode2 instanceof ContainerSchemaNode containerNode) {
                    final DataSchemaNode leaf = containerNode
                        .getDataChildByName(QName.create(module.getQNameModule(), "type"));
                    assertEquals(0, leaf.getUnknownSchemaNodes().size());

                    final Collection<? extends UnrecognizedStatement> unknownSchemaNodes =
                        containerNode.asEffectiveStatement()
                            .findFirstEffectiveSubstatement(UsesEffectiveStatement.class).orElseThrow()
                            .findFirstEffectiveSubstatement(RefineEffectiveStatement.class).orElseThrow()
                            .getDeclared().declaredSubstatements(UnrecognizedStatement.class);


                    final UnrecognizedStatement unknownSchemaNode = unknownSchemaNodes.iterator().next();
                    assertEquals("sal:dom-async-data-broker", unknownSchemaNode.argument());
                    checked = true;
                }
            }
        }
        assertTrue(checked);
    }

    private static void configModuleTest(final SchemaContext context) {
        final Module configModule = context.findModule("config", Revision.of("2013-04-05")).get();
        final Module module = context.findModule("opendaylight-sal-dom-broker-impl", Revision.of("2013-10-28")).get();

        final DataSchemaNode dataNode = configModule.getDataChildByName(QName.create(configModule.getQNameModule(),
            "modules"));
        final ContainerSchemaNode moduleContainer = assertInstanceOf(ContainerSchemaNode.class, dataNode);

        final DataSchemaNode dataChildList = moduleContainer
            .getDataChildByName(QName.create(configModule.getQNameModule(), "module"));
        final ListSchemaNode listModule = assertInstanceOf(ListSchemaNode.class, dataChildList);

        final DataSchemaNode dataChildChoice = listModule
            .getDataChildByName(QName.create(configModule.getQNameModule(), "configuration"));
        final ChoiceSchemaNode confChoice = assertInstanceOf(ChoiceSchemaNode.class, dataChildChoice);

        final CaseSchemaNode caseNodeByName = confChoice.findCaseNodes("dom-broker-impl").iterator().next();

        assertNotNull(caseNodeByName);
        final DataSchemaNode dataNode2 = caseNodeByName
            .getDataChildByName(QName.create(module.getQNameModule(), "async-data-broker"));
        final ContainerSchemaNode containerNode = assertInstanceOf(ContainerSchemaNode.class, dataNode2);

        final DataSchemaNode leaf = containerNode.getDataChildByName(QName.create(module.getQNameModule(), "type"));
        assertEquals(0, leaf.getUnknownSchemaNodes().size());

        final CaseSchemaNode domInmemoryDataBroker = confChoice.findCaseNodes("dom-inmemory-data-broker").iterator()
            .next();

        assertNotNull(domInmemoryDataBroker);
        final DataSchemaNode schemaService = domInmemoryDataBroker
            .getDataChildByName(QName.create(module.getQNameModule(), "schema-service"));
        final ContainerSchemaNode schemaServiceContainer = assertInstanceOf(ContainerSchemaNode.class, schemaService);

        assertEquals(1, schemaServiceContainer.getUses().size());
        final UsesNode uses = schemaServiceContainer.getUses().iterator().next();
        final QName groupingQName = QName.create("urn:opendaylight:params:xml:ns:yang:controller:config", "2013-04-05",
            "service-ref");
        assertEquals(groupingQName, uses.getSourceGrouping().getQName());
        assertEquals(0, getChildNodeSizeWithoutUses(schemaServiceContainer));

        final DataSchemaNode type = schemaServiceContainer.getDataChildByName(QName.create(module.getQNameModule(),
            "type"));
        assertEquals(0, type.getUnknownSchemaNodes().size());

        final Collection<? extends UnrecognizedStatement> typeUnknownSchemaNodes =
            schemaServiceContainer.asEffectiveStatement()
                .findFirstEffectiveSubstatement(UsesEffectiveStatement.class).orElseThrow()
                .findFirstEffectiveSubstatement(RefineEffectiveStatement.class).orElseThrow()
                .getDeclared().declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, typeUnknownSchemaNodes.size());


        final UnrecognizedStatement typeUnknownSchemaNode = typeUnknownSchemaNodes.iterator().next();
        assertEquals("sal:schema-service", typeUnknownSchemaNode.argument());
        assertEquals(QName.create(groupingQName, "required-identity"),
            typeUnknownSchemaNode.statementDefinition().getStatementName());
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
