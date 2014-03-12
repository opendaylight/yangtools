package org.opendaylight.yangtools.yang.data.impl.schema;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.parser.ContainerNodeDomParser;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.serializer.ContainerNodeDomSerializer;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SimpleXMLParsingTest {
    private ContainerSchemaNode containerNode;
    private SchemaContext schema;

    SchemaContext parseTestSchema() {
        YangParserImpl yangParserImpl = new YangParserImpl();
        Set<Module> modules = yangParserImpl.parseYangModelsFromStreams(getTestYangs());
        return yangParserImpl.resolveSchemaContext(modules);
    }

    List<InputStream> getTestYangs() {

        return Lists.newArrayList(Collections2.transform(Lists.newArrayList("test.yang"),
                new Function<String, InputStream>() {
                    @Override
                    public InputStream apply(String input) {
                        InputStream resourceAsStream = getClass().getResourceAsStream(input);
                        Preconditions.checkNotNull(resourceAsStream, "File %s was null", resourceAsStream);
                        return resourceAsStream;
                    }
                }));
    }

    @Before
    public void setUp() throws Exception {
        schema = parseTestSchema();
        containerNode = (ContainerSchemaNode) getSchemaNode(schema, "test", "container");
    }

    @Test
    public void testFromXml() throws Exception {

        Document doc = loadDocument("simple_xml_with_attributes.xml");
        System.out.println(toString(doc.getDocumentElement()));


        List<Element> elements = new ArrayList<>();
        elements.add(doc.getDocumentElement());

        ContainerNode built = new ContainerNodeDomParser().fromDom(elements, containerNode,
                XmlDocumentUtils.defaultValueCodecProvider());
        System.out.println(built);

        List<Element> els = new ContainerNodeDomSerializer().toDom(containerNode, built, XmlDocumentUtils.defaultValueCodecProvider(), doc);
        System.out.println(toString(els.get(0)));

        Assert.assertEquals(toString(doc.getDocumentElement()).replaceAll("\\s*", ""), toString(els.get(0)).replaceAll("\\s*", ""));
    }


    private Document loadDocument(String xmlPath) throws Exception {
        InputStream resourceAsStream = getClass().getResourceAsStream(xmlPath);

        Document currentConfigElement = readXmlToDocument(resourceAsStream);
        Preconditions.checkNotNull(currentConfigElement);
        return currentConfigElement;
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

    private Document readXmlToDocument(InputStream xmlContent) throws IOException, SAXException {
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

    public static String toString(Element xml) {
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

    DataSchemaNode getSchemaNode(SchemaContext context, String moduleName, String childNodeName) {
        for (Module module : context.getModules()) {
            if (module.getName().equals(moduleName)) {
                DataSchemaNode found = findChildNode(module.getChildNodes(), childNodeName);
                Preconditions.checkState(found!=null, "Unable to find %s", childNodeName);
                return found;
            }
        }
        throw new IllegalStateException("Unable to find child node " + childNodeName);
    }

    DataSchemaNode findChildNode(Set<DataSchemaNode> children, String name) {
        List<DataNodeContainer> containers = Lists.newArrayList();

        for (DataSchemaNode dataSchemaNode : children) {
            if (dataSchemaNode.getQName().getLocalName().equals(name))
                return dataSchemaNode;
            if(dataSchemaNode instanceof DataNodeContainer) {
                containers.add((DataNodeContainer) dataSchemaNode);
            } else if(dataSchemaNode instanceof ChoiceNode) {
                containers.addAll(((ChoiceNode) dataSchemaNode).getCases());
            }
        }

        for (DataNodeContainer container : containers) {
            DataSchemaNode retVal = findChildNode(container.getChildNodes(), name);
            if(retVal != null) {
                return retVal;
            }
        }

        return null;
    }
}
