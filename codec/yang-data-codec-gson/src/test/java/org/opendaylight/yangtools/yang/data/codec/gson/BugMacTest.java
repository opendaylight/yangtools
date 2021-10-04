package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BugMacTest {
    private static final QNameModule macBug = QNameModule.create(XMLNamespace.of("http://example.com/mac-bug"));
    private static final QName MAC_QNAM = QName.create(macBug, "mac");

    private static EffectiveModelContext FULL_SCHEMA_CONTEXT;

    @BeforeClass
    public static void init() {
        FULL_SCHEMA_CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/mac/yang/");
    }

    @AfterClass
    public static void cleanup() {
        FULL_SCHEMA_CONTEXT = null;
    }


    @Test
    public void testRFC7951InstanceMacAddressPath() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/mac/json/macAddress.json");
        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
                JSONCodecFactorySupplier.RFC7951.getShared(FULL_SCHEMA_CONTEXT));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode transformedInput = result.getResult();
        // validation
        assertNotNull(transformedInput);
        assertTrue(transformedInput instanceof ContainerNode);
        final ContainerNode container = (ContainerNode) transformedInput;
        final NormalizedNode child = container.childByArg(new NodeIdentifier(MAC_QNAM));
        assertTrue(child instanceof LeafNode);
        final String parsedMac = "\"" + child.body()+ "\"";
        final String errorMsg = String.format("Leaf value [%s] is not contained in provided json input [%s]", parsedMac,
                inputJson);
        assertTrue(errorMsg, inputJson.contains(parsedMac));
    }

}
