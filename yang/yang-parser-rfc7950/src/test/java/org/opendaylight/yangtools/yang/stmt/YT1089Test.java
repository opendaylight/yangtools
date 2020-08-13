/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.collect.Iterables;
import java.util.Iterator;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;

public class YT1089Test {
    @Test
    public void testPlusLexing() throws Exception {
        final EffectiveModelContext ctx = StmtTestUtils.parseYangSource("/bugs/YT1089/foo.yang");
        assertEquals(1, ctx.getModuleStatements().size());

        final Iterator<? extends EffectiveStatement<?, ?>> it =
                Iterables.getOnlyElement(ctx.getModuleStatements().values()).effectiveSubstatements().iterator();

        assertThat(it.next(), instanceOf(NamespaceEffectiveStatement.class));
        assertThat(it.next(), instanceOf(PrefixEffectiveStatement.class));

        EffectiveStatement<?, ?> stmt = it.next();
        assertThat(stmt, instanceOf(DescriptionEffectiveStatement.class));
        assertEquals("+something", stmt.argument());

        stmt = it.next();
        assertThat(stmt, instanceOf(ContactEffectiveStatement.class));
        assertEquals("contact++", stmt.argument());

        stmt = it.next();
        assertThat(stmt, instanceOf(OrganizationEffectiveStatement.class));
        assertEquals("organiza++tion", stmt.argument());

        assertFoo(it.next());
        assertBar(it.next());
        assertBaz(it.next());
        assertXyzzy(it.next());
        assertFalse(it.hasNext());
    }

     private static void assertFoo(final EffectiveStatement<?, ?> stmt) {
        assertThat(stmt, instanceOf(LeafEffectiveStatement.class));
        assertEquals(QName.create("urn:foo", "foo"), stmt.argument());

        final Iterator<? extends EffectiveStatement<?, ?>> it = stmt.effectiveSubstatements().iterator();
        assertThat(it.next(), instanceOf(TypeEffectiveStatement.class));
        assertEquals("+", it.next().argument());
        assertEquals("squotdquot", it.next().argument());
        assertEquals("++", it.next().argument());
        assertFalse(it.hasNext());
    }

    private static void assertBar(final EffectiveStatement<?, ?> stmt) {
        assertThat(stmt, instanceOf(LeafEffectiveStatement.class));
        assertEquals(QName.create("urn:foo", "bar"), stmt.argument());
        final Iterator<? extends EffectiveStatement<?, ?>> it = stmt.effectiveSubstatements().iterator();
        assertThat(it.next(), instanceOf(TypeEffectiveStatement.class));
        assertEquals("++", it.next().argument());
        assertEquals("+ + ++", it.next().argument());
        assertEquals("++ + +", it.next().argument());
        assertFalse(it.hasNext());
    }

    private static void assertBaz(final EffectiveStatement<?, ?> stmt) {
        assertThat(stmt, instanceOf(LeafEffectiveStatement.class));
        assertEquals(QName.create("urn:foo", "baz"), stmt.argument());
        final Iterator<? extends EffectiveStatement<?, ?>> it = stmt.effectiveSubstatements().iterator();
        assertThat(it.next(), instanceOf(TypeEffectiveStatement.class));
        assertEquals("/", it.next().argument());
        assertEquals(":", it.next().argument());
        assertEquals("*", it.next().argument());
        assertFalse(it.hasNext());
    }

    private static void assertXyzzy(final EffectiveStatement<?, ?> stmt) {
        assertThat(stmt, instanceOf(LeafEffectiveStatement.class));
        assertEquals(QName.create("urn:foo", "xyzzy"), stmt.argument());
        final Iterator<? extends EffectiveStatement<?, ?>> it = stmt.effectiveSubstatements().iterator();
        assertThat(it.next(), instanceOf(TypeEffectiveStatement.class));
        assertEquals("a weird concat", it.next().argument());
        assertEquals("another weird concat", it.next().argument());
        assertFalse(it.hasNext());
    }
}
