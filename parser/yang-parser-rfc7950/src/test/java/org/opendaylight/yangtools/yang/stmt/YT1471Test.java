/*
 * Copyright (c) 2022 Verizon and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;

class YT1471Test extends AbstractYangTest {
    private static final QName FOO = QName.create("urn:foo", "foo");
    private static final QName BAR = QName.create("urn:foo", "bar");
    private static final QName BAZ = QName.create("urn:foo", "baz");
    private static final QName BAZ_LEAF = QName.create("urn:foo", "baz-leaf");

    @Test
    void testAugmentSingleGroupingWithFeatureSupported() {
        assertSupportedFoo("single");
    }

    @Test
    void testAugmentSingleGroupingWithFeatureNotSupported() {
        assertEquals(List.of(), assertFoo("single", Set.of()).effectiveSubstatements());
    }

    @Test
    void testAugmentNestedGroupingWithFeatureSupported() {
        assertSupportedFoo("nested");
    }

    @Test
    void testAugmentNestedGroupingWithFeatureNotSupported() {
        assertThat(assertFoo("nested", Set.of()).effectiveSubstatements())
            .anyMatch(UsesEffectiveStatement.class::isInstance);
    }

    private static ContainerEffectiveStatement assertFoo(final String dirName, final Set<QName> supportedFeatures) {
        final var foo = assertEffectiveModelDir("/bugs/YT1471/" + dirName, supportedFeatures)
            .findModuleStatement(FOO).orElseThrow()
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(FOO, foo.argument());
        return foo;
    }

    private static void assertSupportedFoo(final String dirName) {
        final var bar = assertFoo(dirName, null)
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(BAR, bar.argument());

        final var baz = bar.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(BAZ, baz.argument());

        final var bazLeaf = baz.findFirstEffectiveSubstatement(LeafEffectiveStatement.class).orElseThrow();
        assertEquals(BAZ_LEAF, bazLeaf.argument());
    }
}
