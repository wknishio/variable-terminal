package org.vash.vate;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;

import org.vash.vate.graphics.font.VTGlobalTextStyleManager;

//import com.sixlegs.png.iio.PngImageReader;
//import com.sixlegs.png.iio.PngImageReaderSpi;

@SuppressWarnings("deprecation")
public class VT
{
  public static final int VT_MAJOR_VERSION = 1;
  public static final int VT_MINOR_VERSION = 4;
  public static final int VT_REVISION_VERSION = 2;

  public static final int VT_NETWORK_PACKET_HEADER_SIZE = 8;
  public static final int VT_NETWORK_PACKET_SIZE = 1024 * 4;
  public static final int VT_NETWORK_PACKET_BUFFER_SIZE = 1024 * 64;
  public static final int VT_STANDARD_DATA_BUFFER_SIZE = 1024 * 64;
  public static final int VT_COMPRESSED_DATA_BUFFER_SIZE = 1024 * 64;
  public static final int VT_FILE_DATA_BUFFER_SIZE = 1024 * 64;
  public static final int VT_SMALL_DATA_BUFFER_SIZE = 1024 * 16;

  public static final int VT_MULTIPLEXED_CHANNEL_TYPE_PIPED = 0;
  public static final int VT_MULTIPLEXED_CHANNEL_TYPE_DIRECT = 1;
  public static final int VT_MULTIPLEXED_CHANNEL_TYPE_PERFORMANCE_THROTTLEABLE = 0 << 1;
  public static final int VT_MULTIPLEXED_CHANNEL_TYPE_PERFORMANCE_UNLIMITED = 1 << 1;
  public static final int VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_DISABLED = 0 << 2;
  public static final int VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED = 1 << 2;
  public static final int VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_LZ4 = 0 << 3;
  public static final int VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_ZLIB = 1 << 3;

  public static final int VT_CONNECTION_PROXY_NONE = 0;
  public static final int VT_CONNECTION_PROXY_SOCKS = 1;
  public static final int VT_CONNECTION_PROXY_HTTP = 2;

  public static final int VT_CONNECTION_ENCRYPT_NONE = 0;
  public static final int VT_CONNECTION_ENCRYPT_RC4 = 1;
  public static final int VT_CONNECTION_ENCRYPT_AES = 2;
  public static final int VT_CONNECTION_ENCRYPT_ISAAC = 3;
  //public static final int VT_CONNECTION_ENCRYPT_BLOWFISH = 3;
  public static final int VT_CONNECTION_ENCRYPT_SALSA = 4;
  public static final int VT_CONNECTION_ENCRYPT_HC256 = 5;
  public static final int VT_CONNECTION_ENCRYPT_GRAIN = 6;

  public static final int VT_AUTHENTICATION_TIMEOUT_MILLISECONDS = 60000;
  public static final int VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS = 60000;
  // public static final int VT_CONNECTION_TCP_ACCEPT_TIMEOUT_MILLISECONDS = 60000;
  // public static final int VT_CONNECTION_TCP_CONNECT_TIMEOUT_MILLISECONDS = 60000;
  public static final int VT_CLIENT_RECONNECTION_TIMEOUT_MILLISECONDS = 60000;
  public static final int VT_DAEMON_RECONNECTION_TIMEOUT_MILLISECONDS = 3000;

  public static final int VT_PING_SERVICE_INTERVAL_MILLISECONDS = 15000;

  public static final int VT_FILE_TRANSFER_SESSION_STARTED = 0;
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

  public static final int VT_GRAPHICS_MODE_SESSION_STARTED = 0;
  public static final int VT_GRAPHICS_MODE_SESSION_UNSTARTED = 1;
  public static final int VT_GRAPHICS_MODE_SESSION_FINISHED = 2;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_INDEPENDENT_FRAME_CUSTOM = 3;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_CUSTOM = 4;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_REFRESH_REQUEST = 5;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_CLEAR_REQUEST = 6;
  public static final int VT_GRAPHICS_MODE_MOUSE_INPUT_MOVE = 7;
  public static final int VT_GRAPHICS_MODE_MOUSE_INPUT_KEY_DOWN = 8;
  public static final int VT_GRAPHICS_MODE_MOUSE_INPUT_KEY_UP = 9;
  public static final int VT_GRAPHICS_MODE_MOUSE_INPUT_WHEEL = 10;
  public static final int VT_GRAPHICS_MODE_KEYBOARD_INPUT_KEY_DOWN = 11;
  public static final int VT_GRAPHICS_MODE_KEYBOARD_INPUT_KEY_UP = 12;
  public static final int VT_GRAPHICS_MODE_KEYBOARD_LOCK_KEY_STATE_ON = 13;
  public static final int VT_GRAPHICS_MODE_KEYBOARD_LOCK_KEY_STATE_OFF = 14;
  public static final int VT_GRAPHICS_MODE_ANY_INPUT_RELEASE_ALL_PRESSED_KEYS = 15;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_16 = 16;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_216 = 17;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_32768 = 18;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_REFRESH_MODE_INTERRUPTED = 19;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_REFRESH_MODE_ASYNCHRONOUS = 20;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_REFRESH_MODE_SYNCHRONOUS = 21;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_ON = 22;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_OFF = 23;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_INTERVAL_CHANGE = 24;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_REMOTE_INTERFACE_AREA_CHANGE = 25;
  public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_SEND_REQUEST = 26;
  public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_RECEIVE_REQUEST = 27;
  public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_CANCEL_REQUEST = 28;
  public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_STARTABLE = 29;
  public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_UNSTARTABLE = 30;
  public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_EMPTY = 31;
  public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_TEXT = 32;
  public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_IMAGE = 33;
  public static final int VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_UNSUPPORTED = 34;
  public static final int VT_GRAPHICS_MODE_CLIPBOARD_CLEAR_REQUEST = 35;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_VIEWPORT = 36;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_ENTIRE = 37;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_KEEP_RATIO = 38;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO = 39;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_AREA_CHANGE = 40;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_REFRESH_NOT_NEEDED = 41;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_CODING_DIRECT = 42;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_CODING_DYNAMIC = 43;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_CHANGE_DEVICE_DEFAULT = 44;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_CHANGE_DEVICE_NEXT = 45;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_DIFFERENCE_MIXED_CODING = 46;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_DIFFERENCE_SEPARATED_CODING = 47;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_INDEPENDENT_FRAME_IMAGE = 48;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_IMAGE = 49;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZOF = 50;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG = 51;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG = 52;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_16777216 = 53;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_8 = 54;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_SOF = 55;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_CHANGE_DEVICE_UNIFIED = 56;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_DEFAULT_SCALE = 57;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_INCREASE_SCALE = 58;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_DECREASE_SCALE = 59;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_CHANGE_DEVICE_PREVIOUS = 60;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_INCREASE = 61;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_DECREASE = 62;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_NORMALIZE = 63;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_64 = 64;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_512 = 65;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_4096 = 66;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_32 = 67;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_125 = 68;
  public static final int VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_27 = 69;

