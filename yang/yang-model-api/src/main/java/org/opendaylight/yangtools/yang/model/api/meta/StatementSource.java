package org.opendaylight.yangtools.yang.model.api.meta;

public enum StatementSource {

    /**
     *
     * Statement was explicitly declared by author
     * of the supplied model.
     *
     */
    DECLARATION,
    /**
     *
     * Statement was derived from context of YANG model / statement
     * and represents effective model.
     *
     * Effective context nodes are derived from applicable {@link #DECLARATION}
     * statements by interpreting their semantic meaning in context
     * of current statement.
     *
     */
    CONTEXT

}
