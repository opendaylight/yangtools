/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertTrue;
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

public class YangParserNegativeTest {

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
            assertTrue(rootCause instanceof InferenceException);
            assertTrue(rootCause.getMessage().startsWith("Imported module"));
            assertTrue(rootCause.getMessage().contains("was not found."));
        }
    }

    @Test
    public void testTypeNotFound() throws IOException, ReactorException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/testfile2.yang");
            fail("InferenceException should be thrown");
        } catch (final SomeModifiersUnresolvedException e) {
            final Throwable rootCause = Throwables.getRootCause(e);
            assertTrue(rootCause instanceof InferenceException);
            assertTrue(rootCause.getMessage()
                    .startsWith("Type [(urn:simple.types.data.demo?revision=2013-02-27)int-ext] was not found."));
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
            assertTrue(rootCause instanceof InferenceException);
            assertTrue(rootCause.getMessage().startsWith(
                "Augment target 'Absolute{path=[(urn:simple.container.demo?revision=1970-01-01)unknown]}' not found"));
        }
    }

    @Test
    public void testInvalidRefine() throws IOException, ReactorException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/testfile4.yang");
            fail("ReactorException should be thrown");
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().contains("Error in module 'test4' in the refine of uses " +
                    "'Relative{path=[(urn:simple.container.demo?revision=1970-01-01)node]}': can not perform refine of 'PRESENCE' for" +
                    " the target 'LEAF_LIST'."));
        }
    }

    @Test
    public void testInvalidLength() throws IOException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/testfile5.yang");
            fail("ReactorException should be thrown");
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().contains("Invalid length constraint: <4, 10>"));
        }
    }

    @Test
    public void testInvalidRange() throws IOException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/testfile6.yang");
            fail("ReactorException should be thrown");
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().contains("Invalid range constraint: <5, 20>"));
        }
    }

    @Test
    public void testDuplicateContainer() throws IOException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/duplicity/container.yang");
            fail("SourceException should be thrown");
        } catch (final ReactorException e) {
            final String expected = "Error in module 'container': cannot add '(urn:simple.container" +
                    ".demo?revision=1970-01-01)foo'. Node name collision: '(urn:simple.container" +
                    ".demo?revision=1970-01-01)foo' already declared";
            assertTrue(e.getCause().getMessage().contains(expected));
        }
    }

    @Test
    public void testDuplicateContainerList() throws IOException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/duplicity/container-list.yang");
            fail("SourceException should be thrown");
        } catch (final ReactorException e) {
            final String expected = "Error in module 'container-list': cannot add '(urn:simple.container" +
                    ".demo?revision=1970-01-01)foo'. Node name collision: '(urn:simple.container" +
                    ".demo?revision=1970-01-01)foo' already declared";
            assertTrue(e.getCause().getMessage().contains(expected));
        }
    }

    @Test
    public void testDuplicateContainerLeaf() throws IOException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/duplicity/container-leaf.yang");
            fail("SourceException should be thrown");
        } catch (final ReactorException e) {
            final String expected = "Error in module 'container-leaf': cannot add '(urn:simple.container" +
                    ".demo?revision=1970-01-01)foo'. Node name collision: '(urn:simple.container" +
                    ".demo?revision=1970-01-01)foo' already declared";
            assertTrue(e.getCause().getMessage().contains(expected));
        }
    }

    @Test
    public void testDuplicateTypedef() throws IOException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/duplicity/typedef.yang");
            fail("SourceException should be thrown");
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                "Duplicate name for typedef (urn:simple.container.demo?revision=1970-01-01)int-ext [at"));
        }
    }

    @Test
    public void testDuplicityInAugmentTarget1() throws IOException, ReactorException, YangSyntaxErrorException {
        TestUtils.loadModuleResources(getClass(),
            "/negative-scenario/duplicity/augment0.yang",
                "/negative-scenario/duplicity/augment1.yang");
        testLog = output.toString();
        assertTrue(testLog.contains("An augment cannot add node named 'id' because this name is already used in target"));
    }

    @Test
    public void testDuplicityInAugmentTarget2() throws IOException, ReactorException, YangSyntaxErrorException {
        TestUtils.loadModuleResources(getClass(),
            "/negative-scenario/duplicity/augment0.yang",
                "/negative-scenario/duplicity/augment2.yang");
        testLog = output.toString();
        assertTrue(testLog.contains("An augment cannot add node named 'delta' because this name is already used in target"));
    }

    @Test
    public void testMandatoryInAugment() throws IOException, ReactorException, YangSyntaxErrorException {
        TestUtils.loadModuleResources(getClass(),
            "/negative-scenario/testfile8.yang",
                "/negative-scenario/testfile7.yang");
        testLog = output.toString();
        assertTrue(testLog.contains(
            "An augment cannot add node 'linkleaf' because it is mandatory and in module different than target"));
    }

    @Test
    public void testInvalidListKeyDefinition() throws IOException, YangSyntaxErrorException {
        try {
            TestUtils.loadModuleResources(getClass(), "/negative-scenario/invalid-list-key-def.yang");
            fail("InferenceException should be thrown");
        } catch (final ReactorException e) {
            final String expected = "Key 'rib-id' misses node 'rib-id' in list "
                    + "'(invalid:list:key:def?revision=1970-01-01)application-map'";
            assertTrue(e.getCause().getMessage().startsWith(expected));
        }
    }
}
