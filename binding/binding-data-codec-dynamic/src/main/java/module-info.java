/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTreeFactory;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingCodecContext;
import org.opendaylight.yangtools.binding.data.codec.impl.SimpleBindingCodecTreeFactory;
import org.opendaylight.yangtools.binding.data.codec.impl.SimpleBindingDOMCodecFactory;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecFactory;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecServices;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;

module org.opendaylight.yangtools.binding.data.codec.dynamic {
    // FIXME: MDSAL-828: do not export this package
    exports org.opendaylight.yangtools.binding.data.codec.impl;
    exports org.opendaylight.yangtools.binding.data.codec.impl.di;

    uses BindingRuntimeContext;
    provides BindingDOMCodecFactory with SimpleBindingDOMCodecFactory;
    provides BindingDOMCodecServices with BindingCodecContext;
    provides BindingCodecTreeFactory with SimpleBindingCodecTreeFactory;

    requires transitive org.opendaylight.yangtools.binding.data.codec.api;
    requires transitive org.opendaylight.yangtools.binding.data.codec.spi;
    requires transitive org.opendaylight.yangtools.binding.runtime.api;
    requires com.google.common;
    requires net.bytebuddy;
    requires org.opendaylight.yangtools.binding.loader;
    requires org.opendaylight.yangtools.binding.model;
    requires org.opendaylight.yangtools.binding.reflect;
    requires org.opendaylight.yangtools.binding.spec;
    requires org.opendaylight.yangtools.concepts;
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.data.api;
    requires org.opendaylight.yangtools.yang.data.spi;
    requires org.opendaylight.yangtools.yang.data.impl;
    requires org.opendaylight.yangtools.yang.data.util;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.model.util;
    requires org.slf4j;

    // Annotations
    requires static com.github.spotbugs.annotations;
    requires static transitive javax.inject;
    requires static org.eclipse.jdt.annotation;
    requires static org.kohsuke.metainf_services;
    requires static org.osgi.service.component.annotations;
}
