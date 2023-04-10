package org.vash.vate.client.graphicsmode.options;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ItemEvent;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.vash.vate.VT;
import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuAdjustFrameSizeOptionListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuClearInterfaceOptionListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuCloseTerminalOptionListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuColorCodingOptionsListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuDefaultDeviceOptionListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuDeltaCodingOptionsListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuDrawPointerOptionsListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuDrawPointerSizeOptionsListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuImageCodingOptionsListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuInterruptRefreshOptionListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuNextDeviceOptionListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuPreviousDeviceOptionListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuRefreshOptionsListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureIntervalOptionsListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuToggleFullScreenOptionListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuUnifiedDeviceOptionListener;
import org.vash.vate.graphics.capture.VTAWTScreenCaptureProvider;

public class VTGraphicsModeClientOptionsMenuBarViewMenu extends Menu
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
  private Menu colorCodingMenu;
  private Menu alterationCodingMenu;
  private MenuItem nextDeviceMenu;
  private MenuItem previousDeviceMenu;
  private MenuItem defaultDeviceMenu;
  private MenuItem unifiedDeviceMenu;
  private MenuItem toggleFullScreenMenu;
  private MenuItem adjustFrameSizeMenu;
  private MenuItem clearInterfaceMenu;
  private MenuItem closeTerminalMenu;
  private MenuItem resetCaptureFactorMenu;
  private MenuItem increaseCaptureFactorMenu;
  private MenuItem decreaseCaptureFactorMenu;
  private CheckboxMenuItem lowColorOption;
  private CheckboxMenuItem mediumColorOption;
  private CheckboxMenuItem highColorOption;
  private CheckboxMenuItem trueColorOption;
  private CheckboxMenuItem worstColorOption;
  private CheckboxMenuItem decreasedColorOption;
  private CheckboxMenuItem increasedColorOption;
  private CheckboxMenuItem extraColorOption;
  private CheckboxMenuItem simpleColorOption;
  private CheckboxMenuItem interruptedRefreshOption;
  private CheckboxMenuItem asynchronousRefreshOption;
  private CheckboxMenuItem synchronousRefreshOption;
  private CheckboxMenuItem showPointerOption;
  private CheckboxMenuItem hidePointerOption;
  private CheckboxMenuItem directCodingOption;
  private CheckboxMenuItem dynamicCodingOption;
  private CheckboxMenuItem mixedCodingOption;
  private CheckboxMenuItem separatedCodingOption;
  private CheckboxMenuItem scaledPartialCaptureOption;
  private CheckboxMenuItem scaledCompleteCaptureOption;
  private CheckboxMenuItem adjustedKeepRatioCaptureOption;
  private CheckboxMenuItem adjustedIgnoreRatioCaptureOption;
  private CheckboxMenuItem needFocusOption;
  private CheckboxMenuItem needVisibleOption;
  private CheckboxMenuItem ignoreStateOption;
  private CheckboxMenuItem imageCodingZOFOption;
  private CheckboxMenuItem imageCodingSOFOption;
  private CheckboxMenuItem imageCodingPNGOption;
  private CheckboxMenuItem imageCodingJPGOption;
  // private CheckboxMenuItem imageCodingGIFOption;
  
  private Map<Integer, CheckboxMenuItem> captureIntervalOptions;
  private MenuItem increasePointerOption;
  private MenuItem decreasePointerOption;
  private MenuItem normalizePointerOption;
  
  public VTGraphicsModeClientOptionsMenuBarViewMenu(VTGraphicsModeClientWriter writer)
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
    this.alterationCodingMenu = new Menu("Alteration Coding ");
    this.colorCodingMenu = new Menu("Color Coding ");
    this.nextDeviceMenu = new MenuItem("Next");
    this.nextDeviceMenu.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuNextDeviceOptionListener(writer));
    this.previousDeviceMenu = new MenuItem("Previous");
    this.previousDeviceMenu.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuPreviousDeviceOptionListener(writer));
    this.defaultDeviceMenu = new MenuItem("Default");
    this.defaultDeviceMenu.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuDefaultDeviceOptionListener(writer));
    this.unifiedDeviceMenu = new MenuItem("Unified");
    this.unifiedDeviceMenu.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuUnifiedDeviceOptionListener(writer));
    this.toggleFullScreenMenu = new MenuItem("Toggle Full Screen");
    this.toggleFullScreenMenu.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuToggleFullScreenOptionListener(writer));
    this.adjustFrameSizeMenu = new MenuItem("Resize Interface");
    this.adjustFrameSizeMenu.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuAdjustFrameSizeOptionListener(writer));
    this.clearInterfaceMenu = new MenuItem("Reset Image");
    this.clearInterfaceMenu.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuClearInterfaceOptionListener(writer));
    this.closeTerminalMenu = new MenuItem("Close Link");
    this.closeTerminalMenu.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuCloseTerminalOptionListener(writer));
    this.lowColorOption = new CheckboxMenuItem("Low", false);
    this.lowColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, lowColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_27));
    this.mediumColorOption = new CheckboxMenuItem("Medium", true);
    this.mediumColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, mediumColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216));
    this.highColorOption = new CheckboxMenuItem("High", false);
    this.highColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, highColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_32768));
    this.trueColorOption = new CheckboxMenuItem("True", false);
    this.trueColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, trueColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16777216));
    this.worstColorOption = new CheckboxMenuItem("Worst", false);
    this.worstColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, worstColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_8));
    this.decreasedColorOption = new CheckboxMenuItem("Below", false);
    this.decreasedColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, decreasedColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_125));
    this.increasedColorOption = new CheckboxMenuItem("Above", false);
    this.increasedColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, increasedColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_512));
    this.extraColorOption = new CheckboxMenuItem("Extra", false);
    this.extraColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, extraColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_4096));
    this.simpleColorOption = new CheckboxMenuItem("Simple", false);
    this.simpleColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, simpleColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_64));
    this.interruptedRefreshOption = new CheckboxMenuItem("Interrupted", false);
    this.interruptedRefreshOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuInterruptRefreshOptionListener(writer, interruptedRefreshOption));
    this.asynchronousRefreshOption = new CheckboxMenuItem("Immediate", true);
    this.asynchronousRefreshOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuRefreshOptionsListener(writer, asynchronousRefreshOption));
    this.synchronousRefreshOption = new CheckboxMenuItem("Progressive", false);
    this.synchronousRefreshOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuRefreshOptionsListener(writer, synchronousRefreshOption));
    this.showPointerOption = new CheckboxMenuItem("Show", true);
    this.showPointerOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuDrawPointerOptionsListener(writer, showPointerOption));
    this.hidePointerOption = new CheckboxMenuItem("Hide", false);
    this.hidePointerOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuDrawPointerOptionsListener(writer, hidePointerOption));
    this.increasePointerOption = new MenuItem("Increase");
    this.increasePointerOption.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuDrawPointerSizeOptionsListener(writer, 1));
    this.decreasePointerOption = new MenuItem("Decrease");
    this.decreasePointerOption.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuDrawPointerSizeOptionsListener(writer, -1));
    this.normalizePointerOption = new MenuItem("Normalize");
    this.normalizePointerOption.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuDrawPointerSizeOptionsListener(writer, 0));
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
    this.scaledPartialCaptureOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener(writer, scaledPartialCaptureOption, VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_VIEWPORT));
    this.scaledCompleteCaptureOption = new CheckboxMenuItem("Entire", false);
    this.scaledCompleteCaptureOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener(writer, scaledCompleteCaptureOption, VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_ENTIRE));
    this.resetCaptureFactorMenu = new MenuItem("Normalize");
    this.resetCaptureFactorMenu.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener(writer, VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_DEFAULT_SCALE));
    this.increaseCaptureFactorMenu = new MenuItem("Increase");
    this.increaseCaptureFactorMenu.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener(writer, VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_INCREASE_SCALE));
    this.decreaseCaptureFactorMenu = new MenuItem("Decrease");
    this.decreaseCaptureFactorMenu.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener(writer, VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_DECREASE_SCALE));
    this.adjustedKeepRatioCaptureOption = new CheckboxMenuItem("Proportional", false);
    this.adjustedKeepRatioCaptureOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener(writer, adjustedKeepRatioCaptureOption, VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_KEEP_RATIO));
    this.adjustedIgnoreRatioCaptureOption = new CheckboxMenuItem("Independent", false);
    this.adjustedIgnoreRatioCaptureOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener(writer, adjustedIgnoreRatioCaptureOption, VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO));
    this.directCodingOption = new CheckboxMenuItem("Direct", true);
    this.directCodingOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorCodingOptionsListener(writer, directCodingOption));
    this.dynamicCodingOption = new CheckboxMenuItem("Dynamic", false);
    this.dynamicCodingOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorCodingOptionsListener(writer, dynamicCodingOption));
    this.mixedCodingOption = new CheckboxMenuItem("Mixed", false);
    this.mixedCodingOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuDeltaCodingOptionsListener(writer, mixedCodingOption));
    this.separatedCodingOption = new CheckboxMenuItem("Separated", true);
    this.separatedCodingOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuDeltaCodingOptionsListener(writer, separatedCodingOption));
    this.needFocusOption = new CheckboxMenuItem("Focused", false);
    this.needFocusOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener(writer, needFocusOption, VTGraphicsModeClientWriter.TERMINAL_STATE_FOCUSED));
    this.needVisibleOption = new CheckboxMenuItem("Visible", true);
    this.needVisibleOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener(writer, needVisibleOption, VTGraphicsModeClientWriter.TERMINAL_STATE_VISIBLE));
    this.ignoreStateOption = new CheckboxMenuItem("Always", false);
    this.ignoreStateOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener(writer, ignoreStateOption, VTGraphicsModeClientWriter.TERMINAL_STATE_IGNORE));
    this.imageCodingZOFOption = new CheckboxMenuItem("ZOF", true);
    this.imageCodingZOFOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuImageCodingOptionsListener(writer, imageCodingZOFOption, VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZOF));
    this.imageCodingSOFOption = new CheckboxMenuItem("SOF", false);
    this.imageCodingSOFOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuImageCodingOptionsListener(writer, imageCodingSOFOption, VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_SOF));
    this.imageCodingPNGOption = new CheckboxMenuItem("PNG", false);
    this.imageCodingPNGOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuImageCodingOptionsListener(writer, imageCodingPNGOption, VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG));
    this.imageCodingJPGOption = new CheckboxMenuItem("JPG", false);
    this.imageCodingJPGOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuImageCodingOptionsListener(writer, imageCodingJPGOption, VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG));
    // this.imageCodingGIFOption = new CheckboxMenuItem("GIF", false);
    // this.imageCodingGIFOption.addItemListener(new
    // VTGraphicsModeClientOptionsMenuBarViewMenuImageCodingOptionsListener(writer,
    // imageCodingGIFOption,
    // VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_GIF));
    // VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener
    this.drawPointerSizeMenu = new Menu("Size ");
    this.drawPointerSizeMenu.add(increasePointerOption);
    this.drawPointerSizeMenu.add(decreasePointerOption);
    this.drawPointerSizeMenu.add(normalizePointerOption);
    
