/*
 * Copyright (c) 2022 Verizon and others. All rights reserved.
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

import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;

public class YT1471Test extends AbstractYangTest {
    @Test
    public void testAugmentSingleGroupingWithFeatureSupported() throws Exception {
        assertSupportedFoo(StmtTestUtils.parseYangSource(
            "/bugs/YT1471/augment-if-feature-single-grouping/foo.yang", null));
    }

    @Test
    public void testAugmentSingleGroupingWithFeatureNotSupported() throws Exception {
        final var foo = assertFoo(StmtTestUtils.parseYangSource(
            "/bugs/YT1471/augment-if-feature-single-grouping/foo.yang", Set.of()));
        assertEquals(List.of(), foo.effectiveSubstatements());
    }

    @Test
    public void testAugmentNestedGroupingWithFeatureSupported() throws Exception {
        assertSupportedFoo(StmtTestUtils.parseYangSource(
            "/bugs/YT1471/augment-if-feature-nested-grouping/foo.yang", null));
    }

    @Test
    public void testAugmentNestedGroupingWithFeatureNotSupported() throws Exception {
        final var it = assertFoo(StmtTestUtils.parseYangSource(
            "/bugs/YT1471/augment-if-feature-nested-grouping/foo.yang", Set.of())).effectiveSubstatements().iterator();
        final var first = it.next();
        assertThat(first, instanceOf(UsesEffectiveStatement.class));
        assertFalse(it.hasNext());
    }

    private static ContainerEffectiveStatement assertFoo(final EffectiveModelContext ctx) {
        final var foo = ctx.findModuleStatement(QName.create("urn:foo", "foo")).orElseThrow()
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create("urn:foo", "foo"), foo.argument());
        return foo;
    }

    private static void assertSupportedFoo(final EffectiveModelContext ctx) {
        final var foo = assertFoo(ctx);
        final var bar = foo.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create("urn:foo", "bar"), bar.argument());

        final var baz = bar.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create("urn:foo", "baz"), baz.argument());

        final var bazLeaf = baz.findFirstEffectiveSubstatement(LeafEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create("urn:foo", "baz-leaf"), bazLeaf.argument());
    }
}
