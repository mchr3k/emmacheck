package org.emmacheck;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CommandLine
{
  /**
   * @param argsW
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException
  {
    Map<String,Integer> targetCoverage = new HashMap<String, Integer>();
    targetCoverage.put("org.intrace.agent.AgentSettings.java", 100);
    targetCoverage.put("org.intrace.agent.ClassAnalysis", 100);
    
    File covFile = new File("coverage.emma");
    File metaFile = new File("metadata.emma");
    EmmaCheck coverage = new EmmaCheck(covFile, metaFile, targetCoverage);
    coverage.getFailedRequirements();
  }
}
