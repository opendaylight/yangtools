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
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

public class ControllerStmtParserTest {

    @Test
    public void test() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/sal-broker-impl");
        assertNotNull(context);

        salDomBrokerImplModuleTest(context);
        configModuleTest(context);
    }

    private static void salDomBrokerImplModuleTest(final SchemaContext context)
            throws ParseException {
        final Module module = context.findModuleByName(
                "opendaylight-sal-dom-broker-impl", SimpleDateFormatUtil
                        .getRevisionFormat().parse("2013-10-28"));
        assertNotNull(module);

        final Set<AugmentationSchema> augmentations = module.getAugmentations();
        boolean checked = false;
        for (final AugmentationSchema augmentationSchema : augmentations) {
            final DataSchemaNode dataNode = augmentationSchema
                    .getDataChildByName(QName.create(module.getQNameModule(), "dom-broker-impl"));
            if (dataNode instanceof ChoiceCaseNode) {
                final ChoiceCaseNode caseNode = (ChoiceCaseNode) dataNode;
                final DataSchemaNode dataNode2 = caseNode
                        .getDataChildByName(QName.create(module.getQNameModule(), "async-data-broker"));
                if (dataNode2 instanceof ContainerSchemaNode) {
                    final ContainerSchemaNode containerNode = (ContainerSchemaNode) dataNode2;
                    final DataSchemaNode leaf = containerNode
                            .getDataChildByName(QName.create(module.getQNameModule(), "type"));
                    final List<UnknownSchemaNode> unknownSchemaNodes = leaf
                            .getUnknownSchemaNodes();
                    assertEquals(1, unknownSchemaNodes.size());

                    final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes
                            .get(0);
                    assertEquals("dom-async-data-broker", unknownSchemaNode
                            .getQName().getLocalName());
                    assertEquals(unknownSchemaNode.getQName(),
                            unknownSchemaNode.getPath().getLastComponent());

                    checked = true;
                }
            }
        }
        assertTrue(checked);
    }

    private static void configModuleTest(final SchemaContext context) throws ParseException,
            URISyntaxException {
        final Module configModule = context.findModuleByName("config",
                SimpleDateFormatUtil.getRevisionFormat().parse("2013-04-05"));
        assertNotNull(configModule);

        final Module module = context.findModuleByName(
                "opendaylight-sal-dom-broker-impl", SimpleDateFormatUtil
                        .getRevisionFormat().parse("2013-10-28"));
        assertNotNull(module);

        final DataSchemaNode dataNode = configModule.getDataChildByName(QName.create(configModule.getQNameModule(), "modules"));
        assertTrue(dataNode instanceof ContainerSchemaNode);

        final ContainerSchemaNode moduleContainer = (ContainerSchemaNode) dataNode;
        final DataSchemaNode dataChildList = moduleContainer
                .getDataChildByName(QName.create(configModule.getQNameModule(), "module"));

        assertTrue(dataChildList instanceof ListSchemaNode);

        final ListSchemaNode listModule = (ListSchemaNode) dataChildList;
        final DataSchemaNode dataChildChoice = listModule
                .getDataChildByName(QName.create(configModule.getQNameModule(), "configuration"));

        assertTrue(dataChildChoice instanceof ChoiceSchemaNode);

        final ChoiceSchemaNode confChoice = (ChoiceSchemaNode) dataChildChoice;
        final ChoiceCaseNode caseNodeByName = confChoice
                .getCaseNodeByName("dom-broker-impl");

        assertNotNull(caseNodeByName);
        final DataSchemaNode dataNode2 = caseNodeByName
                .getDataChildByName(QName.create(module.getQNameModule(), "async-data-broker"));
        assertTrue(dataNode2 instanceof ContainerSchemaNode);

        final ContainerSchemaNode containerNode = (ContainerSchemaNode) dataNode2;
        final DataSchemaNode leaf = containerNode.getDataChildByName(QName.create(module.getQNameModule(), "type"));
        final List<UnknownSchemaNode> unknownSchemaNodes = leaf
                .getUnknownSchemaNodes();

        assertEquals(1, unknownSchemaNodes.size());

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.get(0);

        assertEquals(unknownSchemaNode.getQName(), unknownSchemaNode.getPath()
                .getLastComponent());
        assertEquals("dom-async-data-broker", unknownSchemaNode.getQName()
                .getLocalName());

        final ChoiceCaseNode domInmemoryDataBroker = confChoice
                .getCaseNodeByName("dom-inmemory-data-broker");

        assertNotNull(domInmemoryDataBroker);
        final DataSchemaNode schemaService = domInmemoryDataBroker
                .getDataChildByName(QName.create(module.getQNameModule(), "schema-service"));
        assertTrue(schemaService instanceof ContainerSchemaNode);

        final ContainerSchemaNode schemaServiceContainer = (ContainerSchemaNode) schemaService;

        assertEquals(1, schemaServiceContainer.getUses().size());
        final UsesNode uses = schemaServiceContainer.getUses().iterator().next();
        final QName groupingQName = QName.create("urn:opendaylight:params:xml:ns:yang:controller:config","2013-04-05","service-ref");
        final QName usesGroupingPathLastComponent = uses.getGroupingPath().getLastComponent();
        assertEquals(groupingQName, usesGroupingPathLastComponent);
        assertEquals(0, getChildNodeSizeWithoutUses(schemaServiceContainer));

        final DataSchemaNode type = schemaServiceContainer.getDataChildByName(QName.create(module.getQNameModule(), "type"));
        final List<UnknownSchemaNode> typeUnknownSchemaNodes = type
                .getUnknownSchemaNodes();

        assertEquals(1, typeUnknownSchemaNodes.size());

        final UnknownSchemaNode typeUnknownSchemaNode = typeUnknownSchemaNodes.get(0);

        final QNameModule qNameModule = QNameModule
                .create(new URI(
                        "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom"),
                        SimpleDateFormatUtil.getRevisionFormat().parse(
                                "2013-10-28"));
        final QName qName = QName.create(qNameModule, "schema-service");

        assertEquals(qName, typeUnknownSchemaNode.getQName());
        assertEquals(typeUnknownSchemaNode.getQName(), typeUnknownSchemaNode
                .getPath().getLastComponent());
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
