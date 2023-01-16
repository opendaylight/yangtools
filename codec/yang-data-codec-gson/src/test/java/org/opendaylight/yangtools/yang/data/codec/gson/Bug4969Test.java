/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug4969Test {

    @Test
    public void newParserLeafRefTest() throws IOException, URISyntaxException {
        EffectiveModelContext context = YangParserTestUtils.parseYang("""
            module bar {
              namespace "bar";
              prefix bar;

              revision "2016-01-22" {
                description "Initial version";
              }

              typedef ref1 {
                type ref1-2;
              }

              typedef ref2 {
                type ref2-2;
              }

              typedef ref3 {
                type ref3-2;
              }

              typedef ref1-2 {
                type leafref {
                  path "/bar:root/bar:l1";
                }
              }

              typedef ref2-2 {
                type leafref {
                  path "/bar:root/bar:l2";
                }
              }

              typedef ref3-2 {
                type leafref {
                  path "/bar:root/bar:l3";
                }
              }

              container root {
                leaf l1 {
                  type bits {
                    bit a;
                    bit b;
                    bit c;
                    bit d;
                  }
                }
                leaf l2 {
                  type leafref {
                    path "/root/l1";
                  }
                }
                leaf l3 {
                  type leafref {
                    path "../l1";
                  }
                }
              }
            }""", """
            module foo {
              namespace "foo";
              prefix foo;

              import bar {
                prefix bar; revision-date 2016-01-22;
              }

              revision "2016-01-22" {
                description "Initial version";
              }

              container root {
                leaf ref1 {
                  type bar:ref1;
                }
                leaf ref2 {
                  type bar:ref2;
                }
                leaf ref3 {
                  type bar:ref3;
                }
                leaf ref4 {
                  type leafref {
                    path "/bar:root/bar:l1";
                  }
                }
              }
            }""");
        assertNotNull(context);

        verifyNormalizedNodeResult(context);
    }

    private static void verifyNormalizedNodeResult(final EffectiveModelContext context) throws IOException,
            URISyntaxException {
        final String inputJson = TestUtils.loadTextFile("/bug-4969/json/foo.json");
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(context));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();

        assertTrue(transformedInput instanceof ContainerNode);
        ContainerNode root = (ContainerNode) transformedInput;
        final DataContainerChild ref1 = root.childByArg(NodeIdentifier.create(
            QName.create("foo", "2016-01-22", "ref1")));
        final DataContainerChild ref2 = root.childByArg(NodeIdentifier.create(
            QName.create("foo", "2016-01-22", "ref2")));
        final DataContainerChild ref3 = root.childByArg(NodeIdentifier.create(
            QName.create("foo", "2016-01-22", "ref3")));
        final DataContainerChild ref4 = root.childByArg(NodeIdentifier.create(
            QName.create("foo", "2016-01-22", "ref4")));

        assertNotNull(ref1);
        assertNotNull(ref2);
        assertNotNull(ref3);
        assertNotNull(ref4);

        assertEquals("01", ref1.body()); // bit "a" (0)
        assertEquals("03", ref2.body()); // bits "a","b" (0,1)
        assertEquals("07", ref3.body()); // bits "a","b","c" (0-2)
        assertEquals("0F", ref4.body()); // bits "a","b","c","d" (0-3)
    }

    @Test
    public void newParserLeafRefTest2() throws URISyntaxException, IOException {
        EffectiveModelContext context = YangParserTestUtils.parseYang("""
            module augment-leafref-module {
              namespace "augment:leafref:module";
              prefix "auglfrfmo";
              revision 2014-12-16 {
              }
              typedef leafreftype {
                type leafref {
                  path "/auglfrfmo:cont/auglfrfmo:lf3";
                }
              }
              container cont {
                leaf lf3 {
                  type string;
                }
              }
            }""", """
            module leafref-module {
              namespace "leafref:module";
              prefix "lfrfmo";
              import augment-leafref-module { prefix augleafref; revision-date 2014-12-16; }
              revision 2013-11-18 {
              }
              container cont {
                leaf lf1 {
                  type int32;
                }
                leaf lf2 {
                  type leafref {
                    path "/cont/lf1";
                  }
                }
                leaf lf4 {
                  type augleafref:leafreftype;
                }
              }
            }""");
        assertNotNull(context);

        parseJsonToNormalizedNodes(context);
    }

    private static void parseJsonToNormalizedNodes(final EffectiveModelContext context) throws IOException,
            URISyntaxException {
        final String inputJson = TestUtils.loadTextFile("/leafref/json/data.json");
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(context));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }
}
