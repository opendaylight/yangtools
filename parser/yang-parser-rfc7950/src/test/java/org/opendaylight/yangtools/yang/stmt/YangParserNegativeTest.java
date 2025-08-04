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
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.jupiter.api.Test;

class YangParserNegativeTest extends AbstractYangTest {
    @Test
    void testInvalidImport() {
        assertInvalidStateException(allOf(startsWith("Imported module"), containsString("was not found.")),
            "/negative-scenario/testfile1.yang");
    }

    @Test
    void testTypeNotFound() {
        assertInvalidStateException(
            startsWith("Type [(urn:simple.types.data.demo?revision=2013-02-27)int-ext] was not found."),
            "/negative-scenario/testfile2.yang");
    }

    @Test
    void testInvalidAugmentTarget() {
        assertInvalidStateException(
            startsWith("Augment target 'Absolute{qnames=[(urn:simple.container.demo)unknown]}' not found"),
            "/negative-scenario/testfile0.yang", "/negative-scenario/testfile3.yang");
    }

    @Test
    void testInvalidRefine() {
        assertSourceException(
            containsString("Error in module 'test4' in the refine of uses "
            + "'Descendant{qnames=[(urn:simple.container.demo)node]}': can not perform refine of 'PRESENCE' for"
            + " the target 'LEAF_LIST'."),
            "/negative-scenario/testfile4.yang");
    }

    @Test
    void testInvalidLength() {
        assertSourceException(startsWith("Invalid length constraint [4..10]"),
            "/negative-scenario/testfile5.yang");
    }

    @Test
    void testInvalidRange() {
        assertSourceException(startsWith("Invalid range constraint: [[5..20]]"),
            "/negative-scenario/testfile6.yang");
    }

    @Test
    void testDuplicateContainer() {
        assertSourceException(startsWith("Error in module 'container': cannot add "
            + "'(urn:simple.container.demo)foo'. Node name collision: '(urn:simple.container.demo)foo' already "
            + "declared"),
            "/negative-scenario/duplicity/container.yang");
    }

    @Test
    void testDuplicateContainerList() {
        assertSourceException(startsWith("Error in module 'container-list': cannot add "
            + "'(urn:simple.container.demo)foo'. Node name collision: '(urn:simple.container.demo)foo' already "
            + "declared"),
            "/negative-scenario/duplicity/container-list.yang");
    }

    @Test
    void testDuplicateContainerLeaf() {
        assertSourceException(startsWith("Error in module 'container-leaf': cannot add "
            + "'(urn:simple.container.demo)foo'. Node name collision: '(urn:simple.container.demo)foo' already "
            + "declared"),
            "/negative-scenario/duplicity/container-leaf.yang");
    }

    @Test
    void testDuplicateTypedef() {
        assertSourceException(
            startsWith("Duplicate name for typedef (urn:simple.container.demo)int-ext [at"),
            "/negative-scenario/duplicity/typedef.yang");
    }

    @Test
    void testDuplicityInAugmentTarget1() {
        assertInvalidStateException(
            startsWith("An augment cannot add node named 'id' because this name is already used in target"),
            "/negative-scenario/duplicity/augment0.yang", "/negative-scenario/duplicity/augment1.yang");
    }

    @Test
    void testDuplicityInAugmentTarget2() {
        assertSourceException(allOf(
            startsWith("Error in module 'augment0': cannot add "
                + "'(urn:simple.augment2.demo?revision=2014-06-02)delta'. Node name collision: "
                + "'(urn:simple.augment2.demo?revision=2014-06-02)delta' already declared at "),
            endsWith("duplicity/augment2.yang:17:9]")),
            "/negative-scenario/duplicity/augment0.yang", "/negative-scenario/duplicity/augment2.yang");
    }

    @Test
    void testMandatoryInAugment() {
        assertInvalidStateException(startsWith(
            "An augment cannot add node 'linkleaf' because it is mandatory and in module different than target"),
            "/negative-scenario/testfile8.yang", "/negative-scenario/testfile7.yang");
    }

    @Test
    void testInvalidListKeyDefinition() {
        assertInvalidStateException(startsWith(
            "Key 'rib-id' misses node 'rib-id' in list '(invalid:list:key:def)application-map'"),
            "/negative-scenario/invalid-list-key-def.yang");
    }
}
