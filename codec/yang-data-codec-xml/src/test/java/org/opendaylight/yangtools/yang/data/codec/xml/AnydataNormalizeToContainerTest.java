/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import javax.xml.stream.XMLStreamReader;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNormalizationException;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.spi.DefaultSchemaTreeInference;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;

public class AnydataNormalizeToContainerTest extends AbstractAnydataTest {

    @Test
    public void testAnydataNormalizeToContainer() throws Exception {
        //Create Data Scheme from yang file
        final SchemaNode fooSchemaNode = SCHEMA_CONTEXT.findDataTreeChild(FOO_QNAME).orElse(null);
        assertThat(fooSchemaNode, instanceOf(AnydataSchemaNode.class));
        final AnydataSchemaNode anyDataSchemaNode = (AnydataSchemaNode) fooSchemaNode;

        final SchemaNode barSchemaNode = SCHEMA_CONTEXT.findDataTreeChild(CONT_QNAME).orElse(null);
        assertThat(barSchemaNode, instanceOf(ContainerSchemaNode.class));
        final ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) barSchemaNode;

        // deserialization
        final XMLStreamReader reader
                = UntrustedXML.createXMLStreamReader(toInputStream("<foo xmlns=\"test-anydata\">"
                + "<bar xmlns=\"test-anydata\">"
                + "<cont-leaf>somedata</cont-leaf>"
                + "</bar>"
                + "</foo>"));

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
                Inference.ofDataTreePath(SCHEMA_CONTEXT, FOO_QNAME));
        xmlParser.parse(reader);

        final NormalizedNode transformedInput = result.getResult();
        assertThat(transformedInput, instanceOf(AnydataNode.class));
        AnydataNode<?> anydataNode = (AnydataNode<?>) transformedInput;

        //Normalize anydata content to specific container model
        DOMSourceAnydata domSourceAnydata = (DOMSourceAnydata) anydataNode.body();
        NormalizedAnydata normalizedAnydata = domSourceAnydata.normalizeTo(
            DefaultSchemaTreeInference.of(SCHEMA_CONTEXT, Absolute.of(CONT_QNAME)));
        assertNotNull(normalizedAnydata);
    }

    @Test
    public void testEmptyAnydataNormalizeToContainerFails() throws Exception {
        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(
                toInputStream("<foo xmlns=\"test-anydata\" />"));

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
                Inference.ofDataTreePath(SCHEMA_CONTEXT, FOO_QNAME));
        xmlParser.parse(reader);

        final NormalizedNode transformedInput = result.getResult();
        assertThat(transformedInput, instanceOf(AnydataNode.class));
        AnydataNode<?> anydataNode = (AnydataNode<?>) transformedInput;

        // Try to normalize empty anydata content to specific container element
        DOMSourceAnydata domSourceAnydata = (DOMSourceAnydata) anydataNode.body();
        AnydataNormalizationException thrown =
                assertThrows(
                        AnydataNormalizationException.class,
                        () -> domSourceAnydata.normalizeTo(
                                DefaultSchemaTreeInference.of(SCHEMA_CONTEXT, Absolute.of(CONT_QNAME)))
                );
        assertEquals("No anydata content found to normalize", thrown.getMessage());
    }
}
