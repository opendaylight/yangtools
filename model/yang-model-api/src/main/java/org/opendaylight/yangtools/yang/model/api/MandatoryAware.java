/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

/**
 * Mix-in interface for {@link SchemaNode}s which can have a {@code mandatory} statement.
 *
 * @author Robert Varga
 */
public interface MandatoryAware {

    /**
     * Return whether this node is mandatory or not. Note this reflects the declared model, as defined by 'mandatory'
     * statement, not the effective model. This notably means this attribute does not mirror the definition of
     * {@code mandatory node} as per <a href="https://www.rfc-editor.org/rfc/rfc7950#page-14">RFC7950 Terminology</a>.
     *
     * @return True if this node is marked as mandatory.
     */
    boolean isMandatory();
}
