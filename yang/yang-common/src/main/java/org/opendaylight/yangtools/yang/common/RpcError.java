/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

/**
 *
 * Representation of Error in YANG enabled system.
 *
 * Which may be send / received by YANG modeled / enabled systems.
 *
 */
public interface RpcError {

    /**
     *
     * Returns error severity, as determined by component reporting the error.
     *
     * @return error severity
     */
    ErrorSeverity getSeverity();

    /**
     *
     * Returns a string identifying the error condition.
     *
     * @return string identifying the error condition.
     */
    String getTag();

    /**
     *
     * Returns a string identifying the data-model-specific or
     * implementation-specific error condition, if one exists. This element will
     * not be present if no appropriate application error-tag can be associated
     * with a particular error condition. If a data-model-specific and an
     * implementation-specific error-app-tag both exist, then the
     * data-model-specific value MUST be used by the reporter.
     *
     * @return Returns a string identifying the data-model-specific or
     *         implementation-specific error condition, or null if does not
     *         exists.
     */
    String getApplicationTag();

    /**
     *
     * Returns a string suitable for human display that describes the error
     * condition. This element will not be present if no appropriate message is
     * provided for a particular error condition.
     *
     * @return returns an error description for human display.
     */
    String getMessage();

    /**
     *
     * Contains protocol- or data-model-specific error content. This value may
     * be not be present if no such error content is provided for a particular
     * error condition.
     *
     * The list in Appendix A defines any mandatory error-info content for each
     * error. After any protocol-mandated content, a data model definition MAY
     * mandate that certain application-layer error information be included in
     * the error-info container.
     *
     * An implementation MAY include additional information to provide extended
     * and/or implementation- specific debugging information.
     *
     * @return
     */
    String getInfo();

    /**
     *
     * Return a cause if available.
     *
     * @return cause of this error, if error was triggered by exception.
     */
    Throwable getCause();

    /**
     * Returns the conceptual layer that on which the error occurred.
     *
     * @return the conceptual layer that on which the error occurred.
     */
    ErrorType getErrorType();

    public enum ErrorSeverity {
        ERROR, WARNING,
    }

    public enum ErrorType {
        TRANSPORT, RPC, PROTOCOL, APPLICATION
    }
}
