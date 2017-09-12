/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * Namespace class for storing semantic version of yang modules.
 */
@Beta
public interface SemanticVersionNamespace extends IdentifierNamespace<StmtContext<?, ?, ?>, SemVer> {
}
