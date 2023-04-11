/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal161Test {
    /**
     * Test if leaves with inner union type defined in groupings can be used as list keys at the place of instantiation.
     */
    @Test
    public void mdsal161Test() {
        final var types = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal161.yang"));
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
        final Optional<GeneratedType> optType = types.stream().filter(t -> t.getFullyQualifiedName().equals(className))
                .findFirst();
        assertTrue("Type for " + className + " not found", optType.isPresent());

        final GeneratedType type = optType.orElseThrow();
        assertThat(type, instanceOf(GeneratedTransferObject.class));
        final GeneratedTransferObject gto = (GeneratedTransferObject) type;
        assertEquals(2, gto.getEqualsIdentifiers().size());
    }
}
