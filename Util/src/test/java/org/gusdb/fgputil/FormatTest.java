package org.gusdb.fgputil;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil.Style;
import org.junit.Test;

public class FormatTest {

	@Test
	public void testPrettyPrint() throws Exception {
		String NL = FormatUtil.NL;
		Map<Integer,String> emptyMap = new HashMap<>();
		Map<Integer,String> fullMap = new MapBuilder<>(1, "One").put(2, "Two")
			.put(3, "Three").put(4, "Four").put(5, "Five").toMap();
		System.out.println(new StringBuilder()
			.append(FormatUtil.prettyPrint(emptyMap, Style.SINGLE_LINE)).append(NL)
			.append(FormatUtil.prettyPrint(emptyMap, Style.MULTI_LINE)).append(NL)
			.append(FormatUtil.prettyPrint(fullMap, Style.SINGLE_LINE)).append(NL)
			.append(FormatUtil.prettyPrint(fullMap, Style.MULTI_LINE)).append(NL)
			.toString());
	}
}
