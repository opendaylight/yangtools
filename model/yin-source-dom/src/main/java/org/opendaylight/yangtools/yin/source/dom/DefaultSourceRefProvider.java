/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yin.source.dom;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.spi.source.YinDOMSource.SourceRefProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.UserDataHandler;

final class DefaultSourceRefProvider implements SourceRefProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultSourceRefProvider.class);
    private static final String USER_DATA_KEY = StatementSourceReference.class.getName();
    private static final UserDataHandler USER_DATA_HANDLER = (operation, key, data, src, dst) -> {
        if (operation == UserDataHandler.NODE_CLONED && data != null && dst != null && USER_DATA_KEY.equals(key)) {
            dst.setUserData(USER_DATA_KEY, data, DefaultSourceRefProvider.USER_DATA_HANDLER);
        }
    };

    static final @NonNull DefaultSourceRefProvider INSTANCE = new DefaultSourceRefProvider();

    private DefaultSourceRefProvider() {
        // Hidden on purpose
    }

    @Override
    public StatementSourceReference refOf(final Element element) {
        final var data = element.getUserData(USER_DATA_KEY);
        return switch (data) {
            case null -> null;
            case StatementSourceReference sourceRef -> sourceRef;
            default -> {
                LOG.debug("Ignoring {} attached to key {}", data, USER_DATA_KEY);
                yield null;
            }
        };
    }

    @NonNullByDefault
    static void setSourceRef(final Element element, final StatementSourceReference sourceRef) {
        element.setUserData(USER_DATA_KEY, requireNonNull(sourceRef), USER_DATA_HANDLER);
    }
}
