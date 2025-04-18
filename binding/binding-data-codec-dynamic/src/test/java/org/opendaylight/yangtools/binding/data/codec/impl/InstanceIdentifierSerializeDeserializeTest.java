/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;
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
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.KeyStep;
import org.opendaylight.yangtools.binding.data.codec.api.IncorrectNestingException;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

class InstanceIdentifierSerializeDeserializeTest extends AbstractBindingCodecTest {
    public static final String TOP_LEVEL_LIST_KEY_VALUE = "foo";

    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final DataObjectIdentifier<TopLevelList> BA_TOP_LEVEL_LIST =
        DataObjectIdentifier.builder(Top.class).child(TopLevelList.class, TOP_FOO_KEY).build();

    public static final QName TOP_LEVEL_LIST_KEY = QName.create(TopLevelList.QNAME, "name");

    public static final YangInstanceIdentifier BI_TOP_PATH = YangInstanceIdentifier.of(Top.QNAME);
    public static final YangInstanceIdentifier BI_TOP_LEVEL_LIST_PATH = BI_TOP_PATH.node(TopLevelList.QNAME);
    public static final YangInstanceIdentifier BI_TOP_LEVEL_LIST_1_PATH = BI_TOP_LEVEL_LIST_PATH
            .node(NodeIdentifierWithPredicates.of(TopLevelList.QNAME, TOP_LEVEL_LIST_KEY, TOP_LEVEL_LIST_KEY_VALUE));

    @Test
    void testYangIIToBindingAwareII() {
        assertEquals(DataObjectReference.builder(Top.class).build(),
            codecContext.fromYangInstanceIdentifier(BI_TOP_PATH));
    }

    @Test
    void testYangIIToBindingAwareIIListWildcarded() {
        assertEquals(DataObjectReference.builder(Top.class).child(TopLevelList.class).build(),
            codecContext.fromYangInstanceIdentifier(BI_TOP_LEVEL_LIST_PATH));
    }

    @Test
    void testYangIIToBindingAwareIIListWithKey() {
        final var instanceIdentifier = codecContext.fromYangInstanceIdentifier(BI_TOP_LEVEL_LIST_1_PATH);
        final var last = Iterables.getLast(instanceIdentifier.steps());
        assertEquals(TopLevelList.class, instanceIdentifier.lastStep().type());
        assertTrue(instanceIdentifier.isExact());
        assertFalse(instanceIdentifier.isWildcarded());
        final var key = assertInstanceOf(KeyStep.class, last).key();
        assertEquals(TopLevelListKey.class, key.getClass());
        assertEquals(TOP_LEVEL_LIST_KEY_VALUE, ((TopLevelListKey)key).getName());
    }

    @Test
    void testBindingAwareIIToYangIContainer() {
        final var yangInstanceIdentifier = codecContext.toYangInstanceIdentifier(
                InstanceIdentifier.create(Top.class).child(TopLevelList.class));
        final var lastPathArgument = assertInstanceOf(NodeIdentifier.class,
            yangInstanceIdentifier.getLastPathArgument());
        assertEquals(TopLevelList.QNAME, lastPathArgument.getNodeType());
    }

    @Test
    void testBindingAwareIIToYangIIWildcard() {
        final var yangInstanceIdentifier = codecContext.toYangInstanceIdentifier(
                InstanceIdentifier.create(Top.class).child(TopLevelList.class));
        final var lastPathArgument =
            assertInstanceOf(NodeIdentifier.class, yangInstanceIdentifier.getLastPathArgument());
        assertEquals(TopLevelList.QNAME, lastPathArgument.getNodeType());
    }

    @Test
    void testBindingAwareIIToYangIIListWithKey() {
        final var yangInstanceIdentifier = codecContext.toYangInstanceIdentifier(
                InstanceIdentifier.create(Top.class).child(TopLevelList.class, TOP_FOO_KEY));
        final var lastPathArgument = assertInstanceOf(NodeIdentifierWithPredicates.class,
            yangInstanceIdentifier.getLastPathArgument());
        assertTrue(lastPathArgument.values().contains(TOP_LEVEL_LIST_KEY_VALUE));
        assertEquals(TopLevelList.QNAME, lastPathArgument.getNodeType());
    }

