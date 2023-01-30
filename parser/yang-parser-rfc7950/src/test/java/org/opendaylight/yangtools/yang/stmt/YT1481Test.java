/*
 * Copyright (c) 2022 Verizon and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueEffectiveStatement;

class YT1481Test extends AbstractYangTest {
    private static final QName FOO = QName.create("urn:foo", "foo");
    private static final QName BAR = QName.create("urn:foo", "bar");
    private static final QName BAZ = QName.create("urn:foo", "baz");
    private static final QName QUX = QName.create("urn:foo", "qux");

    @Test
    void testFooFeatureSupported() {
        assertBar("foo-feature");
    }

    @Test
    void testFooFeatureNotSupported() {
        assertThat(assertFooModule("foo-feature", Set.of()).effectiveSubstatements(),
            not(contains(instanceOf(ContainerEffectiveStatement.class))));
    }

    @Test
    void testBarFeatureSupported() {
        assertBar("bar-feature");
    }

    @Test
    void testBarFeatureNotSupported() {
        assertEquals(List.of(), assertFoo("bar-feature", Set.of()).effectiveSubstatements());
    }

    private static ModuleEffectiveStatement assertFooModule(final String dirName,
            final Set<QName> supportedFeatures) {
        return assertEffectiveModelDir("/bugs/YT1481/" + dirName, supportedFeatures)
            .findModuleStatement(FOO).orElseThrow();
    }

    private static ContainerEffectiveStatement assertFoo(final String dirName,
            final Set<QName> supportedFeatures) {
        final var foo = assertFooModule(dirName, supportedFeatures)
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(FOO, foo.argument());
        return foo;
    }

    private static void assertBar(final String dirName) {
        final var bar = assertFoo(dirName, null)
            .findFirstEffectiveSubstatement(ListEffectiveStatement.class).orElseThrow();
        assertEquals(BAR, bar.argument());

        final var baz = bar.findFirstEffectiveSubstatement(KeyEffectiveStatement.class).orElseThrow();
        assertEquals(Set.of(BAZ), baz.argument());

        final var qux = bar.findFirstEffectiveSubstatement(UniqueEffectiveStatement.class).orElseThrow();
        assertEquals(Set.of(Descendant.of(QUX)), qux.argument());
    }
}
