/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.yang.model.spi.source.YinTextToDOMSourceTransformer;

/**
 * Integration between YIN Text and YIN DOM source representations.
 *
 * @since 15.0.0
 */
module org.opendaylight.yangtools.yin.source.dom {
    exports org.opendaylight.yangtools.yin.source.dom.dagger;

    provides YinTextToDOMSourceTransformer
        with org.opendaylight.yangtools.yin.source.dom.DefaultYinTextToDOMSourceTransformer;

    requires transitive org.opendaylight.yangtools.concepts;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires transitive org.opendaylight.yangtools.yang.model.spi;

    requires org.opendaylight.yangtools.util;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static dagger;
    requires static java.compiler;
    requires static javax.inject;
    requires static jakarta.inject;
    requires static org.kohsuke.metainf_services;
    requires static org.osgi.service.component.annotations;
}
