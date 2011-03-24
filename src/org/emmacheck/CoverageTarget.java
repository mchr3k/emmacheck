package org.emmacheck;

import java.util.regex.Pattern;

import com.vladium.emma.report.IItemAttribute;

public class CoverageTarget
{
  public final int mTarget;
  public final int mType;
  
  private static final Pattern BARE_NUMBER = Pattern.compile("^\\d+$");
  private static final Pattern LINE_NUMBER = Pattern.compile("^L\\d+$");
  private static final Pattern BLCK_NUMBER = Pattern.compile("^B\\d+$");
  private static final Pattern MTHD_NUMBER = Pattern.compile("^M\\d+$");
  
  public CoverageTarget(String xiTarget)
  {
    if (BARE_NUMBER.matcher(xiTarget).matches())
    {
      mTarget = Integer.parseInt(xiTarget);
      mType = IItemAttribute.ATTRIBUTE_LINE_COVERAGE_ID;
    }
    else if (LINE_NUMBER.matcher(xiTarget).matches())
    {
      mTarget = Integer.parseInt(xiTarget.substring(1));
      mType = IItemAttribute.ATTRIBUTE_LINE_COVERAGE_ID;
    }
    else if (BLCK_NUMBER.matcher(xiTarget).matches())
    {
      mTarget = Integer.parseInt(xiTarget.substring(1));
      mType = IItemAttribute.ATTRIBUTE_BLOCK_COVERAGE_ID;
    }
    else if (MTHD_NUMBER.matcher(xiTarget).matches())
    {
      mTarget = Integer.parseInt(xiTarget.substring(1));
      mType = IItemAttribute.ATTRIBUTE_METHOD_COVERAGE_ID;
    }
    else
    {
      throw new IllegalArgumentException(xiTarget);
    }
  }
}
