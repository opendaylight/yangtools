/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.binding.meta.YangFeatureProvider;
import org.opendaylight.yangtools.binding.meta.YangModelBindingProvider;

/**
 * A collection of YANG models for testing purposes.
 *
 * @provides YangFeatureProvider
 * @provides YangModelBindingProvider
 */
open module org.opendaylight.yangtools.binding.test.model {
    requires transitive org.opendaylight.yangtools.binding.spec;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires com.google.common;
    requires org.opendaylight.yangtools.codegen.extensions;

    exports org.opendaylight.yang.gen.v1.bug5446.rev151105;
    exports org.opendaylight.yang.gen.v1.bug8449.rev170516;
    exports org.opendaylight.yang.gen.v1.bug8903.rev170829;
    exports org.opendaylight.yang.gen.v1.lal.norev;
    exports org.opendaylight.yang.gen.v1.lal.norev.foo;
    exports org.opendaylight.yang.gen.v1.mdsal._182.norev;
    exports org.opendaylight.yang.gen.v1.mdsal._300.aug.norev;
    exports org.opendaylight.yang.gen.v1.mdsal._300.aug.norev.cont.cont.act.input;
    exports org.opendaylight.yang.gen.v1.mdsal._300.norev;
    exports org.opendaylight.yang.gen.v1.mdsal._300.norev.bar;
    exports org.opendaylight.yang.gen.v1.mdsal._300.norev.bar.baz;
    exports org.opendaylight.yang.gen.v1.mdsal._300.norev.cont;
    exports org.opendaylight.yang.gen.v1.mdsal._300.norev.foo;
    exports org.opendaylight.yang.gen.v1.mdsal._300.norev.other;
    exports org.opendaylight.yang.gen.v1.mdsal._355.norev;
    exports org.opendaylight.yang.gen.v1.mdsal426.norev;
    exports org.opendaylight.yang.gen.v1.mdsal437.norev;
    exports org.opendaylight.yang.gen.v1.mdsal437.norev.cont;
    exports org.opendaylight.yang.gen.v1.mdsal437.norev.grp;
    exports org.opendaylight.yang.gen.v1.mdsal438.norev;
    exports org.opendaylight.yang.gen.v1.mdsal438.norev.cont;
    exports org.opendaylight.yang.gen.v1.mdsal438.norev.grp;
    exports org.opendaylight.yang.gen.v1.mdsal442.keydef.norev;
    exports org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.grp;
    exports org.opendaylight.yang.gen.v1.mdsal442.keyuse.norev;
    exports org.opendaylight.yang.gen.v1.mdsal533.norev;
    exports org.opendaylight.yang.gen.v1.mdsal533.norev.foo_cont;
    exports org.opendaylight.yang.gen.v1.mdsal533.norev.foo_cont.foo_cont2;
    exports org.opendaylight.yang.gen.v1.mdsal552.norev;
    exports org.opendaylight.yang.gen.v1.mdsal600.norev;
    exports org.opendaylight.yang.gen.v1.mdsal661.norev;
    exports org.opendaylight.yang.gen.v1.mdsal668.norev;
    exports org.opendaylight.yang.gen.v1.mdsal668.norev.bar;
    exports org.opendaylight.yang.gen.v1.mdsal766.norev;
    exports org.opendaylight.yang.gen.v1.mdsal766.norev.foo;
    exports org.opendaylight.yang.gen.v1.mdsal767.norev;
    exports org.opendaylight.yang.gen.v1.mdsal813.norev;
    exports org.opendaylight.yang.gen.v1.mdsal813.norev.register.listener.test;
    exports org.opendaylight.yang.gen.v1.mdsal.query.norev;
    exports org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp;
    exports org.opendaylight.yang.gen.v1.mdsal.query.norev.second.grp;
    exports org.opendaylight.yang.gen.v1.mdsal.query.norev.third.grp;
    exports org.opendaylight.yang.gen.v1.odl.test.binary.key.rev160101;
    exports org.opendaylight.yang.gen.v1.opendaylight.test.bug._2562.namespace.rev160101;
    exports org.opendaylight.yang.gen.v1.opendaylight.test.bug._2562.namespace.rev160101.root;
    exports org.opendaylight.yang.gen.v1.opendaylight.test.bug._2562.namespace.rev160101.root.fooroot;
    exports org.opendaylight.yang.gen.v1.rpc.norev;
    exports org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev170126;
    exports org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev170126.errors;
    exports org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev170126.errors.errors;
    exports org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev170126.errors.errors.error;
    exports org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev170126.restconf;
    exports org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev170126.restconf.restconf;
    exports org.opendaylight.yang.gen.v1.urn.odl.actions.norev;
    exports org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont;
    exports org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grp;
    exports org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grpcont;
    exports org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grplst;
    exports org.opendaylight.yang.gen.v1.urn.odl.actions.norev.lst;
    exports org.opendaylight.yang.gen.v1.urn.odl.actions.norev.lstio;
    exports org.opendaylight.yang.gen.v1.urn.odl.actions.norev.nested;
    exports org.opendaylight.yang.gen.v1.urn.odl.actions.norev.nested.baz;
    exports org.opendaylight.yang.gen.v1.urn.odl.actions.norev.othercont;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test._2.rev160111;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.opendaylight.test.bug._3090.rev160101;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.opendaylight.test.bug._3090.rev160101.root;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of.migration.test.model.rev150210;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of.migration.test.model.rev150210.aug.grouping;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of.migration.test.model.rev150210.aug.grouping.cont2;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of.migration.test.model.rev150210.aug.grouping.list1;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of.migration.test.model.rev150210.list11.simple.augment;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.notification.rev150205;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.notification.rev150205.wood;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.notification.rev150205.wood.notif.grp;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.notification.rev150205.wood.tree;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.listener.rev150825;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.listener.rev150825.listener.test;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.store.rev140422;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.store.rev140422.lists;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.store.rev140422.lists.ordered.container;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.store.rev140422.lists.unkeyed.container;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.store.rev140422.lists.unordered.container;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.put.top.input.choice.list.choice.in.choice.list;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.put.top.input.top.level.list.choice.in.list;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.choice.augment1;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.choice.augment1.augment.choice1;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.choice.augment1.augment.choice1.case1;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.choice.augment1.augment.choice1.case2;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.choice.augment2;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.choice.augment2.augment.choice2;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.choice.augment2.augment.choice2.case11;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.choice.list.choice.in.choice.list;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.top.level.list.choice.in.list;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.choice;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.choice.identifier;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.choice.identifier.extended;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.choice.identifier.simple;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.choice.list;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.choice.in.list;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.bug._6006.rev160607;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.test.top.via.uses.rev151112;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.test.top.via.uses.rev151112.container.top;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.typedef.empty.rev170829;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428;
    exports org.opendaylight.yang.gen.v1.urn.test.foo4798.rev160101;
    exports org.opendaylight.yang.gen.v1.urn.test.foo4798.rev160101.root;
    exports org.opendaylight.yang.gen.v1.urn.test.foo4798.rev160101.root.bug4798.choice;
    exports org.opendaylight.yang.gen.v1.urn.test.foo4798.rev160101.root.bug4798.choice._case.a;
    exports org.opendaylight.yang.gen.v1.urn.test.foo4798.rev160101.root.bug4798.choice._case.b;
    exports org.opendaylight.yang.gen.v1.urn.test.foo4798.rev160101.root.bug4798.choice._case.b._case.b.container;
    exports org.opendaylight.yang.gen.v1.urn.test.leaf.caching.codec.rev190201;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.grouping.module1;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.grouping.module1.list.module1._1;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.grouping.module1.list.module1._1.list.module1._2;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.uses.grouping.augmet.testgrouping.inner;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.uses.grouping.augmet.testgrouping.outer;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.uses.grouping.augmet.testgrouping.outer.container.augmet._1;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.uses.grouping.testgrouping.inner;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.uses.grouping.testgrouping.inner.container._1;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module2.rev160101;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module2.rev160101.grouping.module2;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.container.manual.list.module1._1;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.container.manual.list.module1._1.container.manual.list.module1._2;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.container.manual.list.module1._1.container.manual.list.module1._2.container.manual.container.module1;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.main;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.manual.list.module1._1;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.manual.list.module1._1.manual.list.module1._2;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.module3.main;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module4.rev160101;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module4.rev160101.module4.main;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module4.rev160101.module4.main.container.module._4;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.addressable.cont;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.with.choice;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.with.choice.foo;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.with.choice.foo.addressable._case;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal309.norev;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal337.rev180424;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.aug.norev;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.aug.norev.cont.cont.choice;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.aug.norev.root;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.cont;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.cont.cont.choice;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.grp;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.root;
    exports org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal483.norev;
    exports org.opendaylight.yang.gen.v1.urn.test.pattern.rev170101;
    exports org.opendaylight.yang.gen.v1.urn.test.rev170101;
    exports org.opendaylight.yang.gen.v1.urn.test.unsigned.rev180408;
    exports org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222;
    exports org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.$$2e$$2f$$23$$;
    exports org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.$$2f$$2e$$23$$;
    exports org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.grp._for.anydata;
    exports org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.grp._for.anyxml;
    exports org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.grp._for.container;
    exports org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.grp._for.list;
    exports org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.yang.data.with.anydata;
    exports org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.yang.data.with.anyxml;
    exports org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.yang.data.with.container;
    exports org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.yang.data.with.list;
    exports org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424;
    exports org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root;
    exports org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system;
    exports org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container;
    exports org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.nested.list.by.system;
    exports org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.nested.list.by.system._double.nested.list.container;
    exports org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user;
    exports org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.nested.list.by.user;
    exports org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.nested.list.no.key;
    exports org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101;
    exports org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101._boolean.container;
    exports org.opendaylight.yang.svc.v1.bug5446.rev151105;
    exports org.opendaylight.yang.svc.v1.bug8449.rev170516;
    exports org.opendaylight.yang.svc.v1.bug8903.rev170829;
    exports org.opendaylight.yang.svc.v1.lal.norev;
    exports org.opendaylight.yang.svc.v1.mdsal._182.norev;
    exports org.opendaylight.yang.svc.v1.mdsal._300.aug.norev;
    exports org.opendaylight.yang.svc.v1.mdsal._300.norev;
    exports org.opendaylight.yang.svc.v1.mdsal._355.norev;
    exports org.opendaylight.yang.svc.v1.mdsal426.norev;
    exports org.opendaylight.yang.svc.v1.mdsal437.norev;
    exports org.opendaylight.yang.svc.v1.mdsal438.norev;
    exports org.opendaylight.yang.svc.v1.mdsal442.keydef.norev;
    exports org.opendaylight.yang.svc.v1.mdsal442.keyuse.norev;
    exports org.opendaylight.yang.svc.v1.mdsal533.norev;
    exports org.opendaylight.yang.svc.v1.mdsal552.norev;
    exports org.opendaylight.yang.svc.v1.mdsal600.norev;
    exports org.opendaylight.yang.svc.v1.mdsal661.norev;
    exports org.opendaylight.yang.svc.v1.mdsal668.norev;
    exports org.opendaylight.yang.svc.v1.mdsal766.norev;
    exports org.opendaylight.yang.svc.v1.mdsal767.norev;
    exports org.opendaylight.yang.svc.v1.mdsal813.norev;
    exports org.opendaylight.yang.svc.v1.mdsal.query.norev;
    exports org.opendaylight.yang.svc.v1.odl.test.binary.key.rev160101;
    exports org.opendaylight.yang.svc.v1.opendaylight.test.bug._2562.namespace.rev160101;
    exports org.opendaylight.yang.svc.v1.rpc.norev;
    exports org.opendaylight.yang.svc.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev170126;
    exports org.opendaylight.yang.svc.v1.urn.odl.actions.norev;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns._default.value.test._2.rev160111;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns._default.value.test.norev;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.opendaylight.test.bug._3090.rev160101;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of.migration.test.model.rev150210;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.notification.rev150205;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.listener.rev150825;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.store.rev140422;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.bug._6006.rev160607;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.md.sal.test.top.via.uses.rev151112;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.typedef.empty.rev170829;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.yang.union.test.rev220428;
    exports org.opendaylight.yang.svc.v1.urn.test.foo4798.rev160101;
    exports org.opendaylight.yang.svc.v1.urn.test.leaf.caching.codec.rev190201;
    exports org.opendaylight.yang.svc.v1.urn.test.opendaylight.bug._5524.module1.rev160101;
    exports org.opendaylight.yang.svc.v1.urn.test.opendaylight.bug._5524.module2.rev160101;
    exports org.opendaylight.yang.svc.v1.urn.test.opendaylight.bug._5524.module3.rev160101;
    exports org.opendaylight.yang.svc.v1.urn.test.opendaylight.bug._5524.module4.rev160101;
    exports org.opendaylight.yang.svc.v1.urn.test.opendaylight.mdsal298.rev180129;
    exports org.opendaylight.yang.svc.v1.urn.test.opendaylight.mdsal309.norev;
    exports org.opendaylight.yang.svc.v1.urn.test.opendaylight.mdsal337.rev180424;
    exports org.opendaylight.yang.svc.v1.urn.test.opendaylight.mdsal45.aug.norev;
    exports org.opendaylight.yang.svc.v1.urn.test.opendaylight.mdsal45.base.norev;
    exports org.opendaylight.yang.svc.v1.urn.test.opendaylight.mdsal483.norev;
    exports org.opendaylight.yang.svc.v1.urn.test.pattern.rev170101;
    exports org.opendaylight.yang.svc.v1.urn.test.rev170101;
    exports org.opendaylight.yang.svc.v1.urn.test.unsigned.rev180408;
    exports org.opendaylight.yang.svc.v1.urn.test.yang.data.demo.rev220222;
    exports org.opendaylight.yang.svc.v1.urn.yang.equals.rev230424;
    exports org.opendaylight.yang.svc.v1.urn.yang.foo.rev160101;

