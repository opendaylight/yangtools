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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;

/**
 * Each test tests whether json input is correctly transformed to normalized node structure.
 */
class JsonStreamToNormalizedNodeTest extends AbstractComplexJsonTest {
    @Test
    void leafNodeInContainer() throws Exception {
        final var inputJson = loadTextFile("/complexjson/leaf-node-in-container.json");
        verifyTransformationToNormalizedNode(inputJson, TestingNormalizedNodeStructuresCreator.leafNodeInContainer());
    }

    @Test
    void leafNodeViaAugmentationInContainer() throws Exception {
        final var inputJson = loadTextFile("/complexjson/leaf-node-via-augmentation-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.leafNodeViaAugmentationInContainer());
    }

    @Test
    void leafListNodeInContainer() throws Exception {
        final var inputJson = loadTextFile("/complexjson/leaflist-node-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.leafListNodeInContainer());
    }

    @Test
    void keyedListNodeInContainer() throws Exception {
        final var inputJson = loadTextFile("/complexjson/keyed-list-node-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.keyedListNodeInContainer());
    }

    @Test
    void choiceNodeInContainer() throws Exception {
        final var inputJson = loadTextFile("/complexjson/choice-node-in-container.json");
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
    void caseNodeAugmentationInChoiceInContainer() throws Exception {
        final var inputJson = loadTextFile("/complexjson/case-node-augmentation-in-choice-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.caseNodeAugmentationInChoiceInContainer());
    }

    /**
     * also test using of namesakes (equal local names with different.
     */
    @Test
    void caseNodeExternalAugmentationInChoiceInContainer() throws Exception {
        final var inputJson =
                loadTextFile("/complexjson/case-node-external-augmentation-in-choice-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.caseNodeExternalAugmentationInChoiceInContainer());
    }

    /**
     * augmentation of choice - adding new case.
     */
    @Test
    void choiceNodeAugmentationInContainer() throws Exception {
        final var inputJson = loadTextFile("/complexjson/choice-node-augmentation-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.choiceNodeAugmentationInContainer());
    }

    @Test
    void unkeyedNodeInContainer() throws Exception {
        final var inputJson = loadTextFile("/complexjson/unkeyed-node-in-container.json");
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
    void missingModuleInfoInTopLevelElement() throws Exception {
        final var inputJson = loadTextFile("/complexjson/missing-module-in-top-level.json");
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
    void leafNamesakes() throws Exception {
        final var inputJson = loadTextFile("/complexjson/namesakes.json");
        final var ex = assertThrows(IllegalStateException.class,
            // second parameter isn't necessary because error will be raised before it is used.
            () -> verifyTransformationToNormalizedNode(inputJson, null));

        final var errorMessage = ex.getMessage();
        assertThat(errorMessage, containsString("Choose suitable module name for element lf11-namesake:"));
        assertThat(errorMessage, containsString("complexjson-augmentation"));
        assertThat(errorMessage, containsString("complexjson-augmentation-namesake"));
    }

    @Test
    void emptyTypeTest() throws Exception {
        final var inputJson = loadTextFile("/complexjson/type-empty.json");
        verifyTransformationToNormalizedNode(inputJson, CONT1_WITH_EMPTYLEAF);
    }

    /**
     * Exception expected.
     *
     * <p>
     * Json input contains element which doesn't exist in YANG schema
     */
    @Test
    void parsingNotExistingElement() throws Exception {
        final var inputJson = loadTextFile("/complexjson/not-existing-element.json");
        final var ex = assertThrows(IllegalStateException.class,
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
    void parsingSkipNotExistingElement() throws Exception {
        final var inputJson = loadTextFile("/complexjson/not-existing-element.json");
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.createLenient(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    void listItemWithoutArray() throws Exception {
        final var inputJson = loadTextFile("/complexjson/keyed-list-restconf-behaviour.json");
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory,
            Inference.ofDataTreePath(schemaContext, CONT_1));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    void listItemWithArray() throws Exception {
        final var inputJson = loadTextFile("/complexjson/keyed-list-yang-json-behaviour.json");
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory,
            Inference.ofDataTreePath(schemaContext, CONT_1));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    void multipleChoiceAugmentation() throws Exception {
        final var inputJson = loadTextFile("/complexjson/multiple-choice-augmentation-in-container.json");

        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final var augmentChoice1QName = QName.create(CONT_1, "augment-choice1");
        final var augmentChoice2QName = QName.create(augmentChoice1QName, "augment-choice2");
        final var containerQName = QName.create(augmentChoice1QName, "case11-choice-case-container");
        final var leafQName = QName.create(augmentChoice1QName, "case11-choice-case-leaf");

        final var cont1Normalized = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(CONT_1))
            .withChild(Builders.choiceBuilder()
                .withNodeIdentifier(new NodeIdentifier(augmentChoice1QName))
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(augmentChoice2QName))
                    .withChild(Builders.containerBuilder()
                        .withNodeIdentifier(new NodeIdentifier(containerQName))
                        .withChild(ImmutableNodes.leafNode(leafQName, "leaf-value"))
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
        assertEquals(awaitedStructure, result.getResult().data(),
                "Transformation of json input to normalized node wasn't successful.");
    }
}
