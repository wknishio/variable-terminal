package org.vate;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;

//import com.sixlegs.png.iio.PngImageReader;
//import com.sixlegs.png.iio.PngImageReaderSpi;

public class VT
{
	public static final int VT_MAJOR_VERSION = 1;
	public static final int VT_MINOR_VERSION = 4;
	public static final int VT_REVISION_VERSION = 2;
	
	public static final int VT_NETWORK_PACKET_SIZE = 1024 * 4;
	public static final int VT_NETWORK_PACKET_BUFFER_SIZE = 1024 * 32;
	public static final int VT_STANDARD_DATA_BUFFER_SIZE = 1024 * 32;
	public static final int VT_SMALL_DATA_BUFFER_SIZE = 1024 * 8;
	
	public static final int VT_MULTIPLEXED_CHANNEL_TYPE_PIPED = 0;
	public static final int VT_MULTIPLEXED_CHANNEL_TYPE_DIRECT = 1;
	public static final int VT_MULTIPLEXED_CHANNEL_TYPE_PERFORMANCE_THROTTLEABLE = 0 << 1;
	public static final int VT_MULTIPLEXED_CHANNEL_TYPE_PERFORMANCE_UNLIMITED = 1 << 1;
	public static final int VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_DISABLED = 0 << 2;
	public static final int VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED = 1 << 2;
	//public static final short VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_LZ4 = 0 << 3;
	//public static final short VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_DEFLATE = 1 << 3;
	
	public static final int VT_CONNECTION_PROXY_NONE = 0;
	public static final int VT_CONNECTION_PROXY_SOCKS = 1;
	public static final int VT_CONNECTION_PROXY_HTTP = 2;
	
	public static final int VT_CONNECTION_ENCRYPT_NONE = 0;
	public static final int VT_CONNECTION_ENCRYPT_RC4 = 1;
	public static final int VT_CONNECTION_ENCRYPT_AES = 2;
	// public static final int VT_CONNECTION_ENCRYPT_HC128 = 3;
	// public static final int VT_CONNECTION_ENCRYPT_GRAIN128 = 4;
	
	public static final int VT_AUTHENTICATION_TIMEOUT_MILLISECONDS = 60000;
	public static final int VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS = 60000;
	//public static final int VT_CONNECTION_TCP_ACCEPT_TIMEOUT_MILLISECONDS = 60000;
	//public static final int VT_CONNECTION_TCP_CONNECT_TIMEOUT_MILLISECONDS = 60000;
	public static final int VT_RECONNECTION_TIMEOUT_MILLISECONDS = 180000;
	
	public static final int VT_PING_SERVICE_INTERVAL = 15000;
	
	public static final int VT_FILE_TRANSFER_SESSION_VERIFYING = 0;
	public static final int VT_FILE_TRANSFER_SESSION_FINISHED = 1;
	public static final int VT_FILE_TRANSFER_FILE_NOT_FOUND = 2;
	public static final int VT_FILE_TRANSFER_FILE_TYPE_FILE = 3;
	public static final int VT_FILE_TRANSFER_FILE_TYPE_DIRECTORY = 4;
	public static final int VT_FILE_TRANSFER_FILE_TYPE_UNKNOWN = 5;
	public static final int VT_FILE_TRANSFER_FILE_ERROR = 6;
	public static final int VT_FILE_TRANSFER_FILE_ACCESS_DENIED = 7;
	public static final int VT_FILE_TRANSFER_FILE_ACCESS_READ_ONLY = 8;
	public static final int VT_FILE_TRANSFER_FILE_ACCESS_WRITE_ONLY = 9;
	public static final int VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE = 10;
	public static final int VT_FILE_TRANSFER_FILE_ACCESS_ERROR = 11;
	
