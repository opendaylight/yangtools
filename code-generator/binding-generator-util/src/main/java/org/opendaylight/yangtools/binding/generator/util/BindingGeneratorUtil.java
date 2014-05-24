/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.yangtools.sal.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;

/**
 * Contains the methods for converting strings to valid JAVA language strings
 * (package names, class names, attribute names).
 *
 *
 */
public final class BindingGeneratorUtil {

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyMMdd");
        }

        @Override
        public void set(final SimpleDateFormat value) {
            throw new UnsupportedOperationException();
        }
    };

    /**
     * Array of strings values which represents JAVA reserved words.
     */
    @Deprecated
    private static final String[] SET_VALUES = new String[] { "abstract", "assert", "boolean", "break", "byte", "case",
            "catch", "char", "class", "const", "continue", "default", "double", "do", "else", "enum", "extends",
            "false", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return",
            "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "true", "try", "void", "volatile", "while" };

    /**
     * Impossible to instantiate this class. All of the methods or attributes
     * are static.
     */
    private BindingGeneratorUtil() {
    }

    /**
     * Hash set of words which are reserved in JAVA language.
     */
    @Deprecated
    private static final Set<String> JAVA_RESERVED_WORDS = new HashSet<String>(Arrays.asList(SET_VALUES));

    /**
     * Pre-compiled replacement pattern.
     */
    private static final Pattern COLON_SLASH_SLASH = Pattern.compile("://", Pattern.LITERAL);
    private static final String QUOTED_DOT = Matcher.quoteReplacement(".");

    /**
     * Converts string <code>packageName</code> to valid JAVA package name.
     *
     * If some words of package name are digits of JAVA reserved words they are
     * prefixed with underscore character.
     *
     * @param packageName
     *            string which contains words separated by point.
     * @return package name which contains words separated by point.
     */
    private static String validateJavaPackage(final String packageName) {
        if (packageName != null) {
            final String[] packNameParts = packageName.toLowerCase().split("\\.");
            if (packNameParts != null) {
                final StringBuilder builder = new StringBuilder();
                for (int i = 0; i < packNameParts.length; ++i) {
                    final String packNamePart = packNameParts[i];
                    if (Character.isDigit(packNamePart.charAt(0))) {
                        packNameParts[i] = "_" + packNamePart;
                    } else if (JAVA_RESERVED_WORDS.contains(packNamePart)) {
                        packNameParts[i] = "_" + packNamePart;
                    }
                    if (i > 0) {
                        builder.append(".");
                    }
                    builder.append(packNameParts[i]);
                }
                return builder.toString();
            }
        }
        return packageName;
    }

    /**
     * Converts <code>parameterName</code> to valid JAVA parameter name.
     *
     * If the <code>parameterName</code> is one of the JAVA reserved words then
     * it is prefixed with underscore character.
     *
     * @param parameterName
     *            string with the parameter name
     * @return string with the admissible parameter name
     */
    public static String resolveJavaReservedWordEquivalency(final String parameterName) {
        if (parameterName != null && JAVA_RESERVED_WORDS.contains(parameterName)) {
            return "_" + parameterName;
        }
        return parameterName;
    }

    /**
     * Converts module name to valid JAVA package name.
     *
     * The package name consists of:
     * <ul>
     * <li>prefix - <i>org.opendaylight.yang.gen.v</i></li>
     * <li>module YANG version - <i>org.opendaylight.yang.gen.v</i></li>
     * <li>module namespace - invalid characters are replaced with dots</li>
     * <li>revision prefix - <i>.rev</i></li>
     * <li>revision - YYYYMMDD (MM and DD aren't spread to the whole length)</li>
     * </ul>
     *
     * @param module
     *            module which contains data about namespace and revision date
     * @return string with the valid JAVA package name
     * @throws IllegalArgumentException
     *             if the revision date of the <code>module</code> equals
     *             <code>null</code>
     */
    public static String moduleNamespaceToPackageName(final Module module) {
        final StringBuilder packageNameBuilder = new StringBuilder();

        if (module.getRevision() == null) {
            throw new IllegalArgumentException("Module " + module.getName() + " does not specify revision date!");
        }
        packageNameBuilder.append("org.opendaylight.yang.gen.v");
        packageNameBuilder.append(module.getYangVersion());
        packageNameBuilder.append('.');

        String namespace = module.getNamespace().toString();
        namespace = COLON_SLASH_SLASH.matcher(namespace).replaceAll(QUOTED_DOT);

        // FIXME: improve these
        namespace = namespace.replace("/", ".");
        namespace = namespace.replace(":", ".");
        namespace = namespace.replace("-", ".");
        namespace = namespace.replace("@", ".");
        namespace = namespace.replace("$", ".");
        namespace = namespace.replace("#", ".");
        namespace = namespace.replace("'", ".");
        namespace = namespace.replace("*", ".");
        namespace = namespace.replace("+", ".");
        namespace = namespace.replace(",", ".");
        namespace = namespace.replace(";", ".");
        namespace = namespace.replace("=", ".");

        packageNameBuilder.append(namespace);
        packageNameBuilder.append(".rev");
        packageNameBuilder.append(DATE_FORMAT.get().format(module.getRevision()));

        return validateJavaPackage(packageNameBuilder.toString());
    }

    public static String packageNameForGeneratedType(final String basePackageName, final SchemaPath schemaPath) {
        return packageNameForGeneratedType(basePackageName, schemaPath, false);
    }

    /**
     * Creates package name from specified <code>basePackageName</code> (package
     * name for module) and <code>schemaPath</code>.
     *
     * Resulting package name is concatenation of <code>basePackageName</code>
     * and all local names of YANG nodes which are parents of some node for
     * which <code>schemaPath</code> is specified.
     *
     * @param basePackageName
     *            string with package name of the module
     * @param schemaPath
     *            list of names of YANG nodes which are parents of some node +
     *            name of this node
     * @return string with valid JAVA package name
     */
    public static String packageNameForGeneratedType(final String basePackageName, final SchemaPath schemaPath,
            final boolean isUsesAugment) {
        if (basePackageName == null) {
            throw new IllegalArgumentException("Base Package Name cannot be NULL!");
        }
        if (schemaPath == null) {
            throw new IllegalArgumentException("Schema Path cannot be NULL!");
        }

        final StringBuilder builder = new StringBuilder();
        builder.append(basePackageName);
        final List<QName> pathToNode = schemaPath.getPath();
        final int traversalSteps;
        if (isUsesAugment) {
            traversalSteps = (pathToNode.size());
        } else {
            traversalSteps = (pathToNode.size() - 1);
        }
        for (int i = 0; i < traversalSteps; ++i) {
            builder.append(".");
            String nodeLocalName = pathToNode.get(i).getLocalName();

            nodeLocalName = nodeLocalName.replace(":", ".");
            nodeLocalName = nodeLocalName.replace("-", ".");
            builder.append(nodeLocalName);
        }
        return validateJavaPackage(builder.toString());
    }

    /**
     * Generates the package name for type definition from
     * <code>typeDefinition</code> and <code>basePackageName</code>.
     *
     * @param basePackageName
     *            string with the package name of the module
     * @param typeDefinition
     *            type definition for which the package name will be generated *
     * @return string with valid JAVA package name
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> equals <code>null</code></li>
     *             <li>if <code>typeDefinition</code> equals <code>null</code></li>
     *             </ul>
     */
    public static String packageNameForTypeDefinition(final String basePackageName,
            final TypeDefinition<?> typeDefinition) {
        if (basePackageName == null) {
            throw new IllegalArgumentException("Base Package Name cannot be NULL!");
        }
        if (typeDefinition == null) {
            throw new IllegalArgumentException("Type Definition reference cannot be NULL!");
        }

        final StringBuilder builder = new StringBuilder();
        builder.append(basePackageName);
        return validateJavaPackage(builder.toString());
    }

    /**
     * Converts <code>token</code> to string which is in accordance with best
     * practices for JAVA class names.
     *
     * @param token
     *            string which contains characters which should be converted to
     *            JAVA class name
     * @return string which is in accordance with best practices for JAVA class
     *         name.
     *
     * @deprecated Use {@link BindingMapping#getClassName(QName)} instead.
     */
    @Deprecated
    public static String parseToClassName(final String token) {
        return parseToCamelCase(token, true);
    }

    /**
     * Converts <code>token</code> to string which is in accordance with best
     * practices for JAVA parameter names.
     *
     * @param token
     *            string which contains characters which should be converted to
     *            JAVA parameter name
     * @return string which is in accordance with best practices for JAVA
     *         parameter name.
     */
    public static String parseToValidParamName(final String token) {
        return resolveJavaReservedWordEquivalency(parseToCamelCase(token, false));
    }

    /**
     *
     * Converts string <code>token</code> to the cammel case format.
     *
     * @param token
     *            string which should be converted to the cammel case format
     * @param uppercase
     *            boolean value which says whether the first character of the
     *            <code>token</code> should|shuldn't be uppercased
     * @return string in the cammel case format
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>token</code> without white spaces is empty</li>
     *             <li>if <code>token</code> equals null</li>
     *             </ul>
     */

    private static String parseToCamelCase(final String token, final boolean uppercase) {
        if (token == null) {
            throw new IllegalArgumentException("Name can not be null");
        }

        String correctStr = token.trim();
        correctStr = correctStr.replace(".", "");

        if (correctStr.isEmpty()) {
            throw new IllegalArgumentException("Name can not be emty");
        }

        correctStr = replaceWithCamelCase(correctStr, ' ');
        correctStr = replaceWithCamelCase(correctStr, '-');
        correctStr = replaceWithCamelCase(correctStr, '_');

        String firstChar = correctStr.substring(0, 1);
        if (uppercase) {
            firstChar = firstChar.toUpperCase();
        } else {
            firstChar = firstChar.toLowerCase();
        }

        if (firstChar.matches("[0-9]")) {
            correctStr = "_" + correctStr;
        } else {
            correctStr = firstChar + correctStr.substring(1);
        }
        return correctStr;
    }

    /**
     * Replaces all the occurances of the <code>removalChar</code> in the
     * <code>text</code> with empty string and converts following character to
     * upper case.
     *
     * @param text
     *            string with source text which should be converted
     * @param removalChar
     *            character which is sought in the <code>text</code>
     * @return string which doesn't contain <code>removalChar</code> and has
     *         following characters converted to upper case
     * @throws IllegalArgumentException
     *             if the length of the returning string has length 0
     */
    private static String replaceWithCamelCase(final String text, final char removalChar) {
        StringBuilder sb = new StringBuilder(text);
        String toBeRemoved = String.valueOf(removalChar);

        int toBeRemovedPos = sb.indexOf(toBeRemoved);
        while (toBeRemovedPos != -1) {
            sb.replace(toBeRemovedPos, toBeRemovedPos + 1, "");
            // check if 'toBeRemoved' character is not the only character in
            // 'text'
            if (sb.length() == 0) {
                throw new IllegalArgumentException("The resulting string can not be empty");
            }
            String replacement = String.valueOf(sb.charAt(toBeRemovedPos)).toUpperCase();
            sb.setCharAt(toBeRemovedPos, replacement.charAt(0));
            toBeRemovedPos = sb.indexOf(toBeRemoved);
        }
        return sb.toString();
    }

    public static long computeDefaultSUID(final GeneratedTOBuilderImpl to) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);

            dout.writeUTF(to.getName());
            dout.writeInt(to.isAbstract() ? 3 : 7);

            List<Type> impl = to.getImplementsTypes();
            Collections.sort(impl, new Comparator<Type>() {
                @Override
                public int compare(final Type o1, final Type o2) {
                    return o1.getFullyQualifiedName().compareTo(o2.getFullyQualifiedName());
                }
            });
            for (Type ifc : impl) {
                dout.writeUTF(ifc.getFullyQualifiedName());
            }

            Comparator<TypeMemberBuilder<?>> comparator = new Comparator<TypeMemberBuilder<?>>() {
                @Override
                public int compare(final TypeMemberBuilder<?> o1, final TypeMemberBuilder<?> o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            };

            List<GeneratedPropertyBuilder> props = to.getProperties();
            Collections.sort(props, comparator);
            for (GeneratedPropertyBuilder gp : props) {
                dout.writeUTF(gp.getName());
            }

            List<MethodSignatureBuilder> methods = to.getMethodDefinitions();
            Collections.sort(methods, comparator);
            for (MethodSignatureBuilder m : methods) {
                if (!(m.getAccessModifier().equals(AccessModifier.PRIVATE))) {
                    dout.writeUTF(m.getName());
                    dout.write(m.getAccessModifier().ordinal());
                }
            }

            dout.flush();

            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] hashBytes = md.digest(bout.toByteArray());
            long hash = 0;
            for (int i = Math.min(hashBytes.length, 8) - 1; i >= 0; i--) {
                hash = (hash << 8) | (hashBytes[i] & 0xFF);
            }
            return hash;
        } catch (IOException ex) {
            throw new InternalError();
        } catch (NoSuchAlgorithmException ex) {
            throw new SecurityException(ex.getMessage());
        }
    }

    public static Restrictions getRestrictions(final TypeDefinition<?> type) {
        final List<LengthConstraint> length = new ArrayList<>();
        final List<PatternConstraint> pattern = new ArrayList<>();
        final List<RangeConstraint> range = new ArrayList<>();

        if (type instanceof ExtendedType) {
            ExtendedType ext = (ExtendedType)type;
            TypeDefinition<?> base = ext.getBaseType();
            length.addAll(ext.getLengthConstraints());
            pattern.addAll(ext.getPatternConstraints());
            range.addAll(ext.getRangeConstraints());

            if (base instanceof IntegerTypeDefinition && range.isEmpty()) {
                range.addAll(((IntegerTypeDefinition)base).getRangeConstraints());
            } else if (base instanceof UnsignedIntegerTypeDefinition && range.isEmpty()) {
                range.addAll(((UnsignedIntegerTypeDefinition)base).getRangeConstraints());
            } else if (base instanceof DecimalTypeDefinition && range.isEmpty()) {
                range.addAll(((DecimalTypeDefinition)base).getRangeConstraints());
            }

        }

        return new Restrictions() {
            @Override
            public List<RangeConstraint> getRangeConstraints() {
                return range;
            }
            @Override
            public List<PatternConstraint> getPatternConstraints() {
                return pattern;
            }
            @Override
            public List<LengthConstraint> getLengthConstraints() {
                return length;
            }
            @Override
            public boolean isEmpty() {
                return range.isEmpty() && pattern.isEmpty() && length.isEmpty();
            }
        };
    }

}
