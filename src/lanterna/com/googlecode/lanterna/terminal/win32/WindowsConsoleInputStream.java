package com.googlecode.lanterna.terminal.win32;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import org.vash.vate.compatibility.VTArrays;
import org.vash.vate.console.VTSystemConsole;

import com.googlecode.lanterna.terminal.win32.WinDef.INPUT_RECORD;
import com.googlecode.lanterna.terminal.win32.WinDef.KEY_EVENT_RECORD;
import com.googlecode.lanterna.terminal.win32.WinDef.MOUSE_EVENT_RECORD;
import com.googlecode.lanterna.terminal.win32.WinDef.WINDOW_BUFFER_SIZE_RECORD;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

public class WindowsConsoleInputStream extends InputStream {

	private final HANDLE hConsoleInput;
	private final Charset encoderCharset;
	private ByteBuffer buffer = ByteBuffer.allocate(0);

	public WindowsConsoleInputStream(Charset encoderCharset) {
		this(Wincon.INSTANCE.GetStdHandle(Wincon.STD_INPUT_HANDLE), encoderCharset);
	}

	public WindowsConsoleInputStream(HANDLE hConsoleInput, Charset encoderCharset) {
		this.hConsoleInput = hConsoleInput;
		this.encoderCharset = encoderCharset;
	}

	public HANDLE getHandle() {
		return hConsoleInput;
	}

	public Charset getEncoderCharset() {
		return encoderCharset;
	}

	private INPUT_RECORD[] readConsoleInput() throws IOException {
		INPUT_RECORD[] lpBuffer = new INPUT_RECORD[64];
		IntByReference lpNumberOfEventsRead = new IntByReference();
		if (Wincon.INSTANCE.ReadConsoleInput(hConsoleInput, lpBuffer, lpBuffer.length, lpNumberOfEventsRead)) {
			int n = lpNumberOfEventsRead.getValue();
			return VTArrays.copyOfRange(lpBuffer, 0, n);
		}
		throw new EOFException();
	}

	private int availableConsoleInput() {
		IntByReference lpcNumberOfEvents = new IntByReference();
		if (Wincon.INSTANCE.GetNumberOfConsoleInputEvents(hConsoleInput, lpcNumberOfEvents)) {
			return lpcNumberOfEvents.getValue();
		}
		return 0;
	}

	
	public synchronized int read() throws IOException {
		while (!buffer.hasRemaining()) {
			buffer = readKeyEvents(true);
		}

		return buffer.get();
	}

	
	public synchronized int read(byte[] b, int offset, int length) throws IOException {
		while (length > 0 && !buffer.hasRemaining()) {
			buffer = readKeyEvents(true);
		}

		int n = Math.min(buffer.remaining(), length);
		buffer.get(b, offset, n);
		return n;
	}

	
	public synchronized int available() throws IOException {
		if (buffer.hasRemaining()) {
			return buffer.remaining();
		}

		buffer = readKeyEvents(false);
		return buffer.remaining();
	}

	private ByteBuffer readKeyEvents(boolean blocking) throws IOException {
		StringBuilder keyEvents = new StringBuilder();

		if (blocking || availableConsoleInput() > 0) {
			for (INPUT_RECORD i : readConsoleInput()) {
				filter(i, keyEvents);
			}
		}

		return encoderCharset.encode(CharBuffer.wrap(keyEvents));
	}

