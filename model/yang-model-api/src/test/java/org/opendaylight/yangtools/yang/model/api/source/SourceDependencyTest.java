/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;

class SourceDependencyTest {
    @Test
    void belongsToToString() {
        assertEquals("BelongsTo[name=Unqualified{localName=foo}, prefix=Unqualified{localName=bar}]",
            new BelongsTo(Unqualified.of("foo"), Unqualified.of("bar")).toString());
    }

    @Test
    void importToToString() {
        assertEquals("Import[name=Unqualified{localName=foo}, prefix=Unqualified{localName=bar}, revision=2026-02-03]",
            new Import(Unqualified.of("foo"), Unqualified.of("bar"), Revision.of("2026-02-03")).toString());
    }

    @Test
    void includeToToString() {
        assertEquals("Include[name=Unqualified{localName=foo}, revision=2026-02-03]",
            new Include(Unqualified.of("foo"), Revision.of("2026-02-03")).toString());
    }
}