  private static final DateFormat VT_ERA_DATEFORMAT;
  private static final Calendar VT_YEAR_CALENDAR;
  public static final AudioFormat VT_AUDIO_FORMAT_8000;
  public static final AudioFormat VT_AUDIO_FORMAT_16000;
  public static final AudioFormat VT_AUDIO_FORMAT_24000;
  public static final AudioFormat VT_AUDIO_FORMAT_32000;
  public static final AudioFormat VT_AUDIO_FORMAT_48000;

  public static final Map<RenderingHints.Key, Object> VT_GRAPHICS_RENDERING_HINTS;

  static
  {
    try
    {
      System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
      LogManager logManager = java.util.logging.LogManager.getLogManager();
      Enumeration<String> loggerNames = logManager.getLoggerNames();
      java.util.logging.Logger.global.setLevel(Level.OFF);
      while (loggerNames.hasMoreElements())
      {
        String loggerName = loggerNames.nextElement();
        logManager.getLogger(loggerName).setLevel(Level.OFF);
      }
    }
    catch (Throwable t)
    {
      
    }
    
    VT_ERA_DATEFORMAT = new SimpleDateFormat("G", Locale.ENGLISH);
    VT_YEAR_CALENDAR = Calendar.getInstance();
    //float sampleRate = 8000;
    int sampleSizeInBits = 16;
    int channels = 1;
    boolean signed = true;
    boolean bigEndian = false;
    
    VT_AUDIO_FORMAT_8000 = new AudioFormat(8000, sampleSizeInBits, channels, signed, bigEndian);
    VT_AUDIO_FORMAT_16000 = new AudioFormat(16000, sampleSizeInBits, channels, signed, bigEndian);
    VT_AUDIO_FORMAT_24000 = new AudioFormat(24000, sampleSizeInBits, channels, signed, bigEndian);
    VT_AUDIO_FORMAT_32000 = new AudioFormat(32000, sampleSizeInBits, channels, signed, bigEndian);
    VT_AUDIO_FORMAT_48000 = new AudioFormat(48000, sampleSizeInBits, channels, signed, bigEndian);
    
    VT_GRAPHICS_RENDERING_HINTS = new HashMap<RenderingHints.Key, Object>();
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
  }

  public static final int VT_AUDIO_CODEC_SPEEX = 1;
  public static final int VT_AUDIO_CODEC_OPUS = 2;
  public static final int VT_AUDIO_CODEC_DEFAULT = VT_AUDIO_CODEC_OPUS;

  public static final int VT_AUDIO_CODEC_FRAME_MILLISECONDS = 20;
  public static final int VT_AUDIO_LINE_CAPTURE_BUFFER_MILLISECONDS = 320;
  public static final int VT_AUDIO_LINE_PLAYBACK_BUFFER_MILLISECONDS = 320;
  
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
    disableAccessWarnings();
    VTGlobalTextStyleManager.checkScaling();
    ImageIO.setUseCache(false);
    
    try
    {
      remoteIcon = ImageIO.read(VT.class.getResourceAsStream("/org/vash/vate/console/graphical/resource/remote.png"));
    }
    catch (Throwable e)
    {
      remoteIcon = null;
    }
    
    try
    {
      terminalIcon = ImageIO.read(VT.class.getResourceAsStream("/org/vash/vate/console/graphical/resource/terminal.png"));
    }
    catch (Throwable e)
    {
      terminalIcon = null;
    }
    
    try
    {
      desktopIcon = ImageIO.read(VT.class.getResourceAsStream("/org/vash/vate/console/graphical/resource/desktop.png"));
    }
    catch (Throwable e)
    {
      desktopIcon = null;
    }
  }
  
  public static void disableAccessWarnings()
  {
    try
    {
      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
      Field field = unsafeClass.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      Object unsafe = field.get(null);
      
      Method putObjectVolatile = unsafeClass.getDeclaredMethod("putObjectVolatile", Object.class, long.class, Object.class);
      Method staticFieldOffset = unsafeClass.getDeclaredMethod("staticFieldOffset", Field.class);
      
      Class<?> loggerClass = Class.forName("jdk.internal.module.IllegalAccessLogger");
      Field loggerField = loggerClass.getDeclaredField("logger");
      Long offset = (Long) staticFieldOffset.invoke(unsafe, loggerField);
      putObjectVolatile.invoke(unsafe, loggerClass, offset, null);
    }
    catch (Throwable ignored)
    {
      
    }
  }
}