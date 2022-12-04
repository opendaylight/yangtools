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
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;

public class YT1471Test extends AbstractYangTest {
    @Test
    public void testAugmentSingleGroupingWithFeatureSupported() {
        assertSupportedFoo("single");
    }

    @Test
    public void testAugmentSingleGroupingWithFeatureNotSupported() {
        final var foo = assertFoo("single", Set.of());
        assertEquals(List.of(), foo.effectiveSubstatements());
    }

    @Test
    public void testAugmentNestedGroupingWithFeatureSupported() {
        assertSupportedFoo("nested");
    }

    @Test
    public void testAugmentNestedGroupingWithFeatureNotSupported() {
        assertThat(assertFoo("nested", Set.of()).effectiveSubstatements(),
            contains(instanceOf(UsesEffectiveStatement.class)));
    }

    private static ContainerEffectiveStatement assertFoo(final String dirName, final Set<QName> supportedFeatures) {
        final var foo = assertEffectiveModelDir("/bugs/YT1471/" + dirName, supportedFeatures)
            .findModuleStatement(QName.create("urn:foo", "foo")).orElseThrow()
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create("urn:foo", "foo"), foo.argument());
        return foo;
    }

    private static void assertSupportedFoo(final String dirName) {
        final var foo = assertFoo(dirName, null);
        final var bar = foo.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create("urn:foo", "bar"), bar.argument());

        final var baz = bar.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create("urn:foo", "baz"), baz.argument());

        final var bazLeaf = baz.findFirstEffectiveSubstatement(LeafEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create("urn:foo", "baz-leaf"), bazLeaf.argument());
    }
}
