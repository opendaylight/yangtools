/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.jupiter.api.Test;

class ImportResolutionBasicTest extends AbstractYangTest {
    private static final String ROOT_WITHOUT_IMPORT = "/semantic-statement-parser/import-arg-parsing/nature.yang";
    private static final String IMPORT_ROOT = "/semantic-statement-parser/import-arg-parsing/mammal.yang";
    private static final String IMPORT_DERIVED = "/semantic-statement-parser/import-arg-parsing/human.yang";

    @Test
    void inImportOrderTest() {
        assertEffectiveModel(ROOT_WITHOUT_IMPORT, IMPORT_ROOT, IMPORT_DERIVED);
    }

    @Test
    void inInverseOfImportOrderTest() {
        assertEffectiveModel(IMPORT_DERIVED, IMPORT_ROOT, ROOT_WITHOUT_IMPORT);
    }

    @Test
    void missingImportedSourceTest() {
        assertFailedPreLinkage("mammal", IMPORT_DERIVED, ROOT_WITHOUT_IMPORT);
    }

    @Test
    void circularImportsTest() {
        assertIllegalStateException(startsWith("Found circular dependency"),
            "/semantic-statement-parser/import-arg-parsing/cycle-yin.yang",
            "/semantic-statement-parser/import-arg-parsing/cycle-yang.yang");
    }

    @Test
    void selfImportTest() {
        assertIllegalStateException(startsWith("Found circular dependency"),
            "/semantic-statement-parser/import-arg-parsing/egocentric.yang", IMPORT_ROOT, ROOT_WITHOUT_IMPORT);
    }

    @Test
    void bug2649Test() {
        assertEffectiveModel(
            "/semantic-statement-parser/bug2649/foo.yang",
            "/semantic-statement-parser/bug2649/import-module.yang");
    }

    private static void assertFailedPreLinkage(final String name, final String... sources) {
        assertIllegalStateException(allOf(
            startsWith("Imported module [" + name),
            containsString("] was not found. [at ")),
            sources);
    }
}
