/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ImportRevisionsTest extends AbstractYangTest {
    @Test
    void equalRevisionDatesTest() {
        assertEffectiveModel(
            "/import-revision-date-test/root-with-equal-revision-date.yang",
            "/import-revision-date-test/imported-module-with-equal-revision-date.yang");
    }

    @Test
    void unequalRevisionDatesTest() {
        assertEquals("""
            Imported module imported-module-with-unequal-revision-date was not found [at \
            root-with-unequal-revision-date:5:5]""", assertInferenceException(
            "/import-revision-date-test/root-with-unequal-revision-date.yang",
            "/import-revision-date-test/imported-module-with-unequal-revision-date.yang").getMessage());
    }

    @Test
    void revisionDatesInRootOnlyTest() {
        assertEquals("""
            Imported module imported-module-without-revision-date was not found [at root-with-revision-date:5:5]""",
            assertInferenceException(
                "/import-revision-date-test/root-with-revision-date.yang",
                "/import-revision-date-test/imported-module-without-revision-date.yang").getMessage());
    }

    @Test
    void revisionDatesInImportedOnlyTest() {
        assertEffectiveModel(
            "/import-revision-date-test/root-without-revision-date.yang",
            "/import-revision-date-test/imported-module-with-revision-date.yang");
    }

    @Test
    void noRevisionInRootAndImportedTest() {
        assertEffectiveModel(
            "/import-revision-date-test/root-with-no-revision-date.yang",
            "/import-revision-date-test/imported-module-with-no-revision-date.yang");
    }
}
