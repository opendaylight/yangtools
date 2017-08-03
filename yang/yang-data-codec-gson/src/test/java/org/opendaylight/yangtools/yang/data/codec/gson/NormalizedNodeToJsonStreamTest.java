/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.childArray;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.childPrimitive;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.resolveCont1;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Each test tests whether json output obtained after transformation contains is corect. The transformation takes
 * normalized node data structure and transform it to json output. To make it easier validate json output it is loaded
 * via gson as structure of json elements which are walked and compared with awaited values.
 *
 */
public class NormalizedNodeToJsonStreamTest {

    private static final QName CONT_1 = QName.create("ns:complex:json", "2014-08-11", "cont1");
    private static final QName EMPTY_LEAF = QName.create(CONT_1, "empty");
    private static SchemaContext schemaContext;

    public interface JsonValidator {
        void validate(final String jsonOutput);
    }

    @BeforeClass
    public static void initialization() throws IOException, URISyntaxException, ReactorException {
        schemaContext = YangParserTestUtils.parseYangSources("/complexjson/yang");
    }

    @Test
    public void leafNodeInContainer() throws IOException, URISyntaxException {
        final Writer writer = new StringWriter();
        final NormalizedNode<?, ?> leafNodeInContainer = TestingNormalizedNodeStructuresCreator.leafNodeInContainer();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, leafNodeInContainer);
        ((JsonValidator) jsonOutput1 -> {
            final JsonObject cont1 = resolveCont1(jsonOutput1);
            assertNotNull(cont1);

            final JsonPrimitive lf11 = childPrimitive(cont1, "complexjson:lf11", "lf11");
            assertNotNull(lf11);
            final int asInt = lf11.getAsInt();
            assertEquals(453, asInt);
        }).validate(jsonOutput);

    }

    @Test
    public void leafListNodeInContainerMultiline() throws IOException, URISyntaxException {
        final Writer writer = new StringWriter();
        final NormalizedNode<?, ?> leafListNodeInContainer = TestingNormalizedNodeStructuresCreator
                .leafListNodeInContainerMultiline();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, leafListNodeInContainer);
        ((JsonValidator) jsonOutput1 -> {
            final JsonObject cont1 = resolveCont1(jsonOutput1);
            assertNotNull(cont1);
            final JsonArray lflst11 = childArray(cont1, "complexjson:lflst11", "lflst11");
            assertNotNull(lflst11);

            final HashSet<Object> lflst11Values = Sets.newHashSet();
            for (final JsonElement jsonElement : lflst11) {
                assertTrue(jsonElement instanceof JsonPrimitive);
                lflst11Values.add(jsonElement.getAsString());
            }

            assertEquals(Sets.newHashSet("lflst11 value2\r\nanother line 2", "lflst11 value1\nanother line 1"),
                    lflst11Values);
        }).validate(jsonOutput);

    }

    @Test
    public void leafNodeViaAugmentationInContainer() throws IOException, URISyntaxException {
        final Writer writer = new StringWriter();
        final NormalizedNode<?, ?> leafNodeViaAugmentationInContainer = TestingNormalizedNodeStructuresCreator
                .leafNodeViaAugmentationInContainer();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, leafNodeViaAugmentationInContainer);
        ((JsonValidator) jsonOutput1 -> {
            final JsonObject cont1 = resolveCont1(jsonOutput1);
            assertNotNull(cont1);

            final JsonPrimitive lf12_1 = childPrimitive(cont1, "complexjson:lf12_1", "lf12_1");
            assertNotNull(lf12_1);
            final String asString = lf12_1.getAsString();
            assertEquals("lf12 value", asString);
        }).validate(jsonOutput);

    }

    @Test
    public void leafListNodeInContainer() throws IOException, URISyntaxException {
        final Writer writer = new StringWriter();
        final NormalizedNode<?, ?> leafListNodeInContainer = TestingNormalizedNodeStructuresCreator
                .leafListNodeInContainer();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, leafListNodeInContainer);
        ((JsonValidator) jsonOutput1 -> {
            final JsonObject cont1 = resolveCont1(jsonOutput1);
            assertNotNull(cont1);
            final JsonArray lflst11 = childArray(cont1, "complexjson:lflst11", "lflst11");
            assertNotNull(lflst11);

            final HashSet<Object> lflst11Values = Sets.newHashSet();
            for (final JsonElement jsonElement : lflst11) {
                assertTrue(jsonElement instanceof JsonPrimitive);
                lflst11Values.add(jsonElement.getAsString());
            }

            assertEquals(Sets.newHashSet("lflst11 value2", "lflst11 value1"), lflst11Values);
        }).validate(jsonOutput);
    }

    @Test
    public void keyedListNodeInContainer() throws IOException, URISyntaxException {
        final Writer writer = new StringWriter();
        final NormalizedNode<?, ?> keyedListNodeInContainer = TestingNormalizedNodeStructuresCreator
                .keyedListNodeInContainer();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, keyedListNodeInContainer);
        ((JsonValidator) jsonOutput1 -> {
            final JsonObject cont1 = resolveCont1(jsonOutput1);
            assertNotNull(cont1);
            final JsonArray lst11 = childArray(cont1, "complexjson:lst11", "lst11");
            assertNotNull(lst11);

            final Iterator<JsonElement> iterator = lst11.iterator();
            assertTrue(iterator.hasNext());
            final JsonElement lst11Entry1Raw = iterator.next();
            assertFalse(iterator.hasNext());
            assertTrue(lst11Entry1Raw instanceof JsonObject);
            final JsonObject lst11Entry1 = (JsonObject) lst11Entry1Raw;

            final JsonPrimitive key111 = childPrimitive(lst11Entry1, "complexjson:key111", "key111");
            assertNotNull(key111);
            final JsonPrimitive lf112 = childPrimitive(lst11Entry1, "complexjson:lf112", "lf112");
            assertNotNull(lf112);
            final JsonPrimitive lf113 = childPrimitive(lst11Entry1, "complexjson:lf113", "lf113");
            assertNotNull(lf113);
            final JsonPrimitive lf111 = childPrimitive(lst11Entry1, "complexjson:lf111", "lf111");
            assertNotNull(lf111);

            assertEquals("key111 value", key111.getAsString());
            assertEquals("/complexjson:cont1/complexjson:lflst11[.='foo']", lf112.getAsString());
            assertEquals("lf113 value", lf113.getAsString());
            assertEquals("lf111 value", lf111.getAsString());
        }).validate(jsonOutput);
    }

    @Test
    public void choiceNodeInContainer() throws IOException, URISyntaxException {
        final Writer writer = new StringWriter();
        final NormalizedNode<?, ?> choiceNodeInContainer = TestingNormalizedNodeStructuresCreator
                .choiceNodeInContainer();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, choiceNodeInContainer);
        ((JsonValidator) jsonOutput1 -> {
            final JsonObject cont1 = resolveCont1(jsonOutput1);
            assertNotNull(cont1);
            final JsonPrimitive lf13 = childPrimitive(cont1, "complexjson:lf13", "lf13");
            assertNotNull(lf13);

            assertEquals("lf13 value", lf13.getAsString());
        }).validate(jsonOutput);
    }

    /**
     * tested case when case c11A in choice choc11 is augmented (two leaves (augment A) and one leaf (augment B) are
     * added)
     *
     * after running this test following exception is raised
     *
     * java.lang.IllegalArgumentException: Augmentation allowed only in DataNodeContainer
     * [ChoiceNodeImpl[qname=(ns:complex:json?revision=2014-08-11)choc11]]
     *
     */
    // @Ignore
    @Test
    public void caseNodeAugmentationInChoiceInContainer() throws IOException, URISyntaxException {
        final Writer writer = new StringWriter();
        final NormalizedNode<?, ?> caseNodeAugmentationInChoiceInContainer = TestingNormalizedNodeStructuresCreator
                .caseNodeAugmentationInChoiceInContainer();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer,
                caseNodeAugmentationInChoiceInContainer);
        ((JsonValidator) jsonOutput1 -> {
            final JsonObject cont1 = resolveCont1(jsonOutput1);
            assertNotNull(cont1);

            final JsonPrimitive lf15_21 = childPrimitive(cont1, "complexjson:lf15_21", "lf15_21");
            assertNotNull(lf15_21);
            final JsonPrimitive lf13 = childPrimitive(cont1, "complexjson:lf13", "lf13");
            assertNotNull(lf13);
            final JsonPrimitive lf15_11 = childPrimitive(cont1, "complexjson:lf15_11", "lf15_11");
            assertNotNull(lf15_11);
            final JsonPrimitive lf15_12 = childPrimitive(cont1, "complexjson:lf15_12", "lf15_12");
            assertNotNull(lf15_12);

            assertEquals("lf15_21 value", lf15_21.getAsString());
            assertEquals("lf13 value", lf13.getAsString());
            assertTrue("one two".equals(lf15_11.getAsString()) || "two one".equals(lf15_11.getAsString()));
            assertEquals("complexjson:lf11", lf15_12.getAsString());

        }).validate(jsonOutput);
    }

    /**
     * tested case when case c11A in choice choc11 is augmented (two leaves (augment A) internally and one two leaves
     * with the same names externally (augment B) are added)
     *
     * after running this test following exception is raised
     *
     * java.lang.IllegalArgumentException: Augmentation allowed only in DataNodeContainer
     * [ChoiceNodeImpl[qname=(ns:complex:json?revision=2014-08-11)choc11]]
     *
     */
    // @Ignore
    @Test
    public void caseNodeExternalAugmentationInChoiceInContainer() throws IOException, URISyntaxException {
        final Writer writer = new StringWriter();
        final NormalizedNode<?, ?> caseNodeExternalAugmentationInChoiceInContainer = TestingNormalizedNodeStructuresCreator
                .caseNodeExternalAugmentationInChoiceInContainer();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer,
                caseNodeExternalAugmentationInChoiceInContainer);
        ((JsonValidator) jsonOutput1 -> {
            final JsonObject cont1 = resolveCont1(jsonOutput1);
            assertNotNull(cont1);

            final JsonPrimitive lf15_11Augment = childPrimitive(cont1, "complexjson-augmentation:lf15_11");
            assertNotNull(lf15_11Augment);
            final JsonPrimitive lf15_12Augment = childPrimitive(cont1, "complexjson-augmentation:lf15_12");
            assertNotNull(lf15_12Augment);
            final JsonPrimitive lf13 = childPrimitive(cont1, "complexjson:lf13", "lf13");
            assertNotNull(lf13);
            final JsonPrimitive lf15_11 = childPrimitive(cont1, "complexjson:lf15_11", "lf15_11");
            assertNotNull(lf15_11);
            final JsonPrimitive lf15_12 = childPrimitive(cont1, "complexjson:lf15_12", "lf15_12");
            assertNotNull(lf15_12);

            assertEquals("lf15_11 value from augmentation", lf15_11Augment.getAsString());
            assertEquals("lf15_12 value from augmentation", lf15_12Augment.getAsString());
            assertEquals("lf13 value", lf13.getAsString());
            assertTrue("one two".equals(lf15_11.getAsString()) || "two one".equals(lf15_11.getAsString()));
            assertEquals("complexjson:lf11", lf15_12.getAsString());

        }).validate(jsonOutput);
    }

    /**
     * augmentation of choice - adding new case
     *
     * after running this test following exception is raised
     *
     * java.lang.IllegalArgumentException: Augmentation allowed only in DataNodeContainer
     * [ChoiceNodeImpl[qname=(ns:complex:json?revision=2014-08-11)choc11]]
     *
     */
    // @Ignore
    @Test
    public void choiceNodeAugmentationInContainer() throws IOException, URISyntaxException {
        final Writer writer = new StringWriter();
        final NormalizedNode<?, ?> choiceNodeAugmentationInContainer = TestingNormalizedNodeStructuresCreator
                .choiceNodeAugmentationInContainer();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, choiceNodeAugmentationInContainer);
        ((JsonValidator) jsonOutput1 -> {
            final JsonObject cont1 = resolveCont1(jsonOutput1);
            assertNotNull(cont1);

            final JsonPrimitive lf17 = childPrimitive(cont1, "complexjson:lf17", "lf17");
            assertNotNull(lf17);
            assertEquals("lf17 value", lf17.getAsString());
        }).validate(jsonOutput);
    }

    @Test
    public void unkeyedNodeInContainer() throws IOException, URISyntaxException {
        final Writer writer = new StringWriter();
        final NormalizedNode<?, ?> unkeyedNodeInContainer = TestingNormalizedNodeStructuresCreator
                .unkeyedNodeInContainer();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, unkeyedNodeInContainer);
        ((JsonValidator) jsonOutput1 -> {
            final JsonObject cont1 = resolveCont1(jsonOutput1);
            assertNotNull(cont1);

            final JsonArray lst12 = childArray(cont1, "complexjson:lst12", "lst12");
            assertNotNull(lst12);

            final Iterator<JsonElement> iterator = lst12.iterator();
            assertTrue(iterator.hasNext());
            final JsonElement lst12Entry1Raw = iterator.next();
            assertFalse(iterator.hasNext());

            assertTrue(lst12Entry1Raw instanceof JsonObject);
            final JsonObject lst12Entry1 = (JsonObject) lst12Entry1Raw;
            final JsonPrimitive lf121 = childPrimitive(lst12Entry1, "complexjson:lf121", "lf121");
            assertNotNull(lf121);

            assertEquals("lf121 value", lf121.getAsString());

        }).validate(jsonOutput);

    }

    @Test
    public void emptyTypeTest() throws IOException, URISyntaxException {
        final StringWriter writer = new StringWriter();
        final ContainerNode emptyStructure = Builders.containerBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(CONT_1))
                .addChild(ImmutableNodes.leafNode(EMPTY_LEAF, null)).build();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, emptyStructure);
        final JsonObject cont1 = resolveCont1(jsonOutput);
        final JsonElement emptyObj = cont1.get("empty");
        assertNotNull(emptyObj);
        assertTrue(emptyObj instanceof JsonArray);
        assertEquals(1, emptyObj.getAsJsonArray().size());
        assertTrue(emptyObj.getAsJsonArray().get(0) instanceof JsonNull);
    }

    private static String normalizedNodeToJsonStreamTransformation(final Writer writer,
            final NormalizedNode<?, ?> inputStructure) throws IOException {

        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.
                createExclusiveWriter(JSONCodecFactory.getShared(schemaContext), SchemaPath.ROOT, null,
                    JsonWriterFactory.createJsonWriter(writer, 2));
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);
        nodeWriter.write(inputStructure);

        nodeWriter.close();
        return writer.toString();
    }

}
