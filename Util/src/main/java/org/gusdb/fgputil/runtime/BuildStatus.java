package org.gusdb.fgputil.runtime;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;

public class BuildStatus {

  public static String getLatestBuildStatus() {
    String statusFile = Paths.get(GusHome.getGusHome(), ".buildlog", "git_status").toAbsolutePath().toString();
    List<Map<String,String>> records = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(statusFile))) {
      Map<String,String> buildRecord = new LinkedHashMap<>();
      while (br.ready()) {
        String line = br.readLine().trim();
        if (line.isEmpty()) {
          if (!buildRecord.isEmpty()) {
            records.add(buildRecord);
            buildRecord = new LinkedHashMap<>();
          }
        }
        else {
          String[] tokens = line.split("\\s+", 2);
          buildRecord.put(tokens[0], tokens.length > 1 ? tokens[1] : "");
        }
      }
      if (!buildRecord.isEmpty()) {
        records.add(buildRecord);
      }
    }
    catch (IOException e) {
      throw new RuntimeException("Could not query build status", e);
    }
    Map<String,Map<String,String>> lastBuildRecords = new HashMap<>();
    for (Map<String,String> record : records) {
      lastBuildRecords.put(record.get("Project:")+"."+record.get("Component:"), record);
    }
    List<String> sortedKeys = new ArrayList<>(lastBuildRecords.keySet());
    Collections.sort(sortedKeys);
    return sortedKeys.stream()
      .map(key -> FormatUtil.prettyPrint(lastBuildRecords.get(key), Style.MULTI_LINE))
      .collect(Collectors.joining("\n"));
  }
}
