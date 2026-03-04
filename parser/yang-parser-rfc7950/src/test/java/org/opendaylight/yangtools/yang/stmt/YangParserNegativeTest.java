/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class YangParserNegativeTest extends AbstractYangTest {
    @Test
    void testInvalidImport() {
        assertEquals("Imported module some-module was not found [at testfile1:6:5]",
            assertInferenceException("/negative-scenario/testfile1.yang").getMessage());
    }

    @Test
    void testTypeNotFound() {
        assertInferenceException(
            startsWith("Type [(urn:simple.types.data.demo?revision=2013-02-27)int-ext] was not found."),
            "/negative-scenario/testfile2.yang");
    }

    @Test
    void testInvalidAugmentTarget() {
        assertInferenceException(
            startsWith("Augment target 'Absolute{qnames=[(urn:simple.container.demo)unknown]}' not found"),
            "/negative-scenario/testfile0.yang", "/negative-scenario/testfile3.yang");
    }

    @Test
    void testInvalidRefine() {
        assertThat(assertInferenceException("/negative-scenario/testfile4.yang").getMessage()).contains("""
            unsupported statement presence in target leaf-list while refining uses \
            Descendant{qnames=[(urn:simple.container.demo)node]} [at """);
    }

    @Test
    void testInvalidLength() {
        assertSourceExceptionMessage("/negative-scenario/testfile5.yang")
            .startsWith("Invalid length constraint [4..10]");
    }

    @Test
    void testInvalidRange() {
        assertSourceExceptionMessage("/negative-scenario/testfile6.yang")
            .startsWith("Invalid range constraint: [[5..20]]");
    }

    @Test
    void testDuplicateContainer() {
        assertSourceExceptionMessage("/negative-scenario/duplicity/container.yang").startsWith("""
            Error in module 'container': cannot add '(urn:simple.container.demo)foo'. Node name collision: \
            '(urn:simple.container.demo)foo' already declared""");
    }

    @Test
    void testDuplicateContainerList() {
        assertSourceExceptionMessage("/negative-scenario/duplicity/container-list.yang").startsWith("""
            Error in module 'container-list': cannot add '(urn:simple.container.demo)foo'. Node name collision: \
            '(urn:simple.container.demo)foo' already declared""");
    }

    @Test
    void testDuplicateContainerLeaf() {
        assertSourceExceptionMessage("/negative-scenario/duplicity/container-leaf.yang").startsWith("""
            Error in module 'container-leaf': cannot add '(urn:simple.container.demo)foo'. Node name collision: \
            '(urn:simple.container.demo)foo' already declared""");
    }

    @Test
    void testDuplicateTypedef() {
        assertSourceExceptionMessage("/negative-scenario/duplicity/typedef.yang")
            .startsWith("Duplicate name for typedef (urn:simple.container.demo)int-ext [at");
    }

    @Test
    void testDuplicityInAugmentTarget1() {
        assertInferenceException(
            startsWith("An augment cannot add node named 'id' because this name is already used in target"),
            "/negative-scenario/duplicity/augment0.yang", "/negative-scenario/duplicity/augment1.yang");
    }

    @Test
    void testDuplicityInAugmentTarget2() {
        assertSourceExceptionMessage(
            "/negative-scenario/duplicity/augment0.yang", "/negative-scenario/duplicity/augment2.yang").startsWith("""
                Error in module 'augment0': cannot add \
                '(urn:simple.augment2.demo?revision=2014-06-02)delta'. Node name collision: \
                '(urn:simple.augment2.demo?revision=2014-06-02)delta' already declared at """)
            .endsWith("duplicity/augment2.yang:17:9]");
    }

    @Test
    void testMandatoryInAugment() {
        assertInferenceException(startsWith(
            "An augment cannot add node 'linkleaf' because it is mandatory and in module different than target"),
            "/negative-scenario/testfile8.yang", "/negative-scenario/testfile7.yang");
    }

    @Test
    void testInvalidListKeyDefinition() {
        assertInferenceException(startsWith(
            "Key 'rib-id' misses node 'rib-id' in list '(invalid:list:key:def)application-map'"),
            "/negative-scenario/invalid-list-key-def.yang");
    }
}
