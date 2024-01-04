/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement.ConformanceType;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

/**
 * Specification of a single parsing attempt.
 *
 * @param symbolicName symbolic name of this specification
 * @param modules the set of modules to build
 * @param featureSet the {@link FeatureSet} to apply
 */
@NonNullByDefault
public record BuildSpecification(
        String symbolicName,
        ImmutableMap<QNameModule, SourceSpec.Module> modules,
        @Nullable FeatureSet featureSet) {
    public BuildSpecification {
        requireNonNull(symbolicName);
        requireNonNull(modules);
    }

    /**
     * Specification of how a source should be processed.
     */
    public sealed interface SourceSpec permits SourceSpec.Module, SourceSpec.Submodule {
        /**
         * Return the {@link SourceInfo} about {@link #stream()}.
         *
         * @return the {@link SourceInfo}
         */
        // FIXME: should reside in stream()
        SourceInfo info();

        /**
         * Return the {@link StatementStreamSource} of this source.
         *
         * @return the {@link StatementStreamSource} of this source
         */
        StatementStreamSource stream();


        /**
         * Specification of how a {@code module} source should be processed.
         */
        record Module(
                SourceInfo.Module info,
                StatementStreamSource stream,
                ImmutableMap<SourceIdentifier, Submodule> submodules,
                @Nullable ImmutableSet<QName> allowDeviationsFrom,
                @Nullable ConformanceType conformanceType) implements SourceSpec {
            public Module {
                requireNonNull(info);
                requireNonNull(stream);
                requireNonNull(submodules);
            }
        }

        /**
         * Specification of how a {@code submodule} source should be processed.
         */
        record Submodule(SourceInfo.Submodule info, StatementStreamSource stream) implements SourceSpec {
            public Submodule {
                info = requireNonNull(info);
                stream = requireNonNull(stream);
            }
        }
    }
}
