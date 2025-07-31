package org.vash.vate.client.graphicslink.options;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ItemEvent;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.vash.vate.VT;
import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuClearInterfaceOptionListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuCloseTerminalOptionListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuDefaultDeviceOptionListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuDrawPointerOptionsListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuDrawPointerSizeOptionsListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuImageCodingOptionsListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuInterruptRefreshOptionListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuNextDeviceOptionListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuPreviousDeviceOptionListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuRefreshOptionsListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureIntervalOptionsListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuToggleFullScreenOptionListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuToggleScrollbarsOptionListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuUnifiedDeviceOptionListener;
import org.vash.vate.graphics.capture.VTAWTScreenCaptureProvider;

public class VTGraphicsLinkClientOptionsMenuBarViewMenu extends Menu
{
  private static final long serialVersionUID = 1L;
  private Menu colorQualityMenu;
  private Menu refreshModeMenu;
  private Menu drawPointerMenu;
  private Menu drawPointerSizeMenu;
  private Menu refreshIntervalMenu;
  private Menu captureModeMenu;
  private Menu scaledCaptureMenu;
  private Menu scaledCaptureFactorMenu;
  private Menu adjustedCaptureMenu;
  private Menu refreshClauseMenu;
  private Menu graphicalDeviceMenu;
  private Menu imageFormatMenu;
  // private Menu imageCodingMenu;
  //private Menu colorCodingMenu;
  //private Menu alterationCodingMenu;
  private MenuItem nextDeviceMenu;
  private MenuItem previousDeviceMenu;
  private MenuItem defaultDeviceMenu;
  private MenuItem unifiedDeviceMenu;
  private MenuItem toggleFullScreenMenu;
  private MenuItem toggleScrollBarsMenu;
  private MenuItem clearInterfaceMenu;
  private MenuItem closeTerminalMenu;
  private MenuItem resetCaptureFactorMenu;
  private MenuItem increaseCaptureFactorMenu;
  private MenuItem decreaseCaptureFactorMenu;
  
  private CheckboxMenuItem trueColorOption;
  private CheckboxMenuItem ultraColorOption;
  private CheckboxMenuItem vastColorOption;
  private CheckboxMenuItem highColorOption;
  private CheckboxMenuItem extraColorOption;
  private CheckboxMenuItem nextColorOption;
  private CheckboxMenuItem mediumColorOption;
  private CheckboxMenuItem simpleColorOption;
  private CheckboxMenuItem fewColorOption;
  private CheckboxMenuItem lowColorOption;
  private CheckboxMenuItem grayColorOption;
  private CheckboxMenuItem dullColorOption;
  private CheckboxMenuItem worstColorOption;
  
  private CheckboxMenuItem interruptedRefreshOption;
  private CheckboxMenuItem asynchronousRefreshOption;
  private CheckboxMenuItem synchronousRefreshOption;
  private CheckboxMenuItem showPointerOption;
  private CheckboxMenuItem hidePointerOption;
  //private CheckboxMenuItem directCodingOption;
  //private CheckboxMenuItem dynamicCodingOption;
  //private CheckboxMenuItem mixedCodingOption;
  //private CheckboxMenuItem separatedCodingOption;
  private CheckboxMenuItem scaledPartialCaptureOption;
  private CheckboxMenuItem scaledCompleteCaptureOption;
  private CheckboxMenuItem adjustedKeepRatioCaptureOption;
  private CheckboxMenuItem adjustedIgnoreRatioCaptureOption;
  private CheckboxMenuItem needFocusOption;
  private CheckboxMenuItem needVisibleOption;
  private CheckboxMenuItem ignoreStateOption;
  private CheckboxMenuItem imageCodingZSTDOption;
  private CheckboxMenuItem imageCodingDEFLATEOption;
  private CheckboxMenuItem imageCodingPNGOption;
  private CheckboxMenuItem imageCodingJPGOption;
  // private CheckboxMenuItem imageCodingGIFOption;
  
  private Map<Integer, CheckboxMenuItem> captureIntervalOptions;
  private MenuItem increasePointerOption;
  private MenuItem decreasePointerOption;
  private MenuItem normalizePointerOption;
  
