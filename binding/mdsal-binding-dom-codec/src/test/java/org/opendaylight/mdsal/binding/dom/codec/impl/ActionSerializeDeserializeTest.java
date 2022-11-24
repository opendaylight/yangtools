/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.common.YangConstants.operationInputQName;
import static org.opendaylight.yangtools.yang.common.YangConstants.operationOutputQName;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.leafBuilder;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.Foo;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.FooInput;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.FooInputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.FooOutput;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.FooOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grp.BarInput;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grp.BarInputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grp.BarOutput;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grp.BarOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grpcont.Bar;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.lstio.Fooio;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.lstio.FooioInput;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.lstio.FooioInputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.lstio.FooioOutput;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.lstio.FooioOutputBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

public class ActionSerializeDeserializeTest extends AbstractBindingCodecTest {
    private static final NodeIdentifier FOO_INPUT = NodeIdentifier.create(operationInputQName(Foo.QNAME.getModule()));
    private static final NodeIdentifier FOO_OUTPUT = NodeIdentifier.create(operationOutputQName(Foo.QNAME.getModule()));
    private static final NodeIdentifier FOO_XYZZY = NodeIdentifier.create(QName.create(Foo.QNAME, "xyzzy"));
    private static final ContainerNode DOM_FOO_INPUT = containerBuilder().withNodeIdentifier(FOO_INPUT)
            .withChild(leafBuilder().withNodeIdentifier(FOO_XYZZY).withValue("xyzzy").build())
            .build();
    private static final ContainerNode DOM_FOO_OUTPUT = containerBuilder().withNodeIdentifier(FOO_OUTPUT).build();
    private static final FooInput BINDING_FOO_INPUT = new FooInputBuilder().setXyzzy("xyzzy").build();
    private static final FooOutput BINDING_FOO_OUTPUT = new FooOutputBuilder().build();

    private static final NodeIdentifier BAR_INPUT = NodeIdentifier.create(operationInputQName(Foo.QNAME.getModule()));
    private static final NodeIdentifier BAR_OUTPUT = NodeIdentifier.create(operationOutputQName(Foo.QNAME.getModule()));
    private static final NodeIdentifier BAR_XYZZY = NodeIdentifier.create(QName.create(Bar.QNAME, "xyzzy"));
    private static final ContainerNode DOM_BAR_INPUT = containerBuilder().withNodeIdentifier(BAR_INPUT).build();
    private static final ContainerNode DOM_BAR_OUTPUT = containerBuilder().withNodeIdentifier(BAR_OUTPUT)
            .withChild(leafBuilder().withNodeIdentifier(BAR_XYZZY).withValue("xyzzy").build())
            .build();
    private static final BarInput BINDING_BAR_INPUT = new BarInputBuilder().build();
    private static final BarOutput BINDING_BAR_OUTPUT = new BarOutputBuilder().setXyzzy("xyzzy").build();

    private static final NodeIdentifier FOOIO_INPUT = NodeIdentifier.create(operationInputQName(Fooio.QNAME
            .getModule()));
    private static final NodeIdentifier FOOIO_OUTPUT = NodeIdentifier.create(operationOutputQName(Fooio.QNAME
            .getModule()));
    private static final NodeIdentifier FOOIO_I = NodeIdentifier.create(QName.create(Fooio.QNAME, "fooi"));
    private static final NodeIdentifier FOOIO_O = NodeIdentifier.create(QName.create(Fooio.QNAME, "fooo"));
    private static final ContainerNode DOM_FOOIO_INPUT = containerBuilder().withNodeIdentifier(FOOIO_INPUT).withChild(
            leafBuilder().withNodeIdentifier(FOOIO_I).withValue("ifoo").build()).build();
    private static final ContainerNode DOM_FOOIO_OUTPUT = containerBuilder().withNodeIdentifier(FOOIO_OUTPUT).withChild(
            leafBuilder().withNodeIdentifier(FOOIO_O).withValue("ofoo").build()).build();
    private static final FooioInput BINDING_FOOIO_INPUT = new FooioInputBuilder().setFooi("ifoo").build();
    private static final FooioOutput BINDING_FOOIO_OUTPUT = new FooioOutputBuilder().setFooo("ofoo").build();

    @Test
    public void testSerialization() {
        assertEquals(DOM_FOO_INPUT, codecContext.toLazyNormalizedNodeActionInput(Foo.class, BINDING_FOO_INPUT)
                .getDelegate());
        assertEquals(DOM_BAR_INPUT, codecContext.toLazyNormalizedNodeActionInput(Bar.class, BINDING_BAR_INPUT)
                .getDelegate());
        assertEquals(DOM_FOO_OUTPUT, codecContext.toLazyNormalizedNodeActionOutput(Foo.class, BINDING_FOO_OUTPUT)
                .getDelegate());
        assertEquals(DOM_BAR_OUTPUT, codecContext.toLazyNormalizedNodeActionOutput(Bar.class, BINDING_BAR_OUTPUT)
                .getDelegate());
    }

    @Test
    public void testKeyedListActionSerialization() {
        assertEquals(DOM_FOOIO_INPUT, codecContext.toLazyNormalizedNodeActionInput(Fooio.class, BINDING_FOOIO_INPUT)
                .getDelegate());
        assertEquals(DOM_FOOIO_OUTPUT, codecContext.toLazyNormalizedNodeActionOutput(Fooio.class, BINDING_FOOIO_OUTPUT)
                .getDelegate());
    }

    @Test
    public void testDeserialization() {
        assertEquals(BINDING_FOO_INPUT, codecContext.fromNormalizedNodeActionInput(Foo.class, DOM_FOO_INPUT));
        assertEquals(BINDING_BAR_INPUT, codecContext.fromNormalizedNodeActionInput(Bar.class, DOM_FOO_INPUT));
        assertEquals(BINDING_FOO_OUTPUT, codecContext.fromNormalizedNodeActionOutput(Foo.class, DOM_FOO_OUTPUT));
        assertEquals(BINDING_BAR_OUTPUT, codecContext.fromNormalizedNodeActionOutput(Bar.class, DOM_FOO_INPUT));
    }

    @Test
    public void testKeyedListActionDeserialization() {
        assertEquals(BINDING_FOOIO_INPUT, codecContext.fromNormalizedNodeActionInput(Fooio.class, DOM_FOOIO_INPUT));
        assertEquals(BINDING_FOOIO_OUTPUT, codecContext.fromNormalizedNodeActionOutput(Fooio.class, DOM_FOOIO_OUTPUT));
    }
}
