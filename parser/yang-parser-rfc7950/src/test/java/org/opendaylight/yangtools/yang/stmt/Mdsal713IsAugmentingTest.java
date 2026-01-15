/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
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

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;

class Mdsal713IsAugmentingTest {
    @Test
    void testIsAugmentingForShorthandCase() throws Exception {
        final var schemaContext = TestUtils.parseYangSource(
                "/augment-test/augment-choice-shorthand-case/base.yang",
                "/augment-test/augment-choice-shorthand-case/augment-base.yang");

        assertNotNull(schemaContext, "Failure in yang source parsing");

        final var baseModule = schemaContext.findModule("base", Revision.of("2023-06-06")).orElseThrow();
        final var topCont = assertInstanceOf(ContainerSchemaNode.class,
            baseModule.dataChildByName(QName.create(baseModule.getQNameModule(), "top")));
        final var augmentedChoice = assertInstanceOf(ChoiceEffectiveStatement.class,
            topCont.dataChildByName(QName.create(baseModule.getQNameModule(), "options")));
        final var cases = augmentedChoice.collectEffectiveSubstatements(CaseEffectiveStatement.class);
        final var augmentedCases = cases.stream()
                .filter(c -> c instanceof CopyableNode copy && copy.isAugmenting())
                .toList();
        assertNotNull(augmentedCases);

        for (var caze : augmentedCases) {
            // FIXME: this check ensures the test does not fail. We DO WANT to also check the UndeclaredCase.
            //  Shorthand case https://datatracker.ietf.org/doc/html/rfc7950#section-7.9.2 (UndeclaredCaseEffectiveStmt)
            //  is reported as introduced by augmentation, however its child is NOT (shorthand case has max 1 child).
            //  This is different for DeclaredCaseEffectiveStatement whose children are.
            //  .
            //  Audit this behavior and either fix and remove the check or remove this test altogether
            if (caze.declared() == null) {
                showChildNotFromAugmentation(caze);
            } else {
                // check whether the children of DeclaredCaseEffectiveStatement are also from augmentation
                caze.effectiveSubstatements().stream()
                        .filter(CopyableNode.class::isInstance)
                        .forEach(s -> assertTrue(((CopyableNode) s).isAugmenting(), "The case " + caze
                                + " was introduced by augmentation. So was its child, but reports otherwise"));
            }
        }
    }

    private static void showChildNotFromAugmentation(final CaseEffectiveStatement caze) {
        /* shorthand case (UndeclaredCaseEffectiveStatement) has just 1 child, which is the statement,
         * that is used in the yang file to declare this shorthand case
         *
         * this assertion demonstrates how the child of shorthand case (which was introduced by augmentation)
         * is NOT reported to be from augmentation
         */
        assertEquals(1, caze.effectiveSubstatements().size(), "Shorthand case must have just 1 substatement");
        assertTrue(caze.effectiveSubstatements().getFirst() instanceof CopyableNode copy && !copy.isAugmenting());
    }

}