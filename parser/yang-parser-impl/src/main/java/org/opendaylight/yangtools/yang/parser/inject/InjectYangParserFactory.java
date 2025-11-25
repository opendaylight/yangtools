/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.inject;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.dagger.YangParserFactoryModule;
import org.opendaylight.yangtools.yang.parser.impl.DefaultYangParserFactory;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

/**
 * The {@link YangParserFactory} implementation.
 *
 * @since 14.0.20
 * @deprecated Use {@link YangParserFactoryModule#provideParserFactory(YangXPathParserFactory, java.util.Set)} instead.
 */
@Singleton
@NonNullByDefault
@SuppressWarnings("exports")
@Deprecated(since = "14.0.21", forRemoval = true)
public final class InjectYangParserFactory extends DefaultYangParserFactory {
    @Inject
    public InjectYangParserFactory(final YangXPathParserFactory xpathFactory) {
        super(xpathFactory);
    }
}