  public VTGraphicsLinkClientOptionsMenuBarViewMenu(VTGraphicsLinkClientWriter writer)
  {
    super("View");
    // this.writer = writer;
    this.colorQualityMenu = new Menu("Color Quality ");
    this.refreshModeMenu = new Menu("Refresh Mode ");
    this.drawPointerMenu = new Menu("Remote Pointer ");
    this.refreshIntervalMenu = new Menu("Refresh Interval ");
    this.captureModeMenu = new Menu("Image Size ");
    this.scaledCaptureMenu = new Menu("Scaled ");
    this.scaledCaptureFactorMenu = new Menu("Factor ");
    this.adjustedCaptureMenu = new Menu("Adjusted ");
    // this.rescaleCaptureMenu = new Menu("Capture Rescale ");
    // this.alterationCodingMenu = new Menu("Modification Coding ");
    this.refreshClauseMenu = new Menu("Refresh Clause ");
    this.graphicalDeviceMenu = new Menu("Display Device ");
    this.imageFormatMenu = new Menu("Image Format ");
    // this.imageCodingMenu = new Menu("Image Coding ");
    //this.alterationCodingMenu = new Menu("Alteration Coding ");
    //this.colorCodingMenu = new Menu("Color Coding ");
    this.nextDeviceMenu = new MenuItem("Next");
    this.nextDeviceMenu.addActionListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuNextDeviceOptionListener(writer));
    this.previousDeviceMenu = new MenuItem("Previous");
    this.previousDeviceMenu.addActionListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuPreviousDeviceOptionListener(writer));
    this.defaultDeviceMenu = new MenuItem("Default");
    this.defaultDeviceMenu.addActionListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuDefaultDeviceOptionListener(writer));
    this.unifiedDeviceMenu = new MenuItem("Unified");
    this.unifiedDeviceMenu.addActionListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuUnifiedDeviceOptionListener(writer));
    this.toggleFullScreenMenu = new MenuItem("Toggle Full Screen");
    this.toggleFullScreenMenu.addActionListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuToggleFullScreenOptionListener(writer));
    this.toggleScrollBarsMenu = new MenuItem("Toggle Auto Scroll");
    this.toggleScrollBarsMenu.addActionListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuToggleScrollbarsOptionListener(writer));
    this.clearInterfaceMenu = new MenuItem("Reset Image Data");
    this.clearInterfaceMenu.addActionListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuClearInterfaceOptionListener(writer));
    this.closeTerminalMenu = new MenuItem("Close View");
    this.closeTerminalMenu.addActionListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuCloseTerminalOptionListener(writer));
    
    this.trueColorOption = new CheckboxMenuItem("True-24-Bit", false);
    this.trueColorOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener(writer, trueColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16777216));
    this.ultraColorOption = new CheckboxMenuItem("Ultra-21-Bit", false);
    this.ultraColorOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener(writer, ultraColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_2097152));
    this.vastColorOption = new CheckboxMenuItem("Vast-18-Bit", false);
    this.vastColorOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener(writer, vastColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_262144));
    this.highColorOption = new CheckboxMenuItem("High-15-Bit", false);
    this.highColorOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener(writer, highColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_32768));
    this.extraColorOption = new CheckboxMenuItem("Extra-12-Bit", false);
    this.extraColorOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener(writer, extraColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_4096));
    this.nextColorOption = new CheckboxMenuItem("Next-9-Bit", false);
    this.nextColorOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener(writer, nextColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_512));
    this.mediumColorOption = new CheckboxMenuItem("Medium-8-Bit", true);
    this.mediumColorOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener(writer, mediumColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216));
    this.simpleColorOption = new CheckboxMenuItem("Simple-7-Bit", false);
    this.simpleColorOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener(writer, simpleColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_125));
    this.fewColorOption = new CheckboxMenuItem("Few-6-Bit", false);
    this.fewColorOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener(writer, fewColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_64));
    this.lowColorOption = new CheckboxMenuItem("Low-5-Bit", false);
    this.lowColorOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener(writer, lowColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_27));
    this.grayColorOption = new CheckboxMenuItem("Gray-4-Bit", false);
    this.grayColorOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener(writer, grayColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16));
    this.dullColorOption = new CheckboxMenuItem("Dull-3-Bit", false);
    this.dullColorOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener(writer, dullColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_8));
    this.worstColorOption = new CheckboxMenuItem("Worst-2-Bit", false);
    this.worstColorOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener(writer, worstColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_4));
    
    this.interruptedRefreshOption = new CheckboxMenuItem("Suspend", false);
    this.interruptedRefreshOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuInterruptRefreshOptionListener(writer, interruptedRefreshOption));
    this.asynchronousRefreshOption = new CheckboxMenuItem("Immediate", true);
    this.asynchronousRefreshOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuRefreshOptionsListener(writer, asynchronousRefreshOption));
    this.synchronousRefreshOption = new CheckboxMenuItem("Progressive", false);
    this.synchronousRefreshOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuRefreshOptionsListener(writer, synchronousRefreshOption));
    this.showPointerOption = new CheckboxMenuItem("Show", true);
    this.showPointerOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuDrawPointerOptionsListener(writer, showPointerOption));
    this.hidePointerOption = new CheckboxMenuItem("Hide", false);
    this.hidePointerOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuDrawPointerOptionsListener(writer, hidePointerOption));
    this.increasePointerOption = new MenuItem("Increase");
    this.increasePointerOption.addActionListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuDrawPointerSizeOptionsListener(writer, 1));
    this.decreasePointerOption = new MenuItem("Decrease");
    this.decreasePointerOption.addActionListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuDrawPointerSizeOptionsListener(writer, -1));
    this.normalizePointerOption = new MenuItem("Normalize");
    this.normalizePointerOption.addActionListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuDrawPointerSizeOptionsListener(writer, 0));
    this.captureIntervalOptions = new TreeMap<Integer, CheckboxMenuItem>();
    this.captureIntervalOptions.put(0, new CheckboxMenuItem());
    this.captureIntervalOptions.put(1, new CheckboxMenuItem());
    this.captureIntervalOptions.put(2, new CheckboxMenuItem());
    this.captureIntervalOptions.put(4, new CheckboxMenuItem());
    this.captureIntervalOptions.put(8, new CheckboxMenuItem());
    this.captureIntervalOptions.put(15, new CheckboxMenuItem());
    this.captureIntervalOptions.put(30, new CheckboxMenuItem());
    this.captureIntervalOptions.put(60, new CheckboxMenuItem());
    this.captureIntervalOptions.put(125, new CheckboxMenuItem());
    this.captureIntervalOptions.put(250, new CheckboxMenuItem());
    this.captureIntervalOptions.put(500, new CheckboxMenuItem());
    this.captureIntervalOptions.put(1000, new CheckboxMenuItem());
    this.captureIntervalOptions.put(2000, new CheckboxMenuItem());
    this.captureIntervalOptions.put(4000, new CheckboxMenuItem());
    this.captureIntervalOptions.put(8000, new CheckboxMenuItem());
    this.captureIntervalOptions.put(15000, new CheckboxMenuItem());
    this.captureIntervalOptions.put(30000, new CheckboxMenuItem());
    this.captureIntervalOptions.put(60000, new CheckboxMenuItem());
    this.captureIntervalOptions.put(120000, new CheckboxMenuItem());
    this.scaledPartialCaptureOption = new CheckboxMenuItem("Viewport", true);
    this.scaledPartialCaptureOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener(writer, scaledPartialCaptureOption, VT.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_SCALED_VIEWPORT));
    this.scaledCompleteCaptureOption = new CheckboxMenuItem("Entire", false);
    this.scaledCompleteCaptureOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener(writer, scaledCompleteCaptureOption, VT.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_SCALED_ENTIRE));
    this.resetCaptureFactorMenu = new MenuItem("Normalize");
    this.resetCaptureFactorMenu.addActionListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener(writer, VT.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_DEFAULT_SCALE));
    this.increaseCaptureFactorMenu = new MenuItem("Increase");
    this.increaseCaptureFactorMenu.addActionListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener(writer, VT.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_INCREASE_SCALE));
    this.decreaseCaptureFactorMenu = new MenuItem("Decrease");
    this.decreaseCaptureFactorMenu.addActionListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener(writer, VT.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_DECREASE_SCALE));
    this.adjustedKeepRatioCaptureOption = new CheckboxMenuItem("Proportional", false);
    this.adjustedKeepRatioCaptureOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener(writer, adjustedKeepRatioCaptureOption, VT.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_KEEP_RATIO));
    this.adjustedIgnoreRatioCaptureOption = new CheckboxMenuItem("Independent", false);
    this.adjustedIgnoreRatioCaptureOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener(writer, adjustedIgnoreRatioCaptureOption, VT.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO));
    //this.directCodingOption = new CheckboxMenuItem("Direct", true);
    //this.directCodingOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorCodingOptionsListener(writer, directCodingOption));
    //this.dynamicCodingOption = new CheckboxMenuItem("Dynamic", false);
    //this.dynamicCodingOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuColorCodingOptionsListener(writer, dynamicCodingOption));
    //this.mixedCodingOption = new CheckboxMenuItem("Mixed", false);
    //this.mixedCodingOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuDeltaCodingOptionsListener(writer, mixedCodingOption));
    //this.separatedCodingOption = new CheckboxMenuItem("Separated", true);
    //this.separatedCodingOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuDeltaCodingOptionsListener(writer, separatedCodingOption));
    this.needFocusOption = new CheckboxMenuItem("Focused", false);
    this.needFocusOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener(writer, needFocusOption, VTGraphicsLinkClientWriter.TERMINAL_STATE_FOCUSED));
    this.needVisibleOption = new CheckboxMenuItem("Visible", true);
    this.needVisibleOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener(writer, needVisibleOption, VTGraphicsLinkClientWriter.TERMINAL_STATE_VISIBLE));
    this.ignoreStateOption = new CheckboxMenuItem("Always", false);
    this.ignoreStateOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener(writer, ignoreStateOption, VTGraphicsLinkClientWriter.TERMINAL_STATE_IGNORE));
    this.imageCodingZSTDOption = new CheckboxMenuItem("ZSD", true);
    this.imageCodingZSTDOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuImageCodingOptionsListener(writer, imageCodingZSTDOption, VT.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_ZSD));
    this.imageCodingDEFLATEOption = new CheckboxMenuItem("GZD", false);
    this.imageCodingDEFLATEOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuImageCodingOptionsListener(writer, imageCodingDEFLATEOption, VT.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_GZD));
    this.imageCodingPNGOption = new CheckboxMenuItem("PNG", false);
    this.imageCodingPNGOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuImageCodingOptionsListener(writer, imageCodingPNGOption, VT.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_PNG));
    this.imageCodingJPGOption = new CheckboxMenuItem("JPG", false);
    this.imageCodingJPGOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuImageCodingOptionsListener(writer, imageCodingJPGOption, VT.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_JPG));
    // this.imageCodingGIFOption = new CheckboxMenuItem("GIF", false);
    // this.imageCodingGIFOption.addItemListener(new
    // VTGraphicsLinkClientOptionsMenuBarViewMenuImageCodingOptionsListener(writer,
    // imageCodingGIFOption,
    // VT.VT_GRAPHICS_LINK_GRAPHICS_IMAGE_CODING_GIF));
    // VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener
    this.drawPointerSizeMenu = new Menu("Size ");
    this.drawPointerSizeMenu.add(increasePointerOption);
    this.drawPointerSizeMenu.add(decreasePointerOption);
    this.drawPointerSizeMenu.add(normalizePointerOption);
    
    this.colorQualityMenu.add(trueColorOption);
    this.colorQualityMenu.add(ultraColorOption);
    this.colorQualityMenu.add(vastColorOption);
    this.colorQualityMenu.add(highColorOption);
    this.colorQualityMenu.add(extraColorOption);
    this.colorQualityMenu.add(nextColorOption);
    this.colorQualityMenu.add(mediumColorOption);
    this.colorQualityMenu.add(simpleColorOption);
    this.colorQualityMenu.add(fewColorOption);
    this.colorQualityMenu.add(lowColorOption);
    this.colorQualityMenu.add(grayColorOption);
    this.colorQualityMenu.add(dullColorOption);
    this.colorQualityMenu.add(worstColorOption);
    
    this.refreshModeMenu.add(asynchronousRefreshOption);
    this.refreshModeMenu.add(synchronousRefreshOption);
    this.refreshModeMenu.add(interruptedRefreshOption);
    this.drawPointerMenu.add(showPointerOption);
    this.drawPointerMenu.add(hidePointerOption);
    this.drawPointerMenu.add(drawPointerSizeMenu);
    
    for (Entry<Integer, CheckboxMenuItem> captureIntervalOption : this.captureIntervalOptions.entrySet())
    {
      Integer screenCaptureInterval = captureIntervalOption.getKey();
      CheckboxMenuItem option = captureIntervalOption.getValue();
      if (screenCaptureInterval.intValue() == 250)
      {
        option.setState(true);
      }
      option.setLabel(screenCaptureInterval.intValue() + " ms");
      option.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureIntervalOptionsListener(writer, option, screenCaptureInterval));
      this.refreshIntervalMenu.add(option);
    }
    
    this.scaledCaptureFactorMenu.add(increaseCaptureFactorMenu);
    this.scaledCaptureFactorMenu.add(decreaseCaptureFactorMenu);
    this.scaledCaptureFactorMenu.add(resetCaptureFactorMenu);
    
    this.scaledCaptureMenu.add(scaledCaptureFactorMenu);
    this.scaledCaptureMenu.add(scaledPartialCaptureOption);
    this.scaledCaptureMenu.add(scaledCompleteCaptureOption);
    
    this.adjustedCaptureMenu.add(adjustedKeepRatioCaptureOption);
    this.adjustedCaptureMenu.add(adjustedIgnoreRatioCaptureOption);
    // this.captureModeMenu.add(scaledCaptureFactorMenu);
    this.captureModeMenu.add(scaledCaptureMenu);
    this.captureModeMenu.add(adjustedCaptureMenu);
    this.refreshClauseMenu.add(needFocusOption);
    this.refreshClauseMenu.add(needVisibleOption);
    this.refreshClauseMenu.add(ignoreStateOption);
    this.graphicalDeviceMenu.add(nextDeviceMenu);
    this.graphicalDeviceMenu.add(previousDeviceMenu);
    this.graphicalDeviceMenu.add(defaultDeviceMenu);
    this.graphicalDeviceMenu.add(unifiedDeviceMenu);
    // this.imageCodingMenu.add(imageCodingZOFOption);
    // this.imageCodingMenu.add(imageCodingSOFOption);
    // this.imageCodingMenu.add(imageCodingPNGOption);
    // this.imageCodingMenu.add(imageCodingJPGOption);
    //this.colorCodingMenu.add(directCodingOption);
    //this.colorCodingMenu.add(dynamicCodingOption);
    //this.alterationCodingMenu.add(separatedCodingOption);
    //this.alterationCodingMenu.add(mixedCodingOption);
    // this.imageFormatMenu.add(imageCodingMenu);
    // this.imageFormatMenu.add(alterationCodingMenu);
    // this.imageFormatMenu.add(colorCodingMenu);
    this.imageFormatMenu.add(imageCodingZSTDOption);
    this.imageFormatMenu.add(imageCodingDEFLATEOption);
    this.imageFormatMenu.add(imageCodingJPGOption);
    this.imageFormatMenu.add(imageCodingPNGOption);
    // this.imageFormatMenu.add(imageCodingGIFOption);
    
    this.add(refreshModeMenu);
    this.add(refreshIntervalMenu);
    this.add(refreshClauseMenu);
    this.add(colorQualityMenu);
    this.add(captureModeMenu);
    this.add(imageFormatMenu);
    this.add(drawPointerMenu);
    this.add(graphicalDeviceMenu);
    this.add(clearInterfaceMenu);
    this.add(toggleScrollBarsMenu);
    this.add(toggleFullScreenMenu);
    this.add(closeTerminalMenu);
  }
  
  public void setColorQuality(int colorQuality)
  {
    if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16777216)
    {
      trueColorOption.setState(true);
      ultraColorOption.setState(false);
      vastColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      nextColorOption.setState(false);
      mediumColorOption.setState(false);
      simpleColorOption.setState(false);
      fewColorOption.setState(false);
      lowColorOption.setState(false);
      grayColorOption.setState(false);
      dullColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_2097152)
    {
      trueColorOption.setState(false);
      ultraColorOption.setState(true);
      vastColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      nextColorOption.setState(false);
      mediumColorOption.setState(false);
      simpleColorOption.setState(false);
      fewColorOption.setState(false);
      lowColorOption.setState(false);
      grayColorOption.setState(false);
      dullColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_262144)
    {
      trueColorOption.setState(false);
      ultraColorOption.setState(false);
      vastColorOption.setState(true);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      nextColorOption.setState(false);
      mediumColorOption.setState(false);
      simpleColorOption.setState(false);
      fewColorOption.setState(false);
      lowColorOption.setState(false);
      grayColorOption.setState(false);
      dullColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_32768)
    {
      trueColorOption.setState(false);
      ultraColorOption.setState(false);
      vastColorOption.setState(false);
      highColorOption.setState(true);
      extraColorOption.setState(false);
      nextColorOption.setState(false);
      mediumColorOption.setState(false);
      simpleColorOption.setState(false);
      fewColorOption.setState(false);
      lowColorOption.setState(false);
      grayColorOption.setState(false);
      dullColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_4096)
    {
      trueColorOption.setState(false);
      ultraColorOption.setState(false);
      vastColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(true);
      nextColorOption.setState(false);
      mediumColorOption.setState(false);
      simpleColorOption.setState(false);
      fewColorOption.setState(false);
      lowColorOption.setState(false);
      grayColorOption.setState(false);
      dullColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_512)
    {
      trueColorOption.setState(false);
      ultraColorOption.setState(false);
      vastColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      nextColorOption.setState(true);
      mediumColorOption.setState(false);
      simpleColorOption.setState(false);
      fewColorOption.setState(false);
      lowColorOption.setState(false);
      grayColorOption.setState(false);
      dullColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216)
    {
      trueColorOption.setState(false);
      ultraColorOption.setState(false);
      vastColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      nextColorOption.setState(false);
      mediumColorOption.setState(true);
      simpleColorOption.setState(false);
      fewColorOption.setState(false);
      lowColorOption.setState(false);
      grayColorOption.setState(false);
      dullColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_125)
    {
      trueColorOption.setState(false);
      ultraColorOption.setState(false);
      vastColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      nextColorOption.setState(false);
      mediumColorOption.setState(false);
      simpleColorOption.setState(true);
      fewColorOption.setState(false);
      lowColorOption.setState(false);
      grayColorOption.setState(false);
      dullColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_64)
    {
      trueColorOption.setState(false);
      ultraColorOption.setState(false);
      vastColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      nextColorOption.setState(false);
      mediumColorOption.setState(false);
      simpleColorOption.setState(false);
      fewColorOption.setState(true);
      lowColorOption.setState(false);
      grayColorOption.setState(false);
      dullColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_27)
    {
      trueColorOption.setState(false);
      ultraColorOption.setState(false);
      vastColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      nextColorOption.setState(false);
      mediumColorOption.setState(false);
      simpleColorOption.setState(false);
      fewColorOption.setState(false);
      lowColorOption.setState(true);
      grayColorOption.setState(false);
      dullColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16)
    {
      trueColorOption.setState(false);
      ultraColorOption.setState(false);
      vastColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      nextColorOption.setState(false);
      mediumColorOption.setState(false);
      simpleColorOption.setState(false);
      fewColorOption.setState(false);
      lowColorOption.setState(false);
      grayColorOption.setState(true);
      dullColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_8)
    {
      trueColorOption.setState(false);
      ultraColorOption.setState(false);
      vastColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      nextColorOption.setState(false);
      mediumColorOption.setState(false);
      simpleColorOption.setState(false);
      fewColorOption.setState(false);
      lowColorOption.setState(false);
      grayColorOption.setState(false);
      dullColorOption.setState(true);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_4)
    {
      trueColorOption.setState(false);
      ultraColorOption.setState(false);
      vastColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      nextColorOption.setState(false);
      mediumColorOption.setState(false);
      simpleColorOption.setState(false);
      fewColorOption.setState(false);
      lowColorOption.setState(false);
      grayColorOption.setState(false);
      dullColorOption.setState(false);
      worstColorOption.setState(true);
    }
    else
    {
      // trueColorOption.setState(false);
      // highColorOption.setState(false);
      // extraColorOption.setState(false);
      // increasedColorOption.setState(false);
      // mediumColorOption.setState(false);
      // decreasedColorOption.setState(false);
      // simpleColorOption.setState(true);
      // lowColorOption.setState(false);
      // worstColorOption.setState(false);
    }
  }
  
  public void setSynchronousRefresh(boolean synchronousRefresh)
  {
    if (synchronousRefresh)
    {
      asynchronousRefreshOption.setState(false);
      synchronousRefreshOption.setState(true);
    }
    else
    {
      asynchronousRefreshOption.setState(true);
      synchronousRefreshOption.setState(false);
    }
  }
  
  public void setInterruptRefresh(boolean interruptRefresh)
  {
    if (interruptRefresh)
    {
      asynchronousRefreshOption.setState(false);
      synchronousRefreshOption.setState(false);
    }
  }
  
  public void setDrawPointer(boolean drawPointer)
  {
    if (drawPointer)
    {
      showPointerOption.setState(true);
      hidePointerOption.setState(false);
    }
    else
    {
      showPointerOption.setState(false);
      hidePointerOption.setState(true);
    }
  }
  
  public void setScreenCaptureInterval(int screenCaptureInterval)
  {
    for (Entry<Integer, CheckboxMenuItem> captureIntervalOption : captureIntervalOptions.entrySet())
    {
      if (captureIntervalOption.getKey().intValue() == screenCaptureInterval)
      {
        captureIntervalOption.getValue().setState(true);
      }
      else
      {
        captureIntervalOption.getValue().setState(false);
      }
    }
  }
  
  public void setScreenCaptureMode(int mode)
  {
    if (mode == VT.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_SCALED_VIEWPORT)
    {
      scaledPartialCaptureOption.setState(true);
      scaledCompleteCaptureOption.setState(false);
      adjustedKeepRatioCaptureOption.setState(false);
      adjustedIgnoreRatioCaptureOption.setState(false);
    }
    else if (mode == VT.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_SCALED_ENTIRE)
    {
      scaledPartialCaptureOption.setState(false);
      scaledCompleteCaptureOption.setState(true);
      adjustedKeepRatioCaptureOption.setState(false);
      adjustedIgnoreRatioCaptureOption.setState(false);
    }
    else if (mode == VT.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_KEEP_RATIO)
    {
      scaledPartialCaptureOption.setState(false);
      scaledCompleteCaptureOption.setState(false);
      adjustedKeepRatioCaptureOption.setState(true);
      adjustedIgnoreRatioCaptureOption.setState(false);
    }
    else if (mode == VT.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO)
    {
      scaledPartialCaptureOption.setState(false);
      scaledCompleteCaptureOption.setState(false);
      adjustedKeepRatioCaptureOption.setState(false);
      adjustedIgnoreRatioCaptureOption.setState(true);
    }
  }
  
  /*
   * public void setIgnoreFocus(boolean ignoreFocus) { if (ignoreFocus) {
   * ignoreFocusOption.setState(true); needFocusOption.setState(false); } else {
   * ignoreFocusOption.setState(false); needFocusOption.setState(true); } }
   */
  
  /*
   * public void setIgnoreIconification(boolean ignoreIconification) { if
   * (ignoreIconification) { ignoreIconificationOption.setState(true);
   * considerIconificationOption.setState(false); } else {
   * ignoreIconificationOption.setState(false);
   * considerIconificationOption.setState(true); } }
   */
  
  public void interruptRefresh()
  {
    asynchronousRefreshOption.setState(false);
    synchronousRefreshOption.setState(false);
    interruptedRefreshOption.setState(true);
  }
  
  public void resumeRefresh()
  {
    interruptedRefreshOption.setState(false);
  }
  
