/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.harmony.pack200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * This class provides the binding between the standard Pack200 interface and the
 * internal interface for (un)packing. As this uses generics for the SortedMap,
 * this class must be compiled and run on a Java 1.5 system. However, Java 1.5
 * is not necessary to use the internal libraries for unpacking.
 */
public class Pack200PackerAdapter extends Pack200Adapter {

  String SEGMENT_LIMIT    = "pack.segment.limit";

  /**
   * If this property is set to {@link #TRUE}, the packer will transmit
   * all elements in their original order within the source archive.
   * <p>
   * If it is set to {@link #FALSE}, the packer may reorder elements,
   * and also remove JAR directory entries, which carry no useful
   * information for Java applications.
   * (Typically this enables better compression.)
   * <p>
   * The default is {@link #TRUE}, which preserves the input information,
   * but may cause the transmitted archive to be larger than necessary.
   */
  String KEEP_FILE_ORDER = "pack.keep.file.order";


  /**
   * If this property is set to a single decimal digit, the packer will
   * use the indicated amount of effort in compressing the archive.
   * Level 1 may produce somewhat larger size and faster compression speed,
   * while level 9 will take much longer but may produce better compression.
   * <p>
   * The special value 0 instructs the packer to copy through the
   * original JAR file directly, with no compression.  The JSR 200
   * standard requires any unpacker to understand this special case
   * as a pass-through of the entire archive.
   * <p>
   * The default is 5, investing a modest amount of time to
   * produce reasonable compression.
   */
  String EFFORT           = "pack.effort";

  /**
   * If this property is set to {@link #TRUE} or {@link #FALSE}, the packer
   * will set the deflation hint accordingly in the output archive, and
   * will not transmit the individual deflation hints of archive elements.
   * <p>
   * If this property is set to the special string {@link #KEEP}, the packer
   * will attempt to determine an independent deflation hint for each
   * available element of the input archive, and transmit this hint separately.
   * <p>
   * The default is {@link #KEEP}, which preserves the input information,
   * but may cause the transmitted archive to be larger than necessary.
   * <p>
   * It is up to the unpacker implementation
   * to take action upon the hint to suitably compress the elements of
   * the resulting unpacked jar.
   * <p>
   * The deflation hint of a ZIP or JAR element indicates
   * whether the element was deflated or stored directly.
   */
  String DEFLATE_HINT     = "pack.deflate.hint";

  /**
   * If this property is set to the special string {@link #LATEST},
   * the packer will attempt to determine the latest modification time,
   * among all the available entries in the original archive or the latest
   * modification time of all the available entries in each segment.
   * This single value will be transmitted as part of the segment and applied
   * to all the entries in each segment, {@link #SEGMENT_LIMIT}.
   * <p>
   * This can marginally decrease the transmitted size of the
   * archive, at the expense of setting all installed files to a single
   * date.
   * <p>
   * If this property is set to the special string {@link #KEEP},
   * the packer transmits a separate modification time for each input
   * element.
   * <p>
   * The default is {@link #KEEP}, which preserves the input information,
   * but may cause the transmitted archive to be larger than necessary.
   * <p>
   * It is up to the unpacker implementation to take action to suitably
   * set the modification time of each element of its output file.
   * @see #SEGMENT_LIMIT
   */
  String MODIFICATION_TIME        = "pack.modification.time";

  /**
   * Indicates that a file should be passed through bytewise, with no
   * compression.  Multiple files may be specified by specifying
   * additional properties with distinct strings appended, to
   * make a family of properties with the common prefix.
   * <p>
   * There is no pathname transformation, except
   * that the system file separator is replaced by the JAR file
   * separator '/'.
   * <p>
   * The resulting file names must match exactly as strings with their
   * occurrences in the JAR file.
   * <p>
   * If a property value is a directory name, all files under that
   * directory will be passed also.
   * <p>
   * Examples:
   * <pre>{@code
   *     Map p = packer.properties();
   *     p.put(PASS_FILE_PFX+0, "mutants/Rogue.class");
   *     p.put(PASS_FILE_PFX+1, "mutants/Wolverine.class");
   *     p.put(PASS_FILE_PFX+2, "mutants/Storm.class");
   *     # Pass all files in an entire directory hierarchy:
   *     p.put(PASS_FILE_PFX+3, "police/");
   * }</pre>
   */
  String PASS_FILE_PFX            = "pack.pass.file.";

  /// Attribute control.

