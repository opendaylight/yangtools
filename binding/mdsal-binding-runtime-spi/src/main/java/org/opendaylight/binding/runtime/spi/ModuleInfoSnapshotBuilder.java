/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.binding.runtime.spi;

import com.google.common.annotations.Beta;
import java.util.NoSuchElementException;
import org.opendaylight.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.yangtools.concepts.CheckedBuilder;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;

@Beta
public final class ModuleInfoSnapshotBuilder extends AbstractModuleInfoTracker
        implements CheckedBuilder<ModuleInfoSnapshot, NoSuchElementException> {

    public ModuleInfoSnapshotBuilder(final String name, final YangParserFactory parserFactory) {
        super(YangTextSchemaContextResolver.create(name, parserFactory));
    }

    @Override
    public synchronized ModuleInfoSnapshot build() {
        return updateSnapshot();
    }
}
