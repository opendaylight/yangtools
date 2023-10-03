/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xml;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Throwables;
import com.google.common.base.VerifyException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A hack to instantiate {@link XMLStreamException}s without them mucking with the message.
 */
final class HackXMLStreamException extends XMLStreamException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private static final MethodHandle SET_LOCATION;

    static {
        try {
            SET_LOCATION = MethodHandles.lookup().findSetter(XMLStreamException.class, "location", Location.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private HackXMLStreamException() {
        throw new UnsupportedOperationException();
    }

    static @NonNull XMLStreamException of(final String message, final @Nullable Location location) {
        final var msg = requireNonNull(message);
        if (location == null) {
            return new XMLStreamException(msg);
        }

        final var ret = new XMLStreamException(
            msg + " [at " + location.getLineNumber() + ":" + location.getColumnNumber() + "]");
        setLocation(ret, location);
        return ret;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private static void setLocation(final XMLStreamException obj, final Location location) {
        try {
            SET_LOCATION.invokeExact(obj, location);
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new VerifyException("Unexpected failure", e);
        }
    }
}
