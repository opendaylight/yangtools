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
import static org.junit.Assert.fail;

import com.google.common.base.Throwables;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class YangParserNegativeTest {

    @SuppressWarnings("checkstyle:regexpSinglelineJava")
    private final PrintStream stdout = System.out;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private String testLog;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        System.setOut(new PrintStream(output, true, "UTF-8"));
    }

    @After
    public void cleanUp() {
        System.setOut(stdout);
    }

    @Test
    public void testInvalidImport() throws IOException, ReactorException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/testfile1.yang");
            fail("SomeModifiersUnresolvedException should be thrown");
        } catch (final SomeModifiersUnresolvedException e) {
            final Throwable rootCause = Throwables.getRootCause(e);
            assertThat(rootCause, isA(InferenceException.class));
            assertThat(rootCause.getMessage(), startsWith("Imported module"));
            assertThat(rootCause.getMessage(), containsString("was not found."));
        }
    }

    @Test
    public void testTypeNotFound() throws IOException, ReactorException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/testfile2.yang");
            fail("InferenceException should be thrown");
        } catch (final SomeModifiersUnresolvedException e) {
            final Throwable rootCause = Throwables.getRootCause(e);
            assertThat(rootCause, isA(InferenceException.class));
            assertThat(rootCause.getMessage(),
                startsWith("Type [(urn:simple.types.data.demo?revision=2013-02-27)int-ext] was not found."));
        }
    }

    @Test
    public void testInvalidAugmentTarget() throws IOException, ReactorException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(),
                "/negative-scenario/testfile0.yang",
                "/negative-scenario/testfile3.yang");
            fail("SomeModifiersUnresolvedException should be thrown");
        } catch (final SomeModifiersUnresolvedException e) {
            final Throwable rootCause = Throwables.getRootCause(e);
            assertThat(rootCause, isA(InferenceException.class));
            assertThat(rootCause.getMessage(), startsWith(
                "Augment target 'Absolute{qnames=(urn:simple.container.demo)unknown}' not found"));
        }
    }

    @Test
    public void testInvalidRefine() throws IOException, ReactorException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/testfile4.yang");
            fail("ReactorException should be thrown");
        } catch (final ReactorException e) {
            assertThat(e.getCause().getMessage(), containsString("Error in module 'test4' in the refine of uses "
                    + "'Descendant{qnames=(urn:simple.container.demo)node}': can not perform refine of 'PRESENCE' for"
                    + " the target 'LEAF_LIST'."));
        }
    }

    @Test
    public void testInvalidLength() throws IOException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/testfile5.yang");
            fail("ReactorException should be thrown");
        } catch (final ReactorException e) {
            assertThat(e.getCause().getMessage(), containsString("Invalid length constraint [4..10]"));
        }
    }

    @Test
    public void testInvalidRange() throws IOException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/testfile6.yang");
            fail("ReactorException should be thrown");
        } catch (final ReactorException e) {
            assertThat(e.getCause().getMessage(), startsWith("Invalid range constraint: [[5..20]]"));
        }
    }

    @Test
    public void testDuplicateContainer() throws IOException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/duplicity/container.yang");
            fail("SourceException should be thrown");
        } catch (final ReactorException e) {
            final String expected = "Error in module 'container': cannot add '(urn:simple.container.demo)foo'. "
                    + "Node name collision: '(urn:simple.container.demo)foo' already declared";
            assertThat(e.getCause().getMessage(), containsString(expected));
        }
    }

    @Test
    public void testDuplicateContainerList() throws IOException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/duplicity/container-list.yang");
            fail("SourceException should be thrown");
        } catch (final ReactorException e) {
            final String expected = "Error in module 'container-list': cannot add '(urn:simple.container.demo)foo'. "
                    + "Node name collision: '(urn:simple.container.demo)foo' already declared";
            assertThat(e.getCause().getMessage(), containsString(expected));
        }
    }

    @Test
    public void testDuplicateContainerLeaf() throws IOException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/duplicity/container-leaf.yang");
            fail("SourceException should be thrown");
        } catch (final ReactorException e) {
            final String expected = "Error in module 'container-leaf': cannot add '(urn:simple.container.demo)foo'. "
                    + "Node name collision: '(urn:simple.container.demo)foo' already declared";
            assertThat(e.getCause().getMessage(), containsString(expected));
        }
    }

    @Test
    public void testDuplicateTypedef() throws IOException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/duplicity/typedef.yang");
            fail("SourceException should be thrown");
        } catch (final ReactorException e) {
            assertThat(e.getCause().getMessage(), startsWith(
                "Duplicate name for typedef (urn:simple.container.demo)int-ext [at"));
        }
    }

    @Test
    public void testDuplicityInAugmentTarget1() throws IOException, ReactorException, YangSyntaxErrorException {
        TestUtils.loadModuleResources(getClass(),
            "/negative-scenario/duplicity/augment0.yang",
                "/negative-scenario/duplicity/augment1.yang");
        testLog = output.toString();
        assertThat(testLog, containsString(
            "An augment cannot add node named 'id' because this name is already used in target"));
    }

    @Test
    public void testDuplicityInAugmentTarget2() throws IOException, ReactorException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(),
                "/negative-scenario/duplicity/augment0.yang",
                    "/negative-scenario/duplicity/augment2.yang");
            fail("Duplicate leaf not detected");
        } catch (SomeModifiersUnresolvedException e) {
            final Throwable rootCause = Throwables.getRootCause(e);
            assertThat(rootCause, isA(SourceException.class));
            assertThat(rootCause.getMessage(), containsString("Cannot add schema tree child with name "
                    + "(urn:simple.augment2.demo?revision=2014-06-02)delta, a conflicting child already exists"));

        }
    }

    @Test
    public void testMandatoryInAugment() throws IOException, ReactorException, YangSyntaxErrorException {
        TestUtils.loadModuleResources(getClass(),
            "/negative-scenario/testfile8.yang",
                "/negative-scenario/testfile7.yang");
        testLog = output.toString();
        assertThat(testLog, containsString(
            "An augment cannot add node 'linkleaf' because it is mandatory and in module different than target"));
    }

    @Test
    public void testInvalidListKeyDefinition() throws IOException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/invalid-list-key-def.yang");
            fail("InferenceException should be thrown");
        } catch (final ReactorException e) {
            assertThat(e.getCause().getMessage(),
                startsWith("Key 'rib-id' misses node 'rib-id' in list '(invalid:list:key:def)application-map'"));
        }
    }
}
