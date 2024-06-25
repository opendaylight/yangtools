/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal161Test {
    /**
     * Test if leaves with inner union type defined in groupings can be used as list keys at the place of instantiation.
     */
    @Test
    void mdsal161Test() {
        final var types = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource("/mdsal161.yang"));
        assertNotNull(types);
        assertEquals(20, types.size());

        assertKeyStructure(types, "org.opendaylight.yang.gen.v1.mdsal161.norev.WithGrpKey");
        assertKeyStructure(types, "org.opendaylight.yang.gen.v1.mdsal161.norev.WithGrpExtKey");
        assertKeyStructure(types, "org.opendaylight.yang.gen.v1.mdsal161.norev.WithGrpTypedefKey");
        assertKeyStructure(types, "org.opendaylight.yang.gen.v1.mdsal161.norev.WithoutGrpKey");
        assertKeyStructure(types, "org.opendaylight.yang.gen.v1.mdsal161.norev.WithoutGrpExtKey");
        assertKeyStructure(types, "org.opendaylight.yang.gen.v1.mdsal161.norev.WithoutGrpTypedefKey");
    }

    private static void assertKeyStructure(final Collection<GeneratedType> types, final String className) {
        final var optType = types.stream().filter(t -> t.getFullyQualifiedName().equals(className)).findFirst();
        final var gto = assertInstanceOf(GeneratedTransferObject.class, optType.orElseThrow());
        assertEquals(2, gto.getEqualsIdentifiers().size());
    }
}