	private void filter(INPUT_RECORD input, Appendable keyEvents) throws IOException {
		switch (input.EventType) {
		case INPUT_RECORD.KEY_EVENT:
			if (input.Event.KeyEvent.uChar != 0 && input.Event.KeyEvent.bKeyDown) {
				keyEvents.append(input.Event.KeyEvent.uChar);
			}
			if (keyEventHandler != null) {
				keyEventHandler.accept(input.Event.KeyEvent);
			}
			break;
		case INPUT_RECORD.MOUSE_EVENT:
		  MOUSE_EVENT_RECORD mouseEventRecord = input.Event.MouseEvent;
		  keyEvents.append(convertMouseEventRecord(mouseEventRecord));
		  //VTSystemConsole.setTitle(convertMouseEventRecord(mouseEventRecord));
			if (mouseEventHandler != null) {
				mouseEventHandler.accept(input.Event.MouseEvent);
			}
			break;
		case INPUT_RECORD.WINDOW_BUFFER_SIZE_EVENT:
			if (windowBufferSizeEventHandler != null) {
				windowBufferSizeEventHandler.accept(input.Event.WindowBufferSizeEvent);
			}
			break;
		}
	}

	private Consumer<KEY_EVENT_RECORD> keyEventHandler = null;
	private Consumer<MOUSE_EVENT_RECORD> mouseEventHandler = null;
	private Consumer<WINDOW_BUFFER_SIZE_RECORD> windowBufferSizeEventHandler = null;

	public void onKeyEvent(Consumer<KEY_EVENT_RECORD> handler) {
		if (keyEventHandler == null) {
			keyEventHandler = handler;
		} else {
			keyEventHandler = keyEventHandler.andThen(handler);
		}
	}

	public void onMouseEvent(Consumer<MOUSE_EVENT_RECORD> handler) {
		if (mouseEventHandler == null) {
			mouseEventHandler = handler;
		} else {
			mouseEventHandler = mouseEventHandler.andThen(handler);
		}
	}

	public void onWindowBufferSizeEvent(Consumer<WINDOW_BUFFER_SIZE_RECORD> handler) {
		if (windowBufferSizeEventHandler == null) {
			windowBufferSizeEventHandler = handler;
		} else {
			windowBufferSizeEventHandler = windowBufferSizeEventHandler.andThen(handler);
		}
	}
	
	//dwButtonState
	public static final int FROM_LEFT_1ST_BUTTON_PRESSED = 0x0001;  //The leftmost mouse button.
	public static final int FROM_LEFT_2ND_BUTTON_PRESSED = 0x0004; //The second button fom the left.
	public static final int FROM_LEFT_3RD_BUTTON_PRESSED = 0x0008; //The third button from the left.
  public static final int FROM_LEFT_4TH_BUTTON_PRESSED = 0x0010; //The fourth button from the left.
  public static final int RIGHTMOST_BUTTON_PRESSED = 0x0002; //The rightmost mouse button.
  
  //dwControlKeyState
  public static final int CAPSLOCK_ON = 0x0080; //The CAPS LOCK light is on.
  public static final int ENHANCED_KEY = 0x0100; //The key is enhanced. See remarks.
  public static final int LEFT_ALT_PRESSED = 0x0002; //The left ALT key is pressed.
  public static final int LEFT_CTRL_PRESSED = 0x0008; //The left CTRL key is pressed.
  public static final int NUMLOCK_ON = 0x0020; //The NUM LOCK light is on.
  public static final int RIGHT_ALT_PRESSED = 0x0001; //The right ALT key is pressed.
  public static final int RIGHT_CTRL_PRESSED = 0x0004; //The right CTRL key is pressed.
  public static final int SCROLLLOCK_ON = 0x0040; //The SCROLL LOCK light is on.
  public static final int SHIFT_PRESSED = 0x0010; //The SHIFT key is pressed.
  
  //dwEventFlags
  public static final int DOUBLE_CLICK = 0x0002; //The second click (button press) of a double-click occurred. The first click is returned as a regular button-press event.
  public static final int MOUSE_HWHEELED = 0x0008; //The horizontal mouse wheel was moved. If the high word of the dwButtonState member contains a positive value, the wheel was rotated to the right. Otherwise, the wheel was rotated to the left.
  public static final int MOUSE_MOVED = 0x0001; //A change in mouse position occurred.
  public static final int MOUSE_WHEELED = 0x0004;  //The vertical mouse wheel was moved. If the high word of the dwButtonState member contains a positive value, the wheel was rotated forward, away from the user. Otherwise, the wheel was rotated backward, toward the user.
	
