/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangVersion;

/**
 * ABI compatibility for {@link StatementSupportBundle} to allow us to change the return type of
 * {@link #getSupportedVersions()}.
 */
@Deprecated(since = "14.0.20", forRemoval = true)
sealed interface StatementSupportBundleABI permits StatementSupportBundle {

    @NonNull Set<YangVersion> getSupportedVersions();
}
