package org.opendaylight.yangtools.yang.model.api;

public interface SimpleValueSchemaNode extends DataSchemaNode {
    
    /**
     * Returns the YANG <code>type</code> of the instance of the type
     * <code>LeafSchemaNode</code>.
     * 
     * @return type definition which represents the value of the YANG
     *         <code>type</code> substatement for <code>leaf</code> statement
     */
    TypeDefinition<?> getType();

    /**
     * Returns the default value of YANG <code>leaf</code>.
     * 
     * @return string with the value of the argument of YANG
     *         <code>default</code> substatement of the <code>leaf</code>
     *         statement
     */
    String getDefault();

    /**
     * Returns the units in which are the values of the <code>leaf</code>
     * presented.
     * 
     * @return string with the value of the argument of YANG <code>units</code>
     *         substatement of the <code>leaf</code> statement
     */
    String getUnits();

}
