/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser.inject;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;

/**
 * A Dagger module binding RFC6241 extension.
 */
@Module
@SuppressWarnings("exports")
public abstract class Rfc6241ParserExtensionModule {
    @Binds
    @IntoSet
    public abstract ParserExtension rfc6241ParserExtension(InjectRfc6241ParserExtension extension);
}