	public static final int VT_GRAPHICS_MODE_SESSION_STARTABLE = 0;
	public static final int VT_GRAPHICS_MODE_SESSION_UNSTARTABLE = 1;
	public static final int VT_GRAPHICS_MODE_SESSION_ENDING = 2;
	public static final int VT_GRAPHICS_MODE_SESSION_ENDED = 3;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_NEW_FRAME_CUSTOM = 4;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_CUSTOM = 5;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_REFRESH_REQUEST = 6;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_CLEAR_REQUEST = 7;
	public static final int VT_GRAPHICS_MODE_MOUSE_INPUT_MOVE = 8;
	public static final int VT_GRAPHICS_MODE_MOUSE_INPUT_KEY_DOWN = 9;
	public static final int VT_GRAPHICS_MODE_MOUSE_INPUT_KEY_UP = 10;
	public static final int VT_GRAPHICS_MODE_MOUSE_INPUT_WHEEL = 11;
	public static final int VT_GRAPHICS_MODE_KEYBOARD_INPUT_KEY_DOWN = 12;
	public static final int VT_GRAPHICS_MODE_KEYBOARD_INPUT_KEY_UP = 13;
	public static final int VT_GRAPHICS_MODE_KEYBOARD_LOCK_KEY_STATE_ON = 14;
	public static final int VT_GRAPHICS_MODE_KEYBOARD_LOCK_KEY_STATE_OFF = 15;
	public static final int VT_GRAPHICS_MODE_ANY_INPUT_RELEASE_ALL_PRESSED_KEYS = 16;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_LOW = 17;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_MEDIUM = 18;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_HIGH = 19;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_REFRESH_MODE_INTERRUPTED = 20;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_REFRESH_MODE_ASYNCHRONOUS = 21;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_REFRESH_MODE_SYNCHRONOUS = 22;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_ON = 23;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_OFF = 24;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_INTERVAL_CHANGE = 25;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_REMOTE_INTERFACE_AREA_CHANGE = 26;
	public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_SEND_REQUEST = 27;
	public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_RECEIVE_REQUEST = 28;
	public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_CANCEL_REQUEST = 29;
	public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_STARTABLE = 30;
	public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_UNSTARTABLE = 31;
	public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_EMPTY = 32;
	public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_TEXT = 33;
	public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_IMAGE = 34;
	public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_UNSUPPORTED = 35;
	public static final int VT_GRAPHICS_MODE_CLIPBOARD_CLEAR_REQUEST = 36;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_PARTIAL = 37;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_COMPLETE = 38;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_KEEP_RATIO = 39;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO = 40;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_AREA_CHANGE = 41;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_REFRESH_NOT_NEEDED = 42;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_CODING_DIRECT = 43;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_CODING_DYNAMIC = 44;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_CHANGE_DEVICE_DEFAULT = 45;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_CHANGE_DEVICE_NEXT = 46;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_DIFFERENCE_MIXED_CODING = 47;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_DIFFERENCE_SEPARATED_CODING = 48;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_NEW_FRAME_IMAGE = 49;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_IMAGE = 50;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZOF = 51;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG = 52;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG = 53;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_BEST = 54;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_WORST = 55;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_SOF = 56;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_CHANGE_DEVICE_UNIFIED = 57;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_DEFAULT_SCALE = 58;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_INCREASE_SCALE = 59;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_DECREASE_SCALE = 60;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_CHANGE_DEVICE_PREVIOUS = 61;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_INCREASE = 62;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_DECREASE = 63;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_NORMALIZE = 64;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_NORMAL = 65;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_GOOD = 66;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_EXTRA = 67;
	public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_SIMPLE = 68;

	
	// public static final int
	// VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALE_FACTOR
	// = 59;
	// public static final int
	// VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_FILELIST =
	// 58;
	// public static final int VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_GIF = 58;
	// public static final int
	// VT_GRAPHICS_MODE_GRAPHICS_REMOTE_SCREEN_CAPTURE_TASK
	// = 47;
	
	private static final DateFormat VT_ERA_DATEFORMAT;
	private static final Calendar VT_YEAR_CALENDAR;
	public static final AudioFormat VT_AUDIO_FORMAT;
	public static final Map<RenderingHints.Key, Object> VT_GRAPHICS_RENDERING_HINTS;
	static
	{
		VT_ERA_DATEFORMAT = new SimpleDateFormat("G", Locale.ENGLISH);
		VT_YEAR_CALENDAR = Calendar.getInstance();
		float sampleRate = 16000;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		VT_AUDIO_FORMAT = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		VT_GRAPHICS_RENDERING_HINTS = new HashMap<RenderingHints.Key, Object>();
		VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		//VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
		//VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		//VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
	}
	
	public static final int VT_AUDIO_CODEC_SPEEX = 1;
	public static final int VT_AUDIO_CODEC_OPUS = 2;
	public static final int VT_AUDIO_CODEC_DEFAULT = VT_AUDIO_CODEC_OPUS;
	
	public static final int VT_AUDIO_CODEC_FRAME_MILLISECONDS = 20;
	public static final int VT_AUDIO_LINE_BUFFER_MILLISECONDS = 40;
	
	public static final int VT_ZIP_FILE_COMPRESS = 1;
	public static final int VT_ZIP_FILE_UNCOMPRESS = 2;
	public static final int VT_ZIP_FILE_DECOMPRESS = 3;
	
	public static final String VT_VERSION = "v" + VT.VT_MAJOR_VERSION + "." + VT.VT_MINOR_VERSION + "." + VT.VT_REVISION_VERSION;
	public static final String VT_YEAR = VT_ERA_DATEFORMAT.format(VT_YEAR_CALENDAR.getTime()) + " " + String.valueOf(VT_YEAR_CALENDAR.get(Calendar.YEAR));
	
	public static BufferedImage remoteIcon;
	public static BufferedImage terminalIcon;
	public static BufferedImage desktopIcon;
	
	static
	{
		ImageIO.setUseCache(false);
		//PngImageReader reader = new PngImageReader(new PngImageReaderSpi());
		try
		{
			//reader.setInput(ImageIO.createImageInputStream(VT.class.getResourceAsStream("/org/vate/console/graphical/resource/remote.png")), true, false);
			//remoteIcon = reader.read(0);
			remoteIcon = ImageIO.read(VT.class.getResourceAsStream("/org/vate/console/graphical/resource/remote.png"));
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			remoteIcon = null;
		}
		
		try
		{
			//reader.setInput(ImageIO.createImageInputStream(VT.class.getResourceAsStream("/org/vate/console/graphical/resource/terminal.png")), true, false);
			//terminalIcon = reader.read(0);
			terminalIcon = ImageIO.read(VT.class.getResourceAsStream("/org/vate/console/graphical/resource/terminal.png"));
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			terminalIcon = null;
		}
		
		try
		{
			//reader.setInput(ImageIO.createImageInputStream(VT.class.getResourceAsStream("/org/vate/console/graphical/resource/desktop.png")), true, false);
			//desktopIcon = reader.read(0);
			desktopIcon = ImageIO.read(VT.class.getResourceAsStream("/org/vate/console/graphical/resource/desktop.png"));
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			desktopIcon = null;
		}
	}
}