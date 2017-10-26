/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug6491Test {
    private static final Revision DATE = Revision.of("2016-01-01");

    @Test
    public void tetststs() throws Exception {
        testRevision("withoutRevision", null, Optional.empty());
        testRevision("withRevision", DATE, Optional.of(DATE));
        testRevision("importedModuleRevisionOnly", null, Optional.of(DATE));
        testRevision("moduleRevisionOnly", DATE, Optional.empty());
    }

    private static void testRevision(final String path, final Revision moduleRevision,
            final Optional<Revision> importedRevision) throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6491/".concat(path));
        assertNotNull(context);
        final Module module = context.findModule("bar", moduleRevision).get();
        final Set<ModuleImport> imports = module.getImports();
        assertNotNull(imports);
        assertEquals(1, imports.size());
        assertEquals(importedRevision, imports.iterator().next().getRevision());
    }
}