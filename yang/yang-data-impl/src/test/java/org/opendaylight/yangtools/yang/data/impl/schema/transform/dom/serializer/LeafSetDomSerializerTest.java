package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import java.io.IOException;
import java.text.ParseException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class LeafSetDomSerializerTest extends AbstractDomSerializerTest{
    private LeafSetEntryNodeDomSerializer temp;
    private LeafListSchemaNode currentLeafList;
    private LeafSetEntryNode tempLeafList;

    @Test
    public void leafSetDomSerializerTest() throws IOException, YangSyntaxErrorException, ParseException, ReactorException {
        leafSetEntryNodeDomSerializerTest();
        leafSetNodeDomSerializerTest();
    }

    private void leafSetEntryNodeDomSerializerTest() throws ReactorException, IOException, YangSyntaxErrorException,
            ParseException {

        ContainerSchemaNode currentContainer = (ContainerSchemaNode) getSchemaContext().findModuleByName("serializer-test", null)
                .getDataChildByName(generateQname("root"));
        currentLeafList = (LeafListSchemaNode) currentContainer.getDataChildByName(generateQname("first-leaf-list"));

        final YangInstanceIdentifier.NodeWithValue barPath = new YangInstanceIdentifier.NodeWithValue(generateQname("first-leaf-list"), "bar");
        tempLeafList = ImmutableLeafSetEntryNodeBuilder.create()
                .withNodeIdentifier(barPath)
                .withValue("bar").build();

        temp = new LeafSetEntryNodeDomSerializer(doc, codecProvider);
        element = temp.serializeLeaf(currentLeafList, tempLeafList);

        testResults("<first-leaf-list xmlns=\"dom-serializer-test\">bar</first-leaf-list>");
    }

    private void leafSetNodeDomSerializerTest() {
        LeafSetNodeDomSerializer nodeDomSerializer = new LeafSetNodeDomSerializer(temp);
        LeafSetEntryNodeDomSerializer leafSetEntryNodeSerializer = (LeafSetEntryNodeDomSerializer) nodeDomSerializer.getLeafSetEntryNodeSerializer();
        leafSetEntryNodeSerializer.serializeLeaf(currentLeafList, tempLeafList);

        testResults("<first-leaf-list xmlns=\"dom-serializer-test\">bar</first-leaf-list>");
    }
}