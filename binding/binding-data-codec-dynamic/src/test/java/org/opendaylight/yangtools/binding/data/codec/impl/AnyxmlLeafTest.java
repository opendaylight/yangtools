/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import javax.xml.transform.dom.DOMSource;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal437.norev.Any;
import org.opendaylight.yang.gen.v1.mdsal437.norev.Cont;
import org.opendaylight.yang.gen.v1.mdsal437.norev.ContBuilder;
import org.opendaylight.yang.gen.v1.mdsal437.norev.cont.ContAny;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.binding.LeafPropertyStep;
import org.opendaylight.yangtools.binding.OpaqueData;
import org.opendaylight.yangtools.binding.PropertyIdentifier;
import org.opendaylight.yangtools.binding.lib.AbstractOpaqueData;
import org.opendaylight.yangtools.binding.lib.AbstractOpaqueObject;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AnyxmlLeafTest extends AbstractBindingCodecTest {
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
            .withChild(ImmutableNodes.newAnyxmlBuilder(DOMSource.class)
                .withNodeIdentifier(new NodeIdentifier(ContAny.QNAME))
                .withValue(domSource)
                .build())
            .build();
    }

    @Test
    public void testAnyxmlToBinding() {
        final var entry = codecContext.fromNormalizedNode(YangInstanceIdentifier.of(CONT_NODE_ID), cont);
        assertEquals(InstanceIdentifier.create(Cont.class), entry.getKey());

        // So no... GrpAny should be null ..
        final Cont contValue = assertInstanceOf(Cont.class, entry.getValue());
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
    public void testAnyxmlFromBinding() {
        final var entry = codecContext.toNormalizedDataObject(InstanceIdentifier.create(Cont.class),
            new ContBuilder().setContAny(new FakeCont()).build());
        assertEquals(YangInstanceIdentifier.of(CONT_NODE_ID), entry.path());
        assertEquals(cont, entry.node());
    }

    @Test
    public void anyxmlIsPropertyAddressable() {
        assertEquals(YangInstanceIdentifier.of(Cont.QNAME, ContAny.QNAME), codecContext.getInstanceIdentifierCodec()
            .fromBinding((BindingInstanceIdentifier) new PropertyIdentifier<>(
                InstanceIdentifier.create(Cont.class).toIdentifier(),
                new LeafPropertyStep<>(Cont.class, ContAny.class, Unqualified.of("cont-any")))));
    }

    @Test
    public void anyxmlHasCodecNode() {
        final var contAny = assertInstanceOf(AnyxmlCodecContext.class,
            codecContext.getSubtreeCodec(YangInstanceIdentifier.of(Cont.QNAME, ContAny.QNAME)));
        assertEquals(ContAny.class, contAny.getBindingClass());

        final var any = assertInstanceOf(AnyxmlCodecContext.class,
            codecContext.getSubtreeCodec(YangInstanceIdentifier.of(Any.QNAME)));
        assertEquals(Any.class, any.getBindingClass());
    }

    @Test
    public void anyxmlIsYangAddressable() {
        assertEquals(new PropertyIdentifier<>(InstanceIdentifier.create(Cont.class).toIdentifier(),
            new LeafPropertyStep<>(Cont.class, ContAny.class, Unqualified.of("cont-any"))),
            codecContext.getInstanceIdentifierCodec().toBindingInstanceIdentifier(
                YangInstanceIdentifier.of(Cont.QNAME, ContAny.QNAME)));
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
