/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;

class Bug6491Test extends AbstractYangTest {
    private static final Revision DATE = Revision.of("2016-01-01");

    @Test
    void tetststs() {
        testRevision("withoutRevision", null, Optional.empty());
        testRevision("withRevision", DATE, Optional.of(DATE));
        testRevision("importedModuleRevisionOnly", null, Optional.of(DATE));
        testRevision("moduleRevisionOnly", DATE, Optional.empty());
    }

    private static void testRevision(final String path, final Revision moduleRevision,
            final Optional<Revision> importedRevision) {
        final var context = assertEffectiveModelDir("/bugs/bug6491/" + path);
        final var module = context.findModule("bar", moduleRevision).orElseThrow();
        final var imports = module.getImports();
        assertNotNull(imports);
        assertEquals(1, imports.size());
        assertEquals(importedRevision, imports.iterator().next().getRevision());
    }
}