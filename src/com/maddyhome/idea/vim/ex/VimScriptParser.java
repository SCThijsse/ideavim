/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2014 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.maddyhome.idea.vim.ex;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

/**
 * @author vlan
 */
public class VimScriptParser {
  public static final String[] VIMRC_FILES = {".ideavimrc", "_ideavimrc", ".vimrc", "_vimrc"};
  public static final int BUFSIZE = 4096;
  public static final String CHARSET = "utf-8";

  private VimScriptParser() {
  }

  @Nullable
  public static File findVimrc() {
    final String homeDirName = System.getProperty("user.home");
    if (homeDirName != null) {
      for (String fileName : VIMRC_FILES) {
        final File file = new File(homeDirName, fileName);
        if (file.exists()) {
          return file;
        }
      }
    }
    return null;
  }

  public static void executeFile(@NotNull File file) {
    final String data;
    try {
      data = readFile(file);
    }
    catch (IOException ignored) {
      return;
    }
    executeText(data);
  }

  public static void executeText(@NotNull String text) {
    for (String line : StringUtil.splitByLines(text)) {
      // TODO: Build a proper parse tree for a VimL file instead of ignoring potentially nested lines (VIM-669)
      if (line.startsWith(" ") || line.startsWith("\t")) {
        continue;
      }
      if (line.startsWith(":")) {
        line = line.substring(1);
      }
      final CommandParser commandParser = CommandParser.getInstance();
      try {
        final ExCommand command = commandParser.parse(line);
        final CommandHandler commandHandler = commandParser.getCommandHandler(command);
        if (commandHandler instanceof VimScriptCommandHandler) {
          final VimScriptCommandHandler handler = (VimScriptCommandHandler)commandHandler;
          handler.execute(command);
        }
      }
      catch (ExException ignored) {
      }
    }
  }

  private static String readFile(@NotNull File file) throws IOException {
    final BufferedReader reader = new BufferedReader(new FileReader(file));
    final StringBuilder builder = new StringBuilder();
    final char[] buffer = new char[BUFSIZE];
    int n;
    while ((n = reader.read(buffer)) > 0) {
      builder.append(buffer, 0, n);
    }
    return builder.toString();
  }
}