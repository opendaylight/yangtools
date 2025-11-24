/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser.dagger;

import dagger.Component;
import jakarta.inject.Singleton;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;

@Singleton
@Component(modules = Rfc6241Module.class)
@NonNullByDefault
interface TestComponent {

    Set<ParserExtension> extensions();
}
