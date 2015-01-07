/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadModules;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 *
 * Each test tests whether json input is correctly transformed to normalized node structure
 */
public class JsonStreamToNormalizedNodeTest {

    private static SchemaContext schemaContext;

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
        String inputJson = loadTextFile("/complexjson/leaf-node-in-container.json");
        verifyTransformationToNormalizedNode(inputJson, TestingNormalizedNodeStructuresCreator.leafNodeInContainer());
    }

    @Test
    public void leafNodeViaAugmentationInContainer() throws IOException, URISyntaxException {
        String inputJson = loadTextFile("/complexjson/leaf-node-via-augmentation-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.leafNodeViaAugmentationInContainer());
    }

    @Test
    public void leafListNodeInContainer() throws IOException, URISyntaxException {
        String inputJson = loadTextFile("/complexjson/leaflist-node-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.leafListNodeInContainer());
    }

    @Test
    public void keyedListNodeInContainer() throws IOException, URISyntaxException {
        String inputJson = loadTextFile("/complexjson/keyed-list-node-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.keyedListNodeInContainer());
    }

    @Test
    public void choiceNodeInContainer() throws IOException, URISyntaxException {
        String inputJson = loadTextFile("/complexjson/choice-node-in-container.json");
        verifyTransformationToNormalizedNode(inputJson, TestingNormalizedNodeStructuresCreator.choiceNodeInContainer());
    }

    /**
     * Test of translating internal augmentations to normalized nodes structure
     *
     * 2 nodes are added via internal augmentation A, 1 node via internal augmentation B and one node is originally
     * member of case.
     *
     */
    @Test
    public void caseNodeAugmentationInChoiceInContainer() throws IOException, URISyntaxException {
        String inputJson = loadTextFile("/complexjson/case-node-augmentation-in-choice-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.caseNodeAugmentationInChoiceInContainer());
    }

    /**
     * also test using of namesakes (equal local names with different
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void caseNodeExternalAugmentationInChoiceInContainer() throws IOException, URISyntaxException {
        String inputJson = loadTextFile("/complexjson/case-node-external-augmentation-in-choice-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.caseNodeExternalAugmentationInChoiceInContainer());
    }

    /**
     * augmentation of choice - adding new case
     */
    @Test
    public void choiceNodeAugmentationInContainer() throws IOException, URISyntaxException {
        String inputJson = loadTextFile("/complexjson/choice-node-augmentation-in-container.json");
        verifyTransformationToNormalizedNode(inputJson,
                TestingNormalizedNodeStructuresCreator.choiceNodeAugmentationInContainer());
    }

    @Test
    public void unkeyedNodeInContainer() throws IOException, URISyntaxException {
        String inputJson = loadTextFile("/complexjson/unkeyed-node-in-container.json");
        verifyTransformationToNormalizedNode(inputJson, TestingNormalizedNodeStructuresCreator.unkeyedNodeInContainer());
    }

    /**
     * Top level JSON element contains no information about module name.
     *
     * It should be possible to find out potential module name from available schema context.
     *
     */
    @Test
    public void missingModuleInfoInTopLevelElement() throws IOException, URISyntaxException {
        String inputJson = loadTextFile("/complexjson/missing-module-in-top-level.json");
        verifyTransformationToNormalizedNode(inputJson, TestingNormalizedNodeStructuresCreator.topLevelContainer());
    }

    /**
     *
     * Exception expected.
     *
     * It tests case when several elements with the same name and various namespaces exists and are in JSON specified
     * without module name prefix.
     */
    @Test
    public void leafNamesakes() throws IOException, URISyntaxException {
        String inputJson = loadTextFile("/complexjson/namesakes.json");
        try {
            //second parameter isn't necessary because error will be raised before it is used.
            verifyTransformationToNormalizedNode(inputJson, null);
        } catch (IllegalStateException e) {
            final String errorMessage = e.getMessage();
            assertTrue(errorMessage.contains("Choose suitable module name for element lf11-namesake:"));
            assertTrue(errorMessage.contains("complexjson-augmentation"));
            assertTrue(errorMessage.contains("complexjson-augmentation-namesake"));
        }
    }

    /**
     *
     * Exception expected.
     *
     * Json input contains element which doesn't exist in YANG schema
     */
    @Test
    public void parsingNotExistingElement() throws IOException, URISyntaxException {
        String inputJson = loadTextFile("/complexjson/not-existing-element.json");
        try {
            //second parameter isn't necessary because error will be raised before it is used.
            verifyTransformationToNormalizedNode(inputJson, null);
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Schema node with name dummy-element wasn't found."));
        }
    }


    private void verifyTransformationToNormalizedNode(final String inputJson,
            final NormalizedNode<?, ?> awaitedStructure) {
        NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        NormalizedNode<?, ?> transformedInput = result.getResult();
        assertEquals("Transformation of json input to normalized node wasn't successful.", awaitedStructure,
                transformedInput);
    }

}
