package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 *  Effective statements of TypeDef, ExtendedType, Decimal64, Enumeration, Leafref, Union, IndetityRef, Bits
 *  should implement this interface and method buildType() should create particular object from
 *  yang.model.util (e.g. Decimal64)
 */
public interface TypeDefinitionEffectiveBuilder {

    TypeDefinition<?> buildType();

}