//		this.colorQualityMenu.add(worstColorOption);
//		this.colorQualityMenu.add(lowColorOption);
//		this.colorQualityMenu.add(simpleColorOption);
//		this.colorQualityMenu.add(decreasedColorOption);
//		this.colorQualityMenu.add(mediumColorOption);
//		this.colorQualityMenu.add(increasedColorOption);
//		this.colorQualityMenu.add(extraColorOption);
//		this.colorQualityMenu.add(highColorOption);
//		this.colorQualityMenu.add(bestColorOption);
    
    this.colorQualityMenu.add(trueColorOption);
    this.colorQualityMenu.add(highColorOption);
    this.colorQualityMenu.add(extraColorOption);
    this.colorQualityMenu.add(increasedColorOption);
    this.colorQualityMenu.add(mediumColorOption);
    this.colorQualityMenu.add(decreasedColorOption);
    this.colorQualityMenu.add(simpleColorOption);
    this.colorQualityMenu.add(lowColorOption);
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
      option.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureIntervalOptionsListener(writer, option, screenCaptureInterval));
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
    this.colorCodingMenu.add(directCodingOption);
    this.colorCodingMenu.add(dynamicCodingOption);
    this.alterationCodingMenu.add(separatedCodingOption);
    this.alterationCodingMenu.add(mixedCodingOption);
    // this.imageFormatMenu.add(imageCodingMenu);
    // this.imageFormatMenu.add(alterationCodingMenu);
    // this.imageFormatMenu.add(colorCodingMenu);
    this.imageFormatMenu.add(imageCodingZOFOption);
    this.imageFormatMenu.add(imageCodingSOFOption);
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
    this.add(toggleFullScreenMenu);
    this.add(clearInterfaceMenu);
    this.add(adjustFrameSizeMenu);
    this.add(closeTerminalMenu);
  }
  
  public void setColorQuality(int colorQuality)
  {
    if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16777216)
    {
      trueColorOption.setState(true);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      increasedColorOption.setState(false);
      mediumColorOption.setState(false);
      decreasedColorOption.setState(false);
      simpleColorOption.setState(false);
      lowColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_32768)
    {
      trueColorOption.setState(false);
      highColorOption.setState(true);
      extraColorOption.setState(false);
      increasedColorOption.setState(false);
      mediumColorOption.setState(false);
      decreasedColorOption.setState(false);
      simpleColorOption.setState(false);
      lowColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216)
    {
      trueColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      increasedColorOption.setState(false);
      mediumColorOption.setState(true);
      decreasedColorOption.setState(false);
      simpleColorOption.setState(false);
      lowColorOption.setState(false);
      worstColorOption.setState(false);
    }
