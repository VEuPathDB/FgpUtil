package org.gusdb.fgputil.logging;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.util.TriConsumer;
import org.apache.logging.log4j.util.StringBuilders;
import org.apache.logging.log4j.core.pattern.PatternConverter;

/**
 * This class is identical to Log4J 2.x's MdcPatternConverter EXCEPT for a
 * change to the format() method where we convert the request start time into
 * a request duration at the time this line is being logged.  Unfortunately,
 * it is a lot less efficient than the hack we used in Log4j 1.x since we need
 * to parse the stored String value back into a Long for comparison to the
 * current time.  But the feature is handy enough to warrant this cost.
 * 
 * @author rdoherty
 */
@Plugin(name = "RequestDurationPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "cmdc" })
@PerformanceSensitive("allocation")
public final class RequestDurationPatternConverter extends LogEventPatternConverter {

    /**
     * Name of property to output.
     */
    private final String key;
    private final String[] keys;
    private final boolean full;

    /**
     * Private constructor.
     *
     * @param options options, may be null.
     */
    private RequestDurationPatternConverter(final String[] options) {
        super(options != null && options.length > 0 ? "MDC{" + options[0] + '}' : "MDC", "mdc");
        if (options != null && options.length > 0) {
            full = false;
            if (options[0].indexOf(',') > 0) {
                keys = options[0].split(",");
                for (int i = 0; i < keys.length; i++) {
                    keys[i] = keys[i].trim();
                }
                key = null;
            } else {
                keys = null;
                key = options[0];
            }
        } else {
            full = true;
            key = null;
            keys = null;
        }
    }

    /**
     * Obtains an instance of PropertiesPatternConverter.
     *
     * @param options options, may be null or first element contains name of property to format.
     * @return instance of PropertiesPatternConverter.
     */
    public static RequestDurationPatternConverter newInstance(final String[] options) {
        return new RequestDurationPatternConverter(options);
    }

    private static final TriConsumer<String, Object, StringBuilder> WRITE_KEY_VALUES_INTO = new TriConsumer<String, Object, StringBuilder>() {
        @Override
        public void accept(final String key, final Object value, final StringBuilder sb) {
            sb.append(key).append('=');
            StringBuilders.appendValue(sb, value);
            sb.append(", ");
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        final ReadOnlyStringMap contextData = event.getContextData();
        // if there is no additional options, we output every single
        // Key/Value pair for the MDC in a similar format to Hashtable.toString()
        if (full) {
            if (contextData == null || contextData.size() == 0) {
                toAppendTo.append("{}");
                return;
            }
            appendFully(contextData, toAppendTo);
        } else {
            if (keys != null) {
                if (contextData == null || contextData.size() == 0) {
                    toAppendTo.append("{}");
                    return;
                }
                appendSelectedKeys(keys, contextData, toAppendTo);
            } else if (contextData != null){
                // otherwise they just want a single key output
                final Object value = contextData.getValue(key);
                if (value != null) {
                    //#####################################################################
                    // NOTE!!!  This is the only change to log4j2's MdcPatternConverter!
                    //#####################################################################
                    StringBuilders.appendValue(toAppendTo, getRequestDuration(value));
                }
            }
        }
    }

    /**
     * Converts the object passed to a String first, then tries to convert to a
     * Long for comparison against the current system time (millisecs) in order
     * to calculate the duration from a start time to now.  After subtraction,
     * converts back to a String and appends "ms" before returning. 
     * 
     * @param requestStartTime (String) object stored in MDC as a start time (should be convertable to Long)
     * @return the duration between the start time and "now" in milliseconds (with "ms" suffix)
     */
    public static String getRequestDuration(Object requestStartTime) {
      if (requestStartTime == null) {
        return "N/A";
      }
      try {
        return (System.currentTimeMillis() - Long.valueOf(requestStartTime.toString())) + "ms";
      }
      catch (NumberFormatException e) {
        return "N/A";
      }
    }

    private static void appendFully(final ReadOnlyStringMap contextData, final StringBuilder toAppendTo) {
        toAppendTo.append("{");
        final int start = toAppendTo.length();
        contextData.forEach(WRITE_KEY_VALUES_INTO, toAppendTo);
        final int end = toAppendTo.length();
        if (end > start) {
            toAppendTo.setCharAt(end - 2, '}');
            toAppendTo.deleteCharAt(end - 1);
        } else {
            toAppendTo.append('}');
        }
    }

    private static void appendSelectedKeys(final String[] keys, final ReadOnlyStringMap contextData, final StringBuilder sb) {
        // Print all the keys in the array that have a value.
        final int start = sb.length();
        sb.append('{');
        for (int i = 0; i < keys.length; i++) {
            final String theKey = keys[i];
            final Object value = contextData.getValue(theKey);
            if (value != null) { // !contextData.containskey(theKey)
                if (sb.length() - start > 1) {
                    sb.append(", ");
                }
                sb.append(theKey).append('=');
                StringBuilders.appendValue(sb, value);
            }
        }
        sb.append('}');
    }
}

