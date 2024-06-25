/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal269Test {
    @Test
    void mdsal269Test() {
        final var generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal269.yang"));
        assertEquals(4, generateTypes.size());

        final var mplsLabelType = generateTypes.stream().filter(type -> type.getFullyQualifiedName()
            .equals("org.opendaylight.yang.gen.v1.mdsal269.rev180130.MplsLabel")).findFirst().orElseThrow();

        final var gto = assertInstanceOf(GeneratedTransferObject.class, mplsLabelType);
        final var it = gto.getEqualsIdentifiers().iterator();
        final var special = it.next();
        final var general = it.next();
        assertFalse(it.hasNext());

        assertEquals("mplsLabelGeneralUse", general.getName());
        assertEquals("org.opendaylight.yang.gen.v1.mdsal269.rev180130.MplsLabelGeneralUse",
            general.getReturnType().getFullyQualifiedName());

        assertEquals("mplsLabelSpecialPurpose", special.getName());
        assertEquals("org.opendaylight.yang.gen.v1.mdsal269.rev180130.MplsLabelSpecialPurposeValue",
            special.getReturnType().getFullyQualifiedName());
    }
}
