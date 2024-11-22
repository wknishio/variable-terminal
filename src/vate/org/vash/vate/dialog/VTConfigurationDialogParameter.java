package org.vash.vate.dialog;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.KeyListener;

public class VTConfigurationDialogParameter extends Panel
{
  private static final long serialVersionUID = 1L;
  private Label label;
  private Component parameter;
  
  public VTConfigurationDialogParameter(String label, Component parameter, boolean enabled)
  {
    this.parameter = parameter;
    this.label = new Label(label);
    this.label.setAlignment(Label.CENTER);
    BorderLayout layout = new BorderLayout();
    layout.setHgap(1);
    layout.setVgap(1);
    setLayout(layout);
    Panel labelPanel = new Panel();
    labelPanel.setLayout(new BorderLayout());
    labelPanel.add(this.label, BorderLayout.NORTH);
    add(labelPanel, BorderLayout.WEST);
    Panel parameterPanel = new Panel();
    parameterPanel.setLayout(new BorderLayout());
    parameterPanel.add(this.parameter, BorderLayout.NORTH);
    add(parameterPanel, BorderLayout.CENTER);
    setEnabled(enabled);
  }
  
  public void addKeyListener(KeyListener l)
  {
    super.addKeyListener(l);
    parameter.addKeyListener(l);
  }
  
  public String getParameter()
  {
    if (parameter instanceof TextField)
    {
      return ((TextField) parameter).getText();
    }
    else if (parameter instanceof Choice)
    {
      return ((Choice) parameter).getSelectedItem();
    }
    return null;
  }
  
  public void setLabel(Object value)
  {
    label.setText(value.toString());
  }
  
  public void setParameter(Object value)
  {
    if (value == null)
    {
      value = "";
    }
    if (value instanceof byte[])
    {
      try
      {
        value = new String((byte[]) value, "UTF-8");
      }
      catch (Throwable e)
      {
        return;
      }
    }
    if (parameter instanceof TextField)
    {
      ((TextField) parameter).setText(value.toString());
    }
    else if (parameter instanceof Choice)
    {
      ((Choice) parameter).select(value.toString());
    }
  }
  
  public void addParameter(Object value)
  {
    if (value == null)
    {
      return;
    }
    if (parameter instanceof TextField)
    {
      
    }
    else if (parameter instanceof Choice)
    {
      ((Choice) parameter).add(value.toString());
    }
  }
  
  public boolean hasParameter(String value)
  {
    if (parameter instanceof TextField)
    {
      
    }
    else if (parameter instanceof Choice)
    {
      Choice choice = (Choice) parameter;
      int amount = choice.getItemCount();
      for (int i = 0; i < amount; i++)
      {
        if (choice.getItem(i).equals(value))
        {
          return true;
        }
      }
    }
    return false;
  }
  
  public void removeParameter(String value)
  {
    if (parameter instanceof TextField)
    {
      
    }
    else if (parameter instanceof Choice)
    {
      try
      {
        ((Choice) parameter).remove(value);
      }
      catch (Throwable e)
      {
        
      }
    }
  }
  
  public void setEnabled(boolean enabled)
  {
    super.setEnabled(enabled);
    label.setEnabled(enabled);
    parameter.setEnabled(enabled);
    setEditable(enabled);
  }
  
  public void setEditable(boolean editable)
  {
    if (parameter instanceof TextField)
    {
      ((TextField) parameter).setEditable(editable);
    }
  }
}