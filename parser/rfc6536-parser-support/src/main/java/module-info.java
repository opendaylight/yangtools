/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.rfc6536.parser.impl.Rfc6536ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;

/**
 * A {@link ParserExtension} providing support for extensions introduced in
 * <a href="https://www.rfc-editor.org/rfc/rfc6536">RFC6536</a>.
 *
* @provides ParserExtension
 */
module org.opendaylight.yangtools.rfc6536.parser.support {
    exports org.opendaylight.yangtools.rfc6536.parser;

    provides ParserExtension with Rfc6536ParserExtension;

    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.yang.parser.api;
    requires transitive org.opendaylight.yangtools.rfc6536.model.api;
    requires transitive org.opendaylight.yangtools.yang.parser.spi;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.model.spi;
    requires org.slf4j;

    // Annotations
    requires static transitive javax.inject;
    requires static transitive org.eclipse.jdt.annotation;
    requires static org.kohsuke.metainf_services;
    requires static org.osgi.annotation.bundle;
    requires static org.osgi.service.component.annotations;
}

