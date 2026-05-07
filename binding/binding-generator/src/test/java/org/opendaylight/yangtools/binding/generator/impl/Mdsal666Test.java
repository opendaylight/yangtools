/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.ModulePackageName;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@NonNullByDefault
class Mdsal666Test {
    private static final ModulePackageName FOO_PKG = ModulePackageName.of(QNameModule.of("foo"));
    private static final TypeName FOO = TypeName.of(FOO_PKG, "Foo");
    private static final TypeName FOO_GRP = TypeName.of(FOO_PKG, "Foo$G");
    private static final TypeName BAZ_GRP = TypeName.of(FOO_PKG.subPackage("bar"), "Baz$G");

    @Test
    void rpcPushesGrouping() {
        final var generatedNames = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal666.yang")).stream().map(Archetype::name).toList();
        assertEquals(10, generatedNames.size());

        // 'rpc foo' ...
        assertThat(generatedNames).anyMatch(FOO::equals);
        // ... grouping is relocated for 'rpc foo' ...
        assertThat(generatedNames).anyMatch(FOO_GRP::equals);
        // .. and 'action baz'
        assertThat(generatedNames).anyMatch(BAZ_GRP::equals);
    }
}
