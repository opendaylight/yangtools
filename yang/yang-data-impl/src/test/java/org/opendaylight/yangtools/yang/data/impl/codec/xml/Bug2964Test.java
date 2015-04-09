package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class Bug2964Test {

    public static final String XML_CONTENT = "<cont2 xmlns=\"urn:opendaylight:yangtools:leafref:test\">\n" +
            "<point-to-identityrefleaf>test-identity</point-to-identityrefleaf>\n" +
            "</cont2>";

    private static final DocumentBuilderFactory BUILDERFACTORY;

    private static final String NAMESPACE = "urn:opendaylight:yangtools:leafref:test";
    private static final String TEST_IDENTITY = "test-identity";
    private static final String CONT_2 = "cont2";
    private static final String IDENTITY_LEAFREF = "point-to-identityrefleaf";

    static {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        BUILDERFACTORY = factory;
    }

    private SchemaContext schema;

    @Before
    public void setUp() throws Exception {
        final ByteSource byteSource = new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return Bug2964Test.this.getClass().getResourceAsStream("/leafref-test.yang");
            }
        };
        schema = new YangParserImpl().parseSources(Lists.newArrayList(byteSource));
    }

    public static Document readXmlToDocument(final String xmlContent) throws SAXException, IOException {
        return readXmlToDocument(new ByteArrayInputStream(xmlContent.getBytes(Charsets.UTF_8)));
    }

    @Test
    public void testLeafrefIdentityRefDeserialization() throws Exception {
        final URI namespaceUri = new URI(NAMESPACE);

        final Document document = readXmlToDocument(XML_CONTENT);
        final Element identityLeafRefElement = (Element) document.getDocumentElement().getFirstChild().getNextSibling();

        final Module leafrefModule = schema.findModuleByNamespaceAndRevision(
                namespaceUri, null);
        final ContainerSchemaNode cont2 = (ContainerSchemaNode) leafrefModule.getDataChildByName(CONT_2);
        final DataSchemaNode identityLeafRefSchema = cont2.getDataChildByName(IDENTITY_LEAFREF);
        final Object parsedValue = DomUtils.parseXmlValue(identityLeafRefElement, DomUtils.defaultValueCodecProvider(),
                identityLeafRefSchema, ((LeafSchemaNode) identityLeafRefSchema).getType(), schema);

        assertThat(parsedValue, instanceOf(QName.class));
        final QName parsedQName = (QName) parsedValue;
        assertEquals(namespaceUri, parsedQName.getNamespace());
        assertEquals(TEST_IDENTITY, parsedQName.getLocalName());
    }

    public static Document readXmlToDocument(final InputStream xmlContent) throws SAXException, IOException {
        final DocumentBuilder dBuilder;
        try {
            dBuilder = BUILDERFACTORY.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new IllegalStateException("Failed to parse XML document", e);
        }
        final Document doc = dBuilder.parse(xmlContent);

        doc.getDocumentElement().normalize();
        return doc;
    }
}
