/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.yang.model.spi.source.YangIRToTextSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;

/**
 * Integration between YANG Text and YANG IR source representations.
 *
 * @provides YangIRToTextSourceTransformer
 * @provides YangTextToIRSourceTransformer
 * @since 15.0.0
 */
module org.opendaylight.yangtools.yang.source.ir {
    exports org.opendaylight.yangtools.yang.source.ir.dagger;

    provides YangIRToTextSourceTransformer
        with org.opendaylight.yangtools.yang.source.ir.DefaultYangIRToTextSourceTransformer;
    provides YangTextToIRSourceTransformer
        with org.opendaylight.yangtools.yang.source.ir.DefaultYangTextToIRSourceTransformer;

    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.concepts;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires transitive org.opendaylight.yangtools.yang.model.spi;
    requires transitive org.opendaylight.yangtools.yang.ir;

    requires org.antlr.antlr4.runtime;
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
