/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;

/**
 * A {@link ParserExtension} providing support for extensions defined in
 * <a href="https://github.com/openconfig/public/blob/master/release/models/openconfig-extensions.yang">OpenConfig</a>.
 *
* @provides ParserExtension
 */
module org.opendaylight.yangtools.openconfig.parser.support {
    exports org.opendaylight.yangtools.openconfig.parser.dagger;

    // FIXME: do not export this package
    exports org.opendaylight.yangtools.openconfig.parser;
    // FIXME: remove this package
    exports org.opendaylight.yangtools.openconfig.parser.inject;

    provides ParserExtension with org.opendaylight.yangtools.openconfig.parser.impl.OpenConfigParserExtension;

    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.yang.parser.api;
    requires transitive org.opendaylight.yangtools.yang.parser.spi;
    requires transitive org.opendaylight.yangtools.openconfig.model.api;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.model.spi;
    requires org.slf4j;

    // Annotations
    requires static transitive dagger;
    requires static transitive jakarta.inject;
    requires static transitive java.compiler;
    requires static transitive javax.inject;
    requires static transitive org.eclipse.jdt.annotation;
    requires static org.kohsuke.metainf_services;
    requires static org.osgi.annotation.bundle;
    requires static org.osgi.service.component.annotations;
}

