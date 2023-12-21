/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.concepts.PrettyTreeAware;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Node which is normalized according to the YANG schema
 * is identifiable by a {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier}.
 *
 * <p>
 * See subinterfaces of this interface for concretization of node.
 */
/*
 * FIXME: 8.0.0: NormalizedNode represents the perfectly-compliant view of the data, as evaluated by an implementation,
 *               which is currently singular, with respect of its interpretation of a SchemaContext. This includes
 *               leaf values, which are required to hold normalized representation for a particular implementation,
 *               which may be affected by its understanding of any YANG extensions present -- such as optional type
 *               handling hints and bindings.
 *
 *               Implementations (i.e. the reference implementation and parsers) will need to start using
 *               yang.common.Uint8 and friends and, if possible, express data validation in terms
 *               of yang.common.CanonicalValue and yang.common.CanonicalValueValidator.
 *
 *               This notably means that to efficiently implement any sort of lenient parsing, we need a separate
 *               concept which contains an unverified, potentially non-conformant data tree, which the consumer needs
 *               to check/fixup if it wishes to use it as a NormalizedNode. Such a concept should be called
 *               "UnverifiedData".
 *
 * FIXME: 8.0.0: Once we have UnverifiedData, we should really rename this to "NormalizedData" or similar to unload
 *               some "Node" ambiguity. "Node" should be a generic term reserved for a particular domain -- hence 'node'
 *               can be used to refer to either a 'schema node' in context of yang.model.api, or to
 *               a 'normalized data node' in context of yang.data.api.
 *
 * FIXME: 8.0.0: Well, not quite. The structure of unverified data is really codec specific -- and JSON and XML
 *               do not agree on details. Furthermore things get way more complicated when we have a cross-schema
 *               boundary -- like RFC8528. Hence we cannot really have a reasonably-structured concept of unverified
 *               data. Nevertheless, this interface should be named 'NormalizedData'.
 */
public interface NormalizedNode extends NormalizedData, PrettyTreeAware {
    @Override
    Class<? extends NormalizedNode> contract();

    @Override
    PathArgument name();
}
