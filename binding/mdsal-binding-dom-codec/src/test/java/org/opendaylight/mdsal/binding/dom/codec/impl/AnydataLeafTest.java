/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Map.Entry;
import javax.xml.transform.dom.DOMSource;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal438.norev.Cont;
import org.opendaylight.yang.gen.v1.mdsal438.norev.ContBuilder;
import org.opendaylight.yang.gen.v1.mdsal438.norev.cont.ContAny;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.binding.AbstractOpaqueData;
import org.opendaylight.yangtools.yang.binding.AbstractOpaqueObject;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.OpaqueData;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AnydataLeafTest extends AbstractBindingCodecTest {
    private static final NodeIdentifier CONT_NODE_ID = new NodeIdentifier(Cont.QNAME);

    private DOMSource domSource;
    private ContainerNode cont;

    @Override
    public void before() {
        super.before();

        final Document doc = UntrustedXML.newDocumentBuilder().newDocument();
        final Element element = doc.createElement("foo");
        domSource = new DOMSource(element);

        cont = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(CONT_NODE_ID)
            .withChild(ImmutableNodes.newAnydataBuilder(DOMSource.class)
                .withNodeIdentifier(new NodeIdentifier(ContAny.QNAME))
                .withValue(domSource)
                .build())
            .build();
    }

    @Test
    public void testAnydataToBinding() {
        final Entry<InstanceIdentifier<?>, DataObject> entry = codecContext.fromNormalizedNode(
            YangInstanceIdentifier.of(CONT_NODE_ID), cont);
        assertEquals(InstanceIdentifier.create(Cont.class), entry.getKey());
        final DataObject ldo = entry.getValue();
        assertThat(ldo, instanceOf(Cont.class));

        // So no... GrpAny should be null ..
        final Cont contValue = (Cont) ldo;
        assertNull(contValue.getGrpAny());

        // ContAny is interesting
        final ContAny anyCont = contValue.getContAny();
        assertNotNull(anyCont);
        assertEquals(ContAny.class, anyCont.implementedInterface());

        final OpaqueData<?> value = anyCont.getValue();
        assertNotNull(value);
        assertEquals(DOMSource.class, value.getObjectModel());
        assertSame(domSource, value.getData());

        // Stable hashCode
        final int hashOne = anyCont.hashCode();
        final int hashTwo = anyCont.hashCode();
        assertEquals(hashOne, hashTwo);

        // Basic equality
        assertNotEquals(anyCont, null);
        assertEquals(anyCont, anyCont);
        assertEquals(new FakeCont(), anyCont);
        assertEquals(anyCont, new FakeCont());
        assertNotEquals(anyCont, new TestNormalizedNodeCont());
        assertNotEquals(new TestNormalizedNodeCont(), anyCont);
    }

    @Test
    public void testAnydataFromBinding() {
        final var entry = codecContext.toNormalizedDataObject(InstanceIdentifier.create(Cont.class),
            new ContBuilder().setContAny(new FakeCont()).build());
        assertEquals(YangInstanceIdentifier.of(CONT_NODE_ID), entry.path());
        assertEquals(cont, entry.node());
    }

    private final class FakeData extends AbstractOpaqueData<DOMSource> {
        @Override
        public Class<DOMSource> getObjectModel() {
            return DOMSource.class;
        }

        @Override
        public DOMSource getData() {
            return domSource;
        }
    }

    private abstract static class AbstractTestCont extends AbstractOpaqueObject<ContAny> implements ContAny {

    }

    private final class FakeCont extends AbstractTestCont {
        @Override
        public OpaqueData<?> getValue() {
            return new FakeData();
        }
    }

    private final class TestNormalizedNodeCont extends AbstractTestCont {
        @Override
        public OpaqueData<?> getValue() {
            return new AbstractOpaqueData<NormalizedNode>() {

                @Override
                public Class<NormalizedNode> getObjectModel() {
                    return NormalizedNode.class;
                }

                @Override
                public NormalizedNode getData() {
                    return cont;
                }
            };
        }
    }
}
