/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class DataTreeCandidateInputOutputTest {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create(FOO, "bar");
    private static final QName BAZ = QName.create(FOO, "baz");
    private static final NodeIdentifier FOO_NODEID = new NodeIdentifier(FOO);
    private static final NodeIdentifier BAR_NODEID = new NodeIdentifier(BAR);
    private static final NodeIdentifier BAZ_NODEID = new NodeIdentifier(BAZ);
    private static final YangInstanceIdentifier FOO_BAR_PATH = YangInstanceIdentifier.create(FOO_NODEID, BAR_NODEID);
    private static final YangInstanceIdentifier BAR_PATH = YangInstanceIdentifier.create(BAR_NODEID);
    private static final YangInstanceIdentifier BAR_BAZ_PATH = BAR_PATH.node(BAZ_NODEID);
    private static final String FOO_YANG = """
        module foo {
          namespace foo;
          prefix foo;
          container foo {
            leaf bar {
              type string;
            }
          }
          container bar {
            presence "is explicit";
            leaf baz {
              type string;
            }
          }
        }""";

    private static EffectiveModelContext CONTEXT;

    private DataTree dataTree;

    @BeforeClass
    public static void beforeClass() {
        CONTEXT = YangParserTestUtils.parseYang(FOO_YANG);
    }

    @Before
    public void before() {
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, CONTEXT);
    }

    @Test
    public void testWriteRoot() throws IOException {
        assertSerialization(createCandidate(mod -> mod.write(YangInstanceIdentifier.empty(),
            ImmutableNodes.containerNode(SchemaContext.NAME))));
    }

    @Test
    public void testWrite() throws IOException {
        assertSerialization(createCandidate(mod -> mod.write(BAR_PATH, ImmutableNodes.containerNode(BAR))));
    }

    @Test
    public void testDelete() throws IOException {
        createCandidate(mod -> mod.write(BAR_PATH, ImmutableNodes.containerNode(BAR)));
        assertSerialization(createCandidate(mod -> mod.delete(BAR_PATH)));
    }

    @Test
    public void testWriteAppear() throws IOException {
        assertSerialization(createCandidate(mod -> mod.write(FOO_BAR_PATH, ImmutableNodes.leafNode(BAR, "value"))));
    }

    @Test
    public void testDeleteDisappear() throws IOException {
        createCandidate(mod -> mod.write(FOO_BAR_PATH, ImmutableNodes.leafNode(BAR, "value")));
        assertSerialization(createCandidate(mod -> mod.delete(FOO_BAR_PATH)));
    }

    @Test
    public void testUnmodifiedRoot() throws IOException {
        assertSerialization(createCandidate(mod -> mod.merge(YangInstanceIdentifier.empty(),
            ImmutableNodes.containerNode(SchemaContext.NAME))));
    }

    @Test
    public void testUnmodifiedFoo() throws IOException {
        assertSerialization(createCandidate(mod -> {
            mod.write(FOO_BAR_PATH, ImmutableNodes.leafNode(BAR, "value"));
            mod.delete(FOO_BAR_PATH);
        }));
    }

    private DataTreeCandidate createCandidate(final Consumer<DataTreeModification> function) {
        final DataTreeModification mod = dataTree.takeSnapshot().newModification();
        function.accept(mod);
        mod.ready();

        final DataTreeCandidate ret;
        try {
            ret = dataTree.prepare(mod);
        } catch (DataValidationFailedException e) {
            throw new IllegalStateException(e);
        }
        dataTree.commit(ret);
        return ret;
    }

    private static void assertSerialization(final DataTreeCandidate orig) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutput dos = new DataOutputStream(bos);

        try (NormalizedNodeDataOutput out = NormalizedNodeStreamVersion.current().newDataOutput(dos)) {
            DataTreeCandidateInputOutput.writeDataTreeCandidate(out, orig);
        }

        final DataTreeCandidate read = DataTreeCandidateInputOutput.readDataTreeCandidate(
            NormalizedNodeDataInput.newDataInput(new DataInputStream(new ByteArrayInputStream(bos.toByteArray()))));
        assertEquals(orig.getRootPath(), read.getRootPath());
        assertEqualMod(orig.getRootNode(), read.getRootNode());
    }

    private static void assertEqualMod(final DataTreeCandidateNode expected, final DataTreeCandidateNode actual) {
        assertEquals(expected.getModificationType(), actual.getModificationType());

        switch (expected.getModificationType()) {
            case DELETE:
            case UNMODIFIED:
                // No children to verify
                break;
            default:
                final Collection<DataTreeCandidateNode> expectedChildren = expected.getChildNodes();
                final Collection<DataTreeCandidateNode> actualChildren = actual.getChildNodes();
                assertEquals(expectedChildren.size(), actualChildren.size());

                final Iterator<DataTreeCandidateNode> eit = expectedChildren.iterator();
                final Iterator<DataTreeCandidateNode> ait = actualChildren.iterator();

                while (eit.hasNext()) {
                    assertEqualNodes(eit.next(), ait.next());
                }
        }
    }

    private static void assertEqualNodes(final DataTreeCandidateNode expected, final DataTreeCandidateNode actual) {
        assertEquals(expected.getIdentifier(), actual.getIdentifier());
        assertEqualMod(expected, actual);
    }
}
