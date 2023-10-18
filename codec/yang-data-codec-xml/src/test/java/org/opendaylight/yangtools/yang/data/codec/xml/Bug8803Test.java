/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.StringWriter;
import javax.xml.stream.XMLOutputFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug8803Test {
    private static EffectiveModelContext SCHEMA_CONTEXT;

    @BeforeAll
    static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYang("""
            module bar {
              namespace bar-ns;
              prefix bar;

              import foo {
                prefix foo;
              }

              augment "/foo:top-cont/foo:keyed-list" {
                leaf iid-leaf {
                  type instance-identifier;
                }
              }
            }""", """
            module baz {
              namespace baz-ns;
              prefix baz;

              container top-cont {
                list keyed-list {
                  key key-leaf;
                  leaf key-leaf {
                    type int32;
                  }
                }
              }
            }""", """
            module foo {
              namespace foo-ns;
              prefix foo;

              container top-cont {
                list keyed-list {
                  key key-leaf;
                  leaf key-leaf {
                    type int32;
                  }
                }
              }
            }""");
    }

    @AfterAll
    static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestFactories.class)
    void test(final String factoryMode, final XMLOutputFactory factory) throws Exception {
        final var resourceAsStream = Bug8803Test.class.getResourceAsStream("/bug8803/foo.xml");

        // deserialization
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(SCHEMA_CONTEXT, QName.create("foo-ns", "top-cont")));
        xmlParser.parse(reader);
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);

        // serialization
        final var writer = new StringWriter();
        final var xmlStreamWriter = factory.createXMLStreamWriter(writer);

        final var xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter,
            SCHEMA_CONTEXT);

        final var normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(transformedInput);
        normalizedNodeWriter.flush();

        final String serializedXml = writer.toString();
        assertFalse(serializedXml.isEmpty());
    }
}
