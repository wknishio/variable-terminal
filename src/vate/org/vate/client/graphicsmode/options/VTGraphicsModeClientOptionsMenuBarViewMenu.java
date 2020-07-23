package org.vate.client.graphicsmode.options;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ItemEvent;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.vate.VT;
import org.vate.client.graphicsmode.VTGraphicsModeClientWriter;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuClearInterfaceOptionListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuCloseTerminalOptionListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuColorCodingOptionsListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuDefaultDeviceOptionListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuDeltaCodingOptionsListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuDrawPointerOptionsListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuDrawPointerSizeOptionsListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuImageCodingOptionsListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuInterruptRefreshOptionListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuNextDeviceOptionListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuPreviousDeviceOptionListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuRefreshOptionsListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureIntervalOptionsListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuToggleFullScreenOptionListener;
import org.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuUnifiedDeviceOptionListener;
import org.vate.graphics.capture.VTAWTScreenCaptureProvider;

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
	// private MenuItem adjustFrameSizeMenu;
	private MenuItem clearInterfaceMenu;
	private MenuItem closeTerminalMenu;
	private MenuItem resetCaptureFactorMenu;
	private MenuItem increaseCaptureFactorMenu;
	private MenuItem decreaseCaptureFactorMenu;
	private CheckboxMenuItem lowColorOption;
	private CheckboxMenuItem mediumColorOption;
	private CheckboxMenuItem highColorOption;
	private CheckboxMenuItem bestColorOption;
	private CheckboxMenuItem worstColorOption;
	private CheckboxMenuItem smallColorOption;
	private CheckboxMenuItem goodColorOption;
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
		// this.adjustFrameSizeMenu = new MenuItem("Autosize Interface");
		// this.adjustFrameSizeMenu.addActionListener(new
		// VTGraphicsModeClientOptionsMenuBarViewMenuAdjustFrameSizeOptionListener(writer));
		this.clearInterfaceMenu = new MenuItem("Reset Image");
		this.clearInterfaceMenu.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuClearInterfaceOptionListener(writer));
		this.closeTerminalMenu = new MenuItem("Close Link");
		this.closeTerminalMenu.addActionListener(new VTGraphicsModeClientOptionsMenuBarViewMenuCloseTerminalOptionListener(writer));
		this.lowColorOption = new CheckboxMenuItem("Normal", false);
		this.lowColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, lowColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_LOW));
		this.mediumColorOption = new CheckboxMenuItem("Medium", true);
		this.mediumColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, mediumColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_MEDIUM));
		this.highColorOption = new CheckboxMenuItem("High", false);
		this.highColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, highColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_HIGH));
		this.bestColorOption = new CheckboxMenuItem("Best", false);
		this.bestColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, bestColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_BEST));
		this.worstColorOption = new CheckboxMenuItem("Worst", false);
		this.worstColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, worstColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_WORST));
		this.smallColorOption = new CheckboxMenuItem("Low", false);
		this.smallColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, smallColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_SMALL));
		this.goodColorOption = new CheckboxMenuItem("Good", false);
		this.goodColorOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(writer, goodColorOption, VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_GOOD));
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
		this.captureIntervalOptions.put(5, new CheckboxMenuItem());
		this.captureIntervalOptions.put(15, new CheckboxMenuItem());
		this.captureIntervalOptions.put(30, new CheckboxMenuItem());
		this.captureIntervalOptions.put(60, new CheckboxMenuItem());
		this.captureIntervalOptions.put(125, new CheckboxMenuItem());
		this.captureIntervalOptions.put(250, new CheckboxMenuItem());
		this.captureIntervalOptions.put(500, new CheckboxMenuItem());
		this.captureIntervalOptions.put(1000, new CheckboxMenuItem());
		this.captureIntervalOptions.put(2000, new CheckboxMenuItem());
		this.captureIntervalOptions.put(5000, new CheckboxMenuItem());
		this.captureIntervalOptions.put(15000, new CheckboxMenuItem());
		this.captureIntervalOptions.put(30000, new CheckboxMenuItem());
		this.captureIntervalOptions.put(60000, new CheckboxMenuItem());
		this.scaledPartialCaptureOption = new CheckboxMenuItem("Viewport", true);
		this.scaledPartialCaptureOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener(writer, scaledPartialCaptureOption, VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_PARTIAL));
		this.scaledCompleteCaptureOption = new CheckboxMenuItem("Entire", false);
		this.scaledCompleteCaptureOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener(writer, scaledCompleteCaptureOption, VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_COMPLETE));
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
		this.needFocusOption = new CheckboxMenuItem("Focused", true);
		this.needFocusOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener(writer, needFocusOption, VTGraphicsModeClientWriter.TERMINAL_STATE_FOCUSED));
		this.needVisibleOption = new CheckboxMenuItem("Visible", false);
		this.needVisibleOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener(writer, needVisibleOption, VTGraphicsModeClientWriter.TERMINAL_STATE_VISIBLE));
		this.ignoreStateOption = new CheckboxMenuItem("Always", false);
		this.ignoreStateOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener(writer, ignoreStateOption, VTGraphicsModeClientWriter.TERMINAL_STATE_IGNORE));
		this.imageCodingZOFOption = new CheckboxMenuItem("ZOF", true);
		this.imageCodingZOFOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuImageCodingOptionsListener(writer, imageCodingZOFOption, VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZOF));
		this.imageCodingSOFOption = new CheckboxMenuItem("SOF", false);
		this.imageCodingSOFOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuImageCodingOptionsListener(writer, imageCodingSOFOption, VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_SOF));
		this.imageCodingPNGOption = new CheckboxMenuItem("PNG", false);
		this.imageCodingPNGOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuImageCodingOptionsListener(writer, imageCodingPNGOption, VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG));
		this.imageCodingJPGOption = new CheckboxMenuItem("JPEG", false);
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
		
		this.colorQualityMenu.add(worstColorOption);
		this.colorQualityMenu.add(smallColorOption);
		this.colorQualityMenu.add(lowColorOption);
		this.colorQualityMenu.add(mediumColorOption);
		this.colorQualityMenu.add(goodColorOption);
		this.colorQualityMenu.add(highColorOption);
		this.colorQualityMenu.add(bestColorOption);
		
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
		this.imageFormatMenu.add(imageCodingPNGOption);
		this.imageFormatMenu.add(imageCodingJPGOption);
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
		// this.add(adjustFrameSizeMenu);
		this.add(clearInterfaceMenu);
		this.add(closeTerminalMenu);
	}
	
	public void setColorQuality(int colorQuality)
	{
		if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_BEST)
		{
			bestColorOption.setState(true);
			highColorOption.setState(false);
			goodColorOption.setState(false);
			mediumColorOption.setState(false);
			lowColorOption.setState(false);
			smallColorOption.setState(false);
			worstColorOption.setState(false);
		}
		else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_HIGH)
		{
			bestColorOption.setState(false);
			highColorOption.setState(true);
			goodColorOption.setState(false);
			mediumColorOption.setState(false);
			lowColorOption.setState(false);
			smallColorOption.setState(false);
			worstColorOption.setState(false);
		}
		else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_MEDIUM)
		{
			bestColorOption.setState(false);
			highColorOption.setState(false);
			goodColorOption.setState(false);
			mediumColorOption.setState(true);
			lowColorOption.setState(false);
			smallColorOption.setState(false);
			worstColorOption.setState(false);
		}
		else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_WORST)
		{
			bestColorOption.setState(false);
			highColorOption.setState(false);
			goodColorOption.setState(false);
			mediumColorOption.setState(false);
			lowColorOption.setState(false);
			smallColorOption.setState(false);
			worstColorOption.setState(true);
		}
		else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_SMALL)
		{
			bestColorOption.setState(false);
			highColorOption.setState(false);
			goodColorOption.setState(false);
			mediumColorOption.setState(false);
			lowColorOption.setState(false);
			smallColorOption.setState(true);
			worstColorOption.setState(false);
		}
		else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_GOOD)
		{
			bestColorOption.setState(false);
			highColorOption.setState(false);
			goodColorOption.setState(true);
			mediumColorOption.setState(false);
			lowColorOption.setState(false);
			smallColorOption.setState(false);
			worstColorOption.setState(false);
		}
		else
		{
			bestColorOption.setState(false);
			highColorOption.setState(false);
			goodColorOption.setState(false);
			mediumColorOption.setState(false);
			lowColorOption.setState(true);
			smallColorOption.setState(false);
			worstColorOption.setState(false);
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
		if (mode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_PARTIAL)
		{
			scaledPartialCaptureOption.setState(true);
			scaledCompleteCaptureOption.setState(false);
			adjustedKeepRatioCaptureOption.setState(false);
			adjustedIgnoreRatioCaptureOption.setState(false);
		}
		else if (mode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_COMPLETE)
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
	
	/* public void setIgnoreFocus(boolean ignoreFocus) { if (ignoreFocus) {
	 * ignoreFocusOption.setState(true); needFocusOption.setState(false); } else
	 * { ignoreFocusOption.setState(false); needFocusOption.setState(true); }
	 * } */
	
	/* public void setIgnoreIconification(boolean ignoreIconification) { if
	 * (ignoreIconification) { ignoreIconificationOption.setState(true);
	 * considerIconificationOption.setState(false); } else {
	 * ignoreIconificationOption.setState(false);
	 * considerIconificationOption.setState(true); } } */
	
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
		if (mediumColorOption.getState())
		{
			disable = mediumColorOption;
			enable = lowColorOption;
		}
		if (highColorOption.getState())
		{
			disable = highColorOption;
			enable = mediumColorOption;
		}
		if (bestColorOption.getState())
		{
			disable = bestColorOption;
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
			enable = mediumColorOption;
		}
		if (mediumColorOption.getState())
		{
			disable = mediumColorOption;
			enable = highColorOption;
		}
		if (highColorOption.getState())
		{
			disable = highColorOption;
			enable = bestColorOption;
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