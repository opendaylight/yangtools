/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.binding.codegen {

    provides org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory
        with org.opendaylight.yangtools.binding.codegen.JavaFileGeneratorFactory;

    requires com.google.common;
    requires java.compiler;
    requires java.management;
    requires org.apache.commons.text;
    requires org.eclipse.xtend.lib;
    requires org.eclipse.xtext.xbase.lib;
    requires org.opendaylight.yangtools.binding.generator;
    requires org.opendaylight.yangtools.binding.model;
    requires org.opendaylight.yangtools.plugin.generator.api;
    requires org.opendaylight.yangtools.yang.model.export;
    requires org.slf4j;

    // Annotations
    requires static org.eclipse.jdt.annotation;
    requires static org.gaul.modernizer_maven_annotations;
    requires static org.kohsuke.metainf_services;
}
