/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringReader;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Checks how {@link XmlParserStream} finds the schema node of each element: inside a {@code choice}, also when the
 * entries of a list holding such a child are interleaved with other elements, in another module's namespace, below
 * an {@code rpc}, below a {@code grouping} and in a model holding two revisions of one module. Also checks what happens
 * to names which name no data node, such as the name of a {@code choice} itself.
 */
class YT1899Test {
    private static final QName TOP = QName.create("yt1899", "top");
    private static final QName FOO = QName.create(TOP, "foo");
    private static final QName NAME = QName.create(TOP, "name");
    private static final QName BAR = QName.create(TOP, "bar");
    private static final QName BAZ_LEAF = QName.create(TOP, "baz-leaf");
    private static final QName OTHER = QName.create(TOP, "other");
    private static final QName THUD = QName.create(TOP, "thud");
    private static final QName INPUT = QName.create(TOP, "input");
    private static final QName IN = QName.create(TOP, "in");
    private static final QName GRP = QName.create(TOP, "grp");
    private static final QName GRP_CONT = QName.create(TOP, "grp-cont");
    private static final QName GRP_LEAF = QName.create(TOP, "grp-leaf");
    private static final QName GRP_ACT = QName.create(TOP, "grp-act");
    private static final QName GRP_ACT_IN = QName.create(TOP, "grp-act-in");
    private static final QName AUG_LEAF = QName.create("yt1899-aug", "aug-leaf");
    private static final QName AUG_CASE_LEAF = QName.create(AUG_LEAF, "aug-case-leaf");
    private static final QName OLD = QName.create("yt1899-rev", "2019-01-01", "old");
    private static final QName OLD_LEAF = QName.create(OLD, "old-leaf");
    private static final QName NEW = QName.create("yt1899-rev", "2020-01-01", "new");
    private static final QName NEW_LEAF = QName.create(NEW, "new-leaf");
    private static final QName RENAMED = QName.create(OLD, "renamed");
    private static final QName RENAMED_LEAF = QName.create(OLD, "renamed-leaf");

    private static final EffectiveModelContext MODEL_CONTEXT =
        YangParserTestUtils.parseYangResourceDirectory("/yt1899/yang");

    private static final String CHOICE_NAME = """
        <top xmlns="yt1899">
          <foo>
            <name>one</name>
            <bar>x</bar>
            <baz-leaf>first</baz-leaf>
          </foo>
        </top>""";

    private static final String UNKNOWN_NAME = """
        <top xmlns="yt1899">
          <foo>
            <name>one</name>
            <unknown>x</unknown>
            <baz-leaf>first</baz-leaf>
          </foo>
        </top>""";

