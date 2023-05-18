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
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.choiceBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;

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
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.createLenient(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    public void listItemWithoutArray() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/keyed-list-restconf-behaviour.json");
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory,
            Inference.ofDataTreePath(schemaContext, CONT_1));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    public void listItemWithArray() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/keyed-list-yang-json-behaviour.json");
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory,
            Inference.ofDataTreePath(schemaContext, CONT_1));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    public void multipleChoiceAugmentation() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/multiple-choice-augmentation-in-container.json");

        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final QName augmentChoice1QName = QName.create(CONT_1, "augment-choice1");
        final QName augmentChoice2QName = QName.create(augmentChoice1QName, "augment-choice2");
        final QName containerQName = QName.create(augmentChoice1QName, "case11-choice-case-container");
        final QName leafQName = QName.create(augmentChoice1QName, "case11-choice-case-leaf");

        final var cont1Normalized = containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(CONT_1))
            .withChild(choiceBuilder()
                .withNodeIdentifier(new NodeIdentifier(augmentChoice1QName))
                .withChild(choiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(augmentChoice2QName))
                    .withChild(containerBuilder()
                        .withNodeIdentifier(new NodeIdentifier(containerQName))
                        .withChild(leafNode(leafQName, "leaf-value"))
                        .build())
                    .build())
                .build())
            .build();

        final var jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
        assertEquals(cont1Normalized, transformedInput);
    }

    private static void verifyTransformationToNormalizedNode(final String inputJson,
            final NormalizedNode awaitedStructure) {
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();
        assertEquals("Transformation of json input to normalized node wasn't successful.", awaitedStructure,
                transformedInput);
    }
}
