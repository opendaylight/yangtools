/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.childArray;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.childPrimitive;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.resolveCont1;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.resolveCont2;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;

/**
 * Each test tests whether json output obtained after transformation contains is corect. The transformation takes
 * normalized node data structure and transform it to json output. To make it easier validate json output it is loaded
 * via gson as structure of json elements which are walked and compared with awaited values.
 */
class NormalizedNodeToJsonStreamTest extends AbstractComplexJsonTest {
    @Test
    void leafNodeInContainer() throws Exception {
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(
            TestingNormalizedNodeStructuresCreator.leafNodeInContainer());
        final var cont1 = resolveCont1(jsonOutput);
        assertNotNull(cont1);

        final var lf11 = childPrimitive(cont1, "complexjson:lf11", "lf11");
        assertNotNull(lf11);
        final var asInt = lf11.getAsInt();
        assertEquals(453, asInt);
    }

    @Test
    void leafListNodeInContainerMultiline() throws Exception {
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(
            TestingNormalizedNodeStructuresCreator.leafListNodeInContainerMultiline());
        final var cont1 = resolveCont1(jsonOutput);
        assertNotNull(cont1);
        final var lflst11 = childArray(cont1, "complexjson:lflst11", "lflst11");
        assertNotNull(lflst11);

        final var lflst11Values = new HashSet<>();
        for (var jsonElement : lflst11) {
            lflst11Values.add(assertInstanceOf(JsonPrimitive.class, jsonElement).getAsString());
        }

        assertEquals(Set.of("lflst11 value2\r\nanother line 2", "lflst11 value1\nanother line 1"), lflst11Values);
    }

    @Test
    void leafNodeViaAugmentationInContainer() throws Exception {
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(
            TestingNormalizedNodeStructuresCreator.leafNodeViaAugmentationInContainer());
        final var cont1 = resolveCont1(jsonOutput);
        assertNotNull(cont1);

        final var lf12_1 = childPrimitive(cont1, "complexjson:lf12_1", "lf12_1");
        assertNotNull(lf12_1);
        final var asString = lf12_1.getAsString();
        assertEquals("lf12 value", asString);
    }

    @Test
    void leafListNodeInContainer() throws Exception {
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(
            TestingNormalizedNodeStructuresCreator.leafListNodeInContainer());
        final var cont1 = resolveCont1(jsonOutput);
        assertNotNull(cont1);
        final var lflst11 = childArray(cont1, "complexjson:lflst11", "lflst11");
        assertNotNull(lflst11);

        final var lflst11Values = new HashSet<>();
        for (var jsonElement : lflst11) {
            lflst11Values.add(assertInstanceOf(JsonPrimitive.class, jsonElement).getAsString());
        }

        assertEquals(Set.of("lflst11 value2", "lflst11 value1"), lflst11Values);
    }

    @Test
    void keyedListNodeInContainer() throws Exception {
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(
            TestingNormalizedNodeStructuresCreator.keyedListNodeInContainer());
        final var cont1 = resolveCont1(jsonOutput);
        assertNotNull(cont1);
        final var lst11 = childArray(cont1, "complexjson:lst11", "lst11");
        assertNotNull(lst11);

        final var iterator = lst11.iterator();
        assertTrue(iterator.hasNext());
        final var lst11Entry1Raw = iterator.next();
        assertFalse(iterator.hasNext());
        final var lst11Entry1 = assertInstanceOf(JsonObject.class, lst11Entry1Raw);

        final var key111 = childPrimitive(lst11Entry1, "complexjson:key111", "key111");
        assertNotNull(key111);
        final var lf112 = childPrimitive(lst11Entry1, "complexjson:lf112", "lf112");
        assertNotNull(lf112);
        final var lf113 = childPrimitive(lst11Entry1, "complexjson:lf113", "lf113");
        assertNotNull(lf113);
        final var lf111 = childPrimitive(lst11Entry1, "complexjson:lf111", "lf111");
        assertNotNull(lf111);

        assertEquals("key111 value", key111.getAsString());
        assertEquals("/complexjson:cont1/complexjson:lflst11[.='foo']", lf112.getAsString());
        assertEquals("lf113 value", lf113.getAsString());
        assertEquals("lf111 value", lf111.getAsString());
    }

    @Test
    void choiceNodeInContainer() throws Exception {
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(
            TestingNormalizedNodeStructuresCreator.choiceNodeInContainer());
        final var cont1 = resolveCont1(jsonOutput);
        assertNotNull(cont1);
        final var lf13 = childPrimitive(cont1, "complexjson:lf13", "lf13");
        assertNotNull(lf13);

        assertEquals("lf13 value", lf13.getAsString());
    }

    /**
     * tested case when case c11A in choice choc11 is augmented (two leaves (augment A) and one leaf (augment B) are
     * added).
     *
     * <p>
     * after running this test following exception is raised:
     * java.lang.IllegalArgumentException: Augmentation allowed only in DataNodeContainer
     * [ChoiceNodeImpl[qname=(ns:complex:json?revision=2014-08-11)choc11]]
     */
    @Test
    void caseNodeAugmentationInChoiceInContainer() throws Exception {
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(
            TestingNormalizedNodeStructuresCreator.caseNodeAugmentationInChoiceInContainer());
        final var cont1 = resolveCont1(jsonOutput);
        assertNotNull(cont1);

        final var lf15_21 = childPrimitive(cont1, "complexjson:lf15_21", "lf15_21");
        assertNotNull(lf15_21);
        final var lf13 = childPrimitive(cont1, "complexjson:lf13", "lf13");
        assertNotNull(lf13);
        final var lf15_11 = childPrimitive(cont1, "complexjson:lf15_11", "lf15_11");
        assertNotNull(lf15_11);
        final var lf15_12 = childPrimitive(cont1, "complexjson:lf15_12", "lf15_12");
        assertNotNull(lf15_12);

        assertEquals("lf15_21 value", lf15_21.getAsString());
        assertEquals("lf13 value", lf13.getAsString());
        assertTrue("one two".equals(lf15_11.getAsString()) || "two one".equals(lf15_11.getAsString()));
        assertEquals("complexjson:ident", lf15_12.getAsString());
    }

