/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
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
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadModules;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.resolveCont1;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Each test tests whether json output obtained after transformation contains is corect. The transformation takes
 * normalized node data structure and transform it to json output. To make it easier validate json output it is loaded
 * via gson as structure of json elements which are walked and compared with awaited values.
 *
 */
public class NormalizedNodeToJsonStreamTest {

    private static SchemaContext schemaContext;

    public interface JsonValidator {
        void validate(final String jsonOutput);
   }

    @BeforeClass
    public static void initialization() throws IOException, URISyntaxException {
        schemaContext = loadModules("/complexjson/yang");
    }

    /**
     * case when anyxml contains simple value will be implemented when anyxml normalized node reprezentation will be
     * specified
     */
    @Ignore
    @Test
    public void anyXmlNodeWithSimpleValueInContainer() throws IOException, URISyntaxException {

    }

    /**
     * case when anyxml contains complex xml will be implemented when anyxml normalized node reprezentation will be
     * specified
     */
    @Ignore
    @Test
    public void anyXmlNodeWithCompositeValueInContainer() throws IOException, URISyntaxException {

    }

    @Test
    public void leafNodeInContainer() throws IOException, URISyntaxException {
        Writer writer = new StringWriter();
        NormalizedNode<?, ?> leafNodeInContainer = TestingNormalizedNodeStructuresCreator.leafNodeInContainer();
        String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, leafNodeInContainer);
        new JsonValidator() {

            @Override
            public void validate(String jsonOutput) {
                JsonObject cont1 = resolveCont1(jsonOutput);
                assertNotNull(cont1);

                JsonPrimitive lf11 = childPrimitive(cont1, "complexjson:lf11", "lf11");
                assertNotNull(lf11);
                int asInt = lf11.getAsInt();
                assertEquals(453, asInt);
            }
        }.validate(jsonOutput);

    }

    @Test
    public void leafListNodeInContainerMultiline() throws IOException, URISyntaxException {
        Writer writer = new StringWriter();
        NormalizedNode<?, ?> leafListNodeInContainer = TestingNormalizedNodeStructuresCreator.leafListNodeInContainerMultiline();
        String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, leafListNodeInContainer);
        new JsonValidator() {

            @Override
            public void validate(String jsonOutput) {
                JsonObject cont1 = resolveCont1(jsonOutput);
                assertNotNull(cont1);
                JsonArray lflst11 = childArray(cont1, "complexjson:lflst11", "lflst11");
                assertNotNull(lflst11);

                HashSet<Object> lflst11Values = Sets.newHashSet();
                for (JsonElement jsonElement : lflst11) {
                    assertTrue(jsonElement instanceof JsonPrimitive);
                    lflst11Values.add(((JsonPrimitive) jsonElement).getAsString());
                }

                assertEquals(Sets.newHashSet("lflst11 value2\r\nanother line 2", "lflst11 value1\nanother line 1"), lflst11Values);
            }
        }.validate(jsonOutput);

    }

    @Test
    public void leafNodeViaAugmentationInContainer() throws IOException, URISyntaxException {
        Writer writer = new StringWriter();
        NormalizedNode<?, ?> leafNodeViaAugmentationInContainer = TestingNormalizedNodeStructuresCreator
                .leafNodeViaAugmentationInContainer();
        String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, leafNodeViaAugmentationInContainer);
        new JsonValidator() {

            @Override
            public void validate(String jsonOutput) {
                JsonObject cont1 = resolveCont1(jsonOutput);
                assertNotNull(cont1);

                JsonPrimitive lf12_1 = childPrimitive(cont1, "complexjson:lf12_1", "lf12_1");
                assertNotNull(lf12_1);
                String asString = lf12_1.getAsString();
                assertEquals("lf12 value", asString);
            }
        }.validate(jsonOutput);

    }

    @Test
    public void leafListNodeInContainer() throws IOException, URISyntaxException {
        Writer writer = new StringWriter();
        NormalizedNode<?, ?> leafListNodeInContainer = TestingNormalizedNodeStructuresCreator.leafListNodeInContainer();
        String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, leafListNodeInContainer);
        new JsonValidator() {

            @Override
            public void validate(String jsonOutput) {
                JsonObject cont1 = resolveCont1(jsonOutput);
                assertNotNull(cont1);
                JsonArray lflst11 = childArray(cont1, "complexjson:lflst11", "lflst11");
                assertNotNull(lflst11);

                HashSet<Object> lflst11Values = Sets.newHashSet();
                for (JsonElement jsonElement : lflst11) {
                    assertTrue(jsonElement instanceof JsonPrimitive);
                    lflst11Values.add(((JsonPrimitive) jsonElement).getAsString());
                }

                assertEquals(Sets.newHashSet("lflst11 value2", "lflst11 value1"), lflst11Values);
            }
        }.validate(jsonOutput);
    }

    @Test
    public void keyedListNodeInContainer() throws IOException, URISyntaxException {
        Writer writer = new StringWriter();
        NormalizedNode<?, ?> keyedListNodeInContainer = TestingNormalizedNodeStructuresCreator
                .keyedListNodeInContainer();
        String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, keyedListNodeInContainer);
        new JsonValidator() {

            @Override
            public void validate(String jsonOutput) {
                JsonObject cont1 = resolveCont1(jsonOutput);
                assertNotNull(cont1);
                JsonArray lst11 = childArray(cont1, "complexjson:lst11", "lst11");
                assertNotNull(lst11);

                Iterator<JsonElement> iterator = lst11.iterator();
                assertTrue(iterator.hasNext());
                JsonElement lst11Entry1Raw = iterator.next();
                assertFalse(iterator.hasNext());
                assertTrue(lst11Entry1Raw instanceof JsonObject);
                JsonObject lst11Entry1 = (JsonObject) lst11Entry1Raw;

                JsonPrimitive key111 = childPrimitive(lst11Entry1, "complexjson:key111", "key111");
                assertNotNull(key111);
                JsonPrimitive lf112 = childPrimitive(lst11Entry1, "complexjson:lf112", "lf112");
                assertNotNull(lf112);
                JsonPrimitive lf113 = childPrimitive(lst11Entry1, "complexjson:lf113", "lf113");
                assertNotNull(lf113);
                JsonPrimitive lf111 = childPrimitive(lst11Entry1, "complexjson:lf111", "lf111");
                assertNotNull(lf111);

                assertEquals("key111 value", key111.getAsString());
                assertEquals("/complexjson:cont1/complexjson:lflst11[.='foo']", lf112.getAsString());
                assertEquals("lf113 value", lf113.getAsString());
                assertEquals("lf111 value", lf111.getAsString());
            }
        }.validate(jsonOutput);
    }

    @Test
    public void choiceNodeInContainer() throws IOException, URISyntaxException {
        Writer writer = new StringWriter();
        NormalizedNode<?, ?> choiceNodeInContainer = TestingNormalizedNodeStructuresCreator.choiceNodeInContainer();
        String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, choiceNodeInContainer);
        new JsonValidator() {

            @Override
            public void validate(String jsonOutput) {
                JsonObject cont1 = resolveCont1(jsonOutput);
                assertNotNull(cont1);
                JsonPrimitive lf13 = childPrimitive(cont1, "complexjson:lf13", "lf13");
                assertNotNull(lf13);

                assertEquals("lf13 value", lf13.getAsString());
            }
        }.validate(jsonOutput);
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
//    @Ignore
    @Test
    public void caseNodeAugmentationInChoiceInContainer() throws IOException, URISyntaxException {
        Writer writer = new StringWriter();
        NormalizedNode<?, ?> caseNodeAugmentationInChoiceInContainer = TestingNormalizedNodeStructuresCreator
                .caseNodeAugmentationInChoiceInContainer();
        String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, caseNodeAugmentationInChoiceInContainer);
        new JsonValidator() {

            @Override
            public void validate(String jsonOutput) {
                JsonObject cont1 = resolveCont1(jsonOutput);
                assertNotNull(cont1);

                JsonPrimitive lf15_21 = childPrimitive(cont1, "complexjson:lf15_21", "lf15_21");
                assertNotNull(lf15_21);
                JsonPrimitive lf13 = childPrimitive(cont1, "complexjson:lf13", "lf13");
                assertNotNull(lf13);
                JsonPrimitive lf15_11 = childPrimitive(cont1, "complexjson:lf15_11", "lf15_11");
                assertNotNull(lf15_11);
                JsonPrimitive lf15_12 = childPrimitive(cont1, "complexjson:lf15_12", "lf15_12");
                assertNotNull(lf15_12);

                assertEquals("lf15_21 value", lf15_21.getAsString());
                assertEquals("lf13 value", lf13.getAsString());
                assertTrue("one two".equals(lf15_11.getAsString()) || "two one".equals(lf15_11.getAsString()));
                assertEquals("complexjson:lf11", lf15_12.getAsString());

            }
        }.validate(jsonOutput);
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
//    @Ignore
    @Test
    public void caseNodeExternalAugmentationInChoiceInContainer() throws IOException, URISyntaxException {
        Writer writer = new StringWriter();
        NormalizedNode<?, ?> caseNodeExternalAugmentationInChoiceInContainer = TestingNormalizedNodeStructuresCreator
                .caseNodeExternalAugmentationInChoiceInContainer();
        String jsonOutput = normalizedNodeToJsonStreamTransformation(writer,
                caseNodeExternalAugmentationInChoiceInContainer);
        new JsonValidator() {

            @Override
            public void validate(String jsonOutput) {
                JsonObject cont1 = resolveCont1(jsonOutput);
                assertNotNull(cont1);

                JsonPrimitive lf15_11Augment = childPrimitive(cont1, "complexjson-augmentation:lf15_11");
                assertNotNull(lf15_11Augment);
                JsonPrimitive lf15_12Augment = childPrimitive(cont1, "complexjson-augmentation:lf15_12");
                assertNotNull(lf15_12Augment);
                JsonPrimitive lf13 = childPrimitive(cont1, "complexjson:lf13", "lf13");
                assertNotNull(lf13);
                JsonPrimitive lf15_11 = childPrimitive(cont1, "complexjson:lf15_11", "lf15_11");
                assertNotNull(lf15_11);
                JsonPrimitive lf15_12 = childPrimitive(cont1, "complexjson:lf15_12", "lf15_12");
                assertNotNull(lf15_12);

                assertEquals("lf15_11 value from augmentation", lf15_11Augment.getAsString());
                assertEquals("lf15_12 value from augmentation", lf15_12Augment.getAsString());
                assertEquals("lf13 value", lf13.getAsString());
                assertTrue("one two".equals(lf15_11.getAsString()) || "two one".equals(lf15_11.getAsString()));
                assertEquals("complexjson:lf11", lf15_12.getAsString());

            }
        }.validate(jsonOutput);
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
//    @Ignore
    @Test
    public void choiceNodeAugmentationInContainer() throws IOException, URISyntaxException {
        Writer writer = new StringWriter();
        NormalizedNode<?, ?> choiceNodeAugmentationInContainer = TestingNormalizedNodeStructuresCreator
                .choiceNodeAugmentationInContainer();
        String jsonOutput = normalizedNodeToJsonStreamTransformation(writer,
                choiceNodeAugmentationInContainer);
        new JsonValidator() {

            @Override
            public void validate(String jsonOutput) {
                JsonObject cont1 = resolveCont1(jsonOutput);
                assertNotNull(cont1);

                JsonPrimitive lf17 = childPrimitive(cont1, "complexjson:lf17","lf17");
                assertNotNull(lf17);
                assertEquals("lf17 value",lf17.getAsString());
            }
        }.validate(jsonOutput);
    }

    @Test
    public void unkeyedNodeInContainer() throws IOException, URISyntaxException {
        Writer writer = new StringWriter();
        NormalizedNode<?, ?> unkeyedNodeInContainer = TestingNormalizedNodeStructuresCreator
                .unkeyedNodeInContainer();
        String jsonOutput = normalizedNodeToJsonStreamTransformation(writer,
                unkeyedNodeInContainer);
        new JsonValidator() {

            @Override
            public void validate(String jsonOutput) {
                JsonObject cont1 = resolveCont1(jsonOutput);
                assertNotNull(cont1);

                JsonArray lst12 = childArray(cont1, "complexjson:lst12","lst12");
                assertNotNull(lst12);

                Iterator<JsonElement> iterator = lst12.iterator();
                assertTrue(iterator.hasNext());
                JsonElement lst12Entry1Raw = iterator.next();
                assertFalse(iterator.hasNext());

                assertTrue(lst12Entry1Raw instanceof JsonObject);
                JsonObject lst12Entry1 = (JsonObject)lst12Entry1Raw;
                JsonPrimitive lf121 = childPrimitive(lst12Entry1, "complexjson:lf121", "lf121");
                assertNotNull(lf121);

                assertEquals("lf121 value",lf121.getAsString());

            }
        }.validate(jsonOutput);

    }

    private String normalizedNodeToJsonStreamTransformation(final Writer writer,
            final NormalizedNode<?, ?> inputStructure) throws IOException {

        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.create(schemaContext, writer, 2);
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);
        nodeWriter.write(inputStructure);

        nodeWriter.close();
        return writer.toString();
    }

}
