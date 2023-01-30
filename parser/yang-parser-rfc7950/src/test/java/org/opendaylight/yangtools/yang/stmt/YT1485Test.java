/*
 * Copyright (c) 2023 Verizon and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;

class YT1485Test extends AbstractYangTest {
    private static final QName FOO = QName.create("urn:foo", "foo");

    @Test
    void testLeafFooFeatureSupported() {
        assertEquals(List.of(), assertFoo("leaf", null).effectiveSubstatements());
    }

    @Test
    void testLeafFooFeatureNotSupported() {
        assertEquals(List.of(), assertFoo("leaf", Set.of()).effectiveSubstatements());
    }

    @Test
    void testAugmentFooFeatureSupported() {
        assertThat(assertFoo("augment", null).effectiveSubstatements(),
            not(hasItem(instanceOf(LeafEffectiveStatement.class))));
    }

    @Test
    void testAugmentFooFeatureNotSupported() {
        assertEquals(List.of(), assertFoo("augment", Set.of()).effectiveSubstatements());
    }

    private static ContainerEffectiveStatement assertFoo(final String dirName,
            final Set<QName> supportedFeatures) {
        final var foo = assertEffectiveModelDir("/bugs/YT1485/" + dirName, supportedFeatures)
            .findModuleStatement(FOO).orElseThrow()
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(FOO, foo.argument());
        return foo;
    }
}
