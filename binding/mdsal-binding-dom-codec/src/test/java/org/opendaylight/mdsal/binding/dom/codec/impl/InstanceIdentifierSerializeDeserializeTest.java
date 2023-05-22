/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.IncorrectNestingException;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Lst;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.lst.Foo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.notification.rev150205.OutOfPixieDustNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.aug.norev.cont.cont.choice.ContAug;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.aug.norev.root.RootAug;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.Cont;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.Root;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.cont.ContChoice;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.cont.cont.choice.ContBase;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.grp.GrpCont;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.root.RootBase;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public class InstanceIdentifierSerializeDeserializeTest extends AbstractBindingCodecTest {
    public static final String TOP_LEVEL_LIST_KEY_VALUE = "foo";

    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier
            .builder(Top.class).child(TopLevelList.class, TOP_FOO_KEY).build();

    public static final QName TOP_QNAME = Top.QNAME;
    public static final QName TOP_LEVEL_LIST_QNAME = QName.create(TOP_QNAME, "top-level-list");
    public static final QName TOP_LEVEL_LIST_KEY = QName.create(TOP_QNAME, "name");

    public static final YangInstanceIdentifier BI_TOP_PATH = YangInstanceIdentifier.of(TOP_QNAME);
    public static final YangInstanceIdentifier BI_TOP_LEVEL_LIST_PATH = BI_TOP_PATH.node(TOP_LEVEL_LIST_QNAME);
    public static final YangInstanceIdentifier BI_TOP_LEVEL_LIST_1_PATH = BI_TOP_LEVEL_LIST_PATH
            .node(NodeIdentifierWithPredicates.of(TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY,
                TOP_LEVEL_LIST_KEY_VALUE));

    @Test
    public void testYangIIToBindingAwareII() {
        final InstanceIdentifier<?> instanceIdentifier = codecContext.fromYangInstanceIdentifier(BI_TOP_PATH);
        assertEquals(Top.class, instanceIdentifier.getTargetType());
    }

    @Test
    public void testYangIIToBindingAwareIIListWildcarded() {
        final InstanceIdentifier<?> instanceIdentifier = codecContext.fromYangInstanceIdentifier(
            BI_TOP_LEVEL_LIST_PATH);
        assertNull(instanceIdentifier);
    }

    @Test
    public void testYangIIToBindingAwareIIListWithKey() {
        final InstanceIdentifier<?> instanceIdentifier = codecContext.fromYangInstanceIdentifier(
            BI_TOP_LEVEL_LIST_1_PATH);
        final InstanceIdentifier.PathArgument last = Iterables.getLast(instanceIdentifier.getPathArguments());
        assertEquals(TopLevelList.class, instanceIdentifier.getTargetType());
        assertFalse(instanceIdentifier.isWildcarded());
        assertTrue(last instanceof InstanceIdentifier.IdentifiableItem);
        final Identifier<?> key = ((InstanceIdentifier.IdentifiableItem<?, ?>) last).getKey();
        assertEquals(TopLevelListKey.class, key.getClass());
        assertEquals(TOP_LEVEL_LIST_KEY_VALUE, ((TopLevelListKey)key).getName());
    }

    @Test
    public void testBindingAwareIIToYangIContainer() {
        final YangInstanceIdentifier yangInstanceIdentifier = codecContext.toYangInstanceIdentifier(
                InstanceIdentifier.create(Top.class).child(TopLevelList.class));
        final PathArgument lastPathArgument = yangInstanceIdentifier.getLastPathArgument();
        assertTrue(lastPathArgument instanceof NodeIdentifier);
        assertEquals(TopLevelList.QNAME, lastPathArgument.getNodeType());
    }

    @Test
    public void testBindingAwareIIToYangIIWildcard() {
        final YangInstanceIdentifier yangInstanceIdentifier = codecContext.toYangInstanceIdentifier(
                InstanceIdentifier.create(Top.class).child(TopLevelList.class));
        final PathArgument lastPathArgument = yangInstanceIdentifier.getLastPathArgument();
        assertTrue(lastPathArgument instanceof NodeIdentifier);
        assertEquals(TopLevelList.QNAME, lastPathArgument.getNodeType());
    }

    @Test
    public void testBindingAwareIIToYangIIListWithKey() {
        final YangInstanceIdentifier yangInstanceIdentifier = codecContext.toYangInstanceIdentifier(
                InstanceIdentifier.create(Top.class).child(TopLevelList.class, TOP_FOO_KEY));
        final PathArgument lastPathArgument = yangInstanceIdentifier.getLastPathArgument();
        assertTrue(lastPathArgument instanceof NodeIdentifierWithPredicates);
        assertTrue(((NodeIdentifierWithPredicates) lastPathArgument).values().contains(TOP_LEVEL_LIST_KEY_VALUE));
        assertEquals(TopLevelList.QNAME, lastPathArgument.getNodeType());
    }

    @Test
    public void testChoiceCaseGroupingFromBinding() {
        final YangInstanceIdentifier contBase = codecContext.toYangInstanceIdentifier(
            InstanceIdentifier.builder(Cont.class).child(ContBase.class, GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.create(NodeIdentifier.create(Cont.QNAME),
            NodeIdentifier.create(ContChoice.QNAME), NodeIdentifier.create(GrpCont.QNAME)), contBase);

        final YangInstanceIdentifier contAug = codecContext.toYangInstanceIdentifier(
            InstanceIdentifier.builder(Cont.class).child(ContAug.class, GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.create(NodeIdentifier.create(Cont.QNAME),
            NodeIdentifier.create(ContChoice.QNAME),
            NodeIdentifier.create(GrpCont.QNAME.bindTo(ContAug.QNAME.getModule()))), contAug);

        // Legacy: downcast the child to Class, losing type safety but still working. Faced with ambiguity, it will
        //         select the lexically-lower class
        assertEquals(1, ContBase.class.getCanonicalName().compareTo(ContAug.class.getCanonicalName()));
        final YangInstanceIdentifier contAugLegacy = codecContext.toYangInstanceIdentifier(
            InstanceIdentifier.builder(Cont.class).child((Class) GrpCont.class).build());
        assertEquals(contAug, contAugLegacy);

        final YangInstanceIdentifier rootBase = codecContext.toYangInstanceIdentifier(
            InstanceIdentifier.builder(RootBase.class, GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.create(NodeIdentifier.create(Root.QNAME),
            NodeIdentifier.create(GrpCont.QNAME)), rootBase);

        final YangInstanceIdentifier rootAug = codecContext.toYangInstanceIdentifier(
            InstanceIdentifier.builder(RootAug.class, GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.create(NodeIdentifier.create(Root.QNAME),
            NodeIdentifier.create(GrpCont.QNAME.bindTo(RootAug.QNAME.getModule()))), rootAug);
    }

    @Test
    public void testChoiceCaseGroupingToBinding() {
        final InstanceIdentifier<?> contBase = codecContext.fromYangInstanceIdentifier(
            YangInstanceIdentifier.create(NodeIdentifier.create(Cont.QNAME),
            NodeIdentifier.create(ContChoice.QNAME), NodeIdentifier.create(GrpCont.QNAME)));
        assertEquals(InstanceIdentifier.builder(Cont.class).child(ContBase.class, GrpCont.class).build(), contBase);

        final InstanceIdentifier<?> contAug = codecContext.fromYangInstanceIdentifier(
            YangInstanceIdentifier.create(NodeIdentifier.create(Cont.QNAME), NodeIdentifier.create(ContChoice.QNAME),
                NodeIdentifier.create(GrpCont.QNAME.bindTo(ContAug.QNAME.getModule()))));
        assertEquals(InstanceIdentifier.builder(Cont.class).child(ContAug.class, GrpCont.class).build(), contAug);

        final InstanceIdentifier<?> rootBase = codecContext.fromYangInstanceIdentifier(
            YangInstanceIdentifier.create(NodeIdentifier.create(Root.QNAME), NodeIdentifier.create(GrpCont.QNAME)));
        assertEquals(InstanceIdentifier.builder(RootBase.class, GrpCont.class).build(), rootBase);

        final InstanceIdentifier<?> rootAug = codecContext.fromYangInstanceIdentifier(
            YangInstanceIdentifier.create(NodeIdentifier.create(Root.QNAME),
                NodeIdentifier.create(GrpCont.QNAME.bindTo(RootAug.QNAME.getModule()))));
        assertEquals(InstanceIdentifier.builder(RootAug.class, GrpCont.class).build(), rootAug);
    }

    @Test
    public void testRejectNotificationQName() {
        // A purposely-wrong YangInstanceIdentifier
        final var yiid = YangInstanceIdentifier.create(NodeIdentifier.create(OutOfPixieDustNotification.QNAME));
        final var ex = assertThrows(IncorrectNestingException.class,
            () -> codecContext.fromYangInstanceIdentifier(yiid));
        assertThat(ex.getMessage(),
            startsWith("Argument (urn:opendaylight:params:xml:ns:yang:controller:md:sal:test:bi:ba:notification"
                + "?revision=2015-02-05)out-of-pixie-dust-notification is not valid data tree child of "));
    }

    @Test
    public void testRejectRpcQName() {
        // A purposely-wrong YangInstanceIdentifier
        final var yiid = YangInstanceIdentifier.create(NodeIdentifier.create(
            // TODO: use the RPC interface once we are generating it
            QName.create(KnockKnockInput.QNAME, "knock-knock")));
        final var ex = assertThrows(IncorrectNestingException.class,
            () -> codecContext.fromYangInstanceIdentifier(yiid));
        assertThat(ex.getMessage(), startsWith("Argument (urn:opendaylight:params:xml:ns:yang:md:sal:knock-knock"
            + "?revision=2018-07-23)knock-knock is not valid data tree child of "));
    }

    @Test
    public void testRejectActionQName() {
        // A purposely-wrong YangInstanceIdentifier
        final var yiid = YangInstanceIdentifier.create(
            NodeIdentifier.create(Lst.QNAME),
            NodeIdentifierWithPredicates.of(Lst.QNAME, QName.create(Lst.QNAME, "key"), "foo"),
            NodeIdentifier.create(Foo.QNAME));
        final var ex = assertThrows(IncorrectNestingException.class,
            () -> codecContext.fromYangInstanceIdentifier(yiid));
        assertEquals("Argument (urn:odl:actions)foo is not valid child of "
            + "EmptyListEffectiveStatement{argument=(urn:odl:actions)lst}", ex.getMessage());
    }
}
