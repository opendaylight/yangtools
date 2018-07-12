/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.test;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.common.YangConstants.operationInputQName;
import static org.opendaylight.yangtools.yang.common.YangConstants.operationOutputQName;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.leafBuilder;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.Foo;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.InputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.OutputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grpcont.Bar;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
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
    private static final RpcInput BINDING_FOO_INPUT = new InputBuilder().setXyzzy("xyzzy").build();
    private static final RpcOutput BINDING_FOO_OUTPUT = new OutputBuilder().build();

    private static final NodeIdentifier BAR_INPUT = NodeIdentifier.create(operationInputQName(Foo.QNAME.getModule()));
    private static final NodeIdentifier BAR_OUTPUT = NodeIdentifier.create(operationOutputQName(Foo.QNAME.getModule()));
    private static final NodeIdentifier BAR_XYZZY = NodeIdentifier.create(QName.create(Bar.QNAME, "xyzzy"));
    private static final ContainerNode DOM_BAR_INPUT = containerBuilder().withNodeIdentifier(BAR_INPUT).build();
    private static final ContainerNode DOM_BAR_OUTPUT = containerBuilder().withNodeIdentifier(BAR_OUTPUT)
            .withChild(leafBuilder().withNodeIdentifier(BAR_XYZZY).withValue("xyzzy").build())
            .build();
    private static final RpcInput BINDING_BAR_INPUT =
            new org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grp.bar.InputBuilder().build();
    private static final RpcOutput BINDING_BAR_OUTPUT =
            new org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grp.bar.OutputBuilder().setXyzzy("xyzzy").build();

    @Test
    public void testSerialization() {
        assertEquals(DOM_FOO_INPUT, registry.toLazyNormalizedNodeActionInput(Foo.class, BINDING_FOO_INPUT)
            .getDelegate());
        assertEquals(DOM_BAR_INPUT, registry.toLazyNormalizedNodeActionInput(Bar.class, BINDING_BAR_INPUT)
                .getDelegate());
        assertEquals(DOM_FOO_OUTPUT, registry.toLazyNormalizedNodeActionOutput(Foo.class, BINDING_FOO_OUTPUT)
                .getDelegate());
        assertEquals(DOM_BAR_OUTPUT, registry.toLazyNormalizedNodeActionOutput(Bar.class, BINDING_BAR_OUTPUT)
                .getDelegate());
    }

    @Test
    public void testDeserialization() {
        assertEquals(BINDING_FOO_INPUT, registry.fromNormalizedNodeActionInput(Foo.class, DOM_FOO_INPUT));
        assertEquals(BINDING_BAR_INPUT, registry.fromNormalizedNodeActionInput(Bar.class, DOM_FOO_INPUT));
        assertEquals(BINDING_FOO_OUTPUT, registry.fromNormalizedNodeActionOutput(Foo.class, DOM_FOO_OUTPUT));
        assertEquals(BINDING_BAR_OUTPUT, registry.fromNormalizedNodeActionOutput(Bar.class, DOM_FOO_INPUT));
    }
}
