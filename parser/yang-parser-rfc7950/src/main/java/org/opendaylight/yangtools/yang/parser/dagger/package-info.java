/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Dagger modules and components for parsing YANG and YIN files. There are two separate modules,
 * {@link YangLibResolverModule} and {@link YangParserFactoryModule}, which provide static factory methods returning
 * a singleton implementation of they underlying construct. These methods can be used to build bindings to other
 * injection frameworks, like Guice or Spring Framework.
 *
 * <p>This package is not exposed to OSGi runtime.
 *
 * @since 14.0.21
 */
package org.opendaylight.yangtools.yang.parser.dagger;
