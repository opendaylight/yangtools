/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;

@NonNullByDefault
class ImmutableNamespaceBindingTest {
    private static final Unqualified FOO = Unqualified.of("foo");
    private static final Unqualified BAR = Unqualified.of("bar");
    private static final Unqualified BAZ = Unqualified.of("baz");
    private static final QNameModule FOONS = QNameModule.of("foo");
    private static final QNameModule BARNS = QNameModule.of("bar");
    private static final QNameModule BAZNS = QNameModule.of("baz", "2025-01-11");
    private static final QName CURRENT = QName.create("currentns", "2026-01-11", "module");

    @Test
    void toStringOrdersNamespaces() {
        final var binding = new ImmutableNamespaceBinding(CURRENT, Map.of(FOO, FOONS, BAR, BARNS, BAZ, BAZNS));
        assertEquals("""
            ImmutableNamespaceBinding{currentModule=currentns@2026-01-11, prefixToModule={bar=bar, baz=baz@2025-01-11, \
            foo=foo}}""", binding.toString());
    }
}
