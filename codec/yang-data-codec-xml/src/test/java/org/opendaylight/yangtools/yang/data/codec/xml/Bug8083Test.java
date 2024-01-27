/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug8083Test {
    private static final QNameModule FOOBAR = QNameModule.of("foobar-ns");
    private static final QNameModule BAZ = QNameModule.of("baz-ns");
    private static final QNameModule ZAB = QNameModule.of("zab-ns");

    @Test
    void testInstanceIdentifierPathWithEmptyListKey() throws Exception {
        final var schemaContext = YangParserTestUtils.parseYang("""
            module baz {
              namespace baz-ns;
              prefix baz-prefix;

              container top-cont {
                list keyed-list {
                  key empty-key-leaf;
                  leaf empty-key-leaf {
                    type empty;
                  }
                  leaf regular-leaf {
                    type int32;
                  }
                }
                leaf iid-leaf {
                  type instance-identifier;
                }
              }
            }""");

        final InputStream resourceAsStream = Bug8083Test.class.getResourceAsStream("/bug8083/xml/baz.xml");

        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        // deserialization
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(BAZ, "top-cont")));
        xmlParser.parse(reader);
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    void testInstanceIdentifierPathWithIdentityrefListKey() throws Exception {
        final var schemaContext = YangParserTestUtils.parseYang("""
            module zab {
              namespace zab-ns;
              prefix zab-prefix;

              identity base-id;

              identity derived-id {
                base base-id;
              }

              container top-cont {
                list keyed-list {
                  key identityref-key-leaf;
                  leaf identityref-key-leaf {
                    type identityref {
                      base base-id;
                    }
                  }
                  leaf regular-leaf {
                    type int32;
                  }
                }
                leaf iid-leaf {
                  type instance-identifier;
                }
              }
            }""");

        final var resourceAsStream = Bug8083Test.class.getResourceAsStream("/bug8083/xml/zab.xml");

        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        // deserialization
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(ZAB, "top-cont")));
        xmlParser.parse(reader);
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    void testInstanceIdentifierPathWithInstanceIdentifierListKey() throws Exception {
        final var schemaContext = YangParserTestUtils.parseYang("""
            module foobar {
              namespace foobar-ns;
              prefix foobar-prefix;

              container top-cont {
                list keyed-list {
                  key iid-key-leaf;
                  leaf iid-key-leaf {
                    type instance-identifier;
                  }
                  leaf regular-leaf {
                    type int32;
                  }
                }
                leaf iid-leaf {
                  type instance-identifier;
                }
                leaf leaf-b {
                  type int32;
                }
              }
            }""");
        final var resourceAsStream = Bug8083Test.class.getResourceAsStream("/bug8083/xml/foobar.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        // deserialization
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOOBAR, "top-cont")));
        xmlParser.parse(reader);
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }
}
