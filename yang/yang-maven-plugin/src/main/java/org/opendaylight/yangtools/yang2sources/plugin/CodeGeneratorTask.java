/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;

/**
 * Bridge to legacy {@link BasicCodeGenerator} generation.
 *
 * @author Robert Varga
 *
 * @deprecated Scheduled for removal with {@link BasicCodeGenerator}.
 */
@Deprecated
final class CodeGeneratorTask extends AbstractGeneratorTask {
    private final BasicCodeGenerator gen;

    CodeGeneratorTask(final BasicCodeGenerator gen) {
        this.gen = Preconditions.checkNotNull(gen);
    }
}
