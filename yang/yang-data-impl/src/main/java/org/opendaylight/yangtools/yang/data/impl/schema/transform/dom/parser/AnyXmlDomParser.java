package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser;

import com.google.common.base.Optional;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Element;

public class AnyXmlDomParser implements ToNormalizedNodeParser<Element,AnyXmlNode,AnyXmlSchemaNode> {

    @Override
    public AnyXmlNode parse(final Iterable<Element> xmlDom, final AnyXmlSchemaNode schema) {
        final Element value = xmlDom.iterator().next();
        return new AnyXmlNode() {
            @Override
            public YangInstanceIdentifier.NodeIdentifier getIdentifier() {
                return new YangInstanceIdentifier.NodeIdentifier(schema.getQName());
            }

            @Override
            public Node<?> getValue() {
                return XmlDocumentUtils.toDomNode(value, Optional.<DataSchemaNode>absent(), Optional.<XmlCodecProvider>absent());
            }

            @Override
            public Map<QName, String> getAttributes() {
                return Collections.emptyMap();
            }

            @Override
            public Object getAttributeValue(final QName name) {
                return null;
            }

            @Override
            public QName getNodeType() {
                return schema.getQName();
            }
        };
    };
}
