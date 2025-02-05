/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.IdentOne;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.IdentTwo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.TopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.UnionType;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

class UnionTypeWithMultipleIdentityrefsTest extends AbstractBindingCodecTest {
    private static final QName MODULE_QNAME = QName.create("urn:opendaylight:yang:union:test",
            "2022-04-28", "union-with-multi-identityref");
    private static final QName TOP_QNAME = QName.create(MODULE_QNAME, "top");
    private static final QName UNION_LEAF_QNAME = QName.create(TOP_QNAME, "test-union-leaf");
    private static final QName IDENTITY_ONE_QNAME = QName.create(MODULE_QNAME, "ident-one");
    private static final QName IDENTITY_TWO_QNAME = QName.create(MODULE_QNAME, "ident-two");


    @Test
    void fromBindingToNNTest() {
        verifyIdentityWasTranslatedToNNCorrectly(new UnionType(IdentOne.VALUE), IdentOne.QNAME);
        verifyIdentityWasTranslatedToNNCorrectly(new UnionType(IdentTwo.VALUE), IdentTwo.QNAME);
    }

    @Test
    void fromNNToBindingTest() {
        verifyIdentityWasTranslatedToBindingCorrectly(IDENTITY_ONE_QNAME, new UnionType(IdentOne.VALUE));
        verifyIdentityWasTranslatedToBindingCorrectly(IDENTITY_TWO_QNAME, new UnionType(IdentTwo.VALUE));
    }

    @Test
    void bindingToNNAndBackAgain() {
        final var topIdentOne = new TopBuilder().setTestUnionLeaf(new UnionType(IdentOne.VALUE)).build();
        final var topIdentOneReturned = thereAndBackAgain(DataObjectIdentifier.builder(Top.class).build(), topIdentOne);
        assertNull(topIdentOneReturned.getTestUnionLeaf().getIdentTwo());
        assertEquals(topIdentOneReturned.getTestUnionLeaf().getIdentOne().implementedInterface(), IdentOne.class);
        final var topIdentTwo = new TopBuilder().setTestUnionLeaf(new UnionType(IdentTwo.VALUE)).build();
        final var topIdentTwoReturned = thereAndBackAgain(DataObjectIdentifier.builder(Top.class).build(), topIdentTwo);
        assertNull(topIdentTwoReturned.getTestUnionLeaf().getIdentOne());
        assertEquals(topIdentTwoReturned.getTestUnionLeaf().getIdentTwo().implementedInterface(), IdentTwo.class);
    }

    private void verifyIdentityWasTranslatedToBindingCorrectly(final QName identityQname, final UnionType union) {
        final var top = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP_QNAME))
            .withChild(ImmutableNodes.leafNode(NodeIdentifier.create(UNION_LEAF_QNAME), identityQname))
            .build();
        final var translated = codecContext.fromNormalizedNode(YangInstanceIdentifier.of(TOP_QNAME), top);
        assertNotNull(translated);
        final var translatedTop = assertInstanceOf(Top.class, translated.getValue());
        assertEquals(new TopBuilder().setTestUnionLeaf(union).build(), translatedTop);
    }

    private void verifyIdentityWasTranslatedToNNCorrectly(final UnionType chosenIdentity, final QName identityQname) {
        // create binding instance with identity
        final var topContainer = new TopBuilder().setTestUnionLeaf(chosenIdentity).build();
        // translate via codec into NN
        final var translated = codecContext.toNormalizedDataObject(DataObjectIdentifier.builder(Top.class).build(),
            topContainer);
        assertNotNull(translated);
        // verify translation worked
        final var translatedNN = translated.node();
        assertNotNull(translatedNN);
        // verify the union leaf is present
        // verify the leaf is the correct identity
        assertEquals(Optional.of(identityQname),
            NormalizedNodes.findNode(translatedNN, NodeIdentifier.create(UNION_LEAF_QNAME)).map(NormalizedNode::body));
    }
}
