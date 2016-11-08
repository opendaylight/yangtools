/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

import com.google.common.base.Preconditions;

/**
 * Exception thrown when a proposed change fails validation before being
 * applied into the datastore. This can have multiple reasons, for example
 * the datastore has been concurrently modified such that a conflicting
 * node is present, or the modification is structurally incorrect.
 */
public class DataValidationFailedException extends Exception {
    private static final long serialVersionUID = 1L;
    private final YangInstanceIdentifier path;

    /**
     * Create a new instance without a cause.
     *
     * @param path Object path which caused this exception
     * @param message Specific message describing the failure
     */
    public DataValidationFailedException(final YangInstanceIdentifier path, final String message) {
        this(path, message, null);
    }
    /**
     * Create a new instance, with the path string appended to the message.
     *
     * @param path Object path which caused this exception
     * @param message Start of a sentence describing the failure
     * @param cause Exception which triggered this failure, may be null
     */
    public DataValidationFailedException(final YangInstanceIdentifier path, final String message, final Throwable cause) {
        super(appendPathToMessage(path, message), cause);
        this.path = path;
    }

    /**
     * Return the offending object path.
     *
     * @return Path of the offending object
     */
    public YangInstanceIdentifier getPath() {
        return path;
    }

    /**
     * Check path is non-null and return message with path string appended.
     *
     * @param path Object path which caused this exception
     * @param message Start of a sentence describing the failure
     * @return Message with the path string appended
     */
    protected static String appendPathToMessage(final YangInstanceIdentifier path, final String message) {
        Preconditions.checkNotNull(path);
        return message + (message.isEmpty() ? "" : " at ") + "path " + path.toString();
    }
}