  /**
   * Indicates the action to take when a class-file containing an unknown
   * attribute is encountered.  Possible values are the strings {@link #ERROR},
   * {@link #STRIP}, and {@link #PASS}.
   * <p>
   * The string {@link #ERROR} means that the pack operation
   * as a whole will fail, with an exception of type <code>IOException</code>.
   * The string
   * {@link #STRIP} means that the attribute will be dropped.
   * The string
   * {@link #PASS} means that the whole class-file will be passed through
   * (as if it were a resource file) without compression, with  a suitable warning.
   * This is the default value for this property.
   * <p>
   * Examples:
   * <pre>{@code
   *     Map p = pack200.getProperties();
   *     p.put(UNKNOWN_ATTRIBUTE, ERROR);
   *     p.put(UNKNOWN_ATTRIBUTE, STRIP);
   *     p.put(UNKNOWN_ATTRIBUTE, PASS);
   * }</pre>
   */
  String UNKNOWN_ATTRIBUTE        = "pack.unknown.attribute";

  /**
   * When concatenated with a class attribute name,
   * indicates the format of that attribute,
   * using the layout language specified in the JSR 200 specification.
   * <p>
   * For example, the effect of this option is built in:
   * <code>pack.class.attribute.SourceFile=RUH</code>.
   * <p>
   * The special strings {@link #ERROR}, {@link #STRIP}, and {@link #PASS} are
   * also allowed, with the same meaning as {@link #UNKNOWN_ATTRIBUTE}.
   * This provides a way for users to request that specific attributes be
   * refused, stripped, or passed bitwise (with no class compression).
   * <p>
   * Code like this might be used to support attributes for JCOV:
   * <pre><code>
   *     Map p = packer.properties();
   *     p.put(CODE_ATTRIBUTE_PFX+"CoverageTable",       "NH[PHHII]");
   *     p.put(CODE_ATTRIBUTE_PFX+"CharacterRangeTable", "NH[PHPOHIIH]");
   *     p.put(CLASS_ATTRIBUTE_PFX+"SourceID",           "RUH");
   *     p.put(CLASS_ATTRIBUTE_PFX+"CompilationID",      "RUH");
   * </code></pre>
   * <p>
   * Code like this might be used to strip debugging attributes:
   * <pre><code>
   *     Map p = packer.properties();
   *     p.put(CODE_ATTRIBUTE_PFX+"LineNumberTable",    STRIP);
   *     p.put(CODE_ATTRIBUTE_PFX+"LocalVariableTable", STRIP);
   *     p.put(CLASS_ATTRIBUTE_PFX+"SourceFile",        STRIP);
   * </code></pre>
   */
  String CLASS_ATTRIBUTE_PFX      = "pack.class.attribute.";

  /**
   * When concatenated with a field attribute name,
   * indicates the format of that attribute.
   * For example, the effect of this option is built in:
   * <code>pack.field.attribute.Deprecated=</code>.
   * The special strings {@link #ERROR}, {@link #STRIP}, and
   * {@link #PASS} are also allowed.
   * @see #CLASS_ATTRIBUTE_PFX
   */
  String FIELD_ATTRIBUTE_PFX      = "pack.field.attribute.";

  /**
   * When concatenated with a method attribute name,
   * indicates the format of that attribute.
   * For example, the effect of this option is built in:
   * <code>pack.method.attribute.Exceptions=NH[RCH]</code>.
   * The special strings {@link #ERROR}, {@link #STRIP}, and {@link #PASS}
   * are also allowed.
   * @see #CLASS_ATTRIBUTE_PFX
   */
  String METHOD_ATTRIBUTE_PFX     = "pack.method.attribute.";

  /**
   * When concatenated with a code attribute name,
   * indicates the format of that attribute.
   * For example, the effect of this option is built in:
   * <code>pack.code.attribute.LocalVariableTable=NH[PHOHRUHRSHH]</code>.
   * The special strings {@link #ERROR}, {@link #STRIP}, and {@link #PASS}
   * are also allowed.
   * @see #CLASS_ATTRIBUTE_PFX
   */
  String CODE_ATTRIBUTE_PFX       = "pack.code.attribute.";

  /**
   * The unpacker's progress as a percentage, as periodically
   * updated by the unpacker.
   * Values of 0 - 100 are normal, and -1 indicates a stall.
   * Progress can be monitored by polling the value of this
   * property.
   * <p>
   * At a minimum, the unpacker must set progress to 0
   * at the beginning of a packing operation, and to 100
   * at the end.
   */
  String PROGRESS                 = "pack.progress";

  /** The string "keep", a possible value for certain properties.
   * @see #DEFLATE_HINT
   * @see #MODIFICATION_TIME
   */
  String KEEP  = "keep";