    exports org.opendaylight.mdsal.binding.test.model.util;

    provides YangModelBindingProvider with
        org.opendaylight.yang.svc.v1.bug5446.rev151105.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.bug8449.rev170516.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.bug8903.rev170829.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.lal.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal._182.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal._300.aug.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal._300.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal._355.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal426.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal437.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal438.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal442.keydef.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal442.keyuse.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal533.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal552.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal600.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal661.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal668.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal766.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal767.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal813.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.mdsal.query.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.odl.test.binary.key.rev160101.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.opendaylight.test.bug._2562.namespace.rev160101.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.rpc.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev170126.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.odl.actions.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns._default.value.test._2.rev160111.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.opendaylight.test.bug._3090.rev160101.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of.migration.test.model.rev150210.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.notification.rev150205.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.listener.rev150825.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.store.rev140422.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.bug._6006.rev160607.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.md.sal.test.top.via.uses.rev151112.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.typedef.empty.rev170829.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.opendaylight.yang.union.test.rev220428.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.foo4798.rev160101.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.leaf.caching.codec.rev190201.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.opendaylight.bug._5524.module1.rev160101.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.opendaylight.bug._5524.module2.rev160101.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.opendaylight.bug._5524.module3.rev160101.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.opendaylight.bug._5524.module4.rev160101.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.opendaylight.mdsal298.rev180129.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.opendaylight.mdsal309.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.opendaylight.mdsal337.rev180424.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.opendaylight.mdsal45.aug.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.opendaylight.mdsal45.base.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.opendaylight.mdsal483.norev.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.pattern.rev170101.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.rev170101.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.unsigned.rev180408.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.test.yang.data.demo.rev220222.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.yang.equals.rev230424.YangModelBindingProviderImpl,
        org.opendaylight.yang.svc.v1.urn.yang.foo.rev160101.YangModelBindingProviderImpl;

    provides YangFeatureProvider with
        org.opendaylight.mdsal.binding.test.model.util.Mdsal767Support;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static org.kohsuke.metainf_services;
    requires static transitive java.compiler;
    requires static transitive java.management;
}
