package org.opendaylight.yangtools.yang.model.api;

import com.google.common.base.Optional;

/**
 * Schema Node which may be derived from other schema node
 * using augmentation or uses statement.
 *
 */
public interface DerivableSchemaNode extends DataSchemaNode {

    /**
     * If this node is added by uses, returns original node definition from
     * grouping where it was defined.
     *
     * @return original node definition from grouping if this node is added by
     *         uses, Optional.absent otherwise
     */
    Optional<? extends SchemaNode> getOriginal();

}