  /** The string "pass", a possible value for certain properties.
   * @see #UNKNOWN_ATTRIBUTE
   * @see #CLASS_ATTRIBUTE_PFX
   * @see #FIELD_ATTRIBUTE_PFX
   * @see #METHOD_ATTRIBUTE_PFX
   * @see #CODE_ATTRIBUTE_PFX
   */
  String PASS  = "pass";

  /** The string "strip", a possible value for certain properties.
   * @see #UNKNOWN_ATTRIBUTE
   * @see #CLASS_ATTRIBUTE_PFX
   * @see #FIELD_ATTRIBUTE_PFX
   * @see #METHOD_ATTRIBUTE_PFX
   * @see #CODE_ATTRIBUTE_PFX
   */
  String STRIP = "strip";

  /** The string "error", a possible value for certain properties.
   * @see #UNKNOWN_ATTRIBUTE
   * @see #CLASS_ATTRIBUTE_PFX
   * @see #FIELD_ATTRIBUTE_PFX
   * @see #METHOD_ATTRIBUTE_PFX
   * @see #CODE_ATTRIBUTE_PFX
   */
  String ERROR = "error";

  /** The string "true", a possible value for certain properties.
   * @see #KEEP_FILE_ORDER
   * @see #DEFLATE_HINT
   */
  String TRUE = "true";

  /** The string "false", a possible value for certain properties.
   * @see #KEEP_FILE_ORDER
   * @see #DEFLATE_HINT
   */
  String FALSE = "false";

  /** The string "latest", a possible value for certain properties.
   * @see #MODIFICATION_TIME
   */
  String LATEST = "latest";
  
    private final PackingOptions options = new PackingOptions();

    public void pack(JarFile file, OutputStream out) throws IOException {
        if (file == null || out == null)
            throw new IllegalArgumentException(
                    "Must specify both input and output streams");
        completed(0);
        try {
            new org.apache.harmony.pack200.Archive(file, out, options).pack();
        } catch (Pack200Exception e) {
            throw new IOException("Failed to pack Jar:" + String.valueOf(e));
        }
        completed(1);
    }

    public void pack(JarInputStream in, OutputStream out) throws IOException {
        if (in == null || out == null)
            throw new IllegalArgumentException(
                    "Must specify both input and output streams");
        completed(0);
        PackingOptions options = new PackingOptions();

        try {
            new org.apache.harmony.pack200.Archive(in, out, options).pack();
        } catch (Pack200Exception e) {
            throw new IOException("Failed to pack Jar:" + String.valueOf(e));
        }
        completed(1);
        in.close();
    }

    protected void firePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
        if(newValue != null && !newValue.equals(oldValue)) {
            if (propertyName.startsWith(CLASS_ATTRIBUTE_PFX)) {
                String attributeName = propertyName.substring(CLASS_ATTRIBUTE_PFX.length());
                options.addClassAttributeAction(attributeName, (String)newValue);
            } else if (propertyName.startsWith(CODE_ATTRIBUTE_PFX)) {
                String attributeName = propertyName.substring(CODE_ATTRIBUTE_PFX.length());
                options.addCodeAttributeAction(attributeName, (String)newValue);
            } else if (propertyName.equals(DEFLATE_HINT)) {
                options.setDeflateHint((String) newValue);
            } else if (propertyName.equals(EFFORT)) {
                options.setEffort(Integer.parseInt((String)newValue));
            } else if (propertyName.startsWith(FIELD_ATTRIBUTE_PFX)) {
                String attributeName = propertyName.substring(FIELD_ATTRIBUTE_PFX.length());
                options.addFieldAttributeAction(attributeName, (String)newValue);
            } else if (propertyName.equals(KEEP_FILE_ORDER)) {
                options.setKeepFileOrder(Boolean.parseBoolean((String)newValue));
            } else if (propertyName.startsWith(METHOD_ATTRIBUTE_PFX)) {
                String attributeName = propertyName.substring(METHOD_ATTRIBUTE_PFX.length());
                options.addMethodAttributeAction(attributeName, (String)newValue);
            } else if (propertyName.equals(MODIFICATION_TIME)) {
                options.setModificationTime((String)newValue);
            } else if (propertyName.startsWith(PASS_FILE_PFX)) {
                if(oldValue != null && !oldValue.equals("")) {
                    options.removePassFile((String)oldValue);
                }
                options.addPassFile((String) newValue);
            } else if (propertyName.equals(SEGMENT_LIMIT)) {
                options.setSegmentLimit(Long.parseLong((String)newValue));
            } else if (propertyName.equals(UNKNOWN_ATTRIBUTE)) {
                options.setUnknownAttributeAction((String)newValue);
            }
        }
    }

}
