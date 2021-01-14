/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThrows;

import com.google.common.base.Throwables;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class YangParserNegativeTest {
    @Test
    public void testInvalidImport() {
        final SomeModifiersUnresolvedException ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModuleResources(getClass(), "/negative-scenario/testfile1.yang"));

        final Throwable rootCause = Throwables.getRootCause(ex);
        assertThat(rootCause, isA(InferenceException.class));
        assertThat(rootCause.getMessage(), startsWith("Imported module"));
        assertThat(rootCause.getMessage(), containsString("was not found."));
    }

    @Test
    public void testTypeNotFound() {
        final SomeModifiersUnresolvedException ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModuleResources(getClass(), "/negative-scenario/testfile2.yang"));
        final Throwable rootCause = Throwables.getRootCause(ex);
        assertThat(rootCause, isA(InferenceException.class));
        assertThat(rootCause.getMessage(),
            startsWith("Type [(urn:simple.types.data.demo?revision=2013-02-27)int-ext] was not found."));
    }

    @Test
    public void testInvalidAugmentTarget() {
        final SomeModifiersUnresolvedException ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModuleResources(getClass(),
                "/negative-scenario/testfile0.yang", "/negative-scenario/testfile3.yang"));
        final Throwable rootCause = Throwables.getRootCause(ex);
        assertThat(rootCause, isA(InferenceException.class));
        assertThat(rootCause.getMessage(), startsWith(
            "Augment target 'Absolute{qnames=[(urn:simple.container.demo)unknown]}' not found"));
    }

    @Test
    public void testInvalidRefine() {
        final SomeModifiersUnresolvedException ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModuleResources(getClass(), "/negative-scenario/testfile4.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, isA(SourceException.class));
        assertThat(cause.getMessage(), containsString("Error in module 'test4' in the refine of uses "
            + "'Descendant{qnames=[(urn:simple.container.demo)node]}': can not perform refine of 'PRESENCE' for"
            + " the target 'LEAF_LIST'."));
    }

    @Test
    public void testInvalidLength() {
        final SomeModifiersUnresolvedException ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModuleResources(getClass(), "/negative-scenario/testfile5.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, isA(SourceException.class));
        assertThat(cause.getMessage(), containsString("Invalid length constraint [4..10]"));
    }

    @Test
    public void testInvalidRange() {
        final SomeModifiersUnresolvedException ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModuleResources(getClass(), "/negative-scenario/testfile6.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, isA(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Invalid range constraint: [[5..20]]"));
    }

    @Test
    public void testDuplicateContainer() {
        final SomeModifiersUnresolvedException ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModuleResources(getClass(), "/negative-scenario/duplicity/container.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, isA(SourceException.class));
        assertThat(cause.getMessage(), containsString("Error in module 'container': cannot add "
            + "'(urn:simple.container.demo)foo'. Node name collision: '(urn:simple.container.demo)foo' already "
            + "declared"));
    }

    @Test
    public void testDuplicateContainerList() {
        final SomeModifiersUnresolvedException ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModuleResources(getClass(), "/negative-scenario/duplicity/container-list.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, isA(SourceException.class));
        assertThat(cause.getMessage(), containsString("Error in module 'container-list': cannot add "
            + "'(urn:simple.container.demo)foo'. Node name collision: '(urn:simple.container.demo)foo' already "
            + "declared"));
    }

    @Test
    public void testDuplicateContainerLeaf() {
        final SomeModifiersUnresolvedException ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModuleResources(getClass(), "/negative-scenario/duplicity/container-leaf.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, isA(SourceException.class));
        assertThat(cause.getMessage(), containsString("Error in module 'container-leaf': cannot add "
            + "'(urn:simple.container.demo)foo'. Node name collision: '(urn:simple.container.demo)foo' already "
            + "declared"));
    }

    @Test
    public void testDuplicateTypedef() {
        final SomeModifiersUnresolvedException ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModuleResources(getClass(), "/negative-scenario/duplicity/typedef.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, isA(SourceException.class));
        assertThat(cause.getMessage(), startsWith(
            "Duplicate name for typedef (urn:simple.container.demo)int-ext [at"));
    }

    @Test
    public void testDuplicityInAugmentTarget1() {
        final SomeModifiersUnresolvedException ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModuleResources(getClass(),
                "/negative-scenario/duplicity/augment0.yang", "/negative-scenario/duplicity/augment1.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, isA(InferenceException.class));
        assertThat(cause.getMessage(), startsWith(
            "An augment cannot add node named 'id' because this name is already used in target"));
    }

    @Test
    public void testDuplicityInAugmentTarget2() {
        final SomeModifiersUnresolvedException ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModuleResources(getClass(),
                "/negative-scenario/duplicity/augment0.yang", "/negative-scenario/duplicity/augment2.yang"));
        final Throwable rootCause = Throwables.getRootCause(ex);
        assertThat(rootCause, isA(SubstatementIndexingException.class));
        assertThat(rootCause.getMessage(), containsString("Cannot add schema tree child with name "
            + "(urn:simple.augment2.demo?revision=2014-06-02)delta, a conflicting child already exists"));
    }

    @Test
    public void testMandatoryInAugment() {
        final SomeModifiersUnresolvedException ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModuleResources(getClass(),
                "/negative-scenario/testfile8.yang", "/negative-scenario/testfile7.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, isA(InferenceException.class));
        assertThat(cause.getMessage(), startsWith(
            "An augment cannot add node 'linkleaf' because it is mandatory and in module different than target"));
    }

    @Test
    public void testInvalidListKeyDefinition() {
        final SomeModifiersUnresolvedException ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModuleResources(getClass(), "/negative-scenario/invalid-list-key-def.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, isA(InferenceException.class));
        assertThat(cause.getMessage(), startsWith(
            "Key 'rib-id' misses node 'rib-id' in list '(invalid:list:key:def)application-map'"));
    }
}
