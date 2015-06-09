package org.opendaylight.yangtools.yang.data.codec.gson;

import static junit.framework.TestCase.assertEquals;
import org.junit.Test;

public class JsonStringEscapeTest {

    @Test
    public void replaceSpecialCharacters() throws Exception {
        /*
         * expected result in text format is :
         *
         * \b backspace \t tab \n new_line \f new_page \r carriage_return \\ backslash
         */
        final String input = "\b backspace \t tab \n new_line \f new_page \r carriage_return \\ backslash";
        final String inputCompare = "\\b backspace \\t tab \\n new_line \\f new_page \\r carriage_return \\\\ " +
                "backslash";
        final String output = JSONNormalizedNodeStreamWriter.replaceAllIllegalChars(input);
        assertEquals(inputCompare, output);
    }
}
