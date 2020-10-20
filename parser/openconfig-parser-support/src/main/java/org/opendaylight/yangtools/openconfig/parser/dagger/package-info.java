/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Dagger modules for supporting extensions defined in {@code openconfig-extensions.yang}. There is a single module,
 *  {@link OpenConfigModule}, which provides a static factory method returning the appropriate
 * {@link org.opendaylight.yangtools.yang.parser.spi.ParserExtension}. This method can be used to build bindings to
 * other injection frameworks, like Guice or Spring Framework.
 *
 * <p>This package is not exposed to OSGi runtime.
 *
 * @since 14.0.21
 */
package org.opendaylight.yangtools.openconfig.parser.dagger;