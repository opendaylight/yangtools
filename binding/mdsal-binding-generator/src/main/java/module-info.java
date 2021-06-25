/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.mdsal.binding.generator.BindingGenerator;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingGenerator;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeGenerator;

module org.opendaylight.mdsal.binding.generator {
    // FIXME: 8.0.0: rename to mdsal.binding.generator.ri, keep implementation hidden in ri.impl
    exports org.opendaylight.mdsal.binding.generator.impl;

    provides BindingGenerator with DefaultBindingGenerator;
    provides BindingRuntimeGenerator with DefaultBindingRuntimeGenerator;

    requires transitive org.opendaylight.mdsal.binding.model.api;
    requires transitive org.opendaylight.mdsal.binding.model.ri;
    requires transitive org.opendaylight.mdsal.binding.runtime.api;
    requires com.google.common;
    requires org.opendaylight.mdsal.binding.spec.util;
    requires org.opendaylight.yangtools.concepts;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.model.spi;
    requires org.opendaylight.yangtools.yang.model.ri;
    requires org.opendaylight.yangtools.yang.model.util;
    requires org.opendaylight.yangtools.odlext.model.api;
    requires org.opendaylight.yangtools.util;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static javax.inject;
    requires static metainf.services;
    requires static org.osgi.service.component.annotations;
}
