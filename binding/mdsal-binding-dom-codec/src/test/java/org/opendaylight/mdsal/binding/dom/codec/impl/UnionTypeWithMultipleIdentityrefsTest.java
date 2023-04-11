/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.IdentOne;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.IdentTwo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.TopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.UnionType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public class UnionTypeWithMultipleIdentityrefsTest extends AbstractBindingCodecTest {

    public static final QName MODULE_QNAME = QName.create("urn:opendaylight:yang:union:test",
            "2022-04-28", "union-with-multi-identityref");
    public static final QName TOP_QNAME = QName.create(MODULE_QNAME, "top");
    public static final QName UNION_LEAF_QNAME = QName.create(TOP_QNAME, "test-union-leaf");
    public static final QName IDENTITY_ONE_QNAME = QName.create(MODULE_QNAME, "ident-one");
    public static final QName IDENTITY_TWO_QNAME = QName.create(MODULE_QNAME, "ident-two");


    @Test
    public void fromBindingToNNTest() {
        verifyIdentityWasTranslatedToNNCorrectly(new UnionType(IdentOne.VALUE), IdentOne.QNAME);
        verifyIdentityWasTranslatedToNNCorrectly(new UnionType(IdentTwo.VALUE), IdentTwo.QNAME);
    }

    @Test
    public void fromNNToBindingTest() throws NoSuchFieldException {
        verifyIdentityWasTranslatedToBindingCorrectly(IDENTITY_ONE_QNAME, new UnionType(IdentOne.VALUE));
        verifyIdentityWasTranslatedToBindingCorrectly(IDENTITY_TWO_QNAME, new UnionType(IdentTwo.VALUE));
    }

    @Test
    public void bindingToNNAndBackAgain() {
        final Top topIdentOne = new TopBuilder().setTestUnionLeaf(new UnionType(IdentOne.VALUE)).build();
        final Top topIdentOneReturned = thereAndBackAgain(InstanceIdentifier.builder(Top.class).build(), topIdentOne);
        assertNull(topIdentOneReturned.getTestUnionLeaf().getIdentTwo());
        assertEquals(topIdentOneReturned.getTestUnionLeaf().getIdentOne().implementedInterface(), IdentOne.class);
        final Top topIdentTwo = new TopBuilder().setTestUnionLeaf(new UnionType(IdentTwo.VALUE)).build();
        final Top topIdentTwoReturned = thereAndBackAgain(InstanceIdentifier.builder(Top.class).build(), topIdentTwo);
        assertNull(topIdentTwoReturned.getTestUnionLeaf().getIdentOne());
        assertEquals(topIdentTwoReturned.getTestUnionLeaf().getIdentTwo().implementedInterface(), IdentTwo.class);
    }

    private void verifyIdentityWasTranslatedToBindingCorrectly(final QName identityQname, final UnionType union) {
        final ContainerNode top = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP_QNAME))
            .withChild(ImmutableNodes.leafNode(NodeIdentifier.create(UNION_LEAF_QNAME), identityQname))
            .build();
        final Map.Entry<InstanceIdentifier<?>, DataObject> translated =
            codecContext.fromNormalizedNode(YangInstanceIdentifier.create(NodeIdentifier.create(TOP_QNAME)), top);
        assertNotNull(translated);
        assertNotNull(translated.getValue());
        assertTrue(translated.getValue() instanceof Top);
        assertEquals(new TopBuilder().setTestUnionLeaf(union).build(), translated.getValue());
    }

    private void verifyIdentityWasTranslatedToNNCorrectly(final UnionType chosenIdentity, final QName identityQname) {
        // create binding instance with identity
        final Top topContainer = new TopBuilder().setTestUnionLeaf(chosenIdentity).build();
        // translate via codec into NN
        final Map.Entry<YangInstanceIdentifier, NormalizedNode> translated =
            codecContext.toNormalizedNode(InstanceIdentifier.builder(Top.class).build(), topContainer);
        assertNotNull(translated);
        // verify translation worked
        final NormalizedNode translatedNN = translated.getValue();
        assertNotNull(translatedNN);
        // verify the union leaf is present
        // verify the leaf is the correct identity
        assertEquals(Optional.of(identityQname),
            NormalizedNodes.findNode(translatedNN, NodeIdentifier.create(UNION_LEAF_QNAME)).map(NormalizedNode::body));
    }
}
