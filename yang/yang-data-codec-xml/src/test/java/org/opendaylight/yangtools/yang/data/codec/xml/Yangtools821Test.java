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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefDataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefValidation;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Yangtools821Test {
    private static final String NS = "urn:opendaylight:params:xml:ns:yang:foo";
    private static final String REV = "2018-07-18";
    private static final QName ROOT = QName.create(NS, REV, "root");
    private static final YangInstanceIdentifier ROOT_ID = YangInstanceIdentifier.of(ROOT);

    private SchemaContext schemaContext;
    private LeafRefContext leafRefContext;
    private DataTree dataTree;

    @Before
    public void setup() {
        schemaContext = YangParserTestUtils.parseYangResource("/yangtools821/foo.yang");
        leafRefContext = LeafRefContext.create(schemaContext);
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);
    }

    @Test
    public void testValidRefFromAugmentation() throws Exception {
        final NormalizedNode<?, ?> root = readNode(schemaContext, "/yangtools821/foo-valid.xml");
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(ROOT_ID, root);
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
        dataTree.commit(writeContributorsCandidate);
    }

    @Test(expected = LeafRefDataValidationFailedException.class)
    public void testInvalidRefFromAugmentation() throws Exception {
        final NormalizedNode<?, ?> root = readNode(schemaContext, "/yangtools821/foo-invalid.xml");
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(ROOT_ID, root);
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
    }

    @Test
    public void testValidRefInContainerFromAugmentation() throws Exception {
        final NormalizedNode<?, ?> root = readNode(schemaContext, "/yangtools821/foo-ref-in-container-valid.xml");
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(ROOT_ID, root);
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
        dataTree.commit(writeContributorsCandidate);
    }

    @Test(expected = LeafRefDataValidationFailedException.class)
    public void testInvalidRefInContainerFromAugmentation() throws Exception {
        final NormalizedNode<?, ?> root = readNode(schemaContext, "/yangtools821/foo-ref-in-container-invalid.xml");
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(ROOT_ID, root);
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
    }

    private static NormalizedNode<?, ?> readNode(final SchemaContext context, final String filename) throws Exception {
        final InputStream resourceAsStream = Yangtools821Test.class.getResourceAsStream(filename);
        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final Module fooModule = context.getModules().iterator().next();
        final ContainerSchemaNode rootContainer = (ContainerSchemaNode) fooModule.findDataChildByName(ROOT).get();
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, context, rootContainer);
        xmlParser.parse(reader);
        return result.getResult();
    }
}