//    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16)
//    {
//      trueColorOption.setState(false);
//      highColorOption.setState(false);
//      extraColorOption.setState(false);
//      increasedColorOption.setState(false);
//      mediumColorOption.setState(false);
//      decreasedColorOption.setState(false);
//      simpleColorOption.setState(false);
//      lowColorOption.setState(false);
//      worstColorOption.setState(true);
//    }
//    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_32)
//    {
//      trueColorOption.setState(false);
//      highColorOption.setState(false);
//      extraColorOption.setState(false);
//      increasedColorOption.setState(false);
//      mediumColorOption.setState(false);
//      decreasedColorOption.setState(false);
//      simpleColorOption.setState(true);
//      lowColorOption.setState(false);
//      worstColorOption.setState(false);
//    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_512)
    {
      trueColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      increasedColorOption.setState(true);
      mediumColorOption.setState(false);
      decreasedColorOption.setState(false);
      simpleColorOption.setState(false);
      lowColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_125)
    {
      trueColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      increasedColorOption.setState(false);
      mediumColorOption.setState(false);
      decreasedColorOption.setState(true);
      simpleColorOption.setState(false);
      lowColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_4096)
    {
      trueColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(true);
      increasedColorOption.setState(false);
      mediumColorOption.setState(false);
      decreasedColorOption.setState(false);
      simpleColorOption.setState(false);
      lowColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_8)
    {
      trueColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      increasedColorOption.setState(false);
      mediumColorOption.setState(false);
      decreasedColorOption.setState(false);
      simpleColorOption.setState(false);
      lowColorOption.setState(false);
      worstColorOption.setState(true);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_64)
    {
      trueColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      increasedColorOption.setState(false);
      mediumColorOption.setState(false);
      decreasedColorOption.setState(false);
      simpleColorOption.setState(true);
      lowColorOption.setState(false);
      worstColorOption.setState(false);
    }
    else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_27)
    {
      trueColorOption.setState(false);
      highColorOption.setState(false);
      extraColorOption.setState(false);
      increasedColorOption.setState(false);
      mediumColorOption.setState(false);
      decreasedColorOption.setState(false);
      simpleColorOption.setState(false);
      lowColorOption.setState(true);
      worstColorOption.setState(false);
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
    if (mode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_VIEWPORT)
    {
      scaledPartialCaptureOption.setState(true);
      scaledCompleteCaptureOption.setState(false);
      adjustedKeepRatioCaptureOption.setState(false);
      adjustedIgnoreRatioCaptureOption.setState(false);
    }
    else if (mode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_ENTIRE)
    {
      scaledPartialCaptureOption.setState(false);
      scaledCompleteCaptureOption.setState(true);
      adjustedKeepRatioCaptureOption.setState(false);
      adjustedIgnoreRatioCaptureOption.setState(false);
    }
    else if (mode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_KEEP_RATIO)
    {
      scaledPartialCaptureOption.setState(false);
      scaledCompleteCaptureOption.setState(false);
      adjustedKeepRatioCaptureOption.setState(true);
      adjustedIgnoreRatioCaptureOption.setState(false);
    }
    else if (mode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO)
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
  
  public void setDynamicCoding(boolean dynamicCoding)
  {
    if (dynamicCoding)
    {
      directCodingOption.setState(false);
      dynamicCodingOption.setState(true);
    }
    else
    {
      directCodingOption.setState(true);
      dynamicCodingOption.setState(false);
    }
  }
  
  public void setSeparatedCoding(boolean separatedCoding)
  {
    if (separatedCoding)
    {
      mixedCodingOption.setState(false);
      separatedCodingOption.setState(true);
    }
    else
    {
      mixedCodingOption.setState(true);
      separatedCodingOption.setState(false);
    }
  }
  
  public void setTerminalRefreshPolicy(int state)
  {
    if (state == VTGraphicsModeClientWriter.TERMINAL_STATE_FOCUSED)
    {
      needFocusOption.setState(true);
      needVisibleOption.setState(false);
      ignoreStateOption.setState(false);
    }
    else if (state == VTGraphicsModeClientWriter.TERMINAL_STATE_VISIBLE)
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
    if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG)
    {
      imageCodingZOFOption.setState(false);
      imageCodingSOFOption.setState(false);
      imageCodingPNGOption.setState(true);
      imageCodingJPGOption.setState(false);
      // imageFormatGIFOption.setState(false);
    }
    else if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG)
    {
      imageCodingZOFOption.setState(false);
      imageCodingSOFOption.setState(false);
      imageCodingPNGOption.setState(false);
      imageCodingJPGOption.setState(true);
      // imageFormatGIFOption.setState(false);
    }
    else if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_SOF)
    {
      imageCodingZOFOption.setState(false);
      imageCodingSOFOption.setState(true);
      imageCodingPNGOption.setState(false);
      imageCodingJPGOption.setState(false);
    }
    else
    {
      imageCodingZOFOption.setState(true);
      imageCodingSOFOption.setState(false);
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
    if (lowColorOption.getState())
    {
      disable = lowColorOption;
      enable = worstColorOption;
    }
    if (simpleColorOption.getState())
    {
      disable = simpleColorOption;
      enable = lowColorOption;
    }
    if (decreasedColorOption.getState())
    {
      disable = decreasedColorOption;
      enable = simpleColorOption;
    }
    if (mediumColorOption.getState())
    {
      disable = mediumColorOption;
      enable = decreasedColorOption;
    }
    if (increasedColorOption.getState())
    {
      disable = increasedColorOption;
      enable = mediumColorOption;
    }
    if (extraColorOption.getState())
    {
      disable = extraColorOption;
      enable = increasedColorOption;
    }
    if (highColorOption.getState())
    {
      disable = highColorOption;
      enable = extraColorOption;
    }
    if (trueColorOption.getState())
    {
      disable = trueColorOption;
      enable = highColorOption;
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
      enable = lowColorOption;
    }
    if (lowColorOption.getState())
    {
      disable = lowColorOption;
      enable = simpleColorOption;
    }
    if (simpleColorOption.getState())
    {
      disable = simpleColorOption;
      enable = decreasedColorOption;
    }
    if (decreasedColorOption.getState())
    {
       disable = decreasedColorOption;
       enable = mediumColorOption;
    }
    if (mediumColorOption.getState())
    {
      disable = mediumColorOption;
      enable = increasedColorOption;
    }
    if (increasedColorOption.getState())
    {
      disable = increasedColorOption;
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