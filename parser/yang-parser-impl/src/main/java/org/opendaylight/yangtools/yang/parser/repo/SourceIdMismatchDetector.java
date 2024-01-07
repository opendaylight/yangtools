/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressModernizer
final class SourceIdMismatchDetector implements Function<List<YangIRSource>, List<YangIRSource>> {
    private static final Logger LOG = LoggerFactory.getLogger(SourceIdMismatchDetector.class);

    private final Set<SourceIdentifier> sourceIdentifiers;

    SourceIdMismatchDetector(final Set<SourceIdentifier> sourceIdentifiers) {
        this.sourceIdentifiers = requireNonNull(sourceIdentifiers);
    }

    @Override
    public List<YangIRSource> apply(final List<YangIRSource> input) {
        final var srcIt = sourceIdentifiers.iterator();
        final var filtered = new LinkedHashMap<SourceIdentifier, YangIRSource>();
        for (var irSchemaSource : input) {
            final SourceIdentifier realSId = irSchemaSource.sourceId();
            if (srcIt.hasNext()) {
                final SourceIdentifier expectedSId = srcIt.next();
                if (!expectedSId.equals(realSId)) {
                    LOG.warn("Source identifier mismatch for module \"{}\", requested as {} but actually is {}. "
                        + "Using actual id", expectedSId.name().getLocalName(), expectedSId, realSId);
                }
            }

            final var prev = filtered.put(realSId, irSchemaSource);
            if (prev != null) {
                LOG.warn("Duplicate source for module {} detected in reactor", realSId);
            }
        }

        return ImmutableList.copyOf(filtered.values());
    }
}