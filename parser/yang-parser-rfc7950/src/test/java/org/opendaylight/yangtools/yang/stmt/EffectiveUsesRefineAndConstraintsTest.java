/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Iterables;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

class EffectiveUsesRefineAndConstraintsTest extends AbstractYangTest {
    @Test
    void refineTest() {
        final var result = assertEffectiveModel("/stmt-test/uses/refine-test.yang");

        final var module = Iterables.getOnlyElement(result.getModuleStatements().values());
        final QNameModule qnameModule = module.localQNameModule();
        final QName rootContainer = QName.create(qnameModule, "root-container");

        final QName containerFromGrouping = QName.create(qnameModule, "container-from-grouping");
        final QName listInContainer = QName.create(qnameModule, "list-in-container");
        final QName choiceFromGrp = QName.create(qnameModule, "choice-from-grp");

        final QName containerFromGrouping2 = QName.create(qnameModule, "container-from-grouping2");
        final QName presenceContainer = QName.create(qnameModule, "presence-container");

        checkRefinedList(module, rootContainer, containerFromGrouping, listInContainer);
        checkRefinedChoice(module, rootContainer, containerFromGrouping, choiceFromGrp);
        checkRefinedContainer(module, rootContainer, containerFromGrouping2, presenceContainer);

        final var grp = module.findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create(qnameModule, "grp-1"), grp.argument());

        checkOriginalList(grp, containerFromGrouping, listInContainer);
        checkOriginalChoice(grp, containerFromGrouping, choiceFromGrp);
        checkOriginalContainer(grp, containerFromGrouping2, presenceContainer);
    }

    private static void checkOriginalContainer(final GroupingEffectiveStatement grp, final QName... qnames) {
        var containerSchemaNode = assertInstanceOf(ContainerSchemaNode.class,
            grp.findSchemaTreeNode(qnames).orElseThrow());
        assertFalse(containerSchemaNode.getReference().isPresent());
        assertFalse(containerSchemaNode.getDescription().isPresent());
        assertEquals(Optional.empty(), containerSchemaNode.effectiveConfig());
        assertFalse(containerSchemaNode.isPresenceContainer());

        assertEquals(0, containerSchemaNode.getMustConstraints().size());
    }

    private static void checkOriginalChoice(final GroupingEffectiveStatement grp, final QName... qnames) {
        var choiceSchemaNode = assertInstanceOf(ChoiceSchemaNode.class, grp.findSchemaTreeNode(qnames).orElseThrow());
        assertFalse(choiceSchemaNode.isMandatory());
    }

    private static void checkOriginalList(final GroupingEffectiveStatement grp, final QName... qnames) {
        final var listSchemaNode = assertInstanceOf(ListSchemaNode.class, grp.findSchemaTreeNode(qnames).orElseThrow());
        assertEquals(Optional.of("original reference"), listSchemaNode.getReference());
        assertEquals(Optional.of("original description"), listSchemaNode.getDescription());
        assertEquals(Optional.of(Boolean.FALSE), listSchemaNode.effectiveConfig());

        var listConstraints = listSchemaNode.getElementCountConstraint().orElseThrow();
        assertEquals((Object) 10, listConstraints.getMinElements());
        assertEquals((Object) 20, listConstraints.getMaxElements());
        assertEquals(1, listSchemaNode.getMustConstraints().size());
    }

    private static void checkRefinedContainer(final ModuleEffectiveStatement module, final QName... qnames) {
        final var containerSchemaNode = assertInstanceOf(ContainerSchemaNode.class,
            module.findSchemaTreeNode(qnames).orElseThrow());
        assertEquals(Optional.of("new reference"), containerSchemaNode.getReference());
        assertEquals(Optional.of("new description"), containerSchemaNode.getDescription());
        assertEquals(Optional.of(Boolean.TRUE), containerSchemaNode.effectiveConfig());
        assertTrue(containerSchemaNode.isPresenceContainer());
        assertEquals(1, containerSchemaNode.getMustConstraints().size());
    }

    private static void checkRefinedChoice(final ModuleEffectiveStatement module, final QName... qnames) {
        final var choiceSchemaNode = assertInstanceOf(ChoiceSchemaNode.class,
            module.findSchemaTreeNode(qnames).orElseThrow());
        assertTrue(choiceSchemaNode.isMandatory());
    }

    private static void checkRefinedList(final ModuleEffectiveStatement module, final QName... qnames) {
        final var listSchemaNode = assertInstanceOf(ListSchemaNode.class,
            module.findSchemaTreeNode(qnames).orElseThrow());
        assertEquals(Optional.of("new reference"), listSchemaNode.getReference());
        assertEquals(Optional.of("new description"), listSchemaNode.getDescription());
        assertEquals(Optional.of(Boolean.TRUE), listSchemaNode.effectiveConfig());

        final var listConstraints = listSchemaNode.getElementCountConstraint().orElseThrow();
        assertEquals((Object) 5, listConstraints.getMinElements());
        assertEquals((Object) 7, listConstraints.getMaxElements());
        assertEquals(2, listSchemaNode.getMustConstraints().size());
    }
}
