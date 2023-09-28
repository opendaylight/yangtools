/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class PreferredPrefixesTest {
    private static final @NonNull XMLNamespace FOONS = XMLNamespace.of("foons");
    private static final @NonNull XMLNamespace BARNS = XMLNamespace.of("barns");

    @Test
    void ignorePrefixWhenConflicting() {
        final var context = YangParserTestUtils.parseYang("""
            module foo {
              namespace foons;
              prefix conflict;
            }""", """
            module bar {
              namespace barns;
              prefix conflict;
            }
            """);
        final var prefixes = new PreferredPrefixes.Shared(context);
        assertNull(prefixes.prefixForNamespace(FOONS));
        assertNull(prefixes.prefixForNamespace(BARNS));
        assertEquals(Map.of(FOONS, Optional.empty(), BARNS, Optional.empty()), prefixes.mappings());
        assertEquals("Precomputed{mappings={}}", prefixes.toPrecomputed().toString());
    }

    @Test
    void bindPrefixAcrossRevisions() {
        final var context = YangParserTestUtils.parseYang("""
            module foo {
              namespace foons;
              prefix f;
            }""", """
            module foo2 {
              namespace foons;
              prefix f;
              revision 2023-09-29;
            }
            """);
        final var prefixes = new PreferredPrefixes.Shared(context);
        assertEquals("f", prefixes.prefixForNamespace(FOONS));
        assertEquals("Shared{mappings={foons=Optional[f]}}", prefixes.toString());
        assertEquals("Precomputed{mappings={foons=f}}", prefixes.toPrecomputed().toString());
    }
}
