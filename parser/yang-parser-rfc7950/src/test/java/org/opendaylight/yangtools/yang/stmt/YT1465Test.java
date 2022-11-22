/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;

public class YT1465Test extends AbstractYangTest {
    @Test
    public void supportedLeafInChoiceAugment() throws Exception {
        final var baz = assertBaz(StmtTestUtils.parseYangSource("/bugs/YT1465/foo.yang", null));
        final var schemas = baz.schemaTreeNodes();
        assertEquals(2, schemas.size());
        final var it = schemas.iterator();

        final var first = it.next();
        assertThat(first, instanceOf(CaseEffectiveStatement.class));
        assertEquals(QName.create("foo", "one"), first.argument());

        final var second = it.next();
        assertThat(second, instanceOf(CaseEffectiveStatement.class));
        assertEquals(QName.create("foo", "two"), second.argument());
    }

    @Test
    public void unsupportedLeafInChoiceAugment() throws Exception {
        final var baz = assertBaz(StmtTestUtils.parseYangSource("/bugs/YT1465/foo.yang", Set.of()));
        final var schemas = baz.schemaTreeNodes();
        assertEquals(1, schemas.size());
        final var first = schemas.iterator().next();
        assertThat(first, instanceOf(CaseEffectiveStatement.class));
        assertEquals(QName.create("foo", "one"), first.argument());
    }

    private static ChoiceEffectiveStatement assertBaz(final EffectiveModelContext ctx) {
        final var foo = ctx.findModuleStatements("foo").iterator().next()
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create("foo", "foo"), foo.argument());

        final var bar = foo.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create("foo", "bar"), bar.argument());

        final var baz = bar.findFirstEffectiveSubstatement(ChoiceEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create("foo", "baz"), baz.argument());
        return baz;
    }
}
