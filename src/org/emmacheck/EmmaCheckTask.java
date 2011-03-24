package org.emmacheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class EmmaCheckTask extends Task
{
  private File coveragefile;
  private File metadatafile;
  private File requiredcoverage;
  private File outputfile;
  private String overallcoverage;

  public void setOverallcoverage(String overallcoverage)
  {
    this.overallcoverage = overallcoverage;    
  }
  
  public void setCoveragefile(File coveragefile)
  {
    this.coveragefile = coveragefile;
  }

  public void setMetadatafile(File metadatafile)
  {
    this.metadatafile = metadatafile;
  }

  public void setRequiredcoverage(File requiredcoverage)
  {
    this.requiredcoverage = requiredcoverage;
  }
  
  public void setOutput(File output)
  {
    this.outputfile = output;
  }

  @Override
  public void execute() throws BuildException
  {
    try
    {
      Properties coverageProps = new Properties();
      coverageProps.load(new FileInputStream(requiredcoverage));
      
      Map<String,CoverageTarget> requiredCov = new HashMap<String, CoverageTarget>();
      
      for (Entry<Object, Object> entry : coverageProps.entrySet())
      {
        String key = entry.getKey().toString();
        String value = entry.getValue().toString();
        
        CoverageTarget covTarget = new CoverageTarget(value);
        
        requiredCov.put(key, covTarget);
      }
      
      CoverageTarget requiredOverallCov = null;
      if (overallcoverage != null)
      {
        requiredOverallCov = new CoverageTarget(overallcoverage);
      }
      
      EmmaCheck coverage = new EmmaCheck(coveragefile, 
                                         metadatafile,
                                         requiredOverallCov,
                                         requiredCov);
      Map<String,String> failedReqs = coverage.getFailedRequirements();
    
      if (failedReqs.size() == 0)
      {
        System.out.println("Coverage requirements met.");
      }
      else
      {
        PrintWriter writer = new PrintWriter(outputfile);
        for (Entry<String,String> entry : failedReqs.entrySet())
        {
          writer.println(entry.getKey() + " : Required: " + 
                         requiredCov.get(entry.getKey()) + 
                         " % : actual : " + entry.getValue());
        }
        writer.flush();
        writer.close();
        System.out.println("Output file: " + outputfile.getAbsolutePath());
        System.out.println("Coverage requirements not met.");
        throw new BuildException("Coverage requirements not met.");
      }
    }
    catch (IOException ex)
    {
      throw new BuildException(ex);
    }
  }
}
