/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;

/**
 * Namespace key matching criterion. A criterion can be used to evaluate multiple sets of candidate keys through
 * instantiation of a {@link NamespaceKeyMatcher}.
 *
 * @param <K> Key type
 *
 * @author Robert Varga
 */
@Beta
@FunctionalInterface
public interface NamespaceKeyCriterion<K> {
    /**
     * Create a matcher to evaluate candidate keys. Implementations of this method may return a shared instance
     * provided the matcher is stateless.
     *
     * @return A matcher with empty state.
     */
    @Nonnull NamespaceKeyMatcher<K> createMatcher();
}
