/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api.type.builder;

import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.Constant;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.TypeComment;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public interface GeneratedTypeBuilderBase<T extends GeneratedTypeBuilderBase<T>> extends Type, AnnotableTypeBuilder {
    /**
     * Adds new Enclosing Transfer Object <code>genTOBuilder</code> into definition of Generated Type.
     *
     * <br>
     * There is no need of specifying of Package Name because enclosing Type is already defined inside Generated Type
     * with specific package name.<br>
     * The name of enclosing Type cannot be same as Name of parent type and if there is already defined enclosing type
     * with the same name, the new enclosing type will simply overwrite the older definition.<br>
     * If the parameter <code>genTOBuilder</code> of enclosing type is <code>null</code> the method SHOULD throw
     * {@link IllegalArgumentException}.
     *
     * @param genTO Name of Enclosing Type
     */
    T addEnclosingTransferObject(GeneratedTransferObject genTO);

    /**
     * Adds String definition of comment into Method Signature definition.<br>
     * The comment String MUST NOT contain any comment specific chars (i.e. "/**" or "//") just plain String text
     * description.
     *
     * @param comment Comment String.
     */
    T addComment(TypeComment comment);

    boolean isAbstract();

    /**
     * Sets the <code>abstract</code> flag to define Generated Type as <i>abstract</i> type.
     *
     * @param isAbstract abstract flag
     */
    T setAbstract(boolean isAbstract);

    List<Type> getImplementsTypes();

    /**
     * Add Type to implements.
     *
     * @param genType Type to implement
     * @return <code>true</code> if the addition of type is successful.
     */
    T addImplementsType(Type genType);

    /**
     * Adds Constant definition and returns <code>new</code> Constant instance.<br>
     * By definition Constant MUST be defined by return Type, Name and assigned value. The name SHOULD be defined
     * with capital letters. Neither of method parameters can be <code>null</code> and the method SHOULD throw
     * {@link IllegalArgumentException} if the contract is broken.
     *
     * @param type Constant Type
     * @param name Name of Constant
     * @param value Assigned Value
     * @return <code>new</code> Constant instance.
     */
    Constant addConstant(Type type, String name, Object value);

    /**
     * Adds new Enumeration definition for Generated Type Builder and returns Enum Builder for specifying all Enum
     * parameters.<br>
     * If there is already Enumeration stored with the same name, the old enum will be simply overwritten byt new enum
     * definition.<br>
     * Name of Enumeration cannot be <code>null</code>, if it is <code>null</code> the method SHOULD throw
     * {@link IllegalArgumentException}.
     *
     * @param enumeration Enumeration to add
     */
    void addEnumeration(Enumeration enumeration);

    List<MethodSignatureBuilder> getMethodDefinitions();

    /**
     * Add new Method Signature definition for Generated Type Builder and returns Method Signature Builder
     * for specifying all Method parameters.<br>
     * Name of Method cannot be <code>null</code>, if it is <code>null</code> the method SHOULD throw
     * {@link IllegalArgumentException}.<br>
     * By <i>Default</i> the MethodSignatureBuilder SHOULD be pre-set as
     * {@link MethodSignatureBuilder#setAbstract(boolean)}, {TypeMemberBuilder#setFinal(boolean)} and
     * {TypeMemberBuilder#setAccessModifier(boolean)}
     *
     * @param name Name of Method
     * @return <code>new</code> instance of Method Signature Builder.
     */
    MethodSignatureBuilder addMethod(String name);

    /**
     * Checks if GeneratedTypeBuilder contains method with name <code>methodName</code>.
     *
     * @param methodName is method name
     */
    boolean containsMethod(String methodName);

    List<GeneratedPropertyBuilder> getProperties();

    /**
     * Returns the YANG definition of this type, if available.
     *
     * @return YANG source definition, or empty when unavailable.
     */
    Optional<YangSourceDefinition> getYangSourceDefinition();

    /**
     * Add new Generated Property definition for Generated Transfer Object Builder and returns Generated Property
     * Builder for specifying Property.<br>
     * Name of Property cannot be <code>null</code>, if it is <code>null</code> the method SHOULD throw
     * {@link IllegalArgumentException}.
     *
     * @param name Name of Property
     * @return <code>new</code> instance of Generated Property Builder.
     */
    GeneratedPropertyBuilder addProperty(String name);

    /**
     * Check whether GeneratedTOBuilder contains property with name <code>name</code>.
     *
     * @param name of property which existence is checked
     * @return true if property <code>name</code> exists in list of properties.
     */
    boolean containsProperty(String name);

    /**
     * Set a string that contains a human-readable textual description of type definition.
     *
     * @param description a string that contains a human-readable textual description of type definition.
     */
    void setDescription(String description);

    /**
     * Set the name of the module, in which generated type was specified.
     *
     * @param moduleName the name of the module
     */
    void setModuleName(String moduleName);

    /**
     * Schema path in schema tree from actual concrete type to the root.
     *
     * @param schemaPath schema path in schema tree
     */
    void setSchemaPath(SchemaPath schemaPath);

    /**
     * Set a string that is used to specify a textual cross-reference to an external document, either another module
     * that defines related management information, or a document that provides additional information relevant to this
     * definition.
     *
     * @param reference a textual cross-reference to an external document.
     */
    void setReference(String reference);

    /**
     * Set the YANG source definition.
     *
     * @param definition YANG source definition, must not be null
     */
    void setYangSourceDefinition(@NonNull YangSourceDefinition definition);
}
