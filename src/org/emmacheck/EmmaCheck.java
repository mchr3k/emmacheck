package org.emmacheck;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.vladium.emma.EMMAProperties;
import com.vladium.emma.EMMARuntimeException;
import com.vladium.emma.data.DataFactory;
import com.vladium.emma.data.ICoverageData;
import com.vladium.emma.data.IMergeable;
import com.vladium.emma.data.IMetaData;
import com.vladium.emma.report.AbstractReportGenerator;
import com.vladium.emma.report.AllItem;
import com.vladium.emma.report.IItem;
import com.vladium.emma.report.IItemAttribute;
import com.vladium.emma.report.PackageItem;
import com.vladium.emma.report.SourcePathCache;
import com.vladium.emma.report.SrcFileItem;
import com.vladium.util.IProperties;

public class EmmaCheck
{
  private final Map<String, Integer> requiredCoverage;
  private final ICoverageData coverageData;
  private final IMetaData metaData;

  public EmmaCheck(File xiCovFile,
                   File xiMetaFile,
                   Map<String,Integer> xiRequiredCoverage) 
                   throws IOException
  {    
    this.requiredCoverage = xiRequiredCoverage;
    
    IMergeable[] covData = DataFactory.load(xiCovFile);
    ICoverageData lCoverageData = null;
    for (IMergeable emmaEntry : covData)
    {
      if ((emmaEntry != null) &&
          (emmaEntry instanceof ICoverageData))
      {
        lCoverageData =(ICoverageData)emmaEntry;
        break;
      }
    }    
    if (lCoverageData == null)
    {
      throw new IOException("No coverage data found");
    }    
    this.coverageData = lCoverageData;
    
    IMergeable[] metadataData = DataFactory.load(xiMetaFile);
    IMetaData lMetaData = null;
    for (IMergeable emmaEntry : metadataData)
    {
      if ((emmaEntry != null) &&
          (emmaEntry instanceof IMetaData))
      {
        lMetaData =(IMetaData)emmaEntry;
        break;
      }
    }    
    if (lMetaData == null)
    {
      throw new IOException("No metadata data found");
    }    
    this.metaData = lMetaData;
  }
  
  public Map<String,String> getFailedRequirements()
  {
    EmmaCheckReport report = new EmmaCheckReport(requiredCoverage);
    SourcePathCache cache = new SourcePathCache(new File[0], false);
    IProperties props = EMMAProperties.getAppProperties();
    
    report.process(metaData, coverageData, cache, props);
    
    return report.failedRequirements;
  }
  
  private static class EmmaCheckReport extends AbstractReportGenerator
  {
    private final Map<String, Integer> _requiredCoverage;
    public final Map<String,String> failedRequirements = new TreeMap<String, String>();

    EmmaCheckReport(Map<String,Integer> xiRequiredCoverage)
    {
      this._requiredCoverage = xiRequiredCoverage;
    }
    
    @Override
    public String getType()
    {
      return "EMMACHECK";
    }

    @Override
    public void process(IMetaData xiMetaData, 
                        ICoverageData xiCovData,
                        SourcePathCache xiCache, 
                        IProperties xiProps) throws EMMARuntimeException
    {
      initialize(xiMetaData, xiCovData, xiCache, xiProps);
      
      IItem head = m_view.getRoot();
      
      head.accept(this, null);
    }
    
    @Override
    public Object visit(AllItem item, Object ctx)
    {
      return visit((IItem)item, ctx);
    }
    
    @Override
    public Object visit(PackageItem item, Object ctx)
    {
      return visit((IItem)item, ctx);
    }
    
    @Override
    public Object visit(SrcFileItem item, Object ctx)
    {
      String name = item.getParent().getName() + "." + item.getName();      
      Integer target = _requiredCoverage.get(name);
      
      if (target != null)
      {
        final int attrID = IItemAttribute.ATTRIBUTE_BLOCK_COVERAGE_ID;
        final IItemAttribute attr = item.getAttribute (attrID, m_settings.getUnitsType ());
        
        boolean fail = !attr.passes (item, target);
        
        if (fail)
        {
          StringBuffer buf = new StringBuffer();
          attr.format (item, buf);
          failedRequirements.put(name, buf.toString());
        }
      }
      
      return ctx;
    }    

    @SuppressWarnings("unchecked")
    private Object visit(IItem item, Object ctx)
    {
      Iterator<IItem> children = item.getChildren();
      
      while (children.hasNext())
      {
        IItem child = children.next();
        child.accept(this, ctx);
      }
      
      return ctx;
    }
  }
}
