/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.google.common.collect.Iterables;
import java.util.Iterator;
import org.junit.jupiter.api.Test;
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

class YT1089Test extends AbstractYangTest {
    @Test
    void testPlusLexing() {
        final EffectiveModelContext ctx = assertEffectiveModel("/bugs/YT1089/foo.yang");
        assertEquals(1, ctx.getModuleStatements().size());

        final Iterator<? extends EffectiveStatement<?, ?>> it =
            Iterables.getOnlyElement(ctx.getModuleStatements().values()).effectiveSubstatements().iterator();

        assertInstanceOf(NamespaceEffectiveStatement.class, it.next());
        assertInstanceOf(PrefixEffectiveStatement.class, it.next());

        EffectiveStatement<?, ?> stmt = it.next();
        assertInstanceOf(DescriptionEffectiveStatement.class, stmt);
        assertEquals("+something", stmt.argument());

        stmt = it.next();
        assertInstanceOf(ContactEffectiveStatement.class, stmt);
        assertEquals("contact++", stmt.argument());

        stmt = it.next();
        assertInstanceOf(OrganizationEffectiveStatement.class, stmt);
        assertEquals("organiza++tion", stmt.argument());

        assertFoo(it.next());
        assertBar(it.next());
        assertBaz(it.next());
        assertXyzzy(it.next());
        assertFalse(it.hasNext());
    }

    private static void assertFoo(final EffectiveStatement<?, ?> stmt) {
        assertInstanceOf(LeafEffectiveStatement.class, stmt);
        assertEquals(QName.create("urn:foo", "foo"), stmt.argument());

        final Iterator<? extends EffectiveStatement<?, ?>> it = stmt.effectiveSubstatements().iterator();
        assertInstanceOf(TypeEffectiveStatement.class, it.next());
        assertEquals("+", it.next().argument());
        assertEquals("squotdquot", it.next().argument());
        assertEquals("++", it.next().argument());
        assertFalse(it.hasNext());
    }

    private static void assertBar(final EffectiveStatement<?, ?> stmt) {
        assertInstanceOf(LeafEffectiveStatement.class, stmt);
        assertEquals(QName.create("urn:foo", "bar"), stmt.argument());
        final Iterator<? extends EffectiveStatement<?, ?>> it = stmt.effectiveSubstatements().iterator();
        assertInstanceOf(TypeEffectiveStatement.class, it.next());
        assertEquals("++", it.next().argument());
        assertEquals("+ + ++", it.next().argument());
        assertEquals("++ + +", it.next().argument());
        assertFalse(it.hasNext());
    }

    private static void assertBaz(final EffectiveStatement<?, ?> stmt) {
        assertInstanceOf(LeafEffectiveStatement.class, stmt);
        assertEquals(QName.create("urn:foo", "baz"), stmt.argument());
        final Iterator<? extends EffectiveStatement<?, ?>> it = stmt.effectiveSubstatements().iterator();
        assertInstanceOf(TypeEffectiveStatement.class, it.next());
        assertEquals("/", it.next().argument());
        assertEquals(":", it.next().argument());
        assertEquals("*", it.next().argument());
        assertFalse(it.hasNext());
    }

    private static void assertXyzzy(final EffectiveStatement<?, ?> stmt) {
        assertInstanceOf(LeafEffectiveStatement.class, stmt);
        assertEquals(QName.create("urn:foo", "xyzzy"), stmt.argument());
        final Iterator<? extends EffectiveStatement<?, ?>> it = stmt.effectiveSubstatements().iterator();
        assertInstanceOf(TypeEffectiveStatement.class, it.next());
        assertEquals("a weird concat", it.next().argument());
        assertEquals("another weird concat", it.next().argument());
        assertFalse(it.hasNext());
    }
}
