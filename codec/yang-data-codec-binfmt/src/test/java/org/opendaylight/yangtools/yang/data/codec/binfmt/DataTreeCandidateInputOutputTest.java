/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
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

class DataTreeCandidateInputOutputTest {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create(FOO, "bar");
    private static final QName BAZ = QName.create(FOO, "baz");
    private static final NodeIdentifier FOO_NODEID = new NodeIdentifier(FOO);
    private static final NodeIdentifier BAR_NODEID = new NodeIdentifier(BAR);
    private static final NodeIdentifier BAZ_NODEID = new NodeIdentifier(BAZ);
    private static final YangInstanceIdentifier FOO_BAR_PATH = YangInstanceIdentifier.of(FOO_NODEID, BAR_NODEID);
    private static final YangInstanceIdentifier BAR_PATH = YangInstanceIdentifier.of(BAR_NODEID);
    private static final YangInstanceIdentifier BAR_BAZ_PATH = BAR_PATH.node(BAZ_NODEID);

    private static EffectiveModelContext CONTEXT;

    private final DataTree dataTree = new InMemoryDataTreeFactory()
        .create(DataTreeConfiguration.DEFAULT_CONFIGURATION, CONTEXT);

    @BeforeAll
    static void beforeAll() {
        CONTEXT = YangParserTestUtils.parseYang("""
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
            }""");
    }

    @Test
    void testWriteRoot() throws Exception {
        assertSerialization(createCandidate(mod -> mod.write(YangInstanceIdentifier.of(),
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(SchemaContext.NAME)).build())));
    }

    @Test
    void testWrite() throws Exception {
        assertSerialization(createCandidate(mod -> mod.write(BAR_PATH,
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(BAR)).build())));
    }

    @Test
    void testDelete() throws Exception {
        createCandidate(mod -> mod.write(BAR_PATH,
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(BAR)).build()));
        assertSerialization(createCandidate(mod -> mod.delete(BAR_PATH)));
    }

    @Test
    void testWriteAppear() throws Exception {
        assertSerialization(createCandidate(mod -> mod.write(FOO_BAR_PATH, ImmutableNodes.leafNode(BAR, "value"))));
    }

    @Test
    void testDeleteDisappear() throws Exception {
        createCandidate(mod -> mod.write(FOO_BAR_PATH, ImmutableNodes.leafNode(BAR, "value")));
        assertSerialization(createCandidate(mod -> mod.delete(FOO_BAR_PATH)));
    }

    @Test
    void testUnmodifiedRoot() throws Exception {
        assertSerialization(createCandidate(mod -> mod.merge(YangInstanceIdentifier.of(),
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(SchemaContext.NAME)).build())));
    }

    @Test
    void testUnmodifiedFoo() throws Exception {
        assertSerialization(createCandidate(mod -> {
            mod.write(FOO_BAR_PATH, ImmutableNodes.leafNode(BAR, "value"));
            mod.delete(FOO_BAR_PATH);
        }));
    }

    private DataTreeCandidate createCandidate(final Consumer<DataTreeModification> function) {
        final var mod = dataTree.takeSnapshot().newModification();
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

    private static void assertSerialization(final DataTreeCandidate orig) throws Exception {
        final var bos = new ByteArrayOutputStream();
        try (var out = NormalizedNodeStreamVersion.current().newDataOutput(new DataOutputStream(bos))) {
            DataTreeCandidateInputOutput.writeDataTreeCandidate(out, orig);
        }

        final var read = DataTreeCandidateInputOutput.readDataTreeCandidate(
            NormalizedNodeDataInput.newDataInput(new DataInputStream(new ByteArrayInputStream(bos.toByteArray()))));
        assertEquals(orig.getRootPath(), read.getRootPath());
        assertEqualMod(orig.getRootNode(), read.getRootNode());
    }

    private static void assertEqualMod(final DataTreeCandidateNode expected, final DataTreeCandidateNode actual) {
        assertEquals(expected.modificationType(), actual.modificationType());

        switch (expected.modificationType()) {
            case DELETE:
            case UNMODIFIED:
                // No children to verify
                break;
            default:
                final var expectedChildren = expected.childNodes();
                final var actualChildren = actual.childNodes();
                assertEquals(expectedChildren.size(), actualChildren.size());

                final var ait = actualChildren.iterator();
                for (var expectedChild : expectedChildren) {
                    assertEqualNodes(expectedChild, ait.next());
                }
        }
    }

    private static void assertEqualNodes(final DataTreeCandidateNode expected, final DataTreeCandidateNode actual) {
        assertEquals(expected.name(), actual.name());
        assertEqualMod(expected, actual);
    }
}
