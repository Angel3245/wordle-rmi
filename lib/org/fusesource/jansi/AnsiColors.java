/*
 * Copyright (C) 2009-2017 the original author(s).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lib.org.fusesource.jansi;

/**
 * Colors support.
 *
 * @since 2.1
 */
public enum AnsiColors {

    Colors16("16 colors"),
    Colors256("256 colors"),
    TrueColor("24-bit colors");

    private final String description;

    AnsiColors(String description) {
        this.description = description;
    }

    String getDescription() {
        return description;
    }
}