    @Test
    void interleavedListWithChoiceChild() throws Exception {
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP))
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO))
                .withChild(fooEntry("one", "first"))
                .withChild(fooEntry("two", "second"))
                .build())
            .withChild(ImmutableNodes.leafNode(OTHER, "between"))
            .build(), parseTop(true, """
                <top xmlns="yt1899">
                  <foo>
                    <name>one</name>
                    <baz-leaf>first</baz-leaf>
                  </foo>
                  <other>between</other>
                  <foo>
                    <name>two</name>
                    <baz-leaf>second</baz-leaf>
                  </foo>
                </top>"""));
    }

    @Test
    void choiceNameIsUnknownWhenStrict() {
        // "bar" is the name of the choice, which never appears in XML
        final var ex = assertThrows(XMLStreamException.class, () -> parseTop(true, CHOICE_NAME));
        assertThat(ex.getMessage()).contains("Schema for node with name bar and namespace yt1899 does not exist");
    }

    @Test
    void choiceNameIsSkippedWhenLenient() throws Exception {
        assertEquals(topWithOneFoo(), parseTop(false, CHOICE_NAME));
    }

    @Test
    void unknownNameIsUnknownWhenStrict() {
        final var ex = assertThrows(XMLStreamException.class, () -> parseTop(true, UNKNOWN_NAME));
        assertThat(ex.getMessage()).contains("Schema for node with name unknown and namespace yt1899 does not exist");
    }

    @Test
    void unknownNameIsSkippedWhenLenient() throws Exception {
        assertEquals(topWithOneFoo(), parseTop(false, UNKNOWN_NAME));
    }

    @Test
    void unknownNamespaceIsUnknown() {
        // The name exists, but not in a namespace of any module
        final var ex = assertThrows(XMLStreamException.class, () -> parseTop(true, """
            <top xmlns="yt1899">
              <foo>
                <name>one</name>
                <baz-leaf xmlns="yt1899-unknown">first</baz-leaf>
              </foo>
            </top>"""));
        assertThat(ex.getMessage())
            .contains("Schema for node with name baz-leaf and namespace yt1899-unknown does not exist");
    }

    @Test
    void augmentedChildren() throws Exception {
        // One child is added to the list entry, the other to the choice, both from another module
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP))
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO))
                .withChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, NAME, "one"))
                    .withChild(ImmutableNodes.leafNode(NAME, "one"))
                    .withChild(ImmutableNodes.leafNode(AUG_LEAF, "a"))
                    .withChild(ImmutableNodes.newChoiceBuilder()
                        .withNodeIdentifier(new NodeIdentifier(BAR))
                        .withChild(ImmutableNodes.leafNode(AUG_CASE_LEAF, "b"))
                        .build())
                    .build())
                .build())
            .build(), parseTop(true, """
                <top xmlns="yt1899">
                  <foo>
                    <name>one</name>
                    <aug-leaf xmlns="yt1899-aug">a</aug-leaf>
                    <aug-case-leaf xmlns="yt1899-aug">b</aug-case-leaf>
                  </foo>
                </top>"""));
    }

    @Test
    void rpcInput() throws Exception {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(THUD);
        stack.enterSchemaTree(INPUT);
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(INPUT))
            .withChild(ImmutableNodes.leafNode(IN, "x"))
            .build(), parse(stack.toInference(), true, """
                <input xmlns="yt1899">
                  <in>x</in>
                </input>"""));
    }

    @Test
    void groupingStart() throws Exception {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterGrouping(GRP);
        stack.enterDataTree(GRP_CONT);
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(GRP_CONT))
            .withChild(ImmutableNodes.leafNode(GRP_LEAF, "x"))
            .build(), parse(stack.toInference(), true, """
                <grp-cont xmlns="yt1899">
                  <grp-leaf>x</grp-leaf>
                </grp-cont>"""));
    }

    @Test
    void groupingStartAtAction() throws Exception {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterGrouping(GRP);
        stack.enterSchemaTree(GRP_ACT);
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(GRP_ACT))
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(INPUT))
                .withChild(ImmutableNodes.leafNode(GRP_ACT_IN, "x"))
                .build())
            .build(), parse(stack.toInference(), true, """
                <grp-act xmlns="yt1899">
                  <input>
                    <grp-act-in>x</grp-act-in>
                  </input>
                </grp-act>"""));
    }

    @Test
    void childrenOfBothRevisions() throws Exception {
        // The XML namespace does not say which revision an element comes from: the old container and the new one
        // are both found
        final var revisions = YangParserTestUtils.parseYangResourceDirectory("/yt1899/revisions");
        final var data = assertInstanceOf(ContainerNode.class, parse(SchemaInferenceStack.of(revisions).toInference(),
            true, """
                <data>
                  <old xmlns="yt1899-rev">
                    <old-leaf>x</old-leaf>
                  </old>
                  <new xmlns="yt1899-rev">
                    <new-leaf>y</new-leaf>
                  </new>
                </data>"""));
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(OLD))
            .withChild(ImmutableNodes.leafNode(OLD_LEAF, "x"))
            .build(), data.childByArg(new NodeIdentifier(OLD)));
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(NEW))
            .withChild(ImmutableNodes.leafNode(NEW_LEAF, "y"))
            .build(), data.childByArg(new NodeIdentifier(NEW)));
    }

    @Test
    void olderContainerNamedLikeNewerChoice() throws Exception {
        // The newest revision has a choice with this name, which never appears in XML: the older revision's container
        // is found instead
        final var revisions = YangParserTestUtils.parseYangResourceDirectory("/yt1899/revisions");
        final var data = assertInstanceOf(ContainerNode.class, parse(SchemaInferenceStack.of(revisions).toInference(),
            true, """
                <data>
                  <renamed xmlns="yt1899-rev">
                    <renamed-leaf>x</renamed-leaf>
                  </renamed>
                </data>"""));
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(RENAMED))
            .withChild(ImmutableNodes.leafNode(RENAMED_LEAF, "x"))
            .build(), data.childByArg(new NodeIdentifier(RENAMED)));
    }

    private static NormalizedNode topWithOneFoo() {
        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP))
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO))
                .withChild(fooEntry("one", "first"))
                .build())
            .build();
    }

    private static MapEntryNode fooEntry(final String name, final String value) {
        return ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, NAME, name))
            .withChild(ImmutableNodes.leafNode(NAME, name))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAR))
                .withChild(ImmutableNodes.leafNode(BAZ_LEAF, value))
                .build())
            .build();
    }

    private static NormalizedNode parseTop(final boolean strict, final String xml) throws Exception {
        return parse(Inference.ofDataTreePath(MODEL_CONTEXT, TOP), strict, xml);
    }

    private static NormalizedNode parse(final EffectiveStatementInference inference, final boolean strict,
            final String xml) throws Exception {
        final var result = new NormalizationResultHolder();
        XmlParserStream.create(ImmutableNormalizedNodeStreamWriter.from(result), inference, strict)
            .parse(UntrustedXML.createXMLStreamReader(new StringReader(xml)));
        return result.getResult().data();
    }
}
