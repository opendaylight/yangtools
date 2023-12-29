/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

abstract class AbstractAnydataTest extends AbstractXmlTest {
    static final QName FOO_QNAME = QName.create("test-anydata", "foo");
    static final QName CONT_QNAME = QName.create(FOO_QNAME, "cont");
    static final QName CONT_ANY_QNAME = QName.create(FOO_QNAME, "cont-any");
    static final QName CONT_LEAF_QNAME = QName.create(FOO_QNAME, "cont-leaf");

    static final NodeIdentifier FOO_NODEID = NodeIdentifier.create(FOO_QNAME);
    static final NodeIdentifier CONT_NODEID = NodeIdentifier.create(CONT_QNAME);
    static final NodeIdentifier CONT_ANY_NODEID = NodeIdentifier.create(CONT_ANY_QNAME);
    static final NodeIdentifier CONT_LEAF_NODEID = NodeIdentifier.create(CONT_LEAF_QNAME);

    static final LeafNode<String> CONT_LEAF = ImmutableNodes.leafNode(CONT_LEAF_NODEID, "abc");

    static EffectiveModelContext SCHEMA_CONTEXT;

    @BeforeAll
    static final void beforeAll() throws Exception {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYang("""
            module test-anydata {
              yang-version 1.1;
              namespace test-anydata;
              prefix ta;

              anydata foo;

              container cont {
                anydata cont-any;
                leaf cont-leaf {
                  type string;
                }
                leaf empty-leaf {
                  type empty;
                }
                container bar {
                  leaf cont-leaf {
                    type string;
                  }
                }
              }

              list lst {
                leaf cont-leaf {
                  type string;
                }
                leaf-list my-leafs {
                  type string;
                }
              }
            }""");
    }

    @AfterAll
    static final void afterAll() {
        SCHEMA_CONTEXT = null;
    }

    static DOMSourceAnydata toDOMSource(final String str) {
        return new DOMSourceAnydata(new DOMSource(
            // DOMSource must have a single document element, which we are ignoring
            readXmlToDocument(toInputStream("<IGNORED>" + str + "</IGNORED>")).getDocumentElement()));
    }

    static InputStream toInputStream(final String str) {
        return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Transform DomSource object to formatted XML string.
     * @param node {@link DOMSource}
     * @return {@link String}
     * @throws TransformerException Internal {@link Transformer} exception
     */
    static String getXmlFromDOMSource(final DOMSource node) throws TransformerException {
        final var writer = new StringWriter();
        final var transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(node, new StreamResult(writer));
        return writer.toString();
    }
}
