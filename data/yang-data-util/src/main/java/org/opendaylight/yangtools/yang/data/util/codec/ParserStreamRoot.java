/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;

/**
 * A simple holder class describing the difference between how a parser stream's context was initialized and how it was
 * adjusted during resolution parsing.
 *
 * <p>
 * This difference may exist because of how {@link YangInstanceIdentifier} and {@link NormalizedNode} hierarchy relates
 * to YANG definition (as expressed by {@link EffectiveStatementInference}) and various external encoding formats (like
 * JSON, XML and others).
 *
 * <p>
 * As a concrete example, a RESTCONF north-bound endpoint works on two pieces of information:
 * <ol>
 *   <li>an {@code instance identifier} encoded in the URL</li>
 *   <li>a document encoded by the request body</li>
 * </ol>
 * Under usual YANG encoding rules the difference between the instance identifier and the document root is either zero
 * (in case of a PUT method) or one (in case of a POST method) step along the YANG data tree axis. Unfortunately this
 * single step can result in a variable number of {@link PathArgument}s.
 *
 * <p>
 * The function of this class is to be an optional return from a {@code parse()} method.
 */
// FIXME: expand this a bit more:
//        - is this optional, or mandatory (pointing to parsing result's context)?
//        - are arguments only silent, or also include the document's root?
@Beta
public record ParserStreamRoot(ImmutableList<PathArgument> rootArguments, EffectiveStatementInference rootInference) {
    public ParserStreamRoot {
        checkArgument(!rootArguments.isEmpty(), "Root arguments may not be empty");
        checkArgument(!rootInference.statementPath().isEmpty(), "Root inference myst not be empty");
    }
}