    @Test
    void testChoiceCaseGroupingFromBinding() {
        final var contBase = codecContext.toYangInstanceIdentifier(
            DataObjectIdentifier.builder(Cont.class).child(ContBase.class, GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.of(NodeIdentifier.create(Cont.QNAME),
            NodeIdentifier.create(ContChoice.QNAME), NodeIdentifier.create(GrpCont.QNAME)), contBase);

        final var contAug = codecContext.toYangInstanceIdentifier(
            DataObjectIdentifier.builder(Cont.class).child(ContAug.class, GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.of(NodeIdentifier.create(Cont.QNAME),
            NodeIdentifier.create(ContChoice.QNAME),
            NodeIdentifier.create(GrpCont.QNAME.bindTo(ContAug.QNAME.getModule()))), contAug);

        // Legacy: downcast the child to Class, losing type safety but still working. Faced with ambiguity, it will
        //         select the lexically-lower class
        assertEquals(1, ContBase.class.getCanonicalName().compareTo(ContAug.class.getCanonicalName()));
        final var contAugLegacy = codecContext.toYangInstanceIdentifier(
            DataObjectIdentifier.builder(Cont.class).child((Class) GrpCont.class).build());
        assertEquals(contAug, contAugLegacy);

        final var rootBase = codecContext.toYangInstanceIdentifier(
            DataObjectIdentifier.builder(RootBase.class, GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.of(NodeIdentifier.create(Root.QNAME),
            NodeIdentifier.create(GrpCont.QNAME)), rootBase);

        final var rootAug = codecContext.toYangInstanceIdentifier(
            DataObjectIdentifier.builder(RootAug.class, GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.of(NodeIdentifier.create(Root.QNAME),
            NodeIdentifier.create(GrpCont.QNAME.bindTo(RootAug.QNAME.getModule()))), rootAug);
    }

    @Test
    void testChoiceCaseGroupingToBinding() {
        final var contBase = codecContext.fromYangInstanceIdentifier(
            YangInstanceIdentifier.of(Cont.QNAME, ContChoice.QNAME, GrpCont.QNAME));
        assertEquals(DataObjectIdentifier.builder(Cont.class).child(ContBase.class, GrpCont.class).build(), contBase);

        final var contAug = codecContext.fromYangInstanceIdentifier(
            YangInstanceIdentifier.of(Cont.QNAME, ContChoice.QNAME, GrpCont.QNAME.bindTo(ContAug.QNAME.getModule())));
        assertEquals(DataObjectIdentifier.builder(Cont.class).child(ContAug.class, GrpCont.class).build(), contAug);

        final var rootBase = codecContext.fromYangInstanceIdentifier(
            YangInstanceIdentifier.of(Root.QNAME, GrpCont.QNAME));
        assertEquals(DataObjectIdentifier.builder(RootBase.class, GrpCont.class).build(), rootBase);

        final var rootAug = codecContext.fromYangInstanceIdentifier(
            YangInstanceIdentifier.of(Root.QNAME, GrpCont.QNAME.bindTo(RootAug.QNAME.getModule())));
        assertEquals(DataObjectIdentifier.builder(RootAug.class, GrpCont.class).build(), rootAug);
    }

    @Test
    void testRejectNotificationQName() {
        // A purposely-wrong YangInstanceIdentifier
        final var yiid = YangInstanceIdentifier.of(OutOfPixieDustNotification.QNAME);
        final var ex = assertThrows(IncorrectNestingException.class,
            () -> codecContext.fromYangInstanceIdentifier(yiid));
        assertThat(ex.getMessage())
            .startsWith("Argument (urn:opendaylight:params:xml:ns:yang:controller:md:sal:test:bi:ba:notification"
                + "?revision=2015-02-05)out-of-pixie-dust-notification is not valid data tree child of ");
    }

    @Test
    void testRejectRpcQName() {
        // A purposely-wrong YangInstanceIdentifier
        final var yiid = YangInstanceIdentifier.of(
            // TODO: use the RPC interface once we are generating it
            QName.create(KnockKnockInput.QNAME, "knock-knock"));
        final var ex = assertThrows(IncorrectNestingException.class,
            () -> codecContext.fromYangInstanceIdentifier(yiid));
        assertThat(ex.getMessage()).startsWith("Argument (urn:opendaylight:params:xml:ns:yang:md:sal:knock-knock"
            + "?revision=2018-07-23)knock-knock is not valid data tree child of ");
    }

    @Test
    void testRejectActionQName() {
        // A purposely-wrong YangInstanceIdentifier
        final var yiid = YangInstanceIdentifier.of(
            NodeIdentifier.create(Lst.QNAME),
            NodeIdentifierWithPredicates.of(Lst.QNAME, QName.create(Lst.QNAME, "key"), "foo"),
            NodeIdentifier.create(Foo.QNAME));
        final var ex = assertThrows(IncorrectNestingException.class,
            () -> codecContext.fromYangInstanceIdentifier(yiid));
        assertEquals("Argument (urn:odl:actions)foo is not valid child of "
            + "EmptyListEffectiveStatement{argument=(urn:odl:actions)lst}", ex.getMessage());
    }
}