  private static final String MOUSE_EVENT_EXTENDED = "\u001B[<";
  private static final String MOUSE_EVENT_PRESS_OR_MOVE = "M";
  private static final String MOUSE_EVENT_RELEASE = "m";
  
  private volatile int lastPressedMouseButton = 0;
  
	private String convertMouseEventRecord(MOUSE_EVENT_RECORD mouseEventRecord)
	{
	  String ansiEvent = "";
	  int dwButtonState = mouseEventRecord.dwButtonState;
	  //int dwControlKeyState = mouseEventRecord.dwControlKeyState;
	  int dwEventFlags = mouseEventRecord.dwEventFlags;
	  int ansiMouseButtonBits = 0;
	  int ansiMouseActionBits = 0;
	  int ansiMouseX = mouseEventRecord.dwMousePosition.X;
	  int ansiMouseY = mouseEventRecord.dwMousePosition.Y;
	  
	  if (dwEventFlags == 0) //mouse button pressed or released
	  {
	    if (dwButtonState == 0)
	    {
	      ansiMouseButtonBits = lastPressedMouseButton;
        ansiEvent += MOUSE_EVENT_EXTENDED;
        ansiEvent += ansiMouseButtonBits | ansiMouseActionBits;
        ansiEvent += ";" + ansiMouseX;
        ansiEvent += ";" + ansiMouseY;
        ansiEvent += MOUSE_EVENT_RELEASE;
	    }
	    else if (dwButtonState == FROM_LEFT_1ST_BUTTON_PRESSED)
	    {
	      ansiMouseButtonBits = 1;
	    }
	    else if (dwButtonState == RIGHTMOST_BUTTON_PRESSED)
      {
	      ansiMouseButtonBits = 0;
      }
	    else if (dwButtonState == FROM_LEFT_2ND_BUTTON_PRESSED)
	    {
	      ansiMouseButtonBits = 3;
	    }
	    else if (dwButtonState == FROM_LEFT_3RD_BUTTON_PRESSED)
      {
	      ansiMouseButtonBits = 0;
      }
	    else if (dwButtonState == FROM_LEFT_4TH_BUTTON_PRESSED)
      {
	      ansiMouseButtonBits = 0;
      }
	    lastPressedMouseButton = ansiMouseButtonBits;
	    
	    ansiEvent += MOUSE_EVENT_EXTENDED;
      ansiEvent += ansiMouseButtonBits | ansiMouseActionBits;
      ansiEvent += ";" + ansiMouseX;
      ansiEvent += ";" + ansiMouseY;
      ansiEvent += MOUSE_EVENT_PRESS_OR_MOVE;
	  }
	  else if (dwEventFlags == MOUSE_MOVED) //mouse moved position
	  {
	    ansiMouseActionBits= 0x23;
	    ansiEvent += MOUSE_EVENT_EXTENDED;
	    ansiEvent += ansiMouseButtonBits | ansiMouseActionBits;
      ansiEvent += ";" + ansiMouseX;
      ansiEvent += ";" + ansiMouseY;
      ansiEvent += MOUSE_EVENT_PRESS_OR_MOVE;
	  }
	  else if (dwEventFlags == MOUSE_WHEELED)
	  {
	    if (dwButtonState > 0)
	    {
	      ansiMouseButtonBits = 64;
	    }
	    else
	    {
	      ansiMouseButtonBits = 65;
	    }
	    ansiEvent += MOUSE_EVENT_EXTENDED;
      ansiEvent += ansiMouseButtonBits | ansiMouseActionBits;
      ansiEvent += ";" + ansiMouseX;
      ansiEvent += ";" + ansiMouseY;
      ansiEvent += MOUSE_EVENT_PRESS_OR_MOVE;
	  }
	  return ansiEvent;
	}
}
