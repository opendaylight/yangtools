/*
 * Copyright (c) 2019 Ericsson AB. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class YT971Test {
    private static final QNameModule NAMESPACE = QNameModule.create(URI.create("test"), Revision.of("2019-03-25"));

    @Test
    public void testBugXXXX() throws URISyntaxException, IOException, YangSyntaxErrorException, ReactorException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/bugs/YT971/test.yang");
        assertNotNull(schemaContext);

        final DataSchemaNode someContainer = schemaContext.findDataChildByName(
            QName.create(NAMESPACE, "some-container")).get();
        assertThat(someContainer, instanceOf(ContainerSchemaNode.class));
        final ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) someContainer;

        final DataSchemaNode someLeaf = containerSchemaNode.findDataChildByName(QName.create(NAMESPACE, "some-leaf"))
                .get();
        assertThat(someLeaf, instanceOf(LeafSchemaNode.class));
        final LeafSchemaNode leafSchemaNode = (LeafSchemaNode) someLeaf;
        assertEquals(Optional.of("Some string that ends with a backslash (with escape backslash too) \\"),
                     leafSchemaNode.getDescription());
        assertThat(leafSchemaNode.getType(), instanceOf(Int16TypeDefinition.class));

        final DataSchemaNode someOtherLeaf = containerSchemaNode.findDataChildByName(
                QName.create(NAMESPACE, "some-other-leaf")).get();
        assertThat(someOtherLeaf, instanceOf(LeafSchemaNode.class));

        final LeafSchemaNode otherLeafSchemaNode = (LeafSchemaNode) someOtherLeaf;
        assertEquals(Optional.of("Some string after the double backslash"), otherLeafSchemaNode.getDescription());
        assertThat(otherLeafSchemaNode.getType(), instanceOf(Int32TypeDefinition.class));
    }
}
