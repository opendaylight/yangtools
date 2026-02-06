/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.spi.source.SourceRef;

/**
 * A single linked source.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public sealed interface SourceLinkage permits ModuleLinkage, SubmoduleLinkage {

    SourceRef ref();

    Unqualified name();

    YangVersion version();

    Map<Unqualified, SourceRef.ToModule> imports();

    Map<Unqualified, SourceRef.ToSubmodule> includes();

    // FIXME: also stream provider at some point?
}
