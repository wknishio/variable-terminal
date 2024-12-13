package org.vash.vate.parser;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@SuppressWarnings(
{ "rawtypes", "unchecked" })
public class VTConfigurationProperties extends Properties
{
  private static final long serialVersionUID = 1L;
  // private PropertiesConfiguration configuration = new
  // PropertiesConfiguration();
  private Map<Object, Object> linkMap = new LinkedHashMap<Object, Object>();
  
  public VTConfigurationProperties()
  {
    super();
  }
  
  public VTConfigurationProperties(Properties fallback)
  {
    super(fallback);
  }
  
  // public void loadProperties(InputStream in) throws Exception
  // {
  // linkMap.clear();
  // configuration.clear();
  // configuration.load(in);
  // Iterator<String> keys = configuration.getKeys();
  // while (keys.hasNext())
  // {
  // String key = keys.next();
  // linkMap.put(key, configuration.getProperty(key));
  // }
  // }
  
  // public void saveProperties(OutputStream out) throws Exception
  // {
  // configuration.save(out);
  // }
  
  public void clear()
  {
    // configuration.clear();
    linkMap.clear();
  }
  
  public boolean contains(Object value)
  {
    return linkMap.containsValue(value);
  }
  
  public boolean containsKey(Object key)
  {
    return linkMap.containsKey(key.toString());
  }
  
  public boolean containsValue(Object value)
  {
    return linkMap.containsValue(value);
  }
  
  public Enumeration elements()
  {
    return Collections.enumeration((Collection) this);
  }
  
  public Set entrySet()
  {
    return linkMap.entrySet();
  }
  
  public boolean equals(Object o)
  {
    return linkMap.equals(o);
  }
  
  public Object get(Object key)
  {
    return linkMap.get(key.toString());
  }
  
  public String getProperty(String key)
  {
    Object oval = get(key); // here the class Properties uses super.get()
    if (oval == null)
      return null;
    return (oval instanceof String) ? (String) oval : null; // behavior of
    // standard
    // properties
  }
  
  public boolean isEmpty()
  {
    return linkMap.isEmpty();
  }
  
  public Enumeration keys()
  {
    Set keys = linkMap.keySet();
    return Collections.enumeration(keys);
  }
  
  public Set keySet()
  {
    return linkMap.keySet();
  }
  
  public void list(PrintStream out)
  {
    this.list(new PrintWriter(out, true));
  }
  
  public void list(PrintWriter out)
  {
    for (Map.Entry e : (Set<Map.Entry>) this.entrySet())
    {
      String key = e.getKey().toString();
      String val = e.getValue().toString();
      out.println(key + "=" + val);
    }
  }
  
  public Object put(Object key, Object value)
  {
    return linkMap.put(key.toString(), value);
  }
  
  public int size()
  {
    return linkMap.size();
  }
  
  public Collection<Object> values()
  {
    return linkMap.values();
  }
}