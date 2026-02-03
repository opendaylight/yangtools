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
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;

class SourceDependencyTest {
    @Test
    void belongsToToString() {
        assertEquals("BelongsTo[name=Unqualified{localName=foo}, prefix=Unqualified{localName=bar}, sourceRef=null]",
            new SourceDependency.BelongsTo(Unqualified.of("foo"), Unqualified.of("bar"), null).toString());
    }
}
