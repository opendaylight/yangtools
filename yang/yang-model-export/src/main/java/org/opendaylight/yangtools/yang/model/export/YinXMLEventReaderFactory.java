/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;

/**
 * Factory for creating {@link XMLEventReader} instances reporting events equivalent to reading a YIN document defining
 * a specified {@link ModuleEffectiveStatement}.
 */
@Beta
public final class YinXMLEventReaderFactory {
    private static final Location DUMMY_LOCATION = new Location() {

        @Override
        public int getLineNumber() {
            return -1;
        }

        @Override
        public int getColumnNumber() {
            return -1;
        }

        @Override
        public int getCharacterOffset() {
            return -1;
        }

        @Override
        public String getPublicId() {
            return null;
        }

        @Override
        public String getSystemId() {
            return null;
        }
    };

    private static final YinXMLEventReaderFactory DEFAULT;

    static {
        final XMLEventFactory eventFactory = XMLEventFactory.newFactory();
        eventFactory.setLocation(DUMMY_LOCATION);
        DEFAULT = new YinXMLEventReaderFactory(eventFactory);
    }

    private final XMLEventFactory eventFactory;

    private YinXMLEventReaderFactory(final XMLEventFactory eventFactory) {
        this.eventFactory = requireNonNull(eventFactory);
    }

    /**
     * Get the system-wide default instance, backed by system-wide default XMLEventFactory.
     *
     * @return Default instance.
     */
    public static YinXMLEventReaderFactory defaultInstance() {
        return DEFAULT;
    }

    public static YinXMLEventReaderFactory ofEventFactory(final XMLEventFactory factory) {
        return new YinXMLEventReaderFactory(factory);
    }

    /**
     * Create a new XMLEventReader iterating of the YIN document equivalent of an effective module.
     *
     * @param module Effective module
     * @return A new XMLEventReader.
     * @throws NullPointerException if module is null
     * @throws IllegalArgumentException if the specified module does not expose declared model
     */
    public XMLEventReader createXMLEventReader(final ModuleEffectiveStatement module) {
        final ModuleStatement declared = module.getDeclared();
        checkArgument(declared != null, "Module %s does not expose declared model", module);

        return new YinXMLEventReader(eventFactory, new ModuleNamespaceContext(module), declared);
    }

    /**
     * Create a new XMLEventReader iterating of the YIN document equivalent of an effective submodule.
     *
     * @param module Effective module
     * @param submodule Effective submodule
     * @return A new XMLEventReader.
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if the specified submodule does not expose declared model
     */
    public XMLEventReader createXMLEventReader(final ModuleEffectiveStatement module,
            final SubmoduleEffectiveStatement submodule) {
        final SubmoduleStatement declared = submodule.getDeclared();
        checkArgument(declared != null, "Submodule %s does not expose declared model", submodule);
        return new YinXMLEventReader(eventFactory, new ModuleNamespaceContext(module), declared);
    }
}
