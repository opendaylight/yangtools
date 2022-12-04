/*
 * Copyright (c) 2022 Verizon and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;

public class YT1471Test extends AbstractYangTest {
    @Test
    public void testAugmentSingleGroupingWithFeatureSupported() throws Exception {
        assertFoo(StmtTestUtils.parseYangSource(
                "/bugs/YT1471/augment-if-feature-single-grouping/foo.yang", null), true);
    }

    @Test
    public void testAugmentSingleGroupingWithFeatureNotSupported() throws Exception {
        assertFoo(StmtTestUtils.parseYangSource(
                "/bugs/YT1471/augment-if-feature-single-grouping/foo.yang", Set.of()), false);
    }

    @Test
    public void testAugmentNestedGroupingWithFeatureSupported() throws Exception {
        assertFoo(StmtTestUtils.parseYangSource(
                "/bugs/YT1471/augment-if-feature-nested-grouping/foo.yang", null), true);
    }

    @Test
    public void testAugmentNestedGroupingWithFeatureNotSupported() throws Exception {
        assertFoo(StmtTestUtils.parseYangSource(
                "/bugs/YT1471/augment-if-feature-nested-grouping/foo.yang", Set.of()), false);
    }

    private static void assertFoo(final EffectiveModelContext ctx, final boolean supported) {
        final var foo = ctx.findModuleStatement(QName.create("urn:foo", "foo")).orElseThrow()
                .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create("urn:foo", "foo"), foo.argument());

        if (supported) {
            final var bar = foo.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
            assertEquals(QName.create("urn:foo", "bar"), bar.argument());

            final var baz = bar.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
            assertEquals(QName.create("urn:foo", "baz"), baz.argument());

            final var bazLeaf = baz.findFirstEffectiveSubstatement(LeafEffectiveStatement.class).orElseThrow();
            assertEquals(QName.create("urn:foo", "baz-leaf"), bazLeaf.argument());
        } else {
            assertEquals(List.of(), foo.effectiveSubstatements());
        }
    }
}
