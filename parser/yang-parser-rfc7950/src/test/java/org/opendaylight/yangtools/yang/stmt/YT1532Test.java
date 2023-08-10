/*
 * Copyright (c) 2023 Verizon and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

class YT1532Test extends AbstractYangTest {
    private static final QName FOO = QName.create("urn:foo", "foo");
    private static final QName BAR1 = QName.create("urn:foo", "bar1");
    private static final QName BAR2 = QName.create("urn:foo", "bar2");
    private static final QName BAZ = QName.create("urn:foo", "baz");

    @ParameterizedTest(name = "Augmentation of grouping with {0}")
    @MethodSource("generateArgs")
    void assertFoo(final String testDesc, final String dirName, final Set<QName> expectedLeafs) {
        final var foo = assertFooModule(dirName)
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(FOO, foo.argument());
        assertEquals(expectedLeafs, findLeafs(foo));
    }

    private static Stream<Arguments> generateArgs() {
        return Stream.of(
            Arguments.of("description and no leaf with if-feature",
                "augment-with-description-no-leaf-with-if-feature",
                Set.of(BAR1, BAR2, BAZ)),
            Arguments.of("no description and all leafs with if-feature",
                "augment-with-no-description-all-leafs-with-if-feature",
                Set.of(BAZ)),
            Arguments.of("description and all leafs with if-feature",
                "augment-with-description-all-leafs-with-if-feature",
                Set.of(BAZ)),
            Arguments.of("no description and one leaf with if-feature",
                "augment-with-no-description-one-leaf-with-if-feature",
                Set.of(BAR2, BAZ)));
    }

    private static ModuleEffectiveStatement assertFooModule(final String dirName) {
        return assertEffectiveModelDir("/bugs/YT1532/" + dirName, Set.of())
            .findModuleStatement(FOO).orElseThrow();
    }

    private static Set<Object> findLeafs(final ContainerEffectiveStatement foo) {
        return foo.effectiveSubstatements().stream().filter(LeafEffectiveStatement.class::isInstance)
            .map(LeafEffectiveStatement.class::cast)
            .map(LeafEffectiveStatement::argument)
            .collect(Collectors.toSet());
    }
}
