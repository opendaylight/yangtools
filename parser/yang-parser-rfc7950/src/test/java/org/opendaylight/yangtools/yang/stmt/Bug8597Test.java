/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;

class Bug8597Test extends AbstractYangTest {
    @Test
    void test() throws Exception {
        final var foo = assertEffectiveModelDir("/bugs/bug8597").findModule("foo").orElseThrow();
        for (ModuleImport moduleImport : foo.getImports()) {
            switch (moduleImport.getModuleName().getLocalName()) {
                case "bar":
                    assertEquals(Revision.ofNullable("1970-01-01"), moduleImport.getRevision());
                    assertEquals(Optional.of("bar-ref"), moduleImport.getReference());
                    assertEquals(Optional.of("bar-desc"), moduleImport.getDescription());
                    break;
                case "baz":
                    assertEquals(Revision.ofNullable("2010-10-10"), moduleImport.getRevision());
                    assertEquals(Optional.of("baz-ref"), moduleImport.getReference());
                    assertEquals(Optional.of("baz-desc"), moduleImport.getDescription());
                    break;
                default:
                    fail("Module 'foo' should only contains import of module 'bar' and 'baz'");
            }
        }
    }
}
