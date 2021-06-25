/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal458Test {
    @Test
    public void testNestedClassFallback() {
        final List<GeneratedType> types = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal458.yang"));
        assertNotNull(types);
        assertEquals(3, types.size());

        final Set<JavaTypeName> typeNames = types.stream().map(GeneratedType::getIdentifier)
            .collect(Collectors.toSet());
        assertEquals(ImmutableSet.of(
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal458.norev", "ExportedTo"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal458.norev", "ExportedToExportedTo$Builder"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal458.norev", "Mdsal458Data")), typeNames);
    }
}
