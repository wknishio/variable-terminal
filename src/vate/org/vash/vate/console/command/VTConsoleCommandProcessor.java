package org.vash.vate.console.command;

public abstract class VTConsoleCommandProcessor
{
  private String fullName = "";
  private String abbreviatedName = "";
  private String fullSyntax = "";
  private String abbreviatedSyntax = "";
  private String resultCode = "";
  private String resultText = "";
  private VTConsoleCommandSelector<?> selector;
  
  public VTConsoleCommandProcessor()
  {
    
  }
  
  public void setSelector(VTConsoleCommandSelector<?> selector)
  {
    this.selector = selector;
  }
  
  public VTConsoleCommandSelector<?> getSelector()
  {
    return selector;
  }
  
  public String getResultCode()
  {
    return resultCode;
  }
  
  public void setResultCode(String resultCode)
  {
    this.resultCode = resultCode;
  }
  
  public String getResultText()
  {
    return resultText;
  }
  
  public void setResultText(String resultText)
  {
    this.resultText = resultText;
  }
  
  public String getFullSyntax()
  {
    return fullSyntax;
  }
  
  public void setFullSyntax(String fullSyntax)
  {
    this.fullSyntax = fullSyntax;
  }
  
  public String getAbbreviatedSyntax()
  {
    return abbreviatedSyntax;
  }
  
  public void setAbbreviatedSyntax(String abbreviatedSyntax)
  {
    this.abbreviatedSyntax = abbreviatedSyntax;
  }
  
  public String getFullName()
  {
    return fullName;
  }
  
  public String getAbbreviatedName()
  {
    return abbreviatedName;
  }
  
  public void setFullName(String fullName)
  {
    this.fullName = fullName;
  }
  
  public void setAbbreviatedName(String abbreviatedName)
  {
    this.abbreviatedName = abbreviatedName;
  }
  
  public boolean equals(Object other)
  {
    boolean equals = false;
    if (other instanceof VTConsoleCommandProcessor)
    {
      VTConsoleCommandProcessor interpreter = (VTConsoleCommandProcessor) other;
      equals = interpreter.match(fullName) || interpreter.match(abbreviatedName);
    }
    return equals;
  }
  
  public boolean match(String name)
  {
    if (name.toUpperCase().equals(fullName.toUpperCase()))
    {
      return true;
    }
    if (name.toUpperCase().equals(abbreviatedName.toUpperCase()))
    {
      return true;
    }
    return false;
  }
  
  public boolean select(String command, String[] parsed) throws Exception
  {
    boolean matched = false;
    if (parsed[0].toUpperCase().equals(fullName.toUpperCase()))
    {
      matched = true;
    }
    if (parsed[0].toUpperCase().equals(abbreviatedName.toUpperCase()))
    {
      matched = true;
    }
    if (matched)
    {
      execute(command, parsed);
    }
    return matched;
  }
  
  public String syntax(String name)
  {
    if (name.toUpperCase().equals(fullName.toUpperCase()))
    {
      return fullSyntax;
    }
    if (name.toUpperCase().equals(abbreviatedName.toUpperCase()))
    {
      return abbreviatedSyntax;
    }
    return null;
  }
  
  public abstract void execute(String command, String[] parsed) throws Exception;
  
  public abstract void close();
  
  public abstract String help(String name);
  
  public abstract void register();
  
  public abstract boolean remote();
  
  public abstract void waitFor();
}