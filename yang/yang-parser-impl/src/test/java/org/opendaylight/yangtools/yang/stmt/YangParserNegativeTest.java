/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.base.Throwables;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;

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
    public void testInvalidImport() throws Exception {
        final File yang = new File(getClass().getResource("/negative-scenario/testfile1.yang").toURI());
        try {
            try (InputStream stream = new NamedFileInputStream(yang, yang.getPath())) {
                TestUtils.loadModule(stream);
                fail("SomeModifiersUnresolvedException should be thrown");
            }
        } catch (final SomeModifiersUnresolvedException e) {
            final Throwable rootCause = Throwables.getRootCause(e);
            assertTrue(rootCause instanceof InferenceException);
            assertTrue(rootCause.getMessage().startsWith("Imported module"));
            assertTrue(rootCause.getMessage().contains("was not found."));
        }
    }

    @Test
    public void testTypeNotFound() throws Exception {
        final File yang = new File(getClass().getResource("/negative-scenario/testfile2.yang").toURI());
        try {
            try (InputStream stream = new NamedFileInputStream(yang, yang.getPath())) {
                TestUtils.loadModule(stream);
                fail("InferenceException should be thrown");
            }
        } catch (final SomeModifiersUnresolvedException e) {
            final Throwable rootCause = Throwables.getRootCause(e);
            assertTrue(rootCause instanceof InferenceException);
            assertTrue(rootCause.getMessage()
                    .startsWith("Type [(urn:simple.types.data.demo?revision=2013-02-27)int-ext] was not found."));
        }
    }

    @Test
    public void testInvalidAugmentTarget() throws Exception {
        final File yang1 = new File(getClass().getResource("/negative-scenario/testfile0.yang").toURI());
        final File yang2 = new File(getClass().getResource("/negative-scenario/testfile3.yang").toURI());
        try {
            final List<InputStream> streams = new ArrayList<>(2);
            try (InputStream testFile0 = new NamedFileInputStream(yang1, yang1.getPath())) {
                streams.add(testFile0);
                try (InputStream testFile3 = new NamedFileInputStream(yang2, yang2.getPath())) {
                    streams.add(testFile3);
                    assertEquals("Expected loaded files count is 2", 2, streams.size());
                    TestUtils.loadModules(streams);
                    fail("SomeModifiersUnresolvedException should be thrown");
                }
            }
        } catch (final SomeModifiersUnresolvedException e) {
            final Throwable rootCause = Throwables.getRootCause(e);
            assertTrue(rootCause instanceof InferenceException);
            assertTrue(rootCause.getMessage().startsWith(
                "Augment target 'Absolute{path=[(urn:simple.container.demo?revision=1970-01-01)unknown]}' not found"));
        }
    }

    @Test
    public void testInvalidRefine() throws Exception {
        final File yang = new File(getClass().getResource("/negative-scenario/testfile4.yang").toURI());
        try {
            try (InputStream stream = new NamedFileInputStream(yang, yang.getPath())) {
                TestUtils.loadModule(stream);
                fail("SourceException should be thrown");
            }
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().contains("Error in module 'test4' in the refine of uses " +
                    "'Relative{path=[(urn:simple.container.demo?revision=1970-01-01)node]}': can not perform refine of 'PRESENCE' for" +
                    " the target 'LEAF_LIST'."));
        }
    }

    @Test
    public void testInvalidLength() throws Exception {
        final File yang = new File(getClass().getResource("/negative-scenario/testfile5.yang").toURI());
        try {
            try (InputStream stream = new NamedFileInputStream(yang, yang.getPath())) {
                TestUtils.loadModule(stream);
                fail("YangParseException should be thrown");
            }
        } catch (final ReactorException e) {
            final String message = e.getCause().getMessage();

            assertTrue(message.contains("Invalid length constraint [4..10]"));
        }
    }

    @Test
    public void testInvalidRange() throws Exception {
        final File yang = new File(getClass().getResource("/negative-scenario/testfile6.yang").toURI());
        try {
            try (InputStream stream = new NamedFileInputStream(yang, yang.getPath())) {
                TestUtils.loadModule(stream);
                fail("Exception should be thrown");
            }
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().contains("Invalid range constraint: <5, 20>"));
        }
    }

    @Test
    public void testDuplicateContainer() throws Exception {
        final File yang = new File(getClass().getResource("/negative-scenario/duplicity/container.yang").toURI());
        try {
            try (InputStream stream = new NamedFileInputStream(yang, yang.getPath())) {
                TestUtils.loadModule(stream);
                fail("SourceException should be thrown");
            }
        } catch (final ReactorException e) {
            final String expected = "Error in module 'container': cannot add '(urn:simple.container" +
                    ".demo?revision=1970-01-01)foo'. Node name collision: '(urn:simple.container" +
                    ".demo?revision=1970-01-01)foo' already declared";
            assertTrue(e.getCause().getMessage().contains(expected));
        }
    }

    @Test
    public void testDuplicateContainerList() throws Exception {
        final File yang = new File(getClass().getResource("/negative-scenario/duplicity/container-list.yang").toURI());
        try {
            try (InputStream stream = new NamedFileInputStream(yang, yang.getPath())) {
                TestUtils.loadModule(stream);
                fail("SourceException should be thrown");
            }
        } catch (final ReactorException e) {
            final String expected = "Error in module 'container-list': cannot add '(urn:simple.container" +
                    ".demo?revision=1970-01-01)foo'. Node name collision: '(urn:simple.container" +
                    ".demo?revision=1970-01-01)foo' already declared";
            assertTrue(e.getCause().getMessage().contains(expected));
        }
    }

    @Test
    public void testDuplicateContainerLeaf() throws Exception {
        final File yang = new File(getClass().getResource("/negative-scenario/duplicity/container-leaf.yang").toURI());
        try {
            try (InputStream stream = new NamedFileInputStream(yang, yang.getPath())) {
                TestUtils.loadModule(stream);
                fail("SourceException should be thrown");
            }
        } catch (final ReactorException e) {
            final String expected = "Error in module 'container-leaf': cannot add '(urn:simple.container" +
                    ".demo?revision=1970-01-01)foo'. Node name collision: '(urn:simple.container" +
                    ".demo?revision=1970-01-01)foo' already declared";
            assertTrue(e.getCause().getMessage().contains(expected));
        }
    }

    @Test
    public void testDuplicateTypedef() throws Exception {
        final File yang = new File(getClass().getResource("/negative-scenario/duplicity/typedef.yang").toURI());
        try {
            try (InputStream stream = new NamedFileInputStream(yang, yang.getPath())) {
                TestUtils.loadModule(stream);
                fail("SourceException should be thrown");
            }
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                "Duplicate name for typedef (urn:simple.container.demo?revision=1970-01-01)int-ext [at"));
        }
    }

    @Test
    public void testDuplicityInAugmentTarget1() throws Exception {
        final File yang1 = new File(getClass().getResource("/negative-scenario/duplicity/augment0.yang").toURI());
        final File yang2 = new File(getClass().getResource("/negative-scenario/duplicity/augment1.yang").toURI());
        try (InputStream stream1 = new NamedFileInputStream(yang1, yang1.getPath());
            InputStream stream2 = new NamedFileInputStream(yang2, yang2.getPath())) {
            TestUtils.loadModules(Arrays.asList(stream1, stream2));
            testLog = output.toString();
            assertTrue(testLog.contains("An augment cannot add node named 'id' because this name is already used in target"));
        }
    }

    @Test
    public void testDuplicityInAugmentTarget2() throws Exception {
        final File yang1 = new File(getClass().getResource("/negative-scenario/duplicity/augment0.yang").toURI());
        final File yang2 = new File(getClass().getResource("/negative-scenario/duplicity/augment2.yang").toURI());
        try (InputStream stream1 = new NamedFileInputStream(yang1, yang1.getPath());
             InputStream stream2 = new NamedFileInputStream(yang2, yang2.getPath())) {
            TestUtils.loadModules(Arrays.asList(stream1, stream2));
            testLog = output.toString();
            assertTrue(testLog.contains("An augment cannot add node named 'delta' because this name is already used in target"));
        }
    }

    @Test
    public void testMandatoryInAugment() throws Exception {
        final File yang1 = new File(getClass().getResource("/negative-scenario/testfile8.yang").toURI());
        final File yang2 = new File(getClass().getResource("/negative-scenario/testfile7.yang").toURI());
        try (InputStream stream1 = new NamedFileInputStream(yang1, yang1.getPath());
             InputStream stream2 = new NamedFileInputStream(yang2, yang2.getPath())) {
            TestUtils.loadModules(Arrays.asList(stream1, stream2));
            testLog = output.toString();
            assertTrue(testLog.contains(
                    "An augment cannot add node 'linkleaf' because it is mandatory and in module different than target"));
        }
    }

    @Test
    public void testInvalidListKeyDefinition() throws Exception {
        final File yang1 = new File(getClass().getResource("/negative-scenario/invalid-list-key-def.yang").toURI());
        try {
            try (InputStream stream1 = new NamedFileInputStream(yang1, yang1.getPath())) {
                TestUtils.loadModule(stream1);
                fail("InferenceException should be thrown");
            }
        } catch (final ReactorException e) {
            final String expected = "Key 'rib-id' misses node 'rib-id' in list '(invalid:list:key:def?revision=1970-01-01)" +
                    "application-map'";
            assertTrue(e.getCause().getMessage().startsWith(expected));
        }
    }

}
