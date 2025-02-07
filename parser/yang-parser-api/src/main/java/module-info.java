/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * YANG parser public API. It allows compiling a set of
 * <a href="https://www.rfc-editor.org/rfc/rfc7950">YANG (or YIN)</a> modules into a
 * {@link org.opendaylight.yangtools.yang.model.api.EffectiveModelContext cross-referenced representation}. The primary
 * entry point is {@link org.opendaylight.yangtools.yang.parser.api.YangParserFactory}, which is a thread-safe service.
 * Its {@link org.opendaylight.yangtools.yang.parser.api.YangParserFactory#createParser()} family of methods return
 * a {@link org.opendaylight.yangtools.yang.parser.api.YangParser}, which in turns mediates the compilation process
 * in the context of a single thread.
 */
module org.opendaylight.yangtools.yang.parser.api {
    exports org.opendaylight.yangtools.yang.parser.api;

    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.concepts;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.model.api;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static org.osgi.annotation.bundle;
}
