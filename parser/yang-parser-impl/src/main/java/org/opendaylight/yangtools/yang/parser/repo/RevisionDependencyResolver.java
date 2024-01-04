/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import java.util.Collection;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;

final class RevisionDependencyResolver extends DependencyResolver {
    RevisionDependencyResolver(final Map<SourceIdentifier, SourceInfo> depInfo) {
        super(depInfo);
    }

    @Override
    YangParserConfiguration parserConfig() {
        return YangParserConfiguration.DEFAULT;
    }

    @Override
    boolean isKnown(final Collection<SourceIdentifier> haystack, final SourceDependency dependency) {
        // Quick lookup
        return haystack.contains(new SourceIdentifier(dependency.name(), dependency.revision()))
            // Slow revision-less walk
            || dependency.revision() == null && haystack.stream().anyMatch(dependency::isSatisfiedBy);
    }
}