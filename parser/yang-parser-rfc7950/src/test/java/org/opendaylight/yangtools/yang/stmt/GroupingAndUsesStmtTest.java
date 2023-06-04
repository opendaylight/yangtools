/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;

class GroupingAndUsesStmtTest extends AbstractYangTest {
    private static final String MODULE = "/model/bar.yang";
    private static final String SUBMODULE = "/model/subfoo.yang";
    private static final String GROUPING_MODULE = "/model/baz.yang";
    private static final String USES_MODULE = "/model/foo.yang";

    @Test
    void groupingTest() {
        final var result = assertEffectiveModel(MODULE, GROUPING_MODULE);

        final Module testModule = result.findModules("baz").iterator().next();
        assertNotNull(testModule);

        final var groupings = testModule.getGroupings();
        assertEquals(1, groupings.size());

        final GroupingDefinition grouping = groupings.iterator().next();
        assertEquals("target", grouping.getQName().getLocalName());
        assertEquals(5, grouping.getChildNodes().size());

        final AnyxmlSchemaNode anyXmlNode = (AnyxmlSchemaNode) grouping.getDataChildByName(
            QName.create(testModule.getQNameModule(), "data"));
        assertNotNull(anyXmlNode);
        final ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) grouping.getDataChildByName(
            QName.create(testModule.getQNameModule(), "how"));
        assertNotNull(choiceNode);
        final LeafSchemaNode leafNode = (LeafSchemaNode) grouping.getDataChildByName(
            QName.create(testModule.getQNameModule(), "address"));
        assertNotNull(leafNode);
        final ContainerSchemaNode containerNode = (ContainerSchemaNode) grouping.getDataChildByName(
            QName.create(testModule.getQNameModule(), "port"));
        assertNotNull(containerNode);
        final ListSchemaNode listNode = (ListSchemaNode) grouping.getDataChildByName(
            QName.create(testModule.getQNameModule(), "addresses"));
        assertNotNull(listNode);

        assertEquals(1, grouping.getGroupings().size());
        assertEquals("target-inner", grouping.getGroupings().iterator().next().getQName().getLocalName());

        assertEquals(1, grouping.getTypeDefinitions().size());
        assertEquals("group-type", grouping.getTypeDefinitions().iterator().next().getQName().getLocalName());

        final var unknownSchemaNodes = grouping.asEffectiveStatement().getDeclared()
            .declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, unknownSchemaNodes.size());
        final UnrecognizedStatement extensionUse = unknownSchemaNodes.iterator().next();
        assertEquals("opendaylight", extensionUse.statementDefinition().getStatementName().getLocalName());
    }

    @Test
    void usesAndRefinesTest() {
        final var result = assertEffectiveModel(MODULE, SUBMODULE, GROUPING_MODULE, USES_MODULE);

        final Module testModule = result.findModules("foo").iterator().next();

        final var usesNodes = testModule.getUses();
        assertEquals(1, usesNodes.size());

        UsesNode usesNode = usesNodes.iterator().next();
        assertEquals("target", usesNode.getSourceGrouping().getQName().getLocalName());
        assertEquals(1, usesNode.getAugmentations().size());

        QName peer = QName.create(testModule.getQNameModule(), "peer");
        ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName(peer);
        assertNotNull(container);
        container = (ContainerSchemaNode) container.getDataChildByName(QName.create(peer, "destination"));
        assertEquals(1, container.getUses().size());

        usesNode = container.getUses().iterator().next();
        assertEquals("target", usesNode.getSourceGrouping().getQName().getLocalName());

        assertEquals(List.of(
                Descendant.of(QName.create(peer, "address")),
                Descendant.of(QName.create(peer, "port")),
                Descendant.of(QName.create(peer, "addresses")),
                Descendant.of(QName.create(peer, "addresses"), QName.create(peer, "id"))),
            List.copyOf(usesNode.getRefines()));
    }
}
