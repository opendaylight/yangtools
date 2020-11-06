/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.binding.runtime.spi {
    exports org.opendaylight.mdsal.binding.runtime.spi;

    requires transitive org.opendaylight.yangtools.yang.parser.api;
    requires transitive org.opendaylight.mdsal.binding.runtime.api;
    requires org.opendaylight.yangtools.concepts;
    requires org.opendaylight.yangtools.yang.parser.impl;
    requires org.opendaylight.mdsal.binding.spec.util;
    requires org.opendaylight.yangtools.util;
    requires org.slf4j;

    uses org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
    uses org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeGenerator;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static org.checkerframework.checker.qual;
}