    /**
     * tested case when case c11A in choice choc11 is augmented (two leaves (augment A) internally and one two leaves
     * with the same names externally (augment B) are added).
     *
     * <p>
     * after running this test following exception is raised:
     * java.lang.IllegalArgumentException: Augmentation allowed only in DataNodeContainer
     * [ChoiceNodeImpl[qname=(ns:complex:json?revision=2014-08-11)choc11]]
     */
    @Test
    void caseNodeExternalAugmentationInChoiceInContainer() throws Exception {
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(
            TestingNormalizedNodeStructuresCreator.caseNodeExternalAugmentationInChoiceInContainer());
        final var cont1 = resolveCont1(jsonOutput);
        assertNotNull(cont1);

        final var lf15_11Augment = childPrimitive(cont1, "complexjson-augmentation:lf15_11");
        assertNotNull(lf15_11Augment);
        final var lf15_12Augment = childPrimitive(cont1, "complexjson-augmentation:lf15_12");
        assertNotNull(lf15_12Augment);
        final var lf13 = childPrimitive(cont1, "complexjson:lf13", "lf13");
        assertNotNull(lf13);
        final var lf15_11 = childPrimitive(cont1, "complexjson:lf15_11", "lf15_11");
        assertNotNull(lf15_11);
        final var lf15_12 = childPrimitive(cont1, "complexjson:lf15_12", "lf15_12");
        assertNotNull(lf15_12);

        assertEquals("lf15_11 value from augmentation", lf15_11Augment.getAsString());
        assertEquals("lf15_12 value from augmentation", lf15_12Augment.getAsString());
        assertEquals("lf13 value", lf13.getAsString());
        assertTrue("one two".equals(lf15_11.getAsString()) || "two one".equals(lf15_11.getAsString()));
        assertEquals("complexjson:ident", lf15_12.getAsString());
    }

    /**
     * augmentation of choice - adding new case.
     *
     * <p>
     * after running this test following exception is raised:
     * java.lang.IllegalArgumentException: Augmentation allowed only in DataNodeContainer
     * [ChoiceNodeImpl[qname=(ns:complex:json?revision=2014-08-11)choc11]]
     */
    @Test
    void choiceNodeAugmentationInContainer() throws Exception {
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(
            TestingNormalizedNodeStructuresCreator.choiceNodeAugmentationInContainer());
        final var cont1 = resolveCont1(jsonOutput);
        assertNotNull(cont1);

        final var lf17 = childPrimitive(cont1, "complexjson:lf17", "lf17");
        assertNotNull(lf17);
        assertEquals("lf17 value", lf17.getAsString());
    }

    @Test
    void unkeyedNodeInContainer() throws Exception {
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(
            TestingNormalizedNodeStructuresCreator.unkeyedNodeInContainer());
        final var cont1 = resolveCont1(jsonOutput);
        assertNotNull(cont1);

        final var lst12 = childArray(cont1, "complexjson:lst12", "lst12");
        assertNotNull(lst12);

        final var iterator = lst12.iterator();
        assertTrue(iterator.hasNext());
        final var lst12Entry1Raw = iterator.next();
        assertFalse(iterator.hasNext());

        final var lst12Entry1 = assertInstanceOf(JsonObject.class, lst12Entry1Raw);
        final var lf121 = childPrimitive(lst12Entry1, "complexjson:lf121", "lf121");
        assertNotNull(lf121);

        assertEquals("lf121 value", lf121.getAsString());
    }

    @Test
    void emptyTypeTest() throws Exception {
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(CONT1_WITH_EMPTYLEAF);
        final var cont1 = resolveCont1(jsonOutput);
        final var emptyObj = assertInstanceOf(JsonArray.class, cont1.get("empty"));
        assertEquals(1, emptyObj.size());
        assertInstanceOf(JsonNull.class, emptyObj.get(0));
    }

    @Test
    void emptyNonPresenceContainerTest() throws Exception {
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(
            TestingNormalizedNodeStructuresCreator.topLevelContainer());
        final var cont1 = resolveCont1(jsonOutput);
        assertNull(cont1);
    }

    @Test
    void emptyNonPresenceContainerInContainerTest() throws Exception {
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(
            TestingNormalizedNodeStructuresCreator.emptyContainerInContainer());
        final var cont1 = resolveCont1(jsonOutput);
        assertNull(cont1);
    }

    @Test
    void emptyPresenceContainerTest() throws Exception {
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(
            TestingNormalizedNodeStructuresCreator.cont2Node());
        final var cont2 = resolveCont2(jsonOutput);
        assertNotNull(cont2);
    }

    private static String normalizedNodeToJsonStreamTransformation(final NormalizedNode inputStructure)
            throws Exception {
        final var writer = new StringWriter();
        final var jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(lhotkaCodecFactory,
            JsonWriterFactory.createJsonWriter(writer, 2));
        try (var nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream)) {
            nodeWriter.write(inputStructure);
        }

        return writer.toString();
    }
}
