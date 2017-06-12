/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.util;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for converting YANG XSD regexes into Java-compatible regexes.
 */
public final class RegexUtils {
    private static final Logger LOG = LoggerFactory.getLogger(RegexUtils.class);
    private static final Pattern BETWEEN_CURLY_BRACES_PATTERN = Pattern.compile("\\{(.+?)\\}");
    private static final Set<String> JAVA_UNICODE_BLOCKS = ImmutableSet.<String>builder()
            .add("AegeanNumbers")
            .add("AlchemicalSymbols")
            .add("AlphabeticPresentationForms")
            .add("AncientGreekMusicalNotation")
            .add("AncientGreekNumbers")
            .add("AncientSymbols")
            .add("Arabic")
            .add("ArabicPresentationForms-A")
            .add("ArabicPresentationForms-B")
            .add("ArabicSupplement")
            .add("Armenian")
            .add("Arrows")
            .add("Avestan")
            .add("Balinese")
            .add("Bamum")
            .add("BamumSupplement")
            .add("BasicLatin")
            .add("Batak")
            .add("Bengali")
            .add("BlockElements")
            .add("Bopomofo")
            .add("BopomofoExtended")
            .add("BoxDrawing")
            .add("Brahmi")
            .add("BraillePatterns")
            .add("Buginese")
            .add("Buhid")
            .add("ByzantineMusicalSymbols")
            .add("Carian")
            .add("Cham")
            .add("Cherokee")
            .add("CJKCompatibility")
            .add("CJKCompatibilityForms")
            .add("CJKCompatibilityIdeographs")
            .add("CJKCompatibilityIdeographsSupplement")
            .add("CJKRadicalsSupplement")
            .add("CJKStrokes")
            .add("CJKSymbolsandPunctuation")
            .add("CJKUnifiedIdeographs")
            .add("CJKUnifiedIdeographsExtensionA")
            .add("CJKUnifiedIdeographsExtensionB")
            .add("CJKUnifiedIdeographsExtensionC")
            .add("CJKUnifiedIdeographsExtensionD")
            .add("CombiningDiacriticalMarks")
            .add("CombiningDiacriticalMarksSupplement")
            .add("CombiningHalfMarks")
            .add("CombiningDiacriticalMarksforSymbols")
            .add("CommonIndicNumberForms")
            .add("ControlPictures")
            .add("Coptic")
            .add("CountingRodNumerals")
            .add("Cuneiform")
            .add("CuneiformNumbersandPunctuation")
            .add("CurrencySymbols")
            .add("CypriotSyllabary")
            .add("Cyrillic")
            .add("CyrillicExtended-A")
            .add("CyrillicExtended-B")
            .add("CyrillicSupplementary")
            .add("Deseret")
            .add("Devanagari")
            .add("DevanagariExtended")
            .add("Dingbats")
            .add("DominoTiles")
            .add("EgyptianHieroglyphs")
            .add("Emoticons")
            .add("EnclosedAlphanumericSupplement")
            .add("EnclosedAlphanumerics")
            .add("EnclosedCJKLettersandMonths")
            .add("EnclosedIdeographicSupplement")
            .add("Ethiopic")
            .add("EthiopicExtended")
            .add("EthiopicExtended-A")
            .add("EthiopicSupplement")
            .add("GeneralPunctuation")
            .add("GeometricShapes")
            .add("Georgian")
            .add("GeorgianSupplement")
            .add("Glagolitic")
            .add("Gothic")
            .add("GreekandCoptic")
            .add("GreekExtended")
            .add("Gujarati")
            .add("Gurmukhi")
            .add("HalfwidthandFullwidthForms")
            .add("HangulCompatibilityJamo")
            .add("HangulJamo")
            .add("HangulJamoExtended-A")
            .add("HangulJamoExtended-B")
            .add("HangulSyllables")
            .add("Hanunoo")
            .add("Hebrew")
            .add("HighPrivateUseSurrogates")
            .add("HighSurrogates")
            .add("Hiragana")
            .add("IdeographicDescriptionCharacters")
            .add("ImperialAramaic")
            .add("InscriptionalPahlavi")
            .add("InscriptionalParthian")
            .add("IPAExtensions")
            .add("Javanese")
            .add("Kaithi")
            .add("KanaSupplement")
            .add("Kanbun")
            .add("Kangxi Radicals")
            .add("Kannada")
            .add("Katakana")
            .add("KatakanaPhoneticExtensions")
            .add("KayahLi")
            .add("Kharoshthi")
            .add("Khmer")
            .add("KhmerSymbols")
            .add("Lao")
            .add("Latin-1Supplement")
            .add("LatinExtended-A")
            .add("LatinExtendedAdditional")
            .add("LatinExtended-B")
            .add("LatinExtended-C")
            .add("LatinExtended-D")
            .add("Lepcha")
            .add("LetterlikeSymbols")
            .add("Limbu")
            .add("LinearBIdeograms")
            .add("LinearBSyllabary")
            .add("Lisu")
            .add("LowSurrogates")
            .add("Lycian")
            .add("Lydian")
            .add("MahjongTiles")
            .add("Malayalam")
            .add("Mandaic")
            .add("MathematicalAlphanumericSymbols")
            .add("MathematicalOperators")
            .add("MeeteiMayek")
            .add("MiscellaneousMathematicalSymbols-A")
            .add("MiscellaneousMathematicalSymbols-B")
            .add("MiscellaneousSymbols")
            .add("MiscellaneousSymbolsandArrows")
            .add("MiscellaneousSymbolsAndPictographs")
            .add("MiscellaneousTechnical")
            .add("ModifierToneLetters")
            .add("Mongolian")
            .add("MusicalSymbols")
            .add("Myanmar")
            .add("MyanmarExtended-A")
            .add("NewTaiLue")
            .add("NKo")
            .add("NumberForms")
            .add("Ogham")
            .add("OlChiki")
            .add("OldItalic")
            .add("OldPersian")
            .add("OldSouthArabian")
            .add("OldTurkic")
            .add("OpticalCharacterRecognition")
            .add("Oriya")
            .add("Osmanya")
            .add("Phags-pa")
            .add("PhaistosDisc")
            .add("Phoenician")
            .add("PhoneticExtensions")
            .add("PhoneticExtensionsSupplement")
            .add("PlayingCards")
            .add("PrivateUseArea")
            .add("Rejang")
            .add("RumiNumeralSymbols")
            .add("Runic")
            .add("Samaritan")
            .add("Saurashtra")
            .add("Shavian")
            .add("Sinhala")
            .add("SmallFormVariants")
            .add("SpacingModifierLetters")
            .add("Specials")
            .add("Sundanese")
            .add("SuperscriptsandSubscripts")
            .add("SupplementalArrows-A")
            .add("SupplementalArrows-B")
            .add("SupplementalMathematicalOperators")
            .add("SupplementalPunctuation")
            .add("SupplementaryPrivateUseArea-A")
            .add("SupplementaryPrivateUseArea-B")
            .add("SylotiNagri")
            .add("Syriac")
            .add("Tagalog")
            .add("Tagbanwa")
            .add("Tags")
            .add("TaiLe")
            .add("TaiTham")
            .add("TaiViet")
            .add("TaiXuanJingSymbols")
            .add("Tamil")
            .add("Telugu")
            .add("Thaana")
            .add("Thai")
            .add("Tibetan")
            .add("Tifinagh")
            .add("TransportAndMapSymbols")
            .add("Ugaritic")
            .add("UnifiedCanadianAboriginalSyllabics")
            .add("UnifiedCanadianAboriginalSyllabicsExtended")
            .add("Vai")
            .add("VariationSelectors")
            .add("VariationSelectorsSupplement")
            .add("VedicExtensions")
            .add("VerticalForms")
            .add("YiRadicals")
            .add("YiSyllables")
            .add("YijingHexagramSymbols").build();

