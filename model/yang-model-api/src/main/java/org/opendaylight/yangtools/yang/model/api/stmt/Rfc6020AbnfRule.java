/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * References ABNF rule defined in RFC6020 - YANG Specification.
 *
 * <p>
 * An interface / class annotated with this annotation
 * is Java representation of data represented by ABNF rule
 * provided as {@link #value()}. Java representation
 * does not need to be direct,
 * but must retain all information in some, publicly
 * accessible form for consumers.
 * </p>
 * <p>
 * Note that this annotation is used currently only for documentation
 * and does not affect any runtime behaviour.
 * </p>
 */
@Deprecated(since = "9.0.2", forRemoval = true)
@Documented
@Retention(RetentionPolicy.SOURCE)
@interface Rfc6020AbnfRule {

    String[] value();
}
