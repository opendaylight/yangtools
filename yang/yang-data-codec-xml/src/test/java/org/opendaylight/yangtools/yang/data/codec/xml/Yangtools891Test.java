/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import java.io.InputStream;
import javax.xml.stream.XMLStreamReader;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefDataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefValidatation;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Yangtools891Test {
    private static final String FOO = "foo";
    private static final String FOO_NS = "urn:opendaylight:params:xml:ns:yang:foo";
    private static final String FOO_REV = "2018-07-27";
    private static final QName FOO_TOP = QName.create(FOO_NS, FOO_REV, "foo-top");
    private static final YangInstanceIdentifier FOO_TOP_ID = YangInstanceIdentifier.of(FOO_TOP);
    private static final String BAZ = "baz";
    private static final String BAZ_NS = "urn:opendaylight:params:xml:ns:yang:baz";
    private static final String BAZ_REV = "2018-07-27";
    private static final QName BAZ_TOP = QName.create(BAZ_NS, BAZ_REV, "baz-top");
    private static final YangInstanceIdentifier BAZ_TOP_ID = YangInstanceIdentifier.of(BAZ_TOP);

    private SchemaContext schemaContext;
    private LeafRefContext leafRefContext;
    private DataTree dataTree;
    private ContainerSchemaNode fooTopNode;
    private ContainerSchemaNode bazTopNode;

    @Before
    public void setup() {
        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/yangtools891");
        leafRefContext = LeafRefContext.create(schemaContext);
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);
        final Module fooModule = schemaContext.findModule(FOO, Revision.of(FOO_REV)).get();
        fooTopNode = (ContainerSchemaNode) fooModule.findDataChildByName(FOO_TOP).get();
        final Module bazModule = schemaContext.findModule(BAZ, Revision.of(BAZ_REV)).get();
        bazTopNode = (ContainerSchemaNode) bazModule.findDataChildByName(BAZ_TOP).get();
    }

    @Test
    public void testValid() throws Exception {
        final NormalizedNode<?, ?> fooTop = readNode("/yangtools891/grouping-with-list-valid.xml", fooTopNode);
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(FOO_TOP_ID, fooTop);
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidatation.validate(writeContributorsCandidate, leafRefContext);
        dataTree.commit(writeContributorsCandidate);
    }

    @Test(expected = LeafRefDataValidationFailedException.class)
    public void testInvalid() throws Exception {
        final NormalizedNode<?, ?> fooTop = readNode("/yangtools891/grouping-with-list-invalid.xml", fooTopNode);
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(FOO_TOP_ID, fooTop);
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidatation.validate(writeContributorsCandidate, leafRefContext);
    }

    @Test
    public void testGroupingWithLeafrefValid() throws Exception {
        final NormalizedNode<?, ?> bazTop = readNode("/yangtools891/baz-top.xml", bazTopNode);
        final NormalizedNode<?, ?> fooTop = readNode("/yangtools891/grouping-with-leafref-valid.xml", fooTopNode);
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(BAZ_TOP_ID, bazTop);
        writeModification.write(FOO_TOP_ID, fooTop);
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidatation.validate(writeContributorsCandidate, leafRefContext);
    }

    @Test(expected = LeafRefDataValidationFailedException.class)
    public void testGroupingWithLeafrefInvalid() throws Exception {
        final NormalizedNode<?, ?> bazTop = readNode("/yangtools891/baz-top.xml", bazTopNode);
        final NormalizedNode<?, ?> fooTop = readNode("/yangtools891/grouping-with-leafref-invalid.xml", fooTopNode);
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(BAZ_TOP_ID, bazTop);
        writeModification.write(FOO_TOP_ID, fooTop);
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidatation.validate(writeContributorsCandidate, leafRefContext);
    }

    private NormalizedNode<?, ?> readNode(final String filename, final ContainerSchemaNode node) throws Exception {
        final InputStream resourceAsStream = Yangtools891Test.class.getResourceAsStream(filename);
        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, node);
        xmlParser.parse(reader);
        return result.getResult();
    }
}