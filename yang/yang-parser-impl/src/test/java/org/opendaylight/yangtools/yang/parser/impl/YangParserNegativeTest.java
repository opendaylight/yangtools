/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.*;
import java.util.*;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;
import org.opendaylight.yangtools.yang.parser.util.YangValidationException;

public class YangParserNegativeTest {

    @Test
    public void testInvalidImport() throws Exception {
        File yang = new File(getClass().getResource("/negative-scenario/testfile1.yang").toURI());
        try {
            try (InputStream stream = new FileInputStream(yang)) {
                TestUtils.loadModule(stream);
                fail("ValidationException should by thrown");
            }
        } catch (YangValidationException e) {
            assertTrue(e.getMessage().contains("Not existing module imported"));
        }
    }

    @Test
    public void testTypeNotFound() throws Exception {
        File yang = new File(getClass().getResource("/negative-scenario/testfile2.yang").toURI());
        try {
            try (InputStream stream = new FileInputStream(yang)) {
                TestUtils.loadModule(stream);
                fail("YangParseException should by thrown");
            }
        } catch (YangParseException e) {
            assertEquals(e.getMessage(), "Error in module 'test2' at line 24: Referenced type 'int-ext' not found.");
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
                    fail("YangParseException should by thrown");
                }
            }
        } catch (YangParseException e) {
            assertEquals(
                    "Error in module 'test3' at line 10: Error in augment parsing: failed to find augment target: augment /data:unknown",
                    e.getMessage());
        }
    }

    @Test
    public void testInvalidRefine() throws Exception {
        File yang = new File(getClass().getResource("/negative-scenario/testfile4.yang").toURI());
        try {
            try (InputStream stream = new FileInputStream(yang)) {
                TestUtils.loadModule(stream);
                fail("YangParseException should by thrown");
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
                fail("YangParseException should by thrown");
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
                fail("YangParseException should by thrown");
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
                fail("YangParseException should by thrown");
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
                fail("YangParseException should by thrown");
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
                fail("YangParseException should by thrown");
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
                fail("YangParseException should by thrown");
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
                fail("YangParseException should by thrown");
            }
        } catch (YangParseException e) {
            String expected = "Error in module 'augment1' at line 10: Failed to perform augmentation: Error in module 'augment0' at line 8: Can not add 'leaf id' to 'container bar' in module 'augment0': node with same name already declared at line 9";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testDuplicityInAugmentTarget2() throws Exception {
        File yang1 = new File(getClass().getResource("/negative-scenario/duplicity/augment0.yang").toURI());
        File yang2 = new File(getClass().getResource("/negative-scenario/duplicity/augment2.yang").toURI());
        try {
            try (InputStream stream1 = new FileInputStream(yang1); InputStream stream2 = new FileInputStream(yang2)) {
                TestUtils.loadModules(Arrays.asList(stream1, stream2));
                fail("YangParseException should by thrown");
            }
        } catch (YangParseException e) {
            String expected = "Error in module 'augment0' at line 17: Can not add 'anyxml delta' to node 'choice-ext' in module 'augment0': case with same name already declared at line 18";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testMandatoryInAugment() throws Exception {
        File yang1 = new File(getClass().getResource("/negative-scenario/testfile8.yang").toURI());
        File yang2 = new File(getClass().getResource("/negative-scenario/testfile7.yang").toURI());
        try {
            try (InputStream stream1 = new FileInputStream(yang1); InputStream stream2 = new FileInputStream(yang2)) {
                TestUtils.loadModules(Arrays.asList(stream1, stream2));
                fail("YangParseException should by thrown");
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
            YangModelParser parser = new YangParserImpl();
            parser.parseYangModels(yangFile, dependenciesDir);
            fail("Exception should by thrown");
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
            YangModelParser parser = new YangParserImpl();
            parser.parseYangModels(yangFile, dependenciesDir);
            fail("Exception should by thrown");
        } catch (YangValidationException e) {
            String expected = "Not existing module imported";
            assertTrue(e.getMessage().contains(expected));
        }
    }

}
