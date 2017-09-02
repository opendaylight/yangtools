/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.augmentationBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.choiceBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import com.google.common.collect.Sets;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Each test tests whether json input is correctly transformed to normalized node structure.
 */
public class JsonStreamToNormalizedNodeTest {

    private static final QName CONT_1 = QName.create("ns:complex:json", "2014-08-11", "cont1");
    private static final QName EMPTY_LEAF = QName.create(CONT_1,"empty");
    private static SchemaContext schemaContext;

    @BeforeClass
    public static void initialization() throws IOException, URISyntaxException, ReactorException {
        schemaContext = YangParserTestUtils.parseYangSources("/complexjson/yang");
    }

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
        try {
            //second parameter isn't necessary because error will be raised before it is used.
            verifyTransformationToNormalizedNode(inputJson, null);
            fail("Expected exception not raised");
        } catch (final IllegalStateException e) {
            final String errorMessage = e.getMessage();
            assertTrue(errorMessage.contains("Choose suitable module name for element lf11-namesake:"));
            assertTrue(errorMessage.contains("complexjson-augmentation"));
            assertTrue(errorMessage.contains("complexjson-augmentation-namesake"));
        }
    }

    @Test
    public void emptyTypeTest() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/type-empty.json");
        final ContainerNode awaitedStructure = containerBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(CONT_1))
                .addChild(leafNode(EMPTY_LEAF, null))
                .build();

        verifyTransformationToNormalizedNode(inputJson, awaitedStructure);
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
        try {
            //second parameter isn't necessary because error will be raised before it is used.
            verifyTransformationToNormalizedNode(inputJson, null);
        } catch (final IllegalStateException e) {
            assertTrue(e.getMessage().contains("Schema node with name dummy-element wasn't found"));
        }
    }

    @Test
    public void listItemWithoutArray() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/keyed-list-restconf-behaviour.json");

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final SchemaNode parentNode = schemaContext.getDataChildByName(CONT_1);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext, parentNode);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void listItemWithArray() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/keyed-list-yang-json-behaviour.json");

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final SchemaNode parentNode = schemaContext.getDataChildByName(CONT_1);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext, parentNode);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void multipleChoiceAugmentation() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/multiple-choice-augmentation-in-container.json");

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final SchemaNode parentNode = schemaContext.getDataChildByName(CONT_1);

        final QName augmentChoice1QName = QName.create(parentNode.getQName(), "augment-choice1");
        final QName augmentChoice2QName = QName.create(augmentChoice1QName, "augment-choice2");
        final QName containerQName = QName.create(augmentChoice1QName, "case11-choice-case-container");
        final QName leafQName = QName.create(augmentChoice1QName, "case11-choice-case-leaf");

        final YangInstanceIdentifier.AugmentationIdentifier aug1Id =
                new YangInstanceIdentifier.AugmentationIdentifier(Sets.newHashSet(augmentChoice1QName));
        final YangInstanceIdentifier.AugmentationIdentifier aug2Id =
                new YangInstanceIdentifier.AugmentationIdentifier(Sets.newHashSet(augmentChoice2QName));
        final YangInstanceIdentifier.NodeIdentifier augmentChoice1Id =
                new YangInstanceIdentifier.NodeIdentifier(augmentChoice1QName);
        final YangInstanceIdentifier.NodeIdentifier augmentChoice2Id =
                new YangInstanceIdentifier.NodeIdentifier(augmentChoice2QName);
        final YangInstanceIdentifier.NodeIdentifier containerId =
                new YangInstanceIdentifier.NodeIdentifier(containerQName);

        final NormalizedNode<?, ?> cont1Normalized =
                containerBuilder().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(parentNode.getQName()))
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

        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
        assertEquals(cont1Normalized, transformedInput);
    }

    private static void verifyTransformationToNormalizedNode(final String inputJson,
            final NormalizedNode<?, ?> awaitedStructure) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertEquals("Transformation of json input to normalized node wasn't successful.", awaitedStructure,
                transformedInput);
    }
}
