/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;

class YT1383Test extends AbstractYangTest {
    @Test
    void testDeclaredImplicitCase() {
        final var foo = assertEffectiveModel("/bugs/YT1383/foo.yang").getModuleStatements().values().iterator()
            .next().findFirstEffectiveSubstatement(ChoiceEffectiveStatement.class).orElseThrow();

        // Effective view of things
        final var effStatements = foo.effectiveSubstatements();
        assertEquals(2, effStatements.size());
        final var bar = effStatements.get(0);
        assertThat(bar, instanceOf(CaseEffectiveStatement.class));
        assertNotNull(bar.getDeclared());
        final var baz = effStatements.get(1);
        assertThat(baz, instanceOf(CaseEffectiveStatement.class));
        assertNull(baz.getDeclared());

        // Declared view of things
        final var fooDecl = foo.getDeclared();
        assertNotNull(fooDecl);
        final var declStatements = fooDecl.declaredSubstatements();
        assertEquals(2, declStatements.size());
        final var barDecl = declStatements.get(0);
        assertThat(barDecl, instanceOf(CaseStatement.class));
        final var bazDecl = declStatements.get(1);
        assertThat(bazDecl, instanceOf(ContainerStatement.class));
    }
}
