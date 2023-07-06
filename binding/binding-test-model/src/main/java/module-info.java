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
    requires static java.compiler;
    requires static java.management;
}
