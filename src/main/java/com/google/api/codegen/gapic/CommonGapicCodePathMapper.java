/* Copyright 2016 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.codegen.gapic;

import com.google.api.codegen.config.ProductConfig;
import com.google.api.codegen.util.Name;
import com.google.api.codegen.util.NameFormatter;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import java.util.ArrayList;

/**
 * An implementation of GapicCodePathMapper that generates the output path from a prefix, and/or
 * package name.
 */
public class CommonGapicCodePathMapper implements GapicCodePathMapper {
  private final String prefix;
  private final boolean shouldAppendPackage;
  private final NameFormatter nameFormatter;

  private static String PACKAGE_SPLIT_REGEX = "[.:\\\\]";

  private CommonGapicCodePathMapper(
      String prefix, boolean shouldAppendPackage, NameFormatter nameFormatter) {
    this.prefix = prefix;
    this.shouldAppendPackage = shouldAppendPackage;
    this.nameFormatter = nameFormatter;
  }

  @Override
  public String getOutputPath(String elementFullName, ProductConfig config) {
    return getOutputPath(config, null);
  }

  @Override
  public String getSamplesOutputPath(
      String elementFullName, ProductConfig config, String methodName) {
    return getOutputPath(config, methodName);
  }

  private String getOutputPath(ProductConfig config, String methodSample) {
    ArrayList<String> dirs = new ArrayList<>();
    boolean haveSample = !Strings.isNullOrEmpty(methodSample);

    if (!Strings.isNullOrEmpty(prefix)) {
      dirs.add(prefix);
    }

    if (haveSample) {
      dirs.add(SAMPLES_DIRECTORY);
    }

    if (shouldAppendPackage && !Strings.isNullOrEmpty(config.getPackageName())) {
      for (String segment : config.getPackageName().split(PACKAGE_SPLIT_REGEX)) {
        // We can have empty segments in cases like Ruby where the separator
        // between path components is multiple characters ("::")
        if (!segment.isEmpty()) {
          dirs.add(format(segment));
        }
      }
    }

    if (haveSample) {
      dirs.add(format(methodSample));
    }
    return Joiner.on("/").join(dirs);
  }

  /** Formats the given segment of a file path. */
  private String format(final String segment) {
    return nameFormatter == null
        ? segment.toLowerCase()
        : nameFormatter.packageFilePathPiece(Name.upperCamel(segment));
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String prefix = "";
    private boolean shouldAppendPackage = false;
    private NameFormatter nameFormatter = null;

    public Builder setPrefix(String prefix) {
      this.prefix = prefix;
      return this;
    }

    public Builder setShouldAppendPackage(boolean shouldAppendPackage) {
      this.shouldAppendPackage = shouldAppendPackage;
      return this;
    }

    public Builder setPackageFilePathNameFormatter(NameFormatter nameFormatter) {
      this.nameFormatter = nameFormatter;
      return this;
    }

    public CommonGapicCodePathMapper build() {
      return new CommonGapicCodePathMapper(prefix, shouldAppendPackage, nameFormatter);
    }
  }
}
