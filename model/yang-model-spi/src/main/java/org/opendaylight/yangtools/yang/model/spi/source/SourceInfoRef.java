/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

/**
 *
 */
public sealed interface SourceInfoRef {

    sealed interface OfModule extends SourceInfoRef permits ModuleInfoRef {

    }

    sealed interface OfSubmodule extends SourceInfoRef permits SubmoduleInfoRef {

    }


    SourceRef ref();

    SourceInfo info();
}
