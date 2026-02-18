/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Dagger module exposing integration between {@link org.opendaylight.yangtools.yang.model.api.source.YinTextSource}
 * and {@link org.opendaylight.yangtools.yang.model.spi.source.YinDOMSource}. The sole module exposed from here is
 * {@link YinDOMSourceModule}, which provide static factory for various services. These methods can be used to build
 * bindings to other injection frameworks, like Guice or Spring Framework.
 *
 * <p>This package is not exposed to OSGi runtime on purpose: use OSGi Service Registry for injection.
 *
 * @since 15.0.0
 */
package org.opendaylight.yangtools.yin.source.dom.dagger;
