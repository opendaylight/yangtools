/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YinTextToDOMSourceTransformer;

/**
 * Reference implementation of YANG parser.
 *
 * @uses YangTextToIRSourceTransformer
 * @uses YinTextToDOMSourceTransformer
 */
module org.opendaylight.yangtools.yang.parser.impl {
    exports org.opendaylight.yangtools.yang.parser.repo;

    uses YangTextToIRSourceTransformer;
    uses YinTextToDOMSourceTransformer;

    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.yang.parser.api;
    requires transitive org.opendaylight.yangtools.yang.parser.spi;
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.model.spi;
    requires org.opendaylight.yangtools.yang.repo.api;
    requires org.opendaylight.yangtools.yang.repo.spi;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static org.checkerframework.checker.qual;
    requires static org.gaul.modernizer_maven_annotations;
    requires static org.osgi.annotation.bundle;
}
