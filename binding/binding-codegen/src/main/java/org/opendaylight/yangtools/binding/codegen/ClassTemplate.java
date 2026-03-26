/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

/**
 * Template for generating JAVA class.
 */
class ClassTemplate extends AbstractClassTemplate {
    @NonNullByDefault
    ClassTemplate(final GeneratedTransferObject genType) {
        this(new TopLevelJavaGeneratedType(genType), genType);
    }

    @NonNullByDefault
    ClassTemplate(final AbstractJavaGeneratedType javaType, final GeneratedTransferObject genType) {
        super(javaType, genType);
    }

    @Override
    CharSequence generateBody(final boolean isInnerClass) {
        //        «type.formatDataForJavaDoc.wrapToDocumentation»
        //        «annotationDeclaration»
        //        «IF !isInnerClass»
        //            «generatedAnnotation»
        //        «ENDIF»
        //        «generateClassDeclaration(isInnerClass)» {
        //            «suidDeclaration»
        //            «generateInnerClasses(type.enclosedTypes)»
        //            «generateInnerEnumTypeObjects(enums)»
        //            «constantsDeclarations»
        //            «generateFields»
        //
        //            «IF restrictions !== null»
        //                «IF restrictions.lengthConstraint.present»
        //                    «LengthGenerator.generateLengthChecker("_value", TypeUtils.encapsulatedValueType(genTO),
        //                        restrictions.lengthConstraint.orElseThrow, this)»
        //                «ENDIF»
        //                «IF restrictions.rangeConstraint.present»
        //                    «rangeGenerator.generateRangeChecker("_value", restrictions.rangeConstraint.orElseThrow,
        // this)»
        //                «ENDIF»
        //            «ENDIF»
        //
        //            «constructors»
        //
        //            «defaultInstance»
        //
        //            «propertyMethods»
        //
        //            «IF isBitsTypeObject»
        //                «validNamesAndValues»
        //            «ENDIF»
        //
        //            «generateHashCode»
        //
        //            «generateEquals»
        //
        //            «generateToString(genTO.toStringIdentifiers)»
        //        }

        final var sc = new StringConcatenation();
        sc.append(wrapToDocumentation(formatDataForJavaDoc(type())));
        sc.newLineIfNotEmpty();
        sc.append(annotationDeclaration());
        sc.newLineIfNotEmpty();
        if (!isInnerClass) {
            sc.append(generatedAnnotation());
            sc.newLineIfNotEmpty();
        }
        sc.append(generateClassDeclaration(isInnerClass));
        sc.append(" {\n");
        sc.append("    ");
        sc.append(suidDeclaration(), "    ");
        sc.newLineIfNotEmpty();
        sc.append("    ");
        sc.append(generateInnerClasses(type().getEnclosedTypes()), "    ");
        sc.newLineIfNotEmpty();
        sc.append("    ");
        sc.append(generateInnerEnumTypeObjects(enums), "    ");
        sc.newLineIfNotEmpty();
        sc.append("    ");
        sc.append(constantsDeclarations(), "    ");
        sc.newLineIfNotEmpty();
        sc.append("    ");
        sc.append(generateFields(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        if (restrictions != null) {
            final var length = restrictions.getLengthConstraint();
            if (length.isPresent()) {
                sc.append("    ");
                sc.append(LengthGenerator.generateLengthChecker("_value", TypeUtils.encapsulatedValueType(genTO),
                    length.orElseThrow(), this), "    ");
                sc.newLineIfNotEmpty();
            }
            final var range = restrictions.getRangeConstraint();
            if (range.isPresent()) {
                sc.append("    ");
                sc.append(rangeGenerator.generateRangeChecker("_value", range.orElseThrow(), this), "    ");
                sc.newLineIfNotEmpty();
            }
        }
        sc.newLine();
        sc.append("    ");
        sc.append(constructors(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(defaultInstance(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(propertyMethods(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        if (isBitsTypeObject()) {
            sc.append("    ");
            sc.append(validNamesAndValues(), "    ");
            sc.newLineIfNotEmpty();
        }
        sc.newLine();
        sc.append("    ");
        sc.append(generateHashCode(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateEquals(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateToString(genTO.getToStringIdentifiers()), "    ");
        sc.newLineIfNotEmpty();
        sc.append("}\n");
        sc.newLine();
        return sc;
    }

    private boolean isBitsTypeObject() {
        GeneratedTransferObject wlk = genTO;
        do {
            for (var impl : wlk.getImplements()) {
                if (BindingTypes.BITS_TYPE_OBJECT.name().equals(impl.name())) {
                    return true;
                }
            }
            wlk = wlk.getSuperType();
        } while (wlk != null);
        return false;
    }

    private CharSequence validNamesAndValues() {
        for (var c : consts) {
            if (TypeConstants.VALID_NAMES_NAME.equals(c.getName())) {
                return validNamesAndValues((BitsTypeDefinition) c.getValue());
            }
        }
        return "";
    }

    private CharSequence validNamesAndValues(final BitsTypeDefinition typedef) {
        final var override = importedName(OVERRIDE);

        final var sc = new StringConcatenation();
        //
        //        @«OVERRIDE.importedName»
        //        public «IMMUTABLE_SET.importedName»<«STRING.importedName»> validNames() {
        //            return «TypeConstants.VALID_NAMES_NAME»;
        //        }
        //
        sc.newLine();
        sc.append("@");
        sc.append(override);
        sc.newLine();
        sc.append("public ");
        sc.append(importedName(IMMUTABLE_SET));
        sc.append("<");
        sc.append(importedName(Types.STRING));
        sc.append("> validNames() {\n");
        sc.append("    return ");
        sc.append(TypeConstants.VALID_NAMES_NAME);
        sc.append(";\n");
        sc.append("}\n");
        sc.newLine();

        //        @«OVERRIDE.importedName»
        //        public boolean[] values() {
        //            return new boolean[] {
        //                    «FOR bit : typedef.bits SEPARATOR ','»
        //                        «Naming.getPropertyName(bit.name).getterMethodName»()
        //                    «ENDFOR»
        //                };
        //        }
        sc.append("@");
        sc.append(override);
        sc.newLine();
        sc.append("public boolean[] values() {\n");
        sc.append("    return new boolean[] {\n");
        {
            boolean first = true;
            for (var bit : typedef.getBits()) {
                if (first) {
                    first = false;
                } else {
                    sc.append(",");
                }
                sc.append("            ");
                sc.append(getterMethodName(Naming.getPropertyName(bit.getName())));
                sc.append("()\n");
            }
        }
        sc.append("        };\n");
        sc.append("}\n");
        return sc;
    }

    /**
     * Template method which generates the method <code>equals()</code>.
     *
     * @return string with the <code>equals()</code> method definition in JAVA format
     */
    private CharSequence generateEquals() {
        final var equalsIdentifiers = genTO.getEqualsIdentifiers();
        if (equalsIdentifiers.isEmpty()) {
            return "";
        }

        //        @«OVERRIDE.importedName»
        //        public final boolean equals(«OBJECT.importedName» obj) {
        //            return this == obj || obj instanceof «type.simpleName» other
        //                «FOR property : genTO.equalsIdentifiers»
        //                    «val fieldName = property.fieldName»
        //                    «val type = property.returnType»
        //                    «IF type.equals(Types.primitiveBooleanType)»
        //                        && «fieldName» == other.«fieldName»«
        //                    »«ELSE»
        //                        && «type.importedUtilClass».equals(«fieldName», other.«fieldName»)«
        //                    »«ENDIF»«
        //                »«ENDFOR»;
        //        }

        final var sc = new StringConcatenation();
        sc.append("@");
        sc.append(importedName(OVERRIDE));
        sc.newLine();
        sc.append("public final boolean equals(");
        sc.append(importedName(OBJECT));
        sc.append(" obj) {\n");
        sc.append("    return this == obj || obj instanceof ");
        sc.append(type().simpleName());
        sc.append(" other\n");
        for (var property : equalsIdentifiers) {
            final var fieldName = BaseTemplate.fieldName(property);
            final var type = property.getReturnType();
            if (type.equals(Types.primitiveBooleanType())) {
                sc.append("        && ");
                sc.append(fieldName);
                sc.append(" == other.");
                sc.append(fieldName);
            } else {
                sc.append("        && ");
                sc.append(importedUtilClass(type));
                sc.append(".equals(");
                sc.append(fieldName);
                sc.append(", other.");
                sc.append(fieldName);
                sc.append(")");
            }
        }
        sc.append(";\n");
        sc.append("}\n");
        return sc;
    }

    private CharSequence generateToString(final Collection<GeneratedProperty> properties) {
        if (properties.isEmpty()) {
            return "";
        }

        //        @«OVERRIDE.importedName»
        //        public «STRING.importedName» toString() {
        //            final var helper = «MOREOBJECTS.importedName».toStringHelper(«type.importedName».class);
        //            «FOR property : properties»
        //                «CODEHELPERS.importedName».«property.valueAppender»(helper, "«property.name»",
        // «property.fieldName»);
        //            «ENDFOR»
        //            return helper.toString();
        //        }

        final var sc = new StringConcatenation();
        sc.append("@");
        sc.append(importedName(OVERRIDE));
        sc.newLine();
        sc.append("public ");
        sc.append(importedName(Types.STRING));
        sc.append(" toString() {\n");
        sc.append("    final var helper = ");
        sc.append(importedName(MOREOBJECTS));
        sc.append(".toStringHelper(");
        sc.append(importedName(type()), "    ");
        sc.append(".class);\n");
        for (var property : properties) {
            sc.append("    ");
            sc.append(importedName(CODEHELPERS));
            sc.append(".");
            sc.append(valueAppender(property));
            sc.append("(helper, \"");
            sc.append(property.getName());
            sc.append("\", ");
            sc.append(fieldName(property));
            sc.append(");\n");
        }
        sc.append("    return helper.toString();\n");
        sc.append("}\n");
        return sc;
    }

    // FIXME: this should be specialized in BitsTypeObjectTemplate
    private static String valueAppender(final GeneratedProperty prop) {
        return prop.getReturnType().equals(Types.primitiveBooleanType()) ? "appendBit" : "appendValue";
    }
}
