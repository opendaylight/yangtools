/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal552Test {
    private static final JavaTypeName BAR_INPUT =
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal552.norev", "BarInput");
    private static final JavaTypeName BAZ =
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal552.norev", "Baz");
    private static final JavaTypeName ENUMERATION =
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal552.norev", "Mdsal552Data").createEnclosed("Foo");

    @Test
    public void enumLeafrefTest() {
        final List<GeneratedType> types = DefaultBindingGenerator.generateFor(
                YangParserTestUtils.parseYangResource("/mdsal552.yang"));
        assertNotNull(types);
        assertEquals(5, types.size());

        final GeneratedType baz = types.stream()
                .filter(type -> BAZ.equals(type.getIdentifier()))
                .findFirst().orElseThrow();
        final MethodSignature bazGetRef = baz.getMethodDefinitions().stream()
                .filter(method -> method.getName().equals("getRef"))
                .findFirst().orElseThrow();
        assertEquals(ENUMERATION, bazGetRef.getReturnType().getIdentifier());

        final GeneratedType input = types.stream()
                .filter(type -> BAR_INPUT.equals(type.getIdentifier()))
                .findFirst().orElseThrow();
        final MethodSignature inputGetRef = input.getMethodDefinitions().stream()
                .filter(method -> method.getName().equals("getRef"))
                .findFirst().orElseThrow();
        assertEquals(ENUMERATION, inputGetRef.getReturnType().getIdentifier());
    }
}
