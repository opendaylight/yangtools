/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

/**
 * Linkage of an individual source.
 */
@NonNullByDefault
public sealed interface SourceLinkage {
    /**
     * {@link SourceLinkage} of a {@link SourceInfo.Module}.
     */
    record OfModule(
            SourceInfo.Module sourceInfo,
            QName sourceName,
            Map<String, QNameModule> prefixToModule,
            List<OfSubmodule> includedModules) implements SourceLinkage {
        public OfModule {
            requireNonNull(sourceName);
            requireNonNull(sourceInfo);
            requireNonNull(prefixToModule);
            requireNonNull(includedModules);
        }
    }

    /**
     * {@link SourceLinkage} of a {@link SourceInfo.Submodule}.
     */
    record OfSubmodule(
            SourceInfo.Submodule sourceInfo,
            QName sourceName,
            Map<String, QNameModule> prefixToModule,
            List<OfSubmodule> includedModules) implements SourceLinkage {
        public OfSubmodule {
            requireNonNull(sourceInfo);
            requireNonNull(sourceName);
            requireNonNull(prefixToModule);
        }
    }

    /**
     * {@return the {@link SourceInfo} processed by this linkage}
     */
    SourceInfo sourceInfo();

    /**
     * {@return the {@link QName} representing this source's canonical name}
     */
    QName sourceName();

    /**
     * {@return the {@code prefix} linkage map}
     */
    Map<String, QNameModule> prefixToModule();

    /**
     *
     * @return
     */
    List<OfSubmodule> includedModules();
}
