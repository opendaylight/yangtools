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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
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

        final var testModule = result.findModules("baz").iterator().next();
        assertNotNull(testModule);

        final var groupings = testModule.getGroupings();
        assertEquals(1, groupings.size());

        final var grouping = groupings.iterator().next();
        assertEquals("target", grouping.getQName().getLocalName());
        assertEquals(5, grouping.getChildNodes().size());

        assertInstanceOf(AnyxmlSchemaNode.class,
            grouping.getDataChildByName(QName.create(testModule.getQNameModule(), "data")));
        assertInstanceOf(ChoiceSchemaNode.class,
            grouping.getDataChildByName(QName.create(testModule.getQNameModule(), "how")));
        assertInstanceOf(LeafSchemaNode.class,
            grouping.getDataChildByName(QName.create(testModule.getQNameModule(), "address")));
        assertInstanceOf(ContainerSchemaNode.class,
            grouping.getDataChildByName(QName.create(testModule.getQNameModule(), "port")));
        assertInstanceOf(ListSchemaNode.class,
            grouping.getDataChildByName(QName.create(testModule.getQNameModule(), "addresses")));

        assertEquals(1, grouping.getGroupings().size());
        assertEquals("target-inner", grouping.getGroupings().iterator().next().getQName().getLocalName());

        assertEquals(1, grouping.getTypeDefinitions().size());
        assertEquals("group-type", grouping.getTypeDefinitions().iterator().next().getQName().getLocalName());

        final var unknownSchemaNodes = grouping.asEffectiveStatement().requireDeclared()
            .declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, unknownSchemaNodes.size());
        final var extensionUse = unknownSchemaNodes.iterator().next();
        assertEquals("opendaylight", extensionUse.statementDefinition().statementName().getLocalName());
    }

    @Test
    void usesAndRefinesTest() {
        final var result = assertEffectiveModel(MODULE, SUBMODULE, GROUPING_MODULE, USES_MODULE);

        final var testModule = result.findModules("foo").iterator().next();
        final var usesNodes = testModule.getUses();
        assertEquals(1, usesNodes.size());

        var usesNode = usesNodes.iterator().next();
        assertEquals("target", usesNode.getSourceGrouping().getQName().getLocalName());
        assertEquals(1, usesNode.getAugmentations().size());

        var peer = QName.create(testModule.getQNameModule(), "peer");
        var container = assertInstanceOf(ContainerSchemaNode.class, testModule.getDataChildByName(peer));
        container = assertInstanceOf(ContainerSchemaNode.class,
            container.getDataChildByName(QName.create(peer, "destination")));
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
