package org.opendaylight.yangtools.binding.generator.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Contains the methods for converting strings to valid JAVA language strings
 * (package names, class names, attribute names).
 * 
 * 
 */
public final class BindingGeneratorUtil {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyMMdd");
    
    /**
     * Array of strings values which represents JAVA reserved words.
     */
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
    private static final Set<String> JAVA_RESERVED_WORDS = new HashSet<String>(Arrays.asList(SET_VALUES));

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
            final String[] packNameParts = packageName.split("\\.");
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
    public static String validateParameterName(final String parameterName) {
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
        packageNameBuilder.append(".");

        String namespace = module.getNamespace().toString();
        namespace = namespace.replace("://", ".");
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
        packageNameBuilder.append(DATE_FORMAT.format(module.getRevision()));
        
        return validateJavaPackage(packageNameBuilder.toString());
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
    public static String packageNameForGeneratedType(final String basePackageName, final SchemaPath schemaPath) {
        if (basePackageName == null) {
            throw new IllegalArgumentException("Base Package Name cannot be NULL!");
        }
        if (schemaPath == null) {
            throw new IllegalArgumentException("Schema Path cannot be NULL!");
        }

        final StringBuilder builder = new StringBuilder();
        builder.append(basePackageName);
        final List<QName> pathToNode = schemaPath.getPath();
        final int traversalSteps = (pathToNode.size() - 1);
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
     */
    public static String parseToClassName(String token) {
        String correctStr = token.replace(".", "");
        correctStr = parseToCamelCase(correctStr);

        // make first char upper-case
        char first = Character.toUpperCase(correctStr.charAt(0));
        if (first >= '0' && first <= '9') {

            correctStr = "_" + correctStr;
        } else {
            correctStr = first + correctStr.substring(1);
        }
        return correctStr;
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
        final String validToken = token.replace(".", "");
        String correctStr = parseToCamelCase(validToken);

        // make first char lower-case
        char first = Character.toLowerCase(correctStr.charAt(0));
        correctStr = first + correctStr.substring(1);
        return validateParameterName(correctStr);
    }

    /**
     * Converts <code>token</code> to capital letters and removes invalid
     * characters.
     * 
     * @param token
     *            string with characters which should be conversed to capital
     * @return string with capital letters
     */
    public static String convertToCapitalLetters(final String token) {
        String convertedStr = token.replace(" ", "_");
        convertedStr = convertedStr.replace(".", "_");
        convertedStr = convertedStr.toUpperCase();
        return convertedStr;
    }

    /**
     * 
     * Converts string <code>token</code> to the cammel case format.
     * 
     * @param token
     *            string which should be converted to the cammel case format
     * @return string in the cammel case format
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>token</code> without white spaces is empty</li>
     *             <li>if <code>token</code> equals null</li>
     *             </ul>
     */
    private static String parseToCamelCase(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Name can not be null");
        }

        String correctStr = token.trim();
        if (correctStr.isEmpty()) {
            throw new IllegalArgumentException("Name can not be emty");
        }

        correctStr = replaceWithCamelCase(correctStr, ' ');
        correctStr = replaceWithCamelCase(correctStr, '-');
        correctStr = replaceWithCamelCase(correctStr, '_');
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
    private static String replaceWithCamelCase(String text, char removalChar) {
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
}
