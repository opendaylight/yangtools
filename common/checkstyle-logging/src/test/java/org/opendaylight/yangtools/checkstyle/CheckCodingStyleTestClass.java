package org.opendaylight.yangtools.checkstyle;

import java.util.Comparator;
import java.util.Map;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

import com.google.common.collect.Maps;
import com.google.common.collect.Lists;

public class CheckCodingStyleTestClass {

	public CheckCodingStyleTestClass() {
		Comparator<String> string = CASE_INSENSITIVE_ORDER; 
		
	   Map<String, String> map = Maps.newHashMap();
	}
}
