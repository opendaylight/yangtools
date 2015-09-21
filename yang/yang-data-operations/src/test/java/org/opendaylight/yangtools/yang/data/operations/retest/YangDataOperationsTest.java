/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations.retest;

import static org.junit.Assert.assertNull;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.activation.UnsupportedDataTypeException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser.DomToNormalizedNodeParserFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer.DomFromNormalizedNodeSerializerFactory;
import org.opendaylight.yangtools.yang.data.operations.DataOperations;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class YangDataOperationsTest {

    public static final String CURRENT_XML_NAME = "/current.xml";
    public static final String MODIFICATION_XML_NAME = "/merge.xml";
    private static final String XML_FOLDER_NAME = "/xmls";
    public static final String RESULT_XML_NAME = "/result.xml";
    private static final Object OPERATION_XML_NAME = "/defaultOperation.txt";

    protected final ContainerSchemaNode containerNode;
    protected final String testDirName;
    protected final Optional<ContainerNode> currentConfig;
    protected final Optional<ContainerNode> modification;
    protected final ModifyAction modifyAction;
    private final SchemaContext schema;

    @Parameterized.Parameters()
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // Container
                { "/containerTest_createContainer" },
                { "/containerTest_deleteContainer" },
                { "/containerTest_innerContainerContainer" },
                { "/containerTest_innerLeavesBaseOperationsContainer" },
                { "/containerTest_noneContainer" },
                { "/containerTest_removeContainer"},
                { "/containerTest_replaceContainer"},
                { "/containerTest_choiceActualModificationSameCase"},
                { "/containerTest_choiceActualModificationDifferentCases"},
                { "/containerTest_choiceActualOneCaseModificationOtherCase"},
                //            LeafList
                { "/leafListTest" },
                // List
                { "/listTest" },
                // Additional
                {"/none_NoChange"},
                {"/listTest_alterInnerValue"}
        });
    }

    public YangDataOperationsTest(final String testDir) throws Exception {
        schema = parseTestSchema();
        containerNode = (ContainerSchemaNode) getSchemaNode(schema, "test", "container");
        this.testDirName = testDir;

        currentConfig = loadXmlToCompositeNode(getXmlFolderName() + testDirName + CURRENT_XML_NAME);
        modification = loadXmlToCompositeNode(getXmlFolderName() + testDirName + MODIFICATION_XML_NAME);
        Preconditions.checkState(modification.isPresent(), "Modification xml has to be present under "
                + getXmlFolderName() + testDirName + MODIFICATION_XML_NAME);

        modifyAction = loadModifyAction(getXmlFolderName() + testDirName + OPERATION_XML_NAME);
    }

    protected String getXmlFolderName() {
        return XML_FOLDER_NAME;
    }

    // TODO unite testing resources e.g. schemas with yang-data-impl
    // TODO create extract common testing infrastructure from this and yang-data-impl e.g. xml dom handling

    @Test
    public void testModification() throws Exception {

        Optional<ContainerNode> result = DataOperations.modify(containerNode,
                currentConfig.orNull(), modification.orNull(), modifyAction);

        String expectedResultXmlPath = getXmlFolderName() + testDirName + RESULT_XML_NAME;
        Optional<ContainerNode> expectedResult = loadXmlToCompositeNode(expectedResultXmlPath);

        if (result.isPresent()) {
            verifyModificationResult(result, expectedResult);
        } else {
            assertNull("Result of modification is empty node, result xml should not be present "
                    + expectedResultXmlPath, getClass().getResourceAsStream(expectedResultXmlPath));
        }

    }

    private ModifyAction loadModifyAction(final String path) throws Exception {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            return ModifyAction.MERGE;
        }

        return ModifyAction.fromXmlValue(Files.toString(new File(resource.toURI()), Charsets.UTF_8).trim());
    }

    private void verifyModificationResult(final Optional<ContainerNode> result, final Optional<ContainerNode> expectedResult)
            throws UnsupportedDataTypeException {
        Assert.assertEquals(
                String.format(
                        "Test result %n %s %n Expected %n %s %n",
                        toString(toDom(result.get())),
                        toString(toDom(expectedResult.get()))), expectedResult.get(), result.get());
    }

    private Element toDom(final ContainerNode container) {
        Iterable<Element> a =
                DomFromNormalizedNodeSerializerFactory.getInstance(newDocument(), DomUtils.defaultValueCodecProvider())
                .getContainerNodeSerializer().serialize(containerNode, container);
        return a.iterator().next();
    }

    private Document newDocument() {
        try {
            return BUILDERFACTORY.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to parse XML document", e);
        }
    }

    private Optional<ContainerNode> loadXmlToCompositeNode(final String xmlPath) throws IOException, SAXException {
        InputStream resourceAsStream = getClass().getResourceAsStream(xmlPath);
        if (resourceAsStream == null) {
            return Optional.absent();
        }

        Document currentConfigElement = readXmlToDocument(resourceAsStream);
        Preconditions.checkNotNull(currentConfigElement);

        return Optional.fromNullable(DomToNormalizedNodeParserFactory.getInstance(DomUtils.defaultValueCodecProvider(), schema).getContainerNodeParser().parse(
                Collections.singletonList(currentConfigElement.getDocumentElement()), containerNode));
    }

    SchemaContext parseTestSchema() throws URISyntaxException, FileNotFoundException, ReactorException {
        File testYang = new File(getClass().getResource("/schemas/test.yang").toURI());
        return RetestUtils.parseYangSources(testYang);
    }

    DataSchemaNode getSchemaNode(final SchemaContext context, final String moduleName, final String childNodeName) {
        for (Module module : context.getModules()) {
            if (module.getName().equals(moduleName)) {
                for (DataSchemaNode dataSchemaNode : module.getChildNodes()) {
                    if (dataSchemaNode.getQName().getLocalName().equals(childNodeName)) {
                        return dataSchemaNode;
                    }
                }
            }
        }

        throw new IllegalStateException("Unable to find child node " + childNodeName);
    }

    private static final DocumentBuilderFactory BUILDERFACTORY;

    static {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        BUILDERFACTORY = factory;
    }

    private Document readXmlToDocument(final InputStream xmlContent) throws IOException, SAXException {
        DocumentBuilder dBuilder;
        try {
            dBuilder = BUILDERFACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to parse XML document", e);
        }
        Document doc = dBuilder.parse(xmlContent);

        doc.getDocumentElement().normalize();
        return doc;
    }

    public static String toString(final Element xml) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(xml);
            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (IllegalArgumentException | TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException("Unable to serialize xml element " + xml, e);
        }
    }

}
