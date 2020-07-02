/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.normalizedNodesToJsonString;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.augmentationBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.choiceBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Each test tests whether json input is correctly transformed to normalized node structure.
 */
public class JsonStreamToNormalizedNodeTest extends AbstractComplexJsonTest {
    @Test
    public void leafNodeInContainer() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/leaf-node-in-container.json");
        verifyTransformationToNormalizedNode(inputJson, TestingNormalizedNodeStructuresCreator.leafNodeInContainer());
    }

    @Test
    public void leafNodeViaAugmentationInContainer() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/leaf-node-via-augmentation-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.leafNodeViaAugmentationInContainer());
    }

    @Test
    public void leafListNodeInContainer() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/leaflist-node-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.leafListNodeInContainer());
    }

    @Test
    public void keyedListNodeInContainer() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/keyed-list-node-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.keyedListNodeInContainer());
    }

    @Test
    public void choiceNodeInContainer() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/choice-node-in-container.json");
        verifyTransformationToNormalizedNode(inputJson, TestingNormalizedNodeStructuresCreator.choiceNodeInContainer());
    }

    /**
     * Test of translating internal augmentations to normalized nodes structure.
     *
     * <p>
     * 2 nodes are added via internal augmentation A, 1 node via internal augmentation B and one node is originally
     * member of case.
     */
    @Test
    public void caseNodeAugmentationInChoiceInContainer() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/case-node-augmentation-in-choice-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.caseNodeAugmentationInChoiceInContainer());
    }

    /**
     * also test using of namesakes (equal local names with different.
     */
    @Test
    public void caseNodeExternalAugmentationInChoiceInContainer() throws IOException, URISyntaxException {
        final String inputJson =
                loadTextFile("/complexjson/case-node-external-augmentation-in-choice-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.caseNodeExternalAugmentationInChoiceInContainer());
    }

    /**
     * augmentation of choice - adding new case.
     */
    @Test
    public void choiceNodeAugmentationInContainer() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/choice-node-augmentation-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.choiceNodeAugmentationInContainer());
    }

    @Test
    public void unkeyedNodeInContainer() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/unkeyed-node-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
            TestingNormalizedNodeStructuresCreator.unkeyedNodeInContainer());
    }

    /**
     * Top level JSON element contains no information about module name.
     *
     * <p>
     * It should be possible to find out potential module name from available schema context.
     */
    @Test
    public void missingModuleInfoInTopLevelElement() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/missing-module-in-top-level.json");
        verifyTransformationToNormalizedNode(inputJson, TestingNormalizedNodeStructuresCreator.topLevelContainer());
    }

    /**
     * Exception expected.
     *
     * <p>
     * It tests case when several elements with the same name and various namespaces exists and are in JSON specified
     * without module name prefix.
     */
    @Test
    public void leafNamesakes() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/namesakes.json");
        final IllegalStateException ex = assertThrows(IllegalStateException.class,
            // second parameter isn't necessary because error will be raised before it is used.
            () -> verifyTransformationToNormalizedNode(inputJson, null));

        final String errorMessage = ex.getMessage();
        assertThat(errorMessage, containsString("Choose suitable module name for element lf11-namesake:"));
        assertThat(errorMessage, containsString("complexjson-augmentation"));
        assertThat(errorMessage, containsString("complexjson-augmentation-namesake"));
    }

    @Test
    public void emptyTypeTest() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/type-empty.json");
        verifyTransformationToNormalizedNode(inputJson, CONT1_WITH_EMPTYLEAF);
    }

    /**
     * Exception expected.
     *
     * <p>
     * Json input contains element which doesn't exist in YANG schema
     */
    @Test
    public void parsingNotExistingElement() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/not-existing-element.json");
        final IllegalStateException ex = assertThrows(IllegalStateException.class,
            //second parameter isn't necessary because error will be raised before it is used.
            () -> verifyTransformationToNormalizedNode(inputJson, null));

        assertThat(ex.getMessage(), containsString("Schema node with name dummy-element was not found"));
    }

    /**
     * Should not fail as we set the parser to be lenient.
     *
     * <p>
     * Json input contains element which doesn't exist in YANG schema
     */
    @Test
    public void parsingSkipNotExistingElement() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/not-existing-element.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.createLenient(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void listItemWithoutArray() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/keyed-list-restconf-behaviour.json");

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final SchemaNode parentNode = schemaContext.findDataChildByName(CONT_1).get();
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory, parentNode);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void listItemWithArray() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/keyed-list-yang-json-behaviour.json");

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final SchemaNode parentNode = schemaContext.findDataChildByName(CONT_1).get();
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory, parentNode);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void multipleChoiceAugmentation() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/multiple-choice-augmentation-in-container.json");

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final SchemaNode parentNode = schemaContext.findDataChildByName(CONT_1).get();

        final QName augmentChoice1QName = QName.create(parentNode.getQName(), "augment-choice1");
        final QName augmentChoice2QName = QName.create(augmentChoice1QName, "augment-choice2");
        final QName containerQName = QName.create(augmentChoice1QName, "case11-choice-case-container");
        final QName leafQName = QName.create(augmentChoice1QName, "case11-choice-case-leaf");

        final AugmentationIdentifier aug1Id = new AugmentationIdentifier(ImmutableSet.of(augmentChoice1QName));
        final AugmentationIdentifier aug2Id = new AugmentationIdentifier(ImmutableSet.of(augmentChoice2QName));
        final NodeIdentifier augmentChoice1Id = new NodeIdentifier(augmentChoice1QName);
        final NodeIdentifier augmentChoice2Id = new NodeIdentifier(augmentChoice2QName);
        final NodeIdentifier containerId = new NodeIdentifier(containerQName);

        final NormalizedNode<?, ?> cont1Normalized =
                containerBuilder().withNodeIdentifier(new NodeIdentifier(parentNode.getQName()))
                        .withChild(augmentationBuilder().withNodeIdentifier(aug1Id)
                                .withChild(choiceBuilder().withNodeIdentifier(augmentChoice1Id)
                                        .withChild(augmentationBuilder().withNodeIdentifier(aug2Id)
                                                .withChild(choiceBuilder().withNodeIdentifier(augmentChoice2Id)
                                                        .withChild(containerBuilder().withNodeIdentifier(containerId)
                                                                .withChild(leafNode(leafQName, "leaf-value"))
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build()).build();

        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
        assertEquals(cont1Normalized, transformedInput);
    }

    @Test
    public void complexJsonToNormalizedNodesParserTest() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/parser/complex.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final EffectiveModelContext context = YangParserTestUtils.parseYangResourceDirectory("/parser/yang");
        JSONCodecFactory codecFactory = JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(
            context);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, codecFactory);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();

        final String serializationResult = normalizedNodesToJsonString(transformedInput, context, SchemaPath.ROOT);

        final JsonParser parser = new JsonParser();
        final JsonElement expected = parser.parse(inputJson);
        final JsonElement actual = parser.parse(serializationResult);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void nestedArrayException() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/parser/nested-array.json");
        try {
            //second parameter isn't necessary because error will be raised before it is used.
            verifyTransformationToNormalizedNode(inputJson, null);
            fail("Expected exception not raised");
        } catch (final IllegalStateException e) {
            final String errorMessage = e.getMessage();
            assertEquals("Found an unexpected array nested under (ns:complex:json?revision=2014-08-11)lflst11",
                    errorMessage);
        }
    }

    @Test
    public void duplicateNameException() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/parser/duplicate-name.json");
        try {
            //second parameter isn't necessary because error will be raised before it is used.
            verifyTransformationToNormalizedNode(inputJson, null);
            fail("Expected exception not raised");
        } catch (final JsonSyntaxException e) {
            final String errorMessage = e.getMessage();
            assertEquals("Duplicate name lflst11 in JSON input.", errorMessage);
        }
    }

    private static void verifyTransformationToNormalizedNode(final String inputJson,
            final NormalizedNode<?, ?> awaitedStructure) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertEquals("Transformation of json input to normalized node wasn't successful.", awaitedStructure,
                transformedInput);
    }
}
