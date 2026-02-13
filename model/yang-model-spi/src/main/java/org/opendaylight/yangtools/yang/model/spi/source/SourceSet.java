/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.spi.source.SourceRef.ToModule;
import org.opendaylight.yangtools.yang.model.spi.source.SourceRef.ToSubmodule;

/**
 * A set of sources which constitute a single {@code module} and all its {@code submodules}.
 */
public interface SourceSet {

    ToModule moduleRef();

    SourceInfo moduleInfo();

    List<ToSubmodule> submodules();

    Set<Import> imports();
}
