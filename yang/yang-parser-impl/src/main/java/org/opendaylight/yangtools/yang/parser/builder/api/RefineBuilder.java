/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;

/**
 * Mutable holder for information contained in <code>refine</code>
 *
 * Represents a local change to node introduced by uses statement
 * e.g. change in documentation, configuration or properties.
 *
 *
 */
public interface RefineBuilder extends DocumentedNodeBuilder {

    /**
     * Get value of config statement.
     *
     * @return value of config statement
     */
    Boolean isConfiguration();

    /**
     * Set config statement to the product.
     *
     *
     * @param config true if config true was set, false if config false was set.
     */
    void setConfiguration(Boolean config);

    /**
     * Returns mandatory state of node or NULL if state was not refined.
     *
     *
     * @return mandatory state of node or NULL if state was not refined.
     */
    Boolean isMandatory();

    void setMandatory(Boolean mandatory);

    /**
     *
     * Returns presence state of refined container.
     *
     * @return Presence state of refined container.
     */
    Boolean isPresence();

    void setPresence(Boolean presence);

    /**
     * Returns <code>must</code> definition associated with this builder.
     *
     * @return <code>must</code> definition associated with this builder.
     */
    MustDefinition getMust();

    /**
     * Adds must definition to product of this builder.
     *
     * @param must <code>must</code> definition which should be associated with parent node.
     */
    void setMust(MustDefinition must);


    /**
    *
    * Returns number of minimum required elements or NULL if minimum elements was not overriden.
    *
    * This constraint has meaning only if associated node is list or leaf-list.
    *
    * @return number of minimum required elements.
    */
   Integer getMinElements();

   /**
    *
    * Sets number of minimum required elements.
    *
    * This constraint has meaning only if associated node is list or leaf-list.
    *
    * @param minElements
    *            number of minimum required elements.
    */
   void setMinElements(Integer minElements);

   /**
   *
   * Returns number of maximum elements or NULL if maximum elements was not overriden.
   *
   * This constraint has meaning only if associated node is list or leaf-list.
   *
   * @return number of maximum required elements.
   */
   Integer getMaxElements();

   /**
   *
   * Sets number of maximum required elements.
   *
   * This constraint has meaning only if associated node is list or leaf-list.
   *
   * @param maxElements number of maximum required elements.
   */
   void setMaxElements(Integer maxElements);

    /**
     *
     * Returns string representation of path to refine target, which is relative to grouping root
     *
     * This string representation does not need to contain prefixes, since parent uses
     * element introduces elements with namespace local to parent module.
     *
     * @return string representation of path to refine target, which is relative to grouping root
     */
    String getTargetPathString();

    /**
     *
     * Returns module (source) name in which refine statement was defined.
     *
     * @return module (source) name in which refine statement was defined.
     */
    String getModuleName();

    /**
     * Line on which element was defined.
     *
     * @return Line on which element was defined.
     */
    int getLine();

    /**
     * Returns list of unknown schema node builders, which are associated
     * with refine statement.
     *
     * @return Set of unknown schema node builders.
     */
    List<UnknownSchemaNodeBuilder> getUnknownNodes();

    /**
     * Returns string representation of default value or null, if default value was not refined.
     *
     * @return  string representation of default value or null, if default value was not refined.
     */
    String getDefaultStr();

}