/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class BugXXXXTest {
    private static final String TEST_NS = "test";
    private static final String REVISION = "2019-03-25";

    @Test public void testBugXXXX() throws URISyntaxException, IOException, YangSyntaxErrorException, ReactorException {
        final SchemaContext schemaContext = StmtTestUtils
                .parseYangSource("/rfc7950/bugXXXX/test.yang");
        assertNotNull(schemaContext);


        final Optional<DataSchemaNode> dataChildByName = schemaContext.findDataChildByName(
                QName.create(TEST_NS, REVISION, "some-container"));
        assertTrue(dataChildByName.isPresent());
        assertThat(dataChildByName.get(), IsInstanceOf.instanceOf(ContainerSchemaNode.class));
        final ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) dataChildByName.get();

        final Optional<DataSchemaNode> dataChildByName1 = containerSchemaNode.findDataChildByName(
                QName.create(TEST_NS, REVISION, "some-leaf"));
        assertTrue(dataChildByName1.isPresent());
        assertThat(dataChildByName1.get(), IsInstanceOf.instanceOf(LeafSchemaNode.class));

        final LeafSchemaNode leafSchemaNode = (LeafSchemaNode) dataChildByName1.get();
        assertTrue(leafSchemaNode.getDescription().isPresent());
        assertEquals("Some string that ends with a backslash (with escape backslash too) \\",
                     leafSchemaNode.getDescription().get());
        assertThat(leafSchemaNode.getType(), IsInstanceOf.instanceOf(Int16TypeDefinition.class));


        final Optional<DataSchemaNode> dataChildByName2 = containerSchemaNode.findDataChildByName(
                QName.create(TEST_NS, REVISION, "some-other-leaf"));
        assertTrue(dataChildByName2.isPresent());
        assertThat(dataChildByName2.get(), IsInstanceOf.instanceOf(LeafSchemaNode.class));

        final LeafSchemaNode otherLeafSchemaNode = (LeafSchemaNode) dataChildByName2.get();
        assertTrue(otherLeafSchemaNode.getDescription().isPresent());
        assertEquals("Some string after the double backslash", otherLeafSchemaNode.getDescription().get());
        assertThat(otherLeafSchemaNode.getType(), IsInstanceOf.instanceOf(Int32TypeDefinition.class));

    }
}