    private static final int UNICODE_SCRIPT_FIX_COUNTER = 30;

    private RegexUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated.");
    }

    /**
     * Converts XSD regex to Java-compatible regex.
     *
     * @param xsdRegex XSD regex pattern as it is defined in a YANG source
     * @return Java-compatible regex
     */
    public static String getJavaRegexFromXSD(final String xsdRegex) {
        return "^" + fixUnicodeScriptPattern(escapeChars(xsdRegex)) + '$';
    }

    /*
     * As both '^' and '$' are special anchor characters in java regular
     * expressions which are implicitly present in XSD regular expressions,
     * we need to escape them in case they are not defined as part of
     * character ranges i.e. inside regular square brackets.
     */
    private static String escapeChars(final String regex) {
        final StringBuilder result = new StringBuilder(regex.length());
        int bracket = 0;
        boolean escape = false;
        for (int i = 0; i < regex.length(); i++) {
            final char ch = regex.charAt(i);
            switch (ch) {
                case '[':
                    if (!escape) {
                        bracket++;
                    }
                    escape = false;
                    result.append(ch);
                    break;
                case ']':
                    if (!escape) {
                        bracket--;
                    }
                    escape = false;
                    result.append(ch);
                    break;
                case '\\':
                    escape = !escape;
                    result.append(ch);
                    break;
                case '^':
                case '$':
                    if (bracket == 0) {
                        result.append('\\');
                    }
                    escape = false;
                    result.append(ch);
                    break;
                default:
                    escape = false;
                    result.append(ch);
            }
        }
        return result.toString();
    }

    private static String fixUnicodeScriptPattern(String rawPattern) {
        for (int i = 0; i < UNICODE_SCRIPT_FIX_COUNTER; i++) {
            try {
                Pattern.compile(rawPattern);
                return rawPattern;
            } catch (final PatternSyntaxException ex) {
                LOG.debug("Invalid regex pattern syntax in: {}", rawPattern, ex);
                if (ex.getMessage().contains("Unknown character script name")) {
                    rawPattern = fixUnknownScripts(ex.getMessage(), rawPattern);
                } else {
                    return rawPattern;
                }
            }
        }

        LOG.warn("Regex pattern could not be fixed: {}", rawPattern);
        return rawPattern;
    }

    private static String fixUnknownScripts(final String exMessage, final String rawPattern) {
        StringBuilder result = new StringBuilder(rawPattern);
        final Matcher matcher = BETWEEN_CURLY_BRACES_PATTERN.matcher(exMessage);
        if (matcher.find()) {
            final String capturedGroup = matcher.group(1);
            if (JAVA_UNICODE_BLOCKS.contains(capturedGroup)) {
                final int idx = rawPattern.indexOf("Is" + capturedGroup);
                result = result.replace(idx, idx + 2, "In");
            }
        }
        return result.toString();
    }
}