//  public void setDynamicCoding(boolean dynamicCoding)
//  {
//    if (dynamicCoding)
//    {
//      directCodingOption.setState(false);
//      dynamicCodingOption.setState(true);
//    }
//    else
//    {
//      directCodingOption.setState(true);
//      dynamicCodingOption.setState(false);
//    }
//  }
  
//  public void setSeparatedCoding(boolean separatedCoding)
//  {
//    if (separatedCoding)
//    {
//      mixedCodingOption.setState(false);
//      separatedCodingOption.setState(true);
//    }
//    else
//    {
//      mixedCodingOption.setState(true);
//      separatedCodingOption.setState(false);
//    }
//  }
  
  public void setTerminalRefreshPolicy(int state)
  {
    if (state == VTGraphicsLinkClientWriter.TERMINAL_STATE_FOCUSED)
    {
      needFocusOption.setState(true);
      needVisibleOption.setState(false);
      ignoreStateOption.setState(false);
    }
    else if (state == VTGraphicsLinkClientWriter.TERMINAL_STATE_VISIBLE)
    {
      needFocusOption.setState(false);
      needVisibleOption.setState(true);
      ignoreStateOption.setState(false);
    }
    else
    {
      needFocusOption.setState(false);
      needVisibleOption.setState(false);
      ignoreStateOption.setState(true);
    }
  }
  
  public void setImageCoding(int imageCoding)
  {
    if (imageCoding == VT.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_PNG)
    {
      imageCodingZSTDOption.setState(false);
      imageCodingDEFLATEOption.setState(false);
      imageCodingPNGOption.setState(true);
      imageCodingJPGOption.setState(false);
      // imageFormatGIFOption.setState(false);
    }
    else if (imageCoding == VT.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_JPG)
    {
      imageCodingZSTDOption.setState(false);
      imageCodingDEFLATEOption.setState(false);
      imageCodingPNGOption.setState(false);
      imageCodingJPGOption.setState(true);
      // imageFormatGIFOption.setState(false);
    }
    else if (imageCoding == VT.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_GZD)
    {
      imageCodingZSTDOption.setState(false);
      imageCodingDEFLATEOption.setState(true);
      imageCodingPNGOption.setState(false);
      imageCodingJPGOption.setState(false);
    }
    else
    {
      imageCodingZSTDOption.setState(true);
      imageCodingDEFLATEOption.setState(false);
      imageCodingPNGOption.setState(false);
      imageCodingJPGOption.setState(false);
      // imageCodingGIFOption.setState(false);
    }
  }
  
  public void increaseCaptureInterval()
  {
    CheckboxMenuItem current = null;
    CheckboxMenuItem disable = null;
    for (Entry<Integer, CheckboxMenuItem> captureIntervalOption : captureIntervalOptions.entrySet())
    {
      current = captureIntervalOption.getValue();
      if (disable != null)
      {
        disable.setState(false);
        current.setState(true);
        ItemEvent event = new ItemEvent(current, ItemEvent.ITEM_STATE_CHANGED, current.getLabel(), ItemEvent.SELECTED);
        current.getItemListeners()[0].itemStateChanged(event);
        return;
      }
      if (current.getState())
      {
        disable = current;
      }
    }
  }
  
  public void decreaseCaptureInterval()
  {
    CheckboxMenuItem current = null;
    CheckboxMenuItem disable = null;
    CheckboxMenuItem previous = null;
    CheckboxMenuItem enable = null;
    for (Entry<Integer, CheckboxMenuItem> captureIntervalOption : captureIntervalOptions.entrySet())
    {
      previous = current;
      current = captureIntervalOption.getValue();
      if (current.getState())
      {
        enable = previous;
        disable = current;
        break;
      }
    }
    if (enable != null)
    {
      disable.setState(false);
      enable.setState(true);
      ItemEvent event = new ItemEvent(enable, ItemEvent.ITEM_STATE_CHANGED, enable.getLabel(), ItemEvent.SELECTED);
      enable.getItemListeners()[0].itemStateChanged(event);
    }
  }
  
  public void decreaseColorQuality()
  {
    CheckboxMenuItem enable = null;
    CheckboxMenuItem disable = null;
    if (dullColorOption.getState())
    {
      disable = dullColorOption;
      enable = worstColorOption;
    }
    if (grayColorOption.getState())
    {
      disable = grayColorOption;
      enable = dullColorOption;
    }
    if (lowColorOption.getState())
    {
      disable = lowColorOption;
      enable = grayColorOption;
    }
    if (fewColorOption.getState())
    {
      disable = fewColorOption;
      enable = lowColorOption;
    }
    if (simpleColorOption.getState())
    {
      disable = simpleColorOption;
      enable = fewColorOption;
    }
    if (mediumColorOption.getState())
    {
      disable = mediumColorOption;
      enable = simpleColorOption;
    }
    if (nextColorOption.getState())
    {
      disable = nextColorOption;
      enable = mediumColorOption;
    }
    if (extraColorOption.getState())
    {
      disable = extraColorOption;
      enable = nextColorOption;
    }
    if (highColorOption.getState())
    {
      disable = highColorOption;
      enable = extraColorOption;
    }
    if (vastColorOption.getState())
    {
      disable = vastColorOption;
      enable = highColorOption;
    }
    if (ultraColorOption.getState())
    {
      disable = ultraColorOption;
      enable = vastColorOption;
    }
    if (trueColorOption.getState())
    {
      disable = trueColorOption;
      enable = ultraColorOption;
    }
    if (enable != null)
    {
      disable.setState(false);
      enable.setState(true);
      ItemEvent event = new ItemEvent(enable, ItemEvent.ITEM_STATE_CHANGED, enable.getLabel(), ItemEvent.SELECTED);
      enable.getItemListeners()[0].itemStateChanged(event);
    }
  }
  
  public void increaseColorQuality()
  {
    CheckboxMenuItem enable = null;
    CheckboxMenuItem disable = null;
    if (worstColorOption.getState())
    {
      disable = worstColorOption;
      enable = dullColorOption;
    }
    if (dullColorOption.getState())
    {
      disable = dullColorOption;
      enable = grayColorOption;
    }
    if (grayColorOption.getState())
    {
      disable = grayColorOption;
      enable = lowColorOption;
    }
    if (lowColorOption.getState())
    {
      disable = lowColorOption;
      enable = fewColorOption;
    }
    if (fewColorOption.getState())
    {
      disable = fewColorOption;
      enable = simpleColorOption;
    }
    if (simpleColorOption.getState())
    {
       disable = simpleColorOption;
       enable = mediumColorOption;
    }
    if (mediumColorOption.getState())
    {
      disable = mediumColorOption;
      enable = nextColorOption;
    }
    if (nextColorOption.getState())
    {
      disable = nextColorOption;
      enable = extraColorOption;
    }
    if (extraColorOption.getState())
    {
      disable = extraColorOption;
      enable = highColorOption;
    }
    if (highColorOption.getState())
    {
      disable = highColorOption;
      enable = vastColorOption;
    }
    if (vastColorOption.getState())
    {
      disable = vastColorOption;
      enable = ultraColorOption;
    }
    if (ultraColorOption.getState())
    {
      disable = ultraColorOption;
      enable = trueColorOption;
    }
    if (enable != null)
    {
      disable.setState(false);
      enable.setState(true);
      ItemEvent event = new ItemEvent(enable, ItemEvent.ITEM_STATE_CHANGED, enable.getLabel(), ItemEvent.SELECTED);
      enable.getItemListeners()[0].itemStateChanged(event);
    }
  }
}