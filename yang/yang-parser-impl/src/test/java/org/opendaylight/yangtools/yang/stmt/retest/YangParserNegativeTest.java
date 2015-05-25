/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;
import org.opendaylight.yangtools.yang.parser.util.YangValidationException;

public class YangParserNegativeTest {

    @Test
    public void testInvalidImport() throws Exception {
        File yang = new File(getClass().getResource("/negative-scenario/testfile1.yang").toURI());
        try {
            try (InputStream stream = new FileInputStream(yang)) {
                TestUtils.loadModule(stream);
                fail("SomeModifiersUnresolvedException should be thrown");
            }
        } catch (SomeModifiersUnresolvedException e) {
            final Throwable suppressed2levelsDown = e.getSuppressed()[0].getSuppressed()[0];
            assertTrue(suppressed2levelsDown instanceof InferenceException);
            assertEquals("Imported module was not found.", suppressed2levelsDown.getMessage());
        }
    }

    @Test
    public void testTypeNotFound() throws Exception {
        File yang = new File(getClass().getResource("/negative-scenario/testfile2.yang").toURI());
        try {
            try (InputStream stream = new FileInputStream(yang)) {
                TestUtils.loadModule(stream);
                fail("IllegalArgumentException should be thrown");
            }
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(),
                    "Statement (urn:ietf:params:xml:ns:yang:yin:1)int-ext does not have type mapping defined.");
        }
    }

    @Test
    public void testInvalidAugmentTarget() throws Exception {
        File yang1 = new File(getClass().getResource("/negative-scenario/testfile0.yang").toURI());
        File yang2 = new File(getClass().getResource("/negative-scenario/testfile3.yang").toURI());
        try {
            final List<InputStream> streams = new ArrayList<>(2);
            try (InputStream testFile0 = new FileInputStream(yang1)) {
                streams.add(testFile0);
                try (InputStream testFile3 = new FileInputStream(yang2)) {
                    streams.add(testFile3);
                    assertEquals("Expected loaded files count is 2", 2, streams.size());
                    TestUtils.loadModules(streams);
                    fail("SomeModifiersUnresolvedException should be thrown");
                }
            }
        } catch (SomeModifiersUnresolvedException e) {
            final Throwable suppressed2levelsDown = e.getSuppressed()[0].getSuppressed()[0];
            assertTrue(suppressed2levelsDown instanceof InferenceException);
            assertEquals(
                    "Augment target not found: Absolute{path=[(urn:simple.container.demo?revision=1970-01-01)unknown]}",
                    suppressed2levelsDown.getMessage());
        }
    }

    @Test
    public void testInvalidRefine() throws Exception {
        File yang = new File(getClass().getResource("/negative-scenario/testfile4.yang").toURI());
        try {
            try (InputStream stream = new FileInputStream(yang)) {
                TestUtils.loadModule(stream);
                fail("YangParseException should be thrown");
            }
        } catch (YangParseException e) {
            assertTrue(e.getMessage().contains("Can not refine 'presence' for 'node'."));
        }
    }

    @Test
    public void testInvalidLength() throws Exception {
        File yang = new File(getClass().getResource("/negative-scenario/testfile5.yang").toURI());
        try {
            try (InputStream stream = new FileInputStream(yang)) {
                TestUtils.loadModule(stream);
                fail("YangParseException should be thrown");
            }
        } catch (YangParseException e) {
            assertTrue(e.getMessage().contains("Invalid length constraint: <4, 10>"));
        }
    }

    @Test
    public void testInvalidRange() throws Exception {
        File yang = new File(getClass().getResource("/negative-scenario/testfile6.yang").toURI());
        try {
            try (InputStream stream = new FileInputStream(yang)) {
                TestUtils.loadModule(stream);
                fail("YangParseException should be thrown");
            }
        } catch (YangParseException e) {
            assertTrue(e.getMessage().contains("Invalid range constraint: <5, 20>"));
        }
    }

    @Test
    public void testDuplicateContainer() throws Exception {
        File yang = new File(getClass().getResource("/negative-scenario/duplicity/container.yang").toURI());
        try {
            try (InputStream stream = new FileInputStream(yang)) {
                TestUtils.loadModule(stream);
                fail("YangParseException should be thrown");
            }
        } catch (YangParseException e) {
            String expected = "Error in module 'container' at line 10: Can not add 'container foo': node with same name 'foo' already declared at line 6.";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testDuplicateContainerList() throws Exception {
        File yang = new File(getClass().getResource("/negative-scenario/duplicity/container-list.yang").toURI());
        try {
            try (InputStream stream = new FileInputStream(yang)) {
                TestUtils.loadModule(stream);
                fail("YangParseException should be thrown");
            }
        } catch (YangParseException e) {
            String expected = "Error in module 'container-list' at line 10: Can not add 'list foo': node with same name 'foo' already declared at line 6.";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testDuplicateContainerLeaf() throws Exception {
        File yang = new File(getClass().getResource("/negative-scenario/duplicity/container-leaf.yang").toURI());
        try {
            try (InputStream stream = new FileInputStream(yang)) {
                TestUtils.loadModule(stream);
                fail("YangParseException should be thrown");
            }
        } catch (YangParseException e) {
            String expected = "Error in module 'container-leaf' at line 10: Can not add 'leaf foo': node with same name 'foo' already declared at line 6.";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testDuplicateTypedef() throws Exception {
        File yang = new File(getClass().getResource("/negative-scenario/duplicity/typedef.yang").toURI());
        try {
            try (InputStream stream = new FileInputStream(yang)) {
                TestUtils.loadModule(stream);
                fail("YangParseException should be thrown");
            }
        } catch (YangParseException e) {
            String expected = "Error in module 'typedef' at line 10: typedef with same name 'int-ext' already declared at line 6.";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testDuplicityInAugmentTarget1() throws Exception {
        File yang1 = new File(getClass().getResource("/negative-scenario/duplicity/augment0.yang").toURI());
        File yang2 = new File(getClass().getResource("/negative-scenario/duplicity/augment1.yang").toURI());
        try {
            try (InputStream stream1 = new FileInputStream(yang1); InputStream stream2 = new FileInputStream(yang2)) {
                TestUtils.loadModules(Arrays.asList(stream1, stream2));
                fail("YangParseException should be thrown");
            }
        } catch (YangParseException e) {
            assertTrue(e.getMessage().contains("Error in module 'augment1'"));
            assertTrue(e.getMessage().contains("Failed to perform augmentation:"));

        }
    }

    @Test
    public void testDuplicityInAugmentTarget2() throws Exception {
        File yang1 = new File(getClass().getResource("/negative-scenario/duplicity/augment0.yang").toURI());
        File yang2 = new File(getClass().getResource("/negative-scenario/duplicity/augment2.yang").toURI());
        try {
            try (InputStream stream1 = new FileInputStream(yang1); InputStream stream2 = new FileInputStream(yang2)) {
                TestUtils.loadModules(Arrays.asList(stream1, stream2));
                fail("YangParseException should be thrown");
            }
        } catch (YangParseException e) {
            assertTrue(e.getMessage().contains("Error in module "));
            assertTrue(e.getMessage().contains("case with same name already declared "));
        }
    }

    @Test
    public void testMandatoryInAugment() throws Exception {
        File yang1 = new File(getClass().getResource("/negative-scenario/testfile8.yang").toURI());
        File yang2 = new File(getClass().getResource("/negative-scenario/testfile7.yang").toURI());
        try {
            try (InputStream stream1 = new FileInputStream(yang1); InputStream stream2 = new FileInputStream(yang2)) {
                TestUtils.loadModules(Arrays.asList(stream1, stream2));
                fail("YangParseException should be thrown");
            }
        } catch (YangParseException e) {
            String expected = "Error in module 'testfile7' at line 18: Error in augment parsing: cannot augment mandatory node linkleaf";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testWrongDependenciesDir() throws Exception {
        try {
            File yangFile = new File(getClass().getResource("/types/custom-types-test@2012-4-4.yang").toURI());
            File dependenciesDir = new File("/invalid");
            YangContextParser parser = new YangParserImpl();
            parser.parseFile(yangFile, dependenciesDir);
            fail("Exception should be thrown");
        } catch (IllegalStateException e) {
            String expected = File.separator + "invalid does not exists";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testWrongDependenciesDir2() throws Exception {
        try {
            File yangFile = new File(getClass().getResource("/types/custom-types-test@2012-4-4.yang").toURI());
            File dependenciesDir = new File(getClass().getResource("/model").toURI());
            YangContextParser parser = new YangParserImpl();
            parser.parseFile(yangFile, dependenciesDir);
            fail("Exception should be thrown");
        } catch (YangValidationException e) {
            String expected = "Not existing module imported";
            assertTrue(e.getMessage().contains(expected));
        }
    }

    @Test
    public void testInvalidListKeyDefinition() throws Exception {
        File yang1 = new File(getClass().getResource("/negative-scenario/invalid-list-key-def.yang").toURI());
        try {
            try (InputStream stream1 = new FileInputStream(yang1)) {
                TestUtils.loadModule(stream1);
                fail("YangParseException should be thrown");
            }
        } catch (YangParseException e) {
            String expected = "Error in module 'invalid-list-key-def' at line 6: Failed to resolve list key for name rib-id";
            assertEquals(expected, e.getMessage());
        }
    }

}
