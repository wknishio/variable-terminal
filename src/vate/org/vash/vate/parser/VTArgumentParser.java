package org.vash.vate.parser;

public class VTArgumentParser
{
  /*
   * public static int getNextDelimiterPosition(String arg, int start) { }
   */
  
  public static int countDelimiterInParameter(String parameter, char delimiter)
  {
    int delimiterCounter = 0;
    boolean insideQuotes = false;
    for (int i = 0; i < parameter.length(); i++)
    {
      if (!insideQuotes)
      {
        if (parameter.charAt(i) == delimiter)
        {
          delimiterCounter++;
        }
        else if (parameter.charAt(i) == '"')
        {
          insideQuotes = true;
        }
      }
      else
      {
        if (parameter.charAt(i) == '"')
        {
          insideQuotes = false;
        }
      }
    }
    return delimiterCounter;
  }
  
  public static String[] parseParameter(String parameter, char delimiter)
  {
    int delimiterCount = countDelimiterInParameter(parameter, delimiter);
    String[] subParameters = new String[delimiterCount + 1];
    int subParameterNumber = 0;
    int lastDelimiterPosition = 0;
    // boolean insideQuotes = false;
    for (int i = 0; i < parameter.length(); i++)
    {
      if (parameter.charAt(i) == delimiter)
      {
        subParameters[subParameterNumber++] = parameter.substring(lastDelimiterPosition, i);
        if (subParameters[subParameterNumber - 1].startsWith("\""))
        {
          subParameters[subParameterNumber - 1] = subParameters[subParameterNumber - 1].substring(0, subParameters[subParameterNumber - 1].length() - 1).substring(1);
        }
        lastDelimiterPosition = i + 1;
      }
    }
    subParameters[subParameterNumber++] = parameter.substring(lastDelimiterPosition, parameter.length());
    return subParameters;
  }
  
  /*
   * public static void main(String[] args) { String[] parms =
   * parseParameter("\"A\"/B", '/'); System.out.println("count: " +
   * parms.length); for (String parm : parms) { System.out.println(parm); } }
   */
}