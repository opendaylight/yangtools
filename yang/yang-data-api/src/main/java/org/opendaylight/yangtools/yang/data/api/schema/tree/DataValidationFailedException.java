/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.helpers.MessageFormatter;

/**
 * Exception thrown when a proposed change fails validation before being applied into the datastore. This can have
 * multiple reasons, for example the datastore has been concurrently modified such that a conflicting node is present,
 * or the modification is structurally incorrect.
 */
public class DataValidationFailedException extends Exception {
    private static final long serialVersionUID = 1L;

    private final YangInstanceIdentifier path;

    /**
     * Create a new instance without a cause, format path string into the message.
     *
     * <p>This uses slf4j message formatting.
     *
     * @param messagePattern Specific message describing the failure, including single {} placeholder for path
     * @param path Object path which caused this exception
     */
    public DataValidationFailedException(final String messagePattern, final YangInstanceIdentifier path) {
        this(messagePattern, path, null);
    }

    /**
     * Create a new instance with a cause, format path string into the message.
     *
     * <p>This uses slf4j message formatting.
     *
     * @param path Object path which caused this exception
     * @param messagePattern Specific message describing the failure, including single {} placeholder for path
     * @param cause Exception which triggered this failure, may be null
     */
    public DataValidationFailedException(final String messagePattern, final YangInstanceIdentifier path, final Throwable cause) {
        this(path, formatMessage(messagePattern, path), cause);
    }

    /**
     * Create a new instance without a cause, using a verbatim message.
     *
     * @param path Object path which caused this exception
     * @param message Verbatim message describing the failure
     */
    public DataValidationFailedException(final YangInstanceIdentifier path, final String message) {
        this(path, message, null);
    }

    /**
     * Create a new instance with a cause, using a verbatim message.
     *
     * @param path Object path which caused this exception
     * @param message Verbatim message describing the failure
     * @param cause Exception which triggered this failure, may be null
     */
    public DataValidationFailedException(final YangInstanceIdentifier path, final String message,
            final Throwable cause) {
        super(message, cause);
        this.path = Preconditions.checkNotNull(path);
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
     * Return message with the placeholder replaced by the path string.
     *
     * @param path Object path which caused this exception
     * @param messagePattern Specific message describing the failure, including single {} placeholder for path
     * @return Message with the path string
     */
    protected static String formatMessage(final String messagePattern, final YangInstanceIdentifier path) {
        return MessageFormatter.format(messagePattern, path).getMessage();
    }
}
