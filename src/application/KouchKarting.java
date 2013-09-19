package application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import material.MaterialLibrary;
import math.*;
import camera.*;
import models.*;

import org.lwjgl.Sys;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;

/**
 * The "KouchKarting" Class.
 * Purpose: this is the main class for the KouchKarting
 * game. It includes main that runs the entire game, so this class must be run
 * on startup
 * 
 * @author Peter G. with help from napier @ potatoland.org
 * @version Jan. 24, 2012
 */
public class KouchKarting {

	// Byte size data: used for allocating native buffers
	private static final int SIZE_FLOAT = 4;
	private static final int SIZE_INT = 4;

	// Application settings
	// Assigned values in main() before calling run(), such as the exit key,
	// window title, if to disable or hide the native cursor, if vSync is
	// enabled or disabled, if to use the desktop display mode, if the
	// program should be full screen, the aspect ratio of the screen,
	// and the number of depth buffer bits
	private static int exitKey;
	private static String windowTitle;
	private static boolean hideNativeCursor;
	private static boolean disableNativeCursor;
	private static boolean vSyncEnabled;
	private static boolean useCurrentDisplay;
	private static boolean fullScreen;
	private static float aspectRatio;
	private static int depthBufferBits;

	// Display settings
	// The program will pick a display that best matches the specifications
	// If no such display is found, the display values are equal to -1 or
	// useCurrentDisplay is true, the program will use the desktop display
	// settings
	private static int displayWidth;
	private static int displayHeight;
	private static int displayColourBits;
	private static int displayFrequency;

	// (Part of display settings) - the viewport is the box inside the window
	// where all the drawing happens, see initDisplay()
	private static int viewportX, viewportY;
	private static int viewportW, viewportH;

	// Display modes
	private static DisplayMode displayMode;
	private static DisplayMode origDisplayMode;

	// OpenGL variables
	private static float viewAngle;
	private static float frontZ;
	private static float backZ;
	private static Hashtable OpenGLextensions;

	// Time, frame rate and other variables for calculating that
	private static long ticksPerSecond;
	private static double secsSinceLastFrame = 0;
	private static double lastFrameTime = 0;
	private static double avgSecsPerFrame = 0.01;
	private static double fpsToAvg = 60;

	// Other program variables
	private static int cursorX, cursorY;

	// Camera position and object
	private static Vector cameraPosition;
	private static Camera camera;

	// Variables for the models
	private static Couch playerCouch;
	private static Track track;
	private static Trees trees;
	private static Coins coins;

	// Variables used by openGL to draw objects
	private static FloatBuffer bbMatrix = allocFloats(16);
	private static FloatBuffer tmpFloats = allocFloats(4);
	private static final float[] colorBlack = { 0f, 0f, 0f, 1f };

	// Keep track of the lap times of the player
	private static float[] lapTimes = { 0f, 0f, 0f };
	private static int currentLapSection;
	private static float lapStartTime;
	private static int currentLap;

	// Variables to hold the font
	private static int fontListBase = -1;
	private static int fontTextureHandle = -1;
	private static String currentFontName;

	// Booleans for navigating the game (i.e. where to go next)
	private static boolean goToCouchMenu;
	private static boolean loading;
	private static boolean loadedMenu;
	private static boolean goToInstructions;
	private static boolean goToAbout;
	private static boolean goToMainMenu;
	private static boolean startGame;
	private static boolean resume;
	private static boolean goToPromptScreen;
	private static boolean goToPostRaceScreen;
	private static boolean restart;
	private static boolean exit;

	// Keep track of when the menu started to ensure no accidental clicks
	private static float menuStartTime;

	// Main menu background CustomImage and texture handle
	private static CustomImage menuBackground;
	private static int menuBackgroundHandle;

	// Instructions background CustomImage and texture handle
	private static CustomImage instructionsBackground;
	private static int instructionsBackgroundHandle;

	// About section background CustomImage and texture handle
	private static CustomImage aboutBackground;
	private static int aboutBackgroundHandle;

	// Cursor CustomImage and texture handle
	private static CustomImage cursorTexture;
	private static int cursorTextureHandle;

	// For each button in the menu, there are two CustomImage objects to hold
	// the images,
	// two texture handles for openGL, a Vector for the position and a boolean
	// that holds
	// if the mouse is over that button.

	// Play button
	private static CustomImage playButton;
	private static int playButtonHandle;
	private static CustomImage playButtonOver;
	private static int playButtonOverHandle;
	private static boolean onPlayButton;
	private static Vector playButtonPosition;

	// Money cheat button
	private static CustomImage cheatButton;
	private static int cheatButtonHandle;
	private static CustomImage cheatButtonOver;
	private static int cheatButtonOverHandle;
	private static boolean onCheatButton;
	private static Vector cheatButtonPosition;

	// About button
	private static CustomImage aboutButton;
	private static int aboutButtonHandle;
	private static CustomImage aboutButtonOver;
	private static int aboutButtonOverHandle;
	private static boolean onAboutButton;
	private static Vector aboutButtonPosition;

	// Help button
	private static CustomImage helpButton;
	private static int helpButtonHandle;
	private static CustomImage helpButtonOver;
	private static int helpButtonOverHandle;
	private static boolean onHelpButton;
	private static Vector helpButtonPosition;

	// Exit button
	private static CustomImage exitButton;
	private static int exitButtonHandle;
	private static CustomImage exitButtonOver;
	private static int exitButtonOverHandle;
	private static boolean onExitButton;
	private static Vector exitButtonPosition;

	// Start game button
	private static CustomImage startGameButton;
	private static int startGameHandle;
	private static CustomImage startGameButtonOver;
	private static int startGameOverHandle;
	private static boolean overStartButton;
	private static Vector startGameButtonPosition;

	// Back button
	private static CustomImage backButton;
	private static int backHandle;
	private static CustomImage backButtonOver;
	private static int backOverHandle;
	private static boolean overBackButton;
	private static Vector backButtonPosition;

	// Enter high score button
	private static CustomImage enterHighScore;
	private static int enterHighScoreHandle;
	private static Vector enterHighScorePosition;
	private static boolean overEnterHighScore;
	private static CustomImage enterHighScoreOver;
	private static int enterHighScoreOverHandle;

	// Continue button
	private static CustomImage continueButton;
	private static int continueButtonHandle;
	private static Vector continueButtonPosition;
	private static boolean overContinueButton;
	private static CustomImage continueButtonOver;
	private static int continueButtonOverHandle;

	// Go to menu button
	private static CustomImage goToMenuButton;
	private static int goToMenuHandle;
	private static Vector goToMenuPosition;
	private static boolean overGoToMenuButton;
	private static CustomImage goToMenuButtonOver;
	private static int goToMenuOverHandle;

	// Restart button
	private static CustomImage restartButton;
	private static int restartHandle;
	private static Vector restartPosition;
	private static boolean overRestartButton;
	private static CustomImage restartButtonOver;
	private static int restartOverHandle;

	// Prompt exit button
	private static CustomImage promptExitButton;
	private static int promptExitHandle;
	private static Vector promptExitPosition;
	private static boolean overPromptExitButton;
	private static CustomImage promptExitButtonOver;
	private static int promptExitOverHandle;

	// Resume button
	private static CustomImage resumeButton;
	private static int resumeHandle;
	private static Vector resumePosition;
	private static boolean overResumeButton;
	private static CustomImage resumeButtonOver;
	private static int resumeOverHandle;

	// Variables for the couch menu

	// An array of couches that display in the couch menu
	private static Couch[] menuCouches = new Couch[4];

	// An array to keep track of which couches were bought
	private static boolean[] bought = { true, false, false, false };

	// The vectors that define the characteristics of the menu couches
	private static Vector menuCouchPosition;
	private static Vector menuCouchUpVector;
	private static Vector menuCouchLookAt;
	private static Vector menuCouchRightVector;

	// The images for the unlocked and locked couches stores as CustomImage
	// objects
	private static CustomImage[] couchImages = new CustomImage[4];
	private static CustomImage[] couchLockedImages = new CustomImage[4];

	// The texture handles for the unlocked and locked couches in openGL
	private static int[] couchTextureHandles = new int[4];
	private static int[] couchLockedTextureHandles = new int[4];

	// The positions for the couch images, if the mouse is over them
	private static Vector[] couchImagePositions = new Vector[4];
	private static boolean[] onCouchButtons = { false, false, false, false };

	// CustomImages and texture handles for the empty and filled stat boxes
	private static CustomImage fillBox;
	private static int fillBoxHandle;
	private static CustomImage emptyBox;
	private static int emptyBoxHandle;

	// Maximum stats to calculate the stats of the each of the couches
	private static int maxAcceleration;
	private static int maxSpeed;
	private static int minGrassFriction;

	// Which couch is selected, the total spin of the couches and the spin speed
	private static int couchSelection;
	private static int totalSpin = 0;
	private static int spinSpeed;

	// Variables for exit prompt

	// The texture handle for the screenShot before the prompt window
	private static int promptScreenHandle;

	// CustomImage, texture and position for prompt window
	private static CustomImage promptWindow;
	private static int promptWindowHandle;
	private static Vector promptWindowPosition;

	// The total money and the money the player gets on each lap
	private static int totalMoney = 0;
	private static int[] lapMoney = new int[3];

	// Time counter to test when to end the game
	private static float timeAfterEnding = 0;

	// Variables for the post race screen

	// The player's best lap and the imported high scores and names
	private static int bestLap;
	private static String[] highScoreNames = new String[5];
	private static float[] highScores = new float[5];

	// CustomImage and texture handle for high score table and stat screen
	private static CustomImage highScoreTable;
	private static int highScoreHandle;
	private static CustomImage statScreen;
	private static int statScreenHandle;

	// Keep track if the player entered their high score and their high score
	// position
	private static boolean enteredHighScore;
	private static int highScorePosition = 0;

	/**
	 * Runs the program.
	 */
	public static void main(String[] args) {
		initVariables();
		initMethods();
		runApp();
	}

	/**
	 * Initialise all the initial variables to their values, allows for easy
	 * editing and making sure no variables are null.
	 */
	public static void initVariables() {
		// Set the exit key and window title
		exitKey = Keyboard.KEY_ESCAPE;
		windowTitle = "Kouch Karting";

		// Set the navigation variables to tell the program where to go first
		// Allows for easy debugging
		goToMainMenu = true;
		goToCouchMenu = false;
		goToInstructions = false;
		goToAbout = false;
		exit = false;
		startGame = false;
		loadedMenu = false;
		loading = true;
		resume = true;
		goToPromptScreen = false;
		goToPostRaceScreen = false;
		restart = false;
		enteredHighScore = false;

		// If the native cursor is disabled or hidden, and if vSync is enabled
		hideNativeCursor = false;
		disableNativeCursor = true;
		vSyncEnabled = true;

		// Display settings
		useCurrentDisplay = true;
		fullScreen = true;
		aspectRatio = 0f;
		depthBufferBits = 24;
		displayWidth = 1280;
		displayHeight = 1024;
		displayColourBits = -1;
		displayFrequency = -1;

		// OpenGL settings
		viewAngle = 50f;
		frontZ = 1f;
		backZ = 30000f;

		// Menu spin speed
		spinSpeed = 90;
	}

	/**
	 * Setup the rest of the settings and variables that require more extensive
	 * calculations: display settings, input, openGL, and call the timer to
	 * update once before the game starts running in order to start it.
	 */
	public static void initMethods() {
		initDisplay();
		initInput();
		initGL();
		updateTimer();
	}

	/**
	 * Initialise the game if not exiting.
	 */
	public static void initGame() {
		if (!exit) {
			setupGame();
		}
	}

	/**
	 * Initialise the display and all of its settings to fit the program display
	 * settings. Also catches any exceptions that may have occurred and exits
	 * the program if necessary.
	 */
	public static void initDisplay() {

		// Get the original display mode (the display mode before the program is
		// run)
		origDisplayMode = Display.getDisplayMode();
		System.out.println("RacingApp.initDisplay(): Current display mode is: "
				+ origDisplayMode);

		try {
			// If the program settings indicate it to use the current display
			// mode,
			// use the original display mode, otherwise find a new one
			if (useCurrentDisplay) {
				displayMode = origDisplayMode;
			} else {
				// If the program cannot find a compatible display mode for the
				// program, output an error
				if ((displayMode = getDisplayMode(displayWidth, displayHeight,
						displayColourBits, displayFrequency)) == null
						&& (displayMode = getDisplayMode(1024, 768, 32, 60)) == null
						&& (displayMode = getDisplayMode(1024, 768, 16, 60)) == null
						&& (displayMode = getDisplayMode(
								origDisplayMode.getHeight(),
								origDisplayMode.getWidth(),
								origDisplayMode.getBitsPerPixel(),
								origDisplayMode.getFrequency())) == null) {
					System.out
							.println("KouchKarting.initDisplay() error: Could not find a "
									+ "compatible display mode!");
				}
			}
			// For debugging, output the current display mode and assign values
			// to the display setting variables
			System.out
					.println("RacingApp.initDisplay(): Setting display mode to "
							+ displayMode
							+ " with pixel depth = "
							+ depthBufferBits);
			Display.setDisplayMode(displayMode);
			displayWidth = displayMode.getWidth();
			displayHeight = displayMode.getHeight();
			displayColourBits = displayMode.getBitsPerPixel();
			displayFrequency = displayMode.getFrequency();
		}
		// Catches any exceptions thrown by setDisplayMode()
		catch (Exception exception) {
			System.err
					.println("RacingApp.initDisplay() failed to creat a display: "
							+ exception);
			System.exit(1);
		}

		// Create a new OpenGl window, if the window could not be created,
		// output an System.out.println and exit the program
		try {
			Display.setTitle(windowTitle);
			Display.setFullscreen(fullScreen);
			Display.setVSyncEnabled(vSyncEnabled);
			Display.create(new PixelFormat(0, depthBufferBits, 8));
		}
		// Catches any thrown excpetions by create() or setFullScreen()
		catch (Exception exception) {
			System.err
					.println("KouchKarting.initDisplay() failed to create OpenGL window: "
							+ exception);
			System.exit(1);
		}

		// If the aspect ratio is 0, use the default one
		if (aspectRatio == 0f) {
			aspectRatio = (float) displayMode.getWidth()
					/ (float) displayMode.getHeight();
		}

		// Calculate the size of the viewport
		viewportH = displayMode.getHeight();
		viewportW = (int) (displayMode.getHeight() * aspectRatio);
		if (viewportW > displayMode.getWidth()) {
			viewportW = displayMode.getWidth();
			viewportH = (int) (displayMode.getWidth() * (1 / aspectRatio));
		}

		// Centre the viewport in the window
		viewportX = (int) ((displayMode.getWidth() - viewportW) / 2);
		viewportY = (int) ((displayMode.getHeight() - viewportH) / 2);
	}

	/**
	 * Finds a display mode compatible with the given settings.
	 * 
	 * @param width
	 *            the given required width of the display mode
	 * @param height
	 *            the given required height of the display mode
	 * @param colourBits
	 *            the given required colour bits per pixel of the display mode
	 * @param frequency
	 *            the given required frequency of the display mode
	 * @return the display mode if found, if not found return null
	 */
	public static DisplayMode getDisplayMode(int width, int height,
			int colourBits, int frequency) {
		try {
			// Import all the available display modes
			DisplayMode allDisplayModes[] = Display.getAvailableDisplayModes();
			DisplayMode foundDisplayMode = null;

			// For each available display mode
			for (int displayMode = 0; displayMode < allDisplayModes.length; displayMode++) {
				foundDisplayMode = allDisplayModes[displayMode];

				// Check if it matches the required settings and if it does,
				// return that display mode
				if (foundDisplayMode.getHeight() == height
						&& foundDisplayMode.getWidth() == width
						&& foundDisplayMode.getBitsPerPixel() == colourBits
						&& foundDisplayMode.getFrequency() == frequency)
					return foundDisplayMode;
			}
		}
		// Catches any exceptions thrown by getAvailableDisplayModes()
		catch (Exception exception) {
			System.out.println("KouchKarting.getDisplayMode() error: "
					+ exception);
		}
		// If no compatible display mode was found, return null
		return null;
	}

	/**
	 * Initialises all the input devices and GUI's the program uses.
	 */
	public static void initInput() {
		try {
			// Create a keyboard
			Keyboard.create();

			// If the native cursor is set to disable, disable it and set
			// the cursor position to the centre of the screen
			if (disableNativeCursor) {
				disableNativeCursor(true);
				cursorX = (int) displayMode.getWidth() / 2;
				cursorY = (int) displayMode.getHeight() / 2;
			}

			// If the native cursor is set to hide, hide it
			if (hideNativeCursor) {
				hideNativeCursor(true);
			}

			// Initialise the high-resolution timer
			ticksPerSecond = Sys.getTimerResolution();
		} catch (Exception exception) {
			System.out.println("KouchKarting.initInput() error: " + exception);
		}
	}

	/**
	 * Completely disable the hardware cursor. User won't be able to move or
	 * click outside the window, and the program can decide whether to draw an
	 * new cursor of its own as a position indicator.
	 * <P>
	 * The method can be run to both disable and re-enable the cursor.
	 * 
	 * @param disable
	 *            whether or not to disable the cursor
	 */
	public static void disableNativeCursor(boolean disable) {
		disableNativeCursor = disable;
		Mouse.setGrabbed(disable);
	}

	/**
	 * If given that the cursor should be hidden, create a new native cursor
	 * that is completely transparent, (this will make the mouse invisible
	 * inside the window, but visible outside).
	 * <P>
	 * The method can be run to both hide and unhide the cursor.
	 * 
	 * @param hide
	 *            whether or not to hide the cursor
	 */
	public static void hideNativeCursor(boolean hide) {
		hideNativeCursor = hide;

		// Check if the the current mouse supports native cursors
		// (if it allows for the creation and use of native cursors)
		if ((Cursor.getCapabilities() & Cursor.CURSOR_ONE_BIT_TRANSPARENCY) == 0) {
			System.out
					.println("KouchKarting.hideNativeCursor() : native cursor not supported in hardware");
		}
		try {
			if (hide) {
				// Initialise variables used for creating a new cursor
				Cursor cursor = null;
				int cursorImageCount = 1;
				int cursorWidth = Cursor.getMaxCursorSize();
				int cursorHeight = cursorWidth;
				IntBuffer cursorImages;
				IntBuffer cursorDelays = null;

				// Make a new cursorImage for the new cursor that is completely
				// transparent
				cursorImages = ByteBuffer
						.allocateDirect(
								cursorWidth * cursorHeight * cursorImageCount
										* SIZE_INT)
						.order(ByteOrder.nativeOrder()).asIntBuffer();
				for (int widthPos = 0; widthPos < cursorWidth; widthPos++) {
					for (int heightPos = 0; heightPos < cursorHeight; heightPos++) {
						cursorImages.put(0x00000000); // Hexadecimal used to
														// shorten amount of
														// typing
					}
				}
				cursorImages.flip();

				// Create a new cursor that is completely transparent using the
				// settings created, and turn it on
				cursor = new Cursor(Cursor.getMaxCursorSize(),
						Cursor.getMaxCursorSize(),
						Cursor.getMaxCursorSize() / 2,
						Cursor.getMaxCursorSize() / 2, cursorImageCount,
						cursorImages, cursorDelays);
				Mouse.setNativeCursor(cursor);
			}
			// If not hidden, set the native cursor to null, so that the mouse
			// uses the default cursor
			else {
				Mouse.setNativeCursor(null);
			}
		} catch (Exception exception) {
			System.out.println("KouchKarting.hidenativeCursor(): error "
					+ exception);
		}
	}

	/**
	 * Initialise all the openGL settings and modes for 3D.
	 */
	public static void initGL() {
		try {
			// Setup the depth testing
			GL11.glEnable(GL11.GL_DEPTH_TEST); // Enable depth testing
			GL11.glDepthFunc(GL11.GL_LEQUAL); // Set the type of depth testing
												// to do

			// Set up basic settings
			GL11.glClearColor(0f, 0f, 0f, 1f); // Set a blue background
			GL11.glEnable(GL11.GL_NORMALIZE); // Set forcing of all normal
												// lengths to 1
			GL11.glEnable(GL11.GL_CULL_FACE); // Don't render hidden faces
			GL11.glEnable(GL11.GL_TEXTURE_2D); // Use textures
			GL11.glEnable(GL11.GL_BLEND); // Enable blending (transparency)

			// Blend by averaging two colours together
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			// Enable alpha test so the transparent backgrounds in texture
			// images don't draw,
			// Preventing transparent areas from affecting the depth or stencil
			// buffer.
			// The alpha function will accept only fragments with alpha greater
			// than 0.
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0f);

			// Draw specular highlights on top of textures
			GL11.glLightModeli(GL12.GL_LIGHT_MODEL_COLOR_CONTROL,
					GL12.GL_SEPARATE_SPECULAR_COLOR);

			// Set the perspective quality
			GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);

			// Set the size and shape of the screen area
			GL11.glViewport(viewportX, viewportY, viewportW, viewportH);

			// Setup the projection matrix (perspective)
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GLU.gluPerspective(viewAngle, aspectRatio, frontZ, backZ);

			// Select model view for subsequent transformations
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();

			// Enable lighting and texture rendering
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_LIGHTING);

			// Enable alpha transparency (so text will have transparent
			// background)
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		} catch (Exception e) {
			System.out.println("KouchKarting.initOpenGL() error: " + e);
		}
	}

	/**
	 * Setup the game including camera, player couch, track, trees and coins.
	 * Also resets lap times and money and creates a light.
	 */
	public static void setupGame() {

		// Set loading to true to tell the menu not to load when updating
		loading = true;

		// Set lap times to 0
		for (int lap = 0; lap < lapTimes.length; lap++) {
			lapTimes[lap] = 0;
		}

		// Set lap money to 0
		for (int lap = 0; lap < lapTimes.length; lap++) {
			lapMoney[lap] = 0;
		}

		// Reset current lap and lap section
		currentLap = 0;
		currentLapSection = 4;

		// Update the load menu to say that it is loading the track
		updateLoadMenu("Loading Track...");

		// Set track position, up vector and look at vector
		Vector trackPosition = new Vector(0, 0, 0);
		Vector trackUpVector = new Vector(0, 1, 0);
		Vector trackLookAt = new Vector(0, 0, 1);

		// Create a track and make a display list
		track = new Track("Track/Track.obj", trackPosition, trackUpVector,
				trackLookAt);
		track.makeDisplayList();

		// Update the load menu to say that it is loading the couch
		updateLoadMenu("Loading Player Couch...");

		// Set kart position, up vector, look at and right vector
		Vector kartPosition = new Vector(0, 40, -100);
		Vector kartUpVector = new Vector(0, 1, 0);
		Vector kartLookAt = new Vector(0, 0, 10);
		Vector kartRightVector = new Vector(-1, 0, 0);

		// Load the couch from the menu selection and reset velocity
		playerCouch = menuCouches[couchSelection];
		playerCouch.changeVectors(kartPosition, kartUpVector, kartLookAt,
				kartRightVector);
		playerCouch.setVelocity(new Vector(0, 0, 0));

		// Update the load menu to say that it is loading the trees
		updateLoadMenu("Loading Trees...");

		// Create trees by making a new position for each tree
		Vector[] treePositions = { new Vector(-300, 0, 0),
				new Vector(-300, 0, 300), new Vector(-500, 0, 1000),
				new Vector(-400, 0, -500), new Vector(-350, 0, -250),
				new Vector(-400, 0, 1500), new Vector(300, 0, 2500),
				new Vector(-100, 0, 1450), new Vector(-100, 0, 2000),
				new Vector(-200, 0, 1250), new Vector(-250, 0, 650),
				new Vector(-500, 0, 2000), new Vector(-250, 0, 1000),
				new Vector(-375, 0, 375), new Vector(100, 0, 1875),
				new Vector(200, 0, 2250), new Vector(500, 0, 2400),
				new Vector(600, 0, 2500), new Vector(800, 0, 2650),
				new Vector(1200, 0, 2550), new Vector(1850, 0, 2450),
				new Vector(1550, 0, 2550), new Vector(2350, 0, 2550),
				new Vector(2900, 0, 2000), new Vector(2450, 0, 2200),
				new Vector(3300, 0, 1050), new Vector(3300, 0, 100),
				new Vector(3500, 0, 600), new Vector(3300, 0, -300),
				new Vector(3000, 0, 1700), new Vector(3300, 0, -800),
				new Vector(3150, 0, -1850), new Vector(3400, 0, 0),
				new Vector(3350, 0, -1250), new Vector(3050, 0, -1700),
				new Vector(2200, 0, -2100), new Vector(2700, 0, -2150),
				new Vector(1950, 0, -2200), new Vector(1100, 0, -2200),
				new Vector(650, 0, -2050), new Vector(-50, 0, -1900),
				new Vector(-400, 0, -1200), new Vector(3200, 0, 2450) };

		// Set the up vectors and look at vectors for the trees
		Vector[] treeUpVectors = new Vector[treePositions.length];
		Vector[] treeLookAts = new Vector[treePositions.length];
		for (int tree = 0; tree < treePositions.length; tree++) {
			treeUpVectors[tree] = new Vector(0, 1, 0);
			treeLookAts[tree] = new Vector(0, 0, 10);
		}

		// Create the trees class and make a display list
		trees = new Trees("Tree/Tree.obj", treePositions, treeUpVectors,
				treeLookAts);
		trees.makeDisplayList();

		// Update the load menu to say that it is loading the coins
		updateLoadMenu("Loading Coins...");

		// Just like the trees, make new coins by setting the position for each
		// coin
		Vector[] coinPositions = { new Vector(30, 5, 200),
				new Vector(30, 5, 300), new Vector(30, 5, 400),
				new Vector(-30, 5, 200), new Vector(-30, 5, 300),
				new Vector(-30, 5, 400), new Vector(0, 5, 500),
				new Vector(0, 5, 600), new Vector(20, 5, 700),
				new Vector(780, 5, 2286), new Vector(176, 5, -1692),
				new Vector(-390, 2, 2515), new Vector(917, 5, 2341),
				new Vector(68, 5, -1572), new Vector(-400, 2, 2515),
				new Vector(1092, 5, 2369), new Vector(0, 5, -476),
				new Vector(-410, 2, 2515), new Vector(2510, 5, 1743),
				new Vector(0, 5, -310), new Vector(-410, 2, 2525),
				new Vector(2703, 5, 1761), new Vector(0, 5, -697),
				new Vector(2749, 2, 941), new Vector(2740, 5, 1457),
				new Vector(649, 2, -1536), new Vector(2832, 2, 684),
				new Vector(2962, 5, 1319), new Vector(455, 2, -1376),
				new Vector(2861, 2, 352), new Vector(3163, 5, 168),
				new Vector(337, 2, -1148), new Vector(3209, 2, -1572),
				new Vector(3163, 5, 296), new Vector(254, 2, -753),
				new Vector(3054, 2, -1765), new Vector(3163, 5, 453),
				new Vector(-410, 2, 2535), new Vector(2776, 2, -1931),
				new Vector(494, 5, -1876), new Vector(-400, 2, 2535),
				new Vector(2529, 2, -1968), new Vector(337, 5, -1821),
				new Vector(-400, 2, 2525) };

		// Set the up vectors, look ats, right vectors and random spin speeds
		// for each coin
		Vector[] coinUpVectors = new Vector[coinPositions.length];
		Vector[] coinLookAts = new Vector[coinPositions.length];
		Vector[] coinRightVectors = new Vector[coinPositions.length];
		float[] coinSpinSpeeds = new float[coinPositions.length];

		for (int coin = 0; coin < coinPositions.length; coin++) {
			coinUpVectors[coin] = new Vector(0, 1, 0);
			coinLookAts[coin] = new Vector(0, 0, 10);
			coinRightVectors[coin] = new Vector(-1, 0, 0);
			coinSpinSpeeds[coin] = (float) (Math.random() * 90 + 90);
		}

		// Create the coins class and make a display list
		coins = new Coins("Coin/Coin.obj", coinPositions, coinUpVectors,
				coinLookAts, coinRightVectors, coinSpinSpeeds);
		coins.makeDisplayList();

		// Make a camera that is directly behind the couch, looking and facing
		// the same direction as the player couch
		updateLoadMenu("Setting Up OpenGL...");
		camera = new Camera(new Vector(playerCouch.getPosition().x,
				playerCouch.getPosition().y + 100,
				playerCouch.getPosition().z - 100), new Vector(
				playerCouch.getLookAt().x, playerCouch.getLookAt().y - 50,
				playerCouch.getLookAt().z), new Vector(
				playerCouch.getUpVector().x, playerCouch.getUpVector().y,
				playerCouch.getUpVector().z));

		// Create a light for the scene
		setLight(GL11.GL_LIGHT1, new float[] { 1f, 1f, 1f, 1f }, new float[] {
				0.5f, 0.5f, .53f, 1f }, new float[] { 1f, 1f, 1f, 1f },
				new float[] { 0f, 100f, 100f, 1f });

		// Set the lap start time to the current time and turn off loading
		lapStartTime = Sys.getTime();
		loading = false;
	}

	/**
	 * Sets up all the variables and loads the buttons and backgrounds to run
	 * the menu.
	 */
	public static void setupMenu() {
		// Load the main menu background
		menuBackground = new CustomImage("Menu/MainMenu.jpg", true, true);
		menuBackgroundHandle = MaterialLibrary.makeTexture(menuBackground);

		// Tell the load menu that the program is loading the buttons
		updateLoadMenu("Loading Buttons...");

		// For each button, load the two images for it and set its position

		// Play button
		playButton = new CustomImage("Menu/PlayButton.png", true, true);
		playButtonHandle = MaterialLibrary.makeTexture(playButton);
		playButtonOver = new CustomImage("Menu/PlayButtonOver.png", true, true);
		playButtonOverHandle = MaterialLibrary.makeTexture(playButtonOver);
		playButtonPosition = new Vector(viewportW / 2 - playButton.getWidth()
				/ 2, viewportH / 5 - playButton.getHeight() / 2, 0);

		// Cheat button
		cheatButton = new CustomImage("Menu/moneyCheatButton.png", true, true);
		cheatButtonHandle = MaterialLibrary.makeTexture(cheatButton);
		cheatButtonOver = new CustomImage("Menu/moneyCheatButtonOver.png",
				true, true);
		cheatButtonOverHandle = MaterialLibrary.makeTexture(cheatButtonOver);
		cheatButtonPosition = new Vector(viewportW / 2 - cheatButton.getWidth()
				/ 2, viewportH / 5 - cheatButton.getHeight() * 2, 0);

		// About button
		aboutButton = new CustomImage("Menu/AboutButton.png", true, true);
		aboutButtonHandle = MaterialLibrary.makeTexture(aboutButton);
		aboutButtonOver = new CustomImage("Menu/AboutButtonOver.png", true,
				true);
		aboutButtonOverHandle = MaterialLibrary.makeTexture(aboutButtonOver);
		aboutButtonPosition = new Vector(viewportW / 4 * 3
				- aboutButton.getWidth() / 2, viewportH / 5
				- aboutButton.getHeight() / 2, 0);

		// Help button
		helpButton = new CustomImage("Menu/HelpButton.png", true, true);
		helpButtonHandle = MaterialLibrary.makeTexture(helpButton);
		helpButtonOver = new CustomImage("Menu/HelpButtonOver.png", true, true);
		helpButtonOverHandle = MaterialLibrary.makeTexture(helpButtonOver);
		helpButtonPosition = new Vector(viewportW / 4 - helpButton.getWidth()
				/ 2, viewportH / 5 - helpButton.getHeight() / 2, 0);

		// Exit button
		exitButton = new CustomImage("Menu/ExitButton.png", true, true);
		exitButtonHandle = MaterialLibrary.makeTexture(exitButton);
		exitButtonOver = new CustomImage("Menu/ExitButtonOver.png", true, true);
		exitButtonOverHandle = MaterialLibrary.makeTexture(exitButtonOver);
		exitButtonPosition = new Vector(viewportW - exitButton.getWidth() * 3
				/ 2, viewportH - exitButton.getHeight() * 3 / 2, 0);

		// Start button
		startGameButton = new CustomImage("Menu/StartGameButton.png");
		startGameHandle = MaterialLibrary.makeTexture(startGameButton);
		startGameButtonOver = new CustomImage("Menu/StartGameButtonOver.png");
		startGameOverHandle = MaterialLibrary.makeTexture(startGameButtonOver);
		startGameButtonPosition = new Vector(viewportW - 50
				- startGameButtonOver.getWidth(), 50, 0);

		// Back button
		backButton = new CustomImage("Menu/BackButton.png");
		backHandle = MaterialLibrary.makeTexture(backButton);
		backButtonOver = new CustomImage("Menu/BackButtonOver.png");
		backOverHandle = MaterialLibrary.makeTexture(backButtonOver);
		backButtonPosition = new Vector(viewportW - 50
				- backButtonOver.getWidth(), 150, 0);

		// Tell the update menu the program is loading the cursor
		updateLoadMenu("Loading Cursor");

		// Load the cursor texture
		cursorTexture = new CustomImage("Menu/Cursor.png");
		cursorTextureHandle = MaterialLibrary.makeTexture(cursorTexture);

		// Set the menu couch position
		menuCouchPosition = new Vector(0, 0, 0);
		menuCouchUpVector = new Vector(0, 1, 0);
		menuCouchLookAt = new Vector(0, 0, 1);
		menuCouchRightVector = new Vector(-1, 0, 0);

		// For each of the four couches, tell the update menu that the program
		// is loading
		// that couch, and create and load a new couch

		// The normal couch
		updateLoadMenu("Loading Couch: Normal Couch...");
		menuCouches[0] = new Couch("Couch/NormalCouch.obj", menuCouchPosition,
				menuCouchUpVector, menuCouchLookAt, menuCouchRightVector, 250,
				420, -500, 0);

		// The modern couch
		updateLoadMenu("Loading Couch: Futuristic Couch...");
		menuCouches[1] = new Couch("Couch/ModernCouch.obj", menuCouchPosition,
				menuCouchUpVector, menuCouchLookAt, menuCouchRightVector, 350,
				560, -500, 300);

		// The off road couch
		updateLoadMenu("Loading Couch: OffRoad Couch...");
		menuCouches[2] = new Couch("Couch/OffRoadCouch.obj", menuCouchPosition,
				menuCouchUpVector, menuCouchLookAt, menuCouchRightVector, 250,
				420, -200, 300);

		// The super couch
		updateLoadMenu("Loading Couch: Super Couch...");
		menuCouches[3] = new Couch("Couch/SuperCouch.obj", menuCouchPosition,
				menuCouchUpVector, menuCouchLookAt, menuCouchRightVector, 450,
				630, -250, 1000);

		// Tell the load menu that the program is making the display lists
		updateLoadMenu("Making Display Lists...");

		// For each couch, make a display list
		for (int couch = 0; couch < menuCouches.length; couch++) {
			menuCouches[couch].makeDisplayList();
		}

		// Set the maximum stats for the couches
		maxSpeed = 700;
		maxAcceleration = 500;
		minGrassFriction = -200;

		// Tell the update menu that the program is loading the couch images
		updateLoadMenu("Loading Menu Images...");

		// Load all four of the unlocked couch images
		couchImages[0] = new CustomImage("Couch/NormalCouchImage.png", true,
				true);
		couchImages[1] = new CustomImage("Couch/ModernCouchImage.png", true,
				true);
		couchImages[2] = new CustomImage("Couch/OffRoadCouchImage.png", true,
				true);
		couchImages[3] = new CustomImage("Couch/SuperCouchImage.png", true,
				true);

		// Load all four of the locked couch images
		couchLockedImages[1] = new CustomImage("Couch/ModernCouchLocked.png",
				true, true);
		couchLockedImages[2] = new CustomImage("Couch/OffRoadCouchLocked.png",
				true, true);
		couchLockedImages[3] = new CustomImage("Couch/SuperCouchLocked.png",
				true, true);

		// For each unlocked image, make a texture in openGL for it
		for (int image = 0; image < couchImages.length; image++) {
			couchTextureHandles[image] = MaterialLibrary
					.makeTexture(couchImages[image]);
		}

		// For each locked image, make a texture in openGL for it
		for (int image = 1; image < couchImages.length; image++) {
			couchLockedTextureHandles[image] = MaterialLibrary
					.makeTexture(couchLockedImages[image]);
		}

		// Load the filled and empty stat boxes and make texures for them
		fillBox = new CustomImage("Menu/BoxFilled.png");
		fillBoxHandle = MaterialLibrary.makeTexture(fillBox);
		emptyBox = new CustomImage("Menu/BoxClear.png");
		emptyBoxHandle = MaterialLibrary.makeTexture(emptyBox);

		// Set that the menu was loaded and that loading finished
		loadedMenu = true;
		loading = false;
	}

	/**
	 * Setup the couch menu, including the camera, the button positions and
	 * couch images.
	 */
	public static void setupCouchMenu() {
		// Create a new camera
		camera = new Camera(new Vector(menuCouchPosition.x + 60,
				menuCouchPosition.y + 10, menuCouchPosition.z + 200),
				new Vector(menuCouchLookAt.x, menuCouchLookAt.y - 50,
						menuCouchLookAt.z), new Vector(menuCouchUpVector.x,
						menuCouchUpVector.y, menuCouchUpVector.z));

		// Set the back button position
		backButtonPosition = new Vector(viewportW - 50
				- backButtonOver.getWidth(), 150, 0);

		// Reset the last couch used to the proper position
		menuCouches[couchSelection].changeVectors(menuCouchPosition,
				menuCouchUpVector, menuCouchLookAt, menuCouchRightVector);

		// Set each of the couch image positions
		couchImagePositions[0] = new Vector(viewportW
				- couchImages[0].getWidth() * 2 - 100, viewportH
				- couchImages[0].getHeight() - 50, 0);

		couchImagePositions[1] = new Vector(viewportW
				- couchImages[1].getWidth() - 50, viewportH
				- couchImages[1].getHeight() - 50, 0);

		couchImagePositions[2] = new Vector(viewportW
				- couchImages[2].getWidth() * 2 - 100, viewportH
				- couchImages[2].getHeight() * 2 - 150, 0);

		couchImagePositions[3] = new Vector(viewportW
				- couchImages[3].getWidth() - 50, viewportH
				- couchImages[3].getHeight() * 2 - 150, 0);

		// Create a new light for the couch menu
		setLight(GL11.GL_LIGHT1, new float[] { 1f, 1f, 1f, 1f }, new float[] {
				0.5f, 0.5f, .53f, 1f }, new float[] { 1f, 1f, 1f, 1f },
				new float[] { 0f, 100f, 100f, 1f });

		// Set the couch selection to the normal couch automatically
		couchSelection = 0;
	}

	/**
	 * Sets up the instructions section.
	 */
	public static void setupInstructions() {
		// Load the instructions background and create a texture in openGL
		instructionsBackground = new CustomImage("Menu/Instructions.png", true,
				true);
		instructionsBackgroundHandle = MaterialLibrary
				.makeTexture(instructionsBackground);

		// Set the back button position
		backButtonPosition = new Vector(900, 75, 0);
	}

	/**
	 * Sets up the about section.
	 */
	public static void setupAbout() {
		// Load the about section background and create a texture in opneGL
		aboutBackground = new CustomImage("Menu/AboutBackground.png", true,
				true);
		aboutBackgroundHandle = MaterialLibrary.makeTexture(aboutBackground);

		// Set the back button position
		backButtonPosition = new Vector(900, 75, 0);
	}

	/**
	 * Sets up the post race screen, including loading the high scores.
	 */
	public static void setupPostRaceScreen() {
		// Set that the user didn't enter their high score yet
		enteredHighScore = false;

		// Find the player's best lap time
		bestLap = 0;
		for (int lap = 1; lap < lapTimes.length; lap++) {
			if (lapTimes[lap] < lapTimes[bestLap]) {
				bestLap = lap;
			}
		}

		try {
			// Make a new buffered reader for the high scores
			BufferedReader highScoreFile = new BufferedReader(new FileReader(
					"HighScores/highScores.txt"));
			String line = "";

			// For each position in the high score table
			for (int position = 0; position < 5; position++) {

				// Read the line from the file and split it
				line = highScoreFile.readLine();
				String[] data = line.split(" ");

				// Input the data into the high scores and names arrays
				highScoreNames[position] = data[0];
				highScores[position] = new Float(data[1]);
			}
		} catch (Exception exception) {
			System.out.println("KouchKarting.setupPostRaceScreen() exception: "
					+ exception);
		}

		// Reset the high position to 0
		highScorePosition = 0;

		// Calculate the position of the player in the highScores list
		int highScoresIndex = 0;

		// If the player's best score is lower than the high score, set their
		// high score position to 5
		if (lapTimes[bestLap] > highScores[4]) {
			highScorePosition = 5;
		}

		// Otherwise, calculate the proper position
		else {
			while (lapTimes[bestLap] > highScores[highScoresIndex]) {
				highScorePosition++;
				highScoresIndex++;
			}
		}

		// Load the high score table, stat screen and menuBackground
		highScoreTable = new CustomImage("HighScores/HighScoreTable.png", true,
				true);
		highScoreHandle = MaterialLibrary.makeTexture(highScoreTable);

		statScreen = new CustomImage("HighScores/StatScreen.png", true, true);
		statScreenHandle = MaterialLibrary.makeTexture(statScreen);

		menuBackground = new CustomImage("Menu/MainMenu.jpg", true, true);
		menuBackgroundHandle = MaterialLibrary.makeTexture(menuBackground);

		// Load the high enter high score and continue buttons
		enterHighScore = new CustomImage("HighScores/EnterHighScore.png");
		enterHighScoreHandle = MaterialLibrary.makeTexture(enterHighScore);
		enterHighScoreOver = new CustomImage(
				"HighScores/EnterHighScoreOver.png");
		enterHighScoreOverHandle = MaterialLibrary
				.makeTexture(enterHighScoreOver);
		enterHighScorePosition = new Vector(300, 200, 0);

		continueButton = new CustomImage("HighScores/ContinueButton.png");
		continueButtonHandle = MaterialLibrary.makeTexture(continueButton);
		continueButtonOver = new CustomImage(
				"HighScores/ContinueButtonOver.png");
		continueButtonOverHandle = MaterialLibrary
				.makeTexture(continueButtonOver);
		continueButtonPosition = new Vector(300, 150, 0);
	}

	/**
	 * Setup the exit prompt by taking a picture of the screen and loading all
	 * the buttons.
	 */
	public static void setupExitPrompt() {
		// Take a screen shot using openGL

		// Set viewport so it matches the size of the texture needed
		GL11.glViewport(0, 0, 1024, 512);

		// Redraw the screen
		draw();

		// Create an empty texture to where the screen image will be copied
		promptScreenHandle = MaterialLibrary.allocateTexture();

		// Select that texture in openGL
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, promptScreenHandle);

		// Set texture parameters
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);

		// Copy the screen to the texture using the size indicated
		GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 0, 0, 1024,
				512, 0);

		// Make texture "anisotropic" so it will 'min'ify more gracefully
		if (KouchKarting.extensionExists("GL_EXT_texture_filter_anisotropic")) {

			// Create a float buffer for the filter
			FloatBuffer max_a = KouchKarting.allocFloats(16);

			// Grab the maximum anisotropic filter.
			GL11.glGetFloat(
					EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT,
					max_a);

			// Setup the anisotropic filter.
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D,
					EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
					max_a.get(0));
		}

		// Reset the viewport back to program settings
		GL11.glViewport(0, 0, viewportW, viewportH);

		// Load the prompt window
		promptWindow = new CustomImage("Menu/PromptWindow.png", true, true);
		promptWindowHandle = MaterialLibrary.makeTexture(promptWindow);
		promptWindowPosition = new Vector(viewportW / 2
				- promptWindow.getWidth() / 2, viewportH / 2
				- promptWindow.getHeight() / 2, 0);

		// Four each of the buttons, load the two images and set positions

		// Go to menu button
		goToMenuButton = new CustomImage("Menu/goToMenuButton.png", true, true);
		goToMenuHandle = MaterialLibrary.makeTexture(goToMenuButton);
		goToMenuButtonOver = new CustomImage("Menu/goToMenuButtonOver.png",
				true, true);
		goToMenuOverHandle = MaterialLibrary.makeTexture(goToMenuButtonOver);
		goToMenuPosition = new Vector(viewportW / 2 - goToMenuButton.getWidth()
				/ 2, viewportH / 2 + 40, 0);

		// Restart button
		restartButton = new CustomImage("Menu/restartButton.png", true, true);
		restartHandle = MaterialLibrary.makeTexture(restartButton);
		restartButtonOver = new CustomImage("Menu/restartButtonOver.png", true,
				true);
		restartOverHandle = MaterialLibrary.makeTexture(restartButtonOver);
		restartPosition = new Vector(viewportW / 2 - restartButton.getWidth()
				/ 2, viewportH / 2 - 40, 0);

		// Prompt exit button
		promptExitButton = new CustomImage("Menu/promptExitButton.png", true,
				true);
		promptExitHandle = MaterialLibrary.makeTexture(promptExitButton);
		promptExitButtonOver = new CustomImage("Menu/promptExitButtonOver.png",
				true, true);
		promptExitOverHandle = MaterialLibrary
				.makeTexture(promptExitButtonOver);
		promptExitPosition = new Vector(viewportW / 2
				- promptExitButton.getWidth() / 2, viewportH / 2 - 120, 0);

		// Resume button
		resumeButton = new CustomImage("Menu/resumeButton.png", true, true);
		resumeHandle = MaterialLibrary.makeTexture(resumeButton);
		resumeButtonOver = new CustomImage("Menu/resumeButtonOver.png", true,
				true);
		resumeOverHandle = MaterialLibrary.makeTexture(resumeButtonOver);
		resumePosition = new Vector(
				viewportW / 2 - resumeButton.getWidth() / 2,
				viewportH / 2 - 200, 0);
	}

	/**
	 * Setup the ask for high score screen, including taking a screen shot.
	 */
	public static void setupAskForHighScore() {
		// Take a screen shot using openGL

		// Set viewport so it matches the size of the texture needed
		GL11.glViewport(0, 0, 1024, 512);

		// Redraw the screen
		drawPostRaceScreen();

		// Create an empty texture to where the screen image will be copied
		promptScreenHandle = MaterialLibrary.allocateTexture();

		// Select that texture in openGL
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, promptScreenHandle);

		// Set texture parameters
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);

		// Copy the screen to the texture using the size indicated
		GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 0, 0, 1024,
				512, 0);

		// Make texture "anisotropic" so it will 'min'ify more gracefully
		if (KouchKarting.extensionExists("GL_EXT_texture_filter_anisotropic")) {

			// Create a float buffer for the filter
			FloatBuffer max_a = KouchKarting.allocFloats(16);

			// Grab the maximum anisotropic filter.
			GL11.glGetFloat(
					EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT,
					max_a);

			// Setup the anisotropic filter.
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D,
					EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
					max_a.get(0));
		}

		// Reset the viewport back to program settings
		GL11.glViewport(0, 0, viewportW, viewportH);

		// Load the ask for high score window, re-using the prompt window
		// variables
		promptWindow = new CustomImage("HighScores/AskForHighScore.png", true,
				true);
		promptWindowHandle = MaterialLibrary.makeTexture(promptWindow);
		promptWindowPosition = new Vector(viewportW / 2
				- promptWindow.getWidth() / 2, viewportH / 2
				- promptWindow.getHeight() / 2, 0);
	}

	/**
	 * Draws everything during a race.
	 */
	public static void draw() {

		// Clear the depth buffer and colour buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// Select model view for subsequent transforms
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		// Set the camera position
		cameraPosition = new Vector(playerCouch.getPosition()).sub(Vector
				.normalize(playerCouch.getRealDirection()).mult(200));
		cameraPosition.y = 75;
		camera.MoveTo(cameraPosition);

		// Set the camera view direction
		Vector cameraView = Vector.sub(playerCouch.getPosition(),
				cameraPosition).add(playerCouch.getRealDirection());
		cameraView.y = -10;
		camera.viewDir(cameraView);

		// Spin all the coins
		coins.spin();

		// Make the camera render the screen
		camera.Render();

		// Draw the objects in the scene
		drawObjects();

		// Print the lap times
		printTime();
	}

	/**
	 * Draws the objects in the game during a race.
	 */
	public static void drawObjects() {
		// Draw the track
		GL11.glPushMatrix();
		{
			// Place track
			billboardPoint(track.getPosition(), track.getLookAt(),
					track.getUpVector());
			// Draw the track
			callDisplayList(track.getDisplayListID());
			// Reset material
			setMaterial(new float[] { .8f, .8f, .7f, 1f }, .4f);
		}
		GL11.glPopMatrix();

		// For each tree
		for (int tree = 0; tree < trees.getNumberOfTrees(); tree++) {
			// Draw it
			GL11.glPushMatrix();
			{
				// Place tree
				billboardPoint(trees.getPositions()[tree],
						trees.getLookAts()[tree], trees.getUpVectors()[tree]);
				// Draw the tree
				callDisplayList(trees.getDisplayListID());
				// Reset material
				setMaterial(new float[] { .8f, .8f, .7f, 1f }, .4f);
			}
			GL11.glPopMatrix();
		}

		// For each coin
		for (int coin = 0; coin < coins.getNumberOfCoins(); coin++) {
			// If it hasn't been collected
			if (!coins.isCollected(coin)) {
				// Draw it
				GL11.glPushMatrix();
				{
					// Place coin
					billboardPoint(coins.getPositions()[coin],
							coins.getLookAts()[coin],
							coins.getUpVectors()[coin]);
					// Draw the coin
					callDisplayList(coins.getDisplayListID());
					// Reset material
					setMaterial(new float[] { .8f, .8f, .7f, 1f }, .4f);
				}
				GL11.glPopMatrix();
			}
		}

		// Draw the player couch
		GL11.glPushMatrix();
		{
			// Place couch
			billboardPoint(playerCouch.getPosition(), playerCouch.getLookAt(),
					playerCouch.getUpVector());
			// Draw the couch
			callDisplayList(playerCouch.getDisplayListID());
			// Reset material
			setMaterial(new float[] { .8f, .8f, .7f, 1f }, .4f);
		}
		GL11.glPopMatrix();
	}

	/**
	 * Given position of object and target, creates a matrix to orient the
	 * object so it faces target.
	 */
	public static void billboardPoint(Vector bbPos, Vector targetPos,
			Vector targetUp) {
		// Calculate the direction the billboard will be facing (looking):
		Vector look = Vector.sub(targetPos, bbPos).normalize();

		// Calculate the billboard right vector (perpendicular to look and
		// targetUp)
		Vector right = Vector.crossProduct(targetUp, look).normalize();

		// Calculate the billboard up vector (perpendicular to look and
		// right)
		Vector up = Vector.crossProduct(look, right).normalize();

		// Create a 4x4 matrix that will orient the object at bbPos to face
		// targetPos
		Matrix.createBillboardMatrix(bbMatrix, right, up, look, bbPos);

		// Apply the billboard matrix
		GL11.glMultMatrix(bbMatrix);
	}

	/**
	 * Draws the main menu.
	 */
	public static void drawMainMenu() {
		// Clear the last screen
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// Turn off lighting
		GL11.glDisable(GL11.GL_LIGHTING);

		// Enable alpha testing (transparent backgrounds)
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// Draw the menu background
		drawRec(menuBackgroundHandle, 0, 0, viewportW, viewportH, 1.0f);

		// Don't draw the buttons if loading (this same method will be called
		// for the loading screen)
		if (!loading) {
			// For each of the buttons, draw the correct image based on if the
			// cursor is over the button

			// About button
			if (onAboutButton) {
				drawRec(aboutButtonOverHandle, (int) aboutButtonPosition.x,
						(int) aboutButtonPosition.y,
						(float) aboutButton.getWidth(),
						(float) aboutButton.getHeight(), 1.0f);
			} else {
				drawRec(aboutButtonHandle, (int) aboutButtonPosition.x,
						(int) aboutButtonPosition.y,
						(float) aboutButton.getWidth(),
						(float) aboutButton.getHeight(), 1.0f);
			}

			// Play button
			if (onPlayButton) {
				drawRec(playButtonOverHandle, (int) playButtonPosition.x,
						(int) playButtonPosition.y,
						(float) playButton.getWidth(),
						(float) playButton.getHeight(), 1.0f);
			} else {
				drawRec(playButtonHandle, (int) playButtonPosition.x,
						(int) playButtonPosition.y,
						(float) playButton.getWidth(),
						(float) playButton.getHeight(), 1.0f);
			}

			// Money cheat button
			if (onCheatButton) {
				drawRec(cheatButtonOverHandle, (int) cheatButtonPosition.x,
						(int) cheatButtonPosition.y,
						(float) cheatButton.getWidth(),
						(float) cheatButton.getHeight(), 1.0f);
			} else {
				drawRec(cheatButtonHandle, (int) cheatButtonPosition.x,
						(int) cheatButtonPosition.y,
						(float) cheatButton.getWidth(),
						(float) cheatButton.getHeight(), 1.0f);
			}

			// Help button
			if (onHelpButton) {
				drawRec(helpButtonOverHandle, (int) helpButtonPosition.x,
						(int) helpButtonPosition.y,
						(float) helpButton.getWidth(),
						(float) helpButton.getHeight(), 1.0f);
			} else {
				drawRec(helpButtonHandle, (int) helpButtonPosition.x,
						(int) helpButtonPosition.y,
						(float) helpButton.getWidth(),
						(float) helpButton.getHeight(), 1.0f);
			}

			// Exit button
			if (onExitButton) {
				drawRec(exitButtonOverHandle, (int) exitButtonPosition.x,
						(int) exitButtonPosition.y,
						(float) exitButton.getWidth(),
						(float) exitButton.getHeight(), 1.0f);
			} else {
				drawRec(exitButtonHandle, (int) exitButtonPosition.x,
						(int) exitButtonPosition.y,
						(float) exitButton.getWidth(),
						(float) exitButton.getHeight(), 1.0f);
			}

			// Draw the cursor
			drawRec(cursorTextureHandle, cursorX,
					cursorY - cursorTexture.getHeight(),
					cursorTexture.getWidth(), cursorTexture.getHeight(), 1.0f);
		}

		GL11.glEnable(GL11.GL_LIGHTING);
	}

	/**
	 * Draws the couch menu.
	 */
	public static void drawCouchMenu() {
		// Clear the depth buffer and colour buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// Select model view for subsequent transforms
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		// Depending on the couch selection, set the camera position and view
		// direction.
		// This is because the off road couch is significantly bigger than the
		// other
		// couches
		if (couchSelection == 2) {
			cameraPosition = new Vector(menuCouchPosition.x + 30,
					menuCouchPosition.y + 15, menuCouchPosition.z + 125);
			camera.MoveTo(cameraPosition);
			Vector cameraView = new Vector(0, -1, -5).normalize();
			camera.viewDir(cameraView);
		} else {
			cameraPosition = new Vector(menuCouchPosition.x + 30,
					menuCouchPosition.y + 10, menuCouchPosition.z + 100);
			camera.MoveTo(cameraPosition);
			Vector cameraView = new Vector(0, -1, -5).normalize();
			camera.viewDir(cameraView);
		}

		// Render the camera
		camera.Render();

		// Spin the couch and store the total spin
		totalSpin += menuCouches[couchSelection].spin(spinSpeed);

		// Draw the couch menu objects
		drawCouchMenuObjects();

		// preserve current GL settings to use Ortho mode (2D)
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT
				| GL11.GL_LIGHTING_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// Turn off lighting
		GL11.glDisable(GL11.GL_LIGHTING);

		// Enable alpha testing (transparent image backgrounds)
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// For each couch
		for (int couch = 0; couch < menuCouches.length; couch++) {
			// If the couch was bought, draw the unlocked couch image
			if (bought[couch]) {
				drawRec(couchTextureHandles[couch],
						(int) couchImagePositions[couch].x,
						(int) couchImagePositions[couch].y,
						couchImages[couch].getWidth(),
						couchImages[couch].getHeight(), 1.0f);
			}
			// If the couch wasn't bought, draw the locked couch image
			else {
				drawRec(couchLockedTextureHandles[couch],
						(int) couchImagePositions[couch].x,
						(int) couchImagePositions[couch].y,
						couchImages[couch].getWidth(),
						couchImages[couch].getHeight(), 1.0f);
			}
		}

		// Print the stat titles
		print(50, 150, "Acceleration", 28, "Font/MenuFont.png");
		print(50, 100, "Speed", 28, "Font/MenuFont.png");
		print(50, 50, "Off Road", 28, "Font/MenuFont.png");

		// Draw the stat boxes for the acceleration

		// For each of the boxes that have to be filled, draw them
		for (int statBox = 0; statBox < menuCouches[couchSelection]
				.getAccelerationRate() / maxAcceleration * 10; statBox++) {
			drawRec(fillBoxHandle, 400 + statBox * (fillBox.getWidth() + 10),
					150, fillBox.getWidth(), fillBox.getHeight(), 1.0f);
		}

		// For each of the boxes that have to be empty, draw them
		for (int statBox = (int) (menuCouches[couchSelection]
				.getAccelerationRate() / maxAcceleration * 10); statBox < 10; statBox++) {
			drawRec(emptyBoxHandle, 400 + statBox * (emptyBox.getWidth() + 10),
					150, emptyBox.getWidth(), emptyBox.getHeight(), 1.0f);
		}

		// Draw the stat boxes for the speed

		// For each of the boxes that have to be filled, draw them
		for (int statBox = 0; statBox < menuCouches[couchSelection]
				.getMaxSpeed() / maxSpeed * 10; statBox++) {
			drawRec(fillBoxHandle, 400 + statBox * (fillBox.getWidth() + 10),
					100, fillBox.getWidth(), fillBox.getHeight(), 1.0f);
		}

		// For each of the boxes that have to be empty, draw them
		for (int statBox = (int) (menuCouches[couchSelection].getMaxSpeed()
				/ maxSpeed * 10); statBox < 10; statBox++) {
			drawRec(emptyBoxHandle, 400 + statBox * (emptyBox.getWidth() + 10),
					100, emptyBox.getWidth(), emptyBox.getHeight(), 1.0f);
		}

		// Draw the stat boxes for the off road

		// For each of the boxes that have to be filled, draw them
		for (int statBox = 0; statBox < minGrassFriction
				/ menuCouches[couchSelection].getGrassFriction() * 10; statBox++) {
			drawRec(fillBoxHandle, 400 + statBox * (fillBox.getWidth() + 10),
					50, fillBox.getWidth(), fillBox.getHeight(), 1.0f);
		}

		// For each of the boxes that have to be empty, draw them
		for (int statBox = (int) (minGrassFriction
				/ menuCouches[couchSelection].getGrassFriction() * 10); statBox < 10; statBox++) {
			drawRec(emptyBoxHandle, 400 + statBox * (emptyBox.getWidth() + 10),
					50, emptyBox.getWidth(), emptyBox.getHeight(), 1.0f);
		}

		// For each of the buttons below, draw the correct image based on if the
		// mouse is over it

		// Start button
		if (overStartButton) {
			drawRec(startGameOverHandle, (int) startGameButtonPosition.x,
					(int) startGameButtonPosition.y,
					startGameButtonOver.getWidth(),
					startGameButtonOver.getHeight(), 1.0f);
		} else {
			drawRec(startGameHandle, (int) startGameButtonPosition.x,
					(int) startGameButtonPosition.y,
					startGameButtonOver.getWidth(),
					startGameButtonOver.getHeight(), 1.0f);
		}

		// Over back button
		if (overBackButton) {
			drawRec(backOverHandle, (int) backButtonPosition.x,
					(int) backButtonPosition.y, backButtonOver.getWidth(),
					backButtonOver.getHeight(), 1.0f);
		} else {
			drawRec(backHandle, (int) backButtonPosition.x,
					(int) backButtonPosition.y, backButtonOver.getWidth(),
					backButtonOver.getHeight(), 1.0f);
		}

		// Draw the cursor texture
		drawRec(cursorTextureHandle, cursorX,
				cursorY - cursorTexture.getHeight(), cursorTexture.getWidth(),
				cursorTexture.getHeight(), 1.0f);

		// For each couch, print its price under the image
		for (int image = 0; image < 4; image++) {
			print((int) couchImagePositions[image].x,
					(int) couchImagePositions[image].y - 30, "$"
							+ menuCouches[image].getPrice(), 25,
					"Font/MenuFont.png");
		}

		// Print the total amount of money the player has
		print(10, viewportH - 50, "Money: $" + totalMoney, 34,
				"Font/MenuFont.png");

		// Print out the correct description of each couch based on which one is
		// selected
		if (couchSelection == 0) {
			print(30,
					250,
					"This normal couch is nothing special, but its a good start.",
					24, "Font/MenuFont.png");
		} else if (couchSelection == 1) {
			print(30,
					250,
					"This futuristic couch is lightning fast, but slow on grass.",
					24, "Font/MenuFont.png");
		} else if (couchSelection == 2) {
			print(30,
					250,
					"This heavy duty offroad couch will take you anywhere you want.",
					24, "Font/MenuFont.png");
		} else if (couchSelection == 3) {
			print(30,
					250,
					"The highest quality couch, just look at how relaxed homer is!",
					24, "Font/MenuFont.png");
		}

		GL11.glPopAttrib();

	}

	/**
	 * Draws the couch menu objects.
	 */
	public static void drawCouchMenuObjects() {
		// Draw the currently selected couch
		GL11.glPushMatrix();
		{
			// Place couch
			billboardPoint(menuCouches[couchSelection].getPosition(),
					menuCouches[couchSelection].getLookAt(),
					menuCouches[couchSelection].getUpVector());
			// Draw the couch
			callDisplayList(menuCouches[couchSelection].getDisplayListID());
			// Reset material
			setMaterial(new float[] { .8f, .8f, .7f, 1f }, .4f);
		}
		GL11.glPopMatrix();
	}

	/**
	 * Draws the exit prompt.
	 */
	public static void drawExitPrompt() {
		// Clear the last screen's colour buffer and depth buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// Preserve current GL settings
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT
				| GL11.GL_LIGHTING_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// Turn off lighting
		GL11.glDisable(GL11.GL_LIGHTING);

		// Enable alpha testing (transparent backgrounds)
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// Draw the screen shot that was taken and the prompt window
		drawRec(promptScreenHandle, 0, 0, viewportW, viewportH, 0.5f);
		drawRec(promptWindowHandle, (int) promptWindowPosition.x,
				(int) promptWindowPosition.y, promptWindow.getWidth(),
				promptWindow.getHeight(), 1.0f);

		// For each of the buttons below, draw the correct image based on if the
		// mouse is over it

		// Go to menu button
		if (overGoToMenuButton) {
			drawRec(goToMenuOverHandle, (int) goToMenuPosition.x,
					(int) goToMenuPosition.y, goToMenuButton.getWidth(),
					goToMenuButton.getHeight(), 1.0f);
		} else {
			drawRec(goToMenuHandle, (int) goToMenuPosition.x,
					(int) goToMenuPosition.y, goToMenuButton.getWidth(),
					goToMenuButton.getHeight(), 1.0f);
		}

		// Restart button
		if (overRestartButton) {
			drawRec(restartOverHandle, (int) restartPosition.x,
					(int) restartPosition.y, restartButton.getWidth(),
					restartButton.getHeight(), 1.0f);
		} else {
			drawRec(restartHandle, (int) restartPosition.x,
					(int) restartPosition.y, restartButton.getWidth(),
					restartButton.getHeight(), 1.0f);
		}

		// Prompt exit button
		if (overPromptExitButton) {
			drawRec(promptExitOverHandle, (int) promptExitPosition.x,
					(int) promptExitPosition.y, promptExitButton.getWidth(),
					promptExitButton.getHeight(), 1.0f);
		} else {
			drawRec(promptExitHandle, (int) promptExitPosition.x,
					(int) promptExitPosition.y, promptExitButton.getWidth(),
					promptExitButton.getHeight(), 1.0f);
		}

		// Resume button
		if (overResumeButton) {
			drawRec(resumeOverHandle, (int) resumePosition.x,
					(int) resumePosition.y, resumeButton.getWidth(),
					resumeButton.getHeight(), 1.0f);
		} else {
			drawRec(resumeHandle, (int) resumePosition.x,
					(int) resumePosition.y, resumeButton.getWidth(),
					resumeButton.getHeight(), 1.0f);
		}

		// Draw the cursor
		drawRec(cursorTextureHandle, cursorX,
				cursorY - cursorTexture.getHeight(), cursorTexture.getWidth(),
				cursorTexture.getHeight(), 1.0f);

		GL11.glPopAttrib();
	}

	/**
	 * This method draws the ask for high score screen using the player's name.
	 * 
	 * @param playerName
	 *            the name of the player who has the high score
	 */
	public static void drawAskHighScore(String playerName) {
		// Clear the last screen
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// preserve current GL settings
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT
				| GL11.GL_LIGHTING_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// turn off lighting
		GL11.glDisable(GL11.GL_LIGHTING);

		// Anable alpha testing (transparent backgrounds)
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// Draw the screen shot and the window prompt window overtop
		drawRec(promptScreenHandle, 0, 0, viewportW, viewportH, 0.5f);
		drawRec(promptWindowHandle, (int) promptWindowPosition.x,
				(int) promptWindowPosition.y, promptWindow.getWidth(),
				promptWindow.getHeight(), 1.0f);

		// Print the players name in the entering box
		print(viewportW / 2 - 150, viewportH / 2 - 55, playerName, 34,
				"Font/menuFont.bmp");

		// Draw the cursor
		drawRec(cursorTextureHandle, cursorX,
				cursorY - cursorTexture.getHeight(), cursorTexture.getWidth(),
				cursorTexture.getHeight(), 1.0f);

		GL11.glPopAttrib();
	}

	/**
	 * Draws the post-race screen (the screen that displays the race results and
	 * the high score table).
	 */
	public static void drawPostRaceScreen() {
		// Clear the last screen
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// Preserve current GL settings
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT
				| GL11.GL_LIGHTING_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// Turn off lighting
		GL11.glDisable(GL11.GL_LIGHTING);

		// Enable alpha testing (transparent backgrounds)
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// Draw the menu background that is used for this screen
		drawRec(menuBackgroundHandle, 0, 0, viewportW, viewportH, 1.0f);

		// Draw the high score background and the stat screen background
		drawRec(highScoreHandle, viewportW - highScoreTable.getWidth() - 100,
				100, highScoreTable.getWidth(), highScoreTable.getHeight(),
				1.0f);
		drawRec(statScreenHandle, 100, 100, statScreen.getWidth(),
				statScreen.getHeight(), 1.0f);

		// Calculate the minutes, seconds and milliseconds of the best lap
		int minutes = (int) Math.floor(lapTimes[bestLap] / 60);
		int seconds = (int) Math.floor(lapTimes[bestLap] % 60);
		int milliseconds = Math.round((lapTimes[bestLap] % 1f) * 100);

		// Using a variable for the y position of printing, you can align text
		// neatly
		// Each time a line is drawn, the y position of printing is changed
		int yPrint = 490;

		// If the player beat the minimum lap time
		if (lapTimes[bestLap] < 25) {

			// Print out that the player beat the time
			print(130, 550, "Congratulations!", 28, "Font/MenuFont.png");
			print(130, yPrint, "You beat the minimum time!", 18,
					"Font/MenuFont.png");
			yPrint -= 30;

			// Print out the best lap time of the user
			print(130, yPrint, String.format("Best Lap: %d:%02d:%02d", minutes,
					seconds, milliseconds), 18, "Font/MenuFont.png");
			yPrint -= 50;

			// If the player earned money, print out how much money they earned,
			// and if they
			// didn't, print out an appropriate message
			if (lapMoney[bestLap] > 0) {
				print(130, yPrint,
						"You also made: $" + lapMoney[bestLap] + "!", 18,
						"Font/MenuFont.png");
			} else {
				print(130, yPrint, "But, unfortunately you didn't", 18,
						"Font/MenuFont.png");
				yPrint -= 30;
				print(130, yPrint, "make any money...", 18, "Font/MenuFont.png");

			}
			yPrint -= 50;

			// If the player made it into the high score list
			if (highScorePosition < 5) {
				// Print out that they made it and their position
				print(130, yPrint, "You also made it into the", 18,
						"Font/MenuFont.png");
				yPrint -= 30;
				print(130, yPrint, "High Scores Table!", 18,
						"Font/MenuFont.png");
				yPrint -= 30;
				print(130, yPrint, "You are in position: "
						+ (highScorePosition + 1), 18, "Font/MenuFont.png");

				// Draw the correct enter high score button (only if user made
				// it into high score list)
				// based on if the cursor is over it
				if (overEnterHighScore) {
					drawRec(enterHighScoreOverHandle,
							(int) enterHighScorePosition.x,
							(int) enterHighScorePosition.y,
							enterHighScore.getWidth(),
							enterHighScore.getHeight(), 1.0f);
				} else {
					drawRec(enterHighScoreHandle,
							(int) enterHighScorePosition.x,
							(int) enterHighScorePosition.y,
							enterHighScore.getWidth(),
							enterHighScore.getHeight(), 1.0f);
				}
			}
			// If the player didn't make it into the high score list, print out
			// an appropriate
			// message
			else {
				print(130, yPrint, "Unfortunately, you didn't", 18,
						"Font/MenuFont.png");
				yPrint -= 30;
				print(130, yPrint, "make it to the high score table.", 18,
						"Font/MenuFont.png");
			}
		}
		// If the player didn't beat the minimum time, print out an appropriate
		// message as well
		else {
			print(130, 550, "Nice Try!", 28, "Font/MenuFont.png");
			print(130, yPrint, "You didn't beat the minimum", 18,
					"Font/MenuFont.png");
			yPrint -= 30;
			print(130, yPrint, "required time, and therefore", 18,
					"Font/MenuFont.png");
			yPrint -= 30;
			print(130, yPrint, "can't make any money...", 18,
					"Font/MenuFont.png");
			yPrint -= 50;
			print(130, yPrint, "Good Luck next time!", 18, "Font/MenuFont.png");
		}

		// Draw the correct continue button based on if the cursor is over it
		if (overContinueButton) {
			drawRec(continueButtonOverHandle, (int) continueButtonPosition.x,
					(int) continueButtonPosition.y, continueButton.getWidth(),
					continueButton.getHeight(), 1.0f);
		} else {
			drawRec(continueButtonHandle, (int) continueButtonPosition.x,
					(int) continueButtonPosition.y, continueButton.getWidth(),
					continueButton.getHeight(), 1.0f);
		}

		// Draw the high score table using the values imported
		// Note: when the user enters a high score, the table is updated
		// automatically
		// because the array values would be changed
		for (int highScore = 0; highScore < highScoreNames.length; highScore++) {
			print(700, 450 - highScore * 70, (highScore + 1) + ". "
					+ highScoreNames[highScore], 28, "Font/MenuFont.png");
			print(850, 450 - highScore * 70 - 30,
					Float.toString(highScores[highScore]), 28,
					"Font/MenuFont.png");
		}

		// Draw the cursor
		drawRec(cursorTextureHandle, cursorX,
				cursorY - cursorTexture.getHeight(), cursorTexture.getWidth(),
				cursorTexture.getHeight(), 1.0f);

		GL11.glPopAttrib();
	}

	/**
	 * Draw the instructions screen.
	 */
	public static void drawInstructions() {
		// Clear the last screen
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// Turn off lighting
		GL11.glDisable(GL11.GL_LIGHTING);

		// Enable alpha testing (transparent backgrounds)
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// Draw the instruction screen image background
		drawRec(instructionsBackgroundHandle, 0, 0, viewportW, viewportH, 1.0f);

		// Based on if the cursor is over the it, draw the correct back button
		if (overBackButton) {
			drawRec(backOverHandle, (int) backButtonPosition.x,
					(int) backButtonPosition.y, backButtonOver.getWidth(),
					backButtonOver.getHeight(), 1.0f);
		} else {
			drawRec(backHandle, (int) backButtonPosition.x,
					(int) backButtonPosition.y, backButtonOver.getWidth(),
					backButtonOver.getHeight(), 1.0f);
		}

		// Draw the cursor
		drawRec(cursorTextureHandle, cursorX,
				cursorY - cursorTexture.getHeight(), cursorTexture.getWidth(),
				cursorTexture.getHeight(), 1.0f);

		GL11.glEnable(GL11.GL_LIGHTING);
	}

	/**
	 * Draw the about section.
	 */
	public static void drawAbout() {
		// Clear the last screen
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// Turn off lighting
		GL11.glDisable(GL11.GL_LIGHTING);

		// Enable alpha testing (transparent backgrounds)
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// Draw the about section background image
		drawRec(aboutBackgroundHandle, 0, 0, viewportW, viewportH, 1.0f);

		// Depending on if the cursor is over it, draw the correct back button
		if (overBackButton) {
			drawRec(backOverHandle, (int) backButtonPosition.x,
					(int) backButtonPosition.y, backButtonOver.getWidth(),
					backButtonOver.getHeight(), 1.0f);
		} else {
			drawRec(backHandle, (int) backButtonPosition.x,
					(int) backButtonPosition.y, backButtonOver.getWidth(),
					backButtonOver.getHeight(), 1.0f);
		}

		// Draw the cursor
		drawRec(cursorTextureHandle, cursorX,
				cursorY - cursorTexture.getHeight(), cursorTexture.getWidth(),
				cursorTexture.getHeight(), 1.0f);

		GL11.glEnable(GL11.GL_LIGHTING);
	}

	/**
	 * Creates and sets a new material in openGL.
	 * 
	 * @param surfaceColor
	 *            the colour of the surface
	 * @param shiny
	 *            how shiny the material is
	 */
	public static void setMaterial(float[] surfaceColor, float shiny) {
		// Create float buffers to import material properties
		FloatBuffer mtldiffuse = allocFloats(4);
		FloatBuffer mtlambient = allocFloats(4);
		FloatBuffer mtlspecular = allocFloats(4);
		FloatBuffer mtlemissive = allocFloats(4);
		FloatBuffer mtlshininess = allocFloats(4);

		// Make reflection a shade of gray based on the shininess
		float[] reflect = { shiny, shiny, shiny, 1 };

		// Make the ambient colour simply a darker surface colour
		float[] ambient = { surfaceColor[0] * .5f, surfaceColor[1] * .5f,
				surfaceColor[2] * .5f, 1 };

		// Transfer the surface, ambient, reflect and black colours to the
		// appropriate float buffers
		mtldiffuse.put(surfaceColor).flip();
		mtlambient.put(ambient).flip();
		mtlspecular.put(reflect).flip();
		mtlemissive.put(colorBlack).flip();

		// Calculate size of reflection by converting from 0-1 to 0-127
		int openglShininess = ((int) (shiny * 127f));

		// Transfer the shininess data to the appropriate float buffer
		if (openglShininess >= 0 && openglShininess <= 127) {
			mtlshininess.put(new float[] { openglShininess, 0, 0, 0 }).flip();
		}

		// Apply the material using the created material float buffers
		applyMaterial(mtldiffuse, mtlambient, mtlspecular, mtlemissive,
				mtlshininess);
	}

	/**
	 * This method applies the material with the given properties in the openGL
	 * environment. This material stays in the environment until lighting is
	 * turned off.
	 * 
	 * @param mtldiffuse
	 *            the diffuse colour of the material
	 * @param mtlambient
	 *            the ambient colour of the material
	 * @param mtlspecular
	 *            the specular colour of the material
	 * @param mtlemissive
	 *            the emission colour of the material
	 * @param mtlshniness
	 *            the shine colour of the material
	 */
	public static void applyMaterial(FloatBuffer mtldiffuse,
			FloatBuffer mtlambient, FloatBuffer mtlspecular,
			FloatBuffer mtlemissive, FloatBuffer mtlshininess) {

		// Apply the material properties, but only for front-facing triangles
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, mtldiffuse);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, mtlambient);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, mtlspecular);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, mtlemissive);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SHININESS, mtlshininess);
	}

	/**
	 * Turn openGL's ortho mode on (used for rendering in 2D).
	 */
	public static void turnOrthoOn() {
		// Prepare projection matrix to render in 2D
		GL11.glMatrixMode(GL11.GL_PROJECTION);

		// Preserve perspective view
		GL11.glPushMatrix();

		// Clear the perspective matrix
		GL11.glLoadIdentity();

		// Set OpenGL to ortho mode, with parameters for left, right, bottom,
		// top, front and back z
		GL11.glOrtho(0, viewportW, 0, viewportH, -500, 500);

		// Clear the modelview matrix
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		// Preserve the modelview matrix
		GL11.glPushMatrix();

		// Clear the modelview matrix
		GL11.glLoadIdentity();

		// Disable depth test so further drawing will go over the current scene
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	/**
	 * Turn openGL's ortho mode off (to draw in 3D again).
	 */
	public static void turnOrthoOff() {
		// Restore the original projection and modelview matrices
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();

		// Turn Depth Testing back on
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	/**
	 * Creates an openGL light with the given settings.
	 * 
	 * @param lightHandle
	 *            the handle for the light in openGL
	 * @param diffuseLightColour
	 *            the diffuse colour of the light
	 * @param ambientLightColour
	 *            the ambient colour of the light
	 * @param specularLightColour
	 *            the specular colour of the light
	 * @param position
	 *            the position of the light
	 */
	public static void setLight(int lightHandle, float[] diffuseLightColour,
			float[] ambientLightColour, float[] specularLightColour,
			float[] position) {
		// Create float buffers for each of the setting in order to import into
		// openGL
		FloatBuffer lightDiffuse = allocFloats(diffuseLightColour);
		FloatBuffer lightAmbient = allocFloats(ambientLightColour);
		FloatBuffer lightSpecular = allocFloats(specularLightColour);
		FloatBuffer lighttPosition = allocFloats(position);

		// Create the light in openGL using these settings
		GL11.glLight(lightHandle, GL11.GL_DIFFUSE, lightDiffuse);
		GL11.glLight(lightHandle, GL11.GL_SPECULAR, lightSpecular);
		GL11.glLight(lightHandle, GL11.GL_AMBIENT, lightAmbient);
		GL11.glLight(lightHandle, GL11.GL_POSITION, lighttPosition);
		GL11.glEnable(lightHandle);
	}

	/**
	 * Draw a textured rectangle in Ortho mode (2D) at the given xy, scaled to
	 * the given width and height. Depth test is turned off so rectangle will be
	 * drawn on top of the current scene. Rectangle will be drawn with current
	 * light and material if any are active.
	 * 
	 * @param textureHandle
	 *            the handle of the texture that will be used for the rectangle
	 * @param xPos
	 *            the x coordinate of the rectangle (bottom-left corner)
	 * @param yPos
	 *            the y coordinate of the rectangle (bottom-left corner)
	 * @param width
	 *            the width of the rectangle
	 * @param height
	 *            the height of the rectangle
	 * @param alpha
	 *            how transparent the rectangle drawn is
	 */
	public static void drawRec(int textureHandle, int xPos, int yPos,
			float width, float height, float alpha) {
		// Activate the given texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);

		// Turn ortho mode (2D) on
		turnOrthoOn();

		// Set the transparency colour of the rectangle
		GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);

		// Make the rectangle's normal face the positive z (towards user)
		GL11.glNormal3f(0.0f, 0.0f, 1.0f);

		// Draw the textured rectangle
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glTexCoord2f(0f, 0f);
			GL11.glVertex3f((float) xPos, (float) yPos, (float) 0);
			GL11.glTexCoord2f(1f, 0f);
			GL11.glVertex3f((float) xPos + width, (float) yPos, (float) 0);
			GL11.glTexCoord2f(1f, 1f);
			GL11.glVertex3f((float) xPos + width, (float) yPos + height,
					(float) 0);
			GL11.glTexCoord2f(0f, 1f);
			GL11.glVertex3f((float) xPos, (float) yPos + height, (float) 0);
		}
		GL11.glEnd();

		// Turn ortho mode (2D) off
		turnOrthoOff();
	}

	/**
	 * Render the geometry stored in a display list. Makes the program draw
	 * objects a lot faster.
	 */
	public static void callDisplayList(int displayListID) {
		GL11.glCallList(displayListID);
	}

	/**
	 * Updates the timer.
	 */
	public static void updateTimer() {
		// Calculate the time elapsed since the last frame
		secsSinceLastFrame = (Sys.getTime() - lastFrameTime) / ticksPerSecond;
		lastFrameTime = Sys.getTime();

		// Keep a moving average of frame elapsed times
		if (secsSinceLastFrame < 1) {
			avgSecsPerFrame = ((avgSecsPerFrame * fpsToAvg) + secsSinceLastFrame)
					/ (fpsToAvg + 1f);
		}
	}

	/**
	 * Handle all the inputs from the last frame during the race.
	 */
	public static void handleInputs() {
		// Search through all the keyboard inputs
		while (Keyboard.next()) {
			// If one of the keys pressed was the exit key, send the program to
			// the prompt screen
			if (Keyboard.getEventKey() == exitKey) {
				goToPromptScreen = true;
			}
		}

		// If the player pressed the up key, tell the couch to accelerate
		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			playerCouch.accelerate();
		}

		// If the player pressed the down key, tell the couch to brake
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			playerCouch.brake();
		}

		// If the player pressed the left key, tell the couch to turn left
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			playerCouch.turnLeft();
		}

		// If the player pressed the right key, tell the couch to turn right
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			playerCouch.turnRight();
		}

		// If the player pressed the reset key (r), tell the couch to reset
		if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
			playerCouch.reset();
		}
	}

	/**
	 * Handle all the main menu inputs.
	 */
	public static void handleMainMenuInputs() {
		// Search through all the keyboard inputs
		while (Keyboard.next()) {
			// If the exit key was pressed, lead the program to exit
			if (Keyboard.getEventKey() == exitKey) {
				// If the key was pressed an not released
				if (Keyboard.getEventKeyState()) {
					exit = true;
				}
			}
		}

		// If the menu screen isn't being used for loading
		if (!loading) {

			// Handle the cursor position
			handleCursorPosition();

			// For each of the buttons below, test if the cursor is over them or
			// not

			// Play button
			if (cursorX >= playButtonPosition.x
					&& cursorX <= playButtonPosition.x + playButton.getWidth()
					&& cursorY >= playButtonPosition.y
					&& cursorY <= playButtonPosition.y + playButton.getHeight()) {
				onPlayButton = true;
			} else {
				onPlayButton = false;
			}

			// Cheat button
			if (cursorX >= cheatButtonPosition.x
					&& cursorX <= cheatButtonPosition.x
							+ cheatButton.getWidth()
					&& cursorY >= cheatButtonPosition.y
					&& cursorY <= cheatButtonPosition.y
							+ cheatButton.getHeight()) {
				onCheatButton = true;
			} else {
				onCheatButton = false;
			}

			// About button
			if (cursorX >= aboutButtonPosition.x
					&& cursorX <= aboutButtonPosition.x
							+ aboutButton.getWidth()
					&& cursorY >= aboutButtonPosition.y
					&& cursorY <= aboutButtonPosition.y
							+ aboutButton.getHeight()) {
				onAboutButton = true;
			} else {
				onAboutButton = false;
			}

			// Help button
			if (cursorX >= helpButtonPosition.x
					&& cursorX <= helpButtonPosition.x + helpButton.getWidth()
					&& cursorY >= helpButtonPosition.y
					&& cursorY <= helpButtonPosition.y + helpButton.getHeight()) {
				onHelpButton = true;
			} else {
				onHelpButton = false;
			}

			// Exit button
			if (cursorX >= exitButtonPosition.x
					&& cursorX <= exitButtonPosition.x + exitButton.getWidth()
					&& cursorY >= exitButtonPosition.y
					&& cursorY <= exitButtonPosition.y + exitButton.getHeight()) {
				onExitButton = true;
			} else {
				onExitButton = false;
			}

			// For each mouse input
			while (Mouse.next()) {
				// If the right or left key was pressed
				// Note: the second check makes sure no buttons can be pressed
				// before
				// 0.2 seconds to ensure no accidental presses
				if ((Mouse.getEventButton() == 0 || Mouse.getEventButton() == 1)) {

					// If the cursor is over the play button, send the program
					// to the couch menu
					if (onPlayButton
							&& (Sys.getTime() - menuStartTime) / ticksPerSecond > 0.2) {
						goToCouchMenu = true;
					}

					// If the cursor is over the cheat button, add 1000 coins to
					// the total money
					// Note: will be called twice (once for click and once for
					// release)
					if (onCheatButton
							&& (Sys.getTime() - menuStartTime) / ticksPerSecond > 0.2) {
						totalMoney += 1000;
					}

					// If the cursor is over the exit button, lead the program
					// to exit
					if (onExitButton
							&& (Sys.getTime() - menuStartTime) / ticksPerSecond > 0.2) {
						exit = true;
					}

					// If the cursor is over the help button, lead the program
					// to the instructions
					if (onHelpButton
							&& (Sys.getTime() - menuStartTime) / ticksPerSecond > 0.2) {
						goToInstructions = true;
					}

					// If the cursor is over the about button, lead the program
					// to the about section
					if (onAboutButton
							&& (Sys.getTime() - menuStartTime) / ticksPerSecond > 0.2) {
						goToAbout = true;
					}
				}
			}
		}
	}

	/**
	 * Handle all the inputs for the couch menu.
	 */
	public static void handleCouchMenuInputs() {
		// Search through the keyboard inputs from the last frame
		while (Keyboard.next()) {
			// If the exit key was pressed, lead the program to exit
			if (Keyboard.getEventKey() == exitKey) {
				// If the key was pressed an not released
				if (Keyboard.getEventKeyState()) {
					exit = true;
				}
			}
		}

		// Handle mouse movements
		handleCursorPosition();

		// For each of the couches
		for (int couch = 0; couch < menuCouches.length; couch++) {
			// Find out if the cursor is over their image or not
			if (cursorX >= couchImagePositions[couch].x
					&& cursorX <= couchImagePositions[couch].x
							+ couchImages[couch].getWidth()
					&& cursorY >= couchImagePositions[couch].y
					&& cursorY <= couchImagePositions[couch].y
							+ couchImages[couch].getHeight()) {
				onCouchButtons[couch] = true;
			} else {
				onCouchButtons[couch] = false;
			}
		}

		// For all the buttons in the couch menu, check if the cusor is over
		// them

		// Start game button
		if (cursorX >= startGameButtonPosition.x
				&& cursorX <= startGameButtonPosition.x
						+ startGameButton.getWidth()
				&& cursorY >= startGameButtonPosition.y
				&& cursorY <= startGameButtonPosition.y
						+ startGameButton.getHeight()) {
			overStartButton = true;
		} else {
			overStartButton = false;
		}

		// Back button
		if (cursorX >= backButtonPosition.x
				&& cursorX <= backButtonPosition.x + backButton.getWidth()
				&& cursorY >= backButtonPosition.y
				&& cursorY <= backButtonPosition.y + backButton.getHeight()) {
			overBackButton = true;
		} else {
			overBackButton = false;
		}

		// Search through the mouse inputs
		while (Mouse.next()) {
			// If the right or left mouse buttons were clicked
			if ((Mouse.getEventButton() == 0 || Mouse.getEventButton() == 1)) {
				// For each couch
				for (int couch = 0; couch < menuCouches.length; couch++) {
					// If that couch's image was clicked
					if (onCouchButtons[couch]) {
						// If the couch wasn't bought, buy it if the player has
						// enough money
						if (!bought[couch]) {
							if (menuCouches[couch].getPrice() <= totalMoney) {
								totalMoney -= menuCouches[couch].getPrice();
								bought[couch] = true;
							}
						}
						// If it was bought, select it
						if (bought[couch]) {
							couchSelection = couch;
							menuCouches[couch].startSpin(totalSpin);
						}
					}
				}
				// If the cursor is over the start button, start the game
				if (overStartButton) {
					startGame = true;
				}
				// If the cursor is over the back button, send the program back
				// to the main menu
				if (overBackButton) {
					goToMainMenu = true;
				}
			}
		}
	}

	/**
	 * A method that handles all the mouse movements
	 */
	public static void handleCursorPosition() {
		// Get the mouse movement
		int mouseDX = Mouse.getDX();
		int mouseDY = Mouse.getDY();

		// Calculate the new cursor position
		cursorX += mouseDX;
		cursorY += mouseDY;

		// Keep the cursor within the screen width
		if (cursorX < 0) {
			cursorX = 0;
		} else if (cursorX > Display.getWidth()) {
			cursorX = Display.getWidth();
		}

		// Keep the cursor within the screen height
		if (cursorY < 0) {
			cursorY = 0;
		} else if (cursorY > Display.getHeight()) {
			cursorY = Display.getHeight();
		}
	}

	/**
	 * Handle all the inputs for the exit prompt.
	 */
	public static void handleExitPromptInputs() {

		// Search through all the keyboard inputs
		while (Keyboard.next()) {
			// If the exit key was pressed, lead the program to exit
			if (Keyboard.getEventKey() == exitKey) {
				// If the key was pressed an not released
				if (Keyboard.getEventKeyState()) {
					resume = true;
				}
			}
		}

		// Handle mouse movements
		handleCursorPosition();

		// For each of the buttons below, test if the cursor is over them or not

		// Go to menu button
		if (cursorX >= goToMenuPosition.x
				&& cursorX <= goToMenuPosition.x + goToMenuButton.getWidth()
				&& cursorY >= goToMenuPosition.y
				&& cursorY <= goToMenuPosition.y + goToMenuButton.getHeight()) {
			overGoToMenuButton = true;
		} else {
			overGoToMenuButton = false;
		}

		// Restart button
		if (cursorX >= restartPosition.x
				&& cursorX <= restartPosition.x + restartButton.getWidth()
				&& cursorY >= restartPosition.y
				&& cursorY <= restartPosition.y + restartButton.getHeight()) {
			overRestartButton = true;
		} else {
			overRestartButton = false;
		}

		// Prompt exit button
		if (cursorX >= promptExitPosition.x
				&& cursorX <= promptExitPosition.x
						+ promptExitButton.getWidth()
				&& cursorY >= promptExitPosition.y
				&& cursorY <= promptExitPosition.y
						+ promptExitButton.getHeight()) {
			overPromptExitButton = true;
		} else {
			overPromptExitButton = false;
		}

		// Resume button
		if (cursorX >= resumePosition.x
				&& cursorX <= resumePosition.x + resumeButton.getWidth()
				&& cursorY >= resumePosition.y
				&& cursorY <= resumePosition.y + resumeButton.getHeight()) {
			overResumeButton = true;
		} else {
			overResumeButton = false;
		}

		// For each mouse input
		while (Mouse.next()) {
			// If the right or left mouse buttons were clicked
			if ((Mouse.getEventButton() == 0 || Mouse.getEventButton() == 1)) {
				if (Mouse.getEventButtonState()) {

					// If the cursor is over the go to menu button, send the
					// program to the main menu
					if (overGoToMenuButton) {
						goToMainMenu = true;
					}

					// If the cursor is over the prompt exit button, lead the
					// program to exit
					if (overPromptExitButton) {
						exit = true;
					}

					// If the cursor is over the resume button, send the program
					// back to the game
					if (overResumeButton) {
						resume = true;
					}

					// If the cursor is over the restart button, send the
					// program to restart
					if (overRestartButton) {
						restart = true;
					}
				}
			}
		}
	}

	/**
	 * Handle all the inputs for the ask for high score screen
	 * 
	 * @param playerName
	 *            the string that holds the last player input
	 * 
	 * @return the new player's name input (note: in eclipse Strings when passed
	 *         over parameters do not stay the same as the original, so you have
	 *         to re-assign the string again after changing it)
	 */
	public static String handleAskHighScoreInputs(String playerName) {

		// Keep track if the user entered a letter
		boolean enteredNextLetter = false;

		// For each keyboard input
		while (Keyboard.next()) {

			// If the name of the input is one character long, it is a letter,
			// the user hasn't already entered a letter and player name isn't
			// too long,
			if (Character.isLetter(Keyboard.getKeyName(Keyboard.getEventKey())
					.charAt(0))
					&& Keyboard.getKeyName(Keyboard.getEventKey()).length() == 1
					&& playerName.length() < 10 && !enteredNextLetter) {

				// And if the key was pressed, not released
				if (Keyboard.getEventKeyState()) {

					// Add the character, in lower case, to the player name
					playerName = playerName
							+ Keyboard.getKeyName(Keyboard.getEventKey())
									.toLowerCase();
					enteredNextLetter = true;
				}
			}
			// If the back key was pressed and the player's name has characters
			// in it
			else if (Keyboard.getEventKey() == Keyboard.KEY_BACK
					&& playerName.length() > 0) {
				if (Keyboard.getEventKeyState()) {

					// Remove one character from the end of the player's name
					playerName = playerName.substring(0,
							playerName.length() - 1);
				}
			}
			// If the exit key was pressed, lead them program to exit the game
			else if (Keyboard.getEventKey() == exitKey) {
				if (Keyboard.getEventKeyState()) {
					goToPostRaceScreen = true;
					playerName = "";
				}
			}
			// If the return (enter) key was pressed, send the program back to
			// the post-race screen
			else if (Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
				goToPostRaceScreen = true;
			}
		}

		// Check if either shift keys is pressed
		boolean shiftKeyPressed = false;
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
				|| Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
			shiftKeyPressed = true;
		}

		// If a shift key is pressed, the players name has characters in it and
		// the player entered a character in the last frame
		if (shiftKeyPressed && playerName.length() > 0 && enteredNextLetter) {

			// Convert the last character in the players name to upper case
			playerName = playerName.substring(0, playerName.length() - 1)
					+ playerName.substring(playerName.length() - 1)
							.toUpperCase();
		}

		// Handle mouse movements
		handleCursorPosition();

		// Return the player's name to re-assign to the original variable
		return playerName;
	}

	/**
	 * Handle all the inputs in the post race screen.
	 */
	public static void handlePostRaceInputs() {

		// Search through the keyboard inputs
		while (Keyboard.next()) {
			// If the exit key was pressed, lead the program to exit
			if (Keyboard.getEventKey() == exitKey) {
				// If the key was pressed an not released
				if (Keyboard.getEventKeyState()) {
					exit = true;
				}
			}
		}

		// Handle mouse movements
		handleCursorPosition();

		// For each of the buttons below, test if the cursor is over them

		// Enter high score button
		if (cursorX >= enterHighScorePosition.x
				&& cursorX <= enterHighScorePosition.x
						+ enterHighScore.getWidth()
				&& cursorY >= enterHighScorePosition.y
				&& cursorY <= enterHighScorePosition.y
						+ enterHighScore.getHeight()) {
			overEnterHighScore = true;
		} else {
			overEnterHighScore = false;
		}

		// Continue button
		if (cursorX >= continueButtonPosition.x
				&& cursorX <= continueButtonPosition.x
						+ continueButton.getWidth()
				&& cursorY >= continueButtonPosition.y
				&& cursorY <= continueButtonPosition.y
						+ continueButton.getHeight()) {
			overContinueButton = true;
		} else {
			overContinueButton = false;
		}

		// Search through the mouse inputs
		while (Mouse.next()) {
			// If the right or left buttons were clicked
			if ((Mouse.getEventButton() == 0 || Mouse.getEventButton() == 1) && Mouse.getEventButtonState()) {
				// If the cursor is over the entered high score button, the
				// player hasn't entered their name, and the user made it into
				// the list
				if (overEnterHighScore && !enteredHighScore
						&& highScorePosition < highScores.length) {

					// Setup and run the ask for high score screen, getting the
					// players name
					setupAskForHighScore();
					String playerName = askForHighScore();

					// If a name was entered
					if (playerName != null && playerName != "") {

						// For each position in the high score table below the
						// player's position, move it down one spot
						for (int position = highScores.length - 1; position > highScorePosition; position--) {
							highScoreNames[position] = highScoreNames[position - 1];
							highScores[position] = highScores[position - 1];
						}

						// Input the player's name and score into the proper
						// position
						highScoreNames[highScorePosition] = playerName;
						highScores[highScorePosition] = lapTimes[bestLap];
						enteredHighScore = true;
					}
				}
				// If the cursor is over the continue button, send the program
				// to the main menu
				if (overContinueButton) {
					goToPostRaceScreen = false;
					goToMainMenu = true;
					totalMoney += lapMoney[bestLap];
				}
			}
		}
	}

	/**
	 * Handle all the inputs for the instructions menu.
	 */
	public static void handleInstructionsInputs() {
		// Search through the keyboard inputs
		while (Keyboard.next()) {
			// If the exit key was pressed, exit the program
			if (Keyboard.getEventKey() == exitKey) {
				// If the key was pressed an not released
				if (Keyboard.getEventKeyState()) {
					exit = true;
				}
			}
		}

		// Handle mouse movements
		handleCursorPosition();

		// Check if the cursor is over the back button or not
		if (cursorX >= backButtonPosition.x
				&& cursorX <= backButtonPosition.x + backButton.getWidth()
				&& cursorY >= backButtonPosition.y
				&& cursorY <= backButtonPosition.y + backButton.getHeight()) {
			overBackButton = true;
		} else {
			overBackButton = false;
		}

		// Search through the mouse inputs
		while (Mouse.next()) {
			// If either mouse buttons were clicked
			if ((Mouse.getEventButton() == 0 || Mouse.getEventButton() == 1)) {
				// And the cursor is over the back button, go back to the main
				// menu
				if (overBackButton) {
					goToMainMenu = true;
				}
			}
		}
	}

	/**
	 * Handle all the inputs for the about section.
	 */
	public static void handleAboutInputs() {
		// Search through the keyboard inputs
		while (Keyboard.next()) {
			// If the exit key was pressed, exit the program
			if (Keyboard.getEventKey() == exitKey) {
				// If the key was pressed an not released
				if (Keyboard.getEventKeyState()) {
					exit = true;
				}
			}
		}

		// Handle mouse movements
		handleCursorPosition();

		// Check if the cursor is over the back button or not
		if (cursorX >= backButtonPosition.x
				&& cursorX <= backButtonPosition.x + backButton.getWidth()
				&& cursorY >= backButtonPosition.y
				&& cursorY <= backButtonPosition.y + backButton.getHeight()) {
			overBackButton = true;
		} else {
			overBackButton = false;
		}

		// Search through the mouse inputs
		while (Mouse.next()) {
			// If either mouse buttons were clicked
			if ((Mouse.getEventButton() == 0 || Mouse.getEventButton() == 1)) {
				// And the cursor is over the back button, go back to the main
				// menu
				if (overBackButton) {
					goToMainMenu = true;
				}
			}
		}
	}

	/**
	 * Updates everything that has to do with the player's couch. This includes
	 * applying friction, gravity, calculating the new velocity, checking
	 * collisions, moving and turning
	 */
	public static void updateCouch() {
		playerCouch.applyFriction();
		playerCouch.applyGravity();
		playerCouch.calculateVelocity();
		playerCouch.checkCollisionsAndMove(track.getMesh());
		playerCouch.turn();
	}

	/**
	 * Checks if there are any collisions with coins and responds appropriately.
	 * Note: collisions with coins are only checked on a 2D level
	 */
	public static void checkCoinCollisions() {
		// Based on the size of the couch, change the radius of the circle being
		// checked
		int checkRadius;
		if (couchSelection == 2) {
			checkRadius = 50;
		} else if (couchSelection == 3) {
			checkRadius = 30;
		} else {
			checkRadius = 20;
		}

		// For each, if the couch is within the check radius away from it,
		// collect the coin
		for (int coin = 0; coin < coins.getNumberOfCoins(); coin++) {
			if ((coins.getPositions()[coin].z - playerCouch.getPosition().z)
					* (coins.getPositions()[coin].z - playerCouch.getPosition().z)
					+ (coins.getPositions()[coin].x - playerCouch.getPosition().x)
					* (coins.getPositions()[coin].x - playerCouch.getPosition().x) < checkRadius
					* checkRadius) {
				collectCoin(coin);
			}
		}
	}

	/**
	 * Collects the coin that was specified if the coin wasn't collected
	 * already, and adds 10 dollars to the total money for that lap
	 * 
	 * @param whichOne
	 *            which coin is being collected
	 */
	public static void collectCoin(int whichOne) {
		if (!coins.isCollected(whichOne)) {
			if (currentLap > 0 && currentLap < 4) {
				lapMoney[currentLap - 1] += 10;
			}
			coins.collectCoin(whichOne);
		}
	}

	/**
	 * Updates the race, including the couch, collisions with coins and lap
	 * times
	 */
	public static void update() {
		updateCouch();
		checkCoinCollisions();
		updateLap();
	}

	/**
	 * In order to make the loading more enjoyable, this method updates the
	 * player on what is actually being loaded at the moment
	 * 
	 * @param message
	 *            the message that should be printed on the screen
	 */
	public static void updateLoadMenu(String message) {
		// The load screen is treated as a main menu, however, none of the
		// buttons
		// or the cursor are drawn
		handleMainMenuInputs();
		drawMainMenu();
		print(viewportW / 8, viewportH / 5, message, 35, "Font/timeFont.png");
		Display.update();
	}

	/**
	 * Main method that runs the entire application (because it is not a
	 * responding program, rather a looping one)
	 */
	public static void runApp() {
		try {
			while (!exit) {
				// For each of the sections below, the loop checks where to go
				// next,
				// then sets up that section and runs it

				// The post race screen
				if (goToPostRaceScreen) {
					goToPostRaceScreen = false;
					setupPostRaceScreen();
					runPostRaceScreen();
				}

				// The main menu
				else if (goToMainMenu) {
					goToMainMenu = false;
					if (!loadedMenu) {
						setupMenu();
					}
					runMainMenu();
				}

				// The couch menu
				else if (goToCouchMenu) {
					goToCouchMenu = false;
					setupCouchMenu();
					runCouchMenu();
				}

				// The instructions screen
				else if (goToInstructions) {
					goToInstructions = false;
					setupInstructions();
					runInstructions();
				}

				// The about section
				else if (goToAbout) {
					goToAbout = false;
					setupAbout();
					runAbout();
				}

				// The main game
				else if (startGame || restart) {
					startGame = false;
					restart = false;
					initGame();
					runRace();
				}

				// If none of the sections were called (which shouldn't happen),
				// exit the program
				else {
					exit = true;
				}

			}
		} catch (Exception exception) {
			System.out.println("KouchKarting.runMenu() error: " + exception);
		}
	}

	/**
	 * Runs the main menu.
	 */
	public static void runMainMenu() {
		// Track the menu start time so that buttons aren't clicked accidentally
		menuStartTime = Sys.getTime();
		try {
			// While the program isn't told to leave the main menu
			while (!exit && !goToCouchMenu && !goToInstructions && !goToAbout) {

				// If the display is not visible (minimised), add much more
				// delay
				if (!Display.isVisible()) {
					Thread.sleep(200);
				}

				// If the display is requested to close, exit the program
				else if (Display.isCloseRequested())
					exit = true;

				// Otherwise, make the thread delay for a bit to let other
				// threads catch up
				else
					Thread.sleep(10);

				// Update the timer, handle the inputs, draw and update the
				// screen
				updateTimer();
				handleMainMenuInputs();
				drawMainMenu();
				Display.update();
			}
		} catch (Exception exception) {
			System.out
					.println("KouchKarting.runMainMenu() error: " + exception);
		}
	}

	/**
	 * Runs the couch menu.
	 */
	public static void runCouchMenu() {
		try {
			// While the program isn't told to leave the couch menu
			while (!startGame && !goToMainMenu && !exit) {

				// If the display is not visible (minimised), add much more
				// delay
				if (!Display.isVisible()) {
					Thread.sleep(200);
				}

				// If the display is requested to close, exit the program
				else if (Display.isCloseRequested())
					exit = true;

				// Otherwise, make the thread delay for a bit to let other
				// threads catch up
				else
					Thread.sleep(10);

				// Update the timer, handle the inputs, draw and update the
				// screen
				updateTimer();
				handleCouchMenuInputs();
				drawCouchMenu();
				Display.update();
			}
		} catch (Exception exception) {
			System.out.println("KouchKarting.runCouchMenu() error: "
					+ exception);
		}
	}

	/**
	 * Runs the instructions screen.
	 */
	public static void runInstructions() {
		try {
			while (!goToMainMenu && !exit) {

				// If the display is not visible (minimised), add much more
				// delay
				if (!Display.isVisible()) {
					Thread.sleep(200);
				}

				// If the display is requested to close, exit the program
				else if (Display.isCloseRequested())
					exit = true;

				// Otherwise, make the thread delay for a bit to let other
				// threads catch up
				else
					Thread.sleep(10);

				// Update the timer, handle the inputs, draw and update the
				// screen
				updateTimer();
				handleInstructionsInputs();
				drawInstructions();
				Display.update();
			}
		} catch (Exception exception) {
			System.out.println("KouchKarting.runCouchMenu() error: "
					+ exception);
		}
	}

	/**
	 * Runs the about screen.
	 */
	public static void runAbout() {
		try {
			while (!goToMainMenu && !exit) {

				// If the display is not visible (minimised), add much more
				// delay
				if (!Display.isVisible()) {
					Thread.sleep(200);
				}

				// If the display is requested to close, exit the program
				else if (Display.isCloseRequested())
					exit = true;

				// Otherwise, make the thread delay for a bit to let other
				// threads catch up
				else
					Thread.sleep(10);

				// Update the timer, handle the inputs, draw and update the
				// screen
				updateTimer();
				handleAboutInputs();
				drawAbout();
				Display.update();
			}
		} catch (Exception exception) {
			System.out.println("KouchKarting.runCouchMenu() error: "
					+ exception);
		}
	}

	/**
	 * Runs the race.
	 */
	public static void runRace() {
		try {
			while (!exit && !goToMainMenu && !goToPostRaceScreen && !restart) {

				// If the display isn't visible (minimised), delay the game more
				if (!Display.isVisible()) {
					Thread.sleep(200);
				}

				// If the display is requested to close, exit the program
				else if (Display.isCloseRequested()) {
					exit = true;
				}

				// If program has to go to the prompt screen, set it up and run
				// it
				else if (goToPromptScreen) {
					goToPromptScreen = false;
					resume = false;
					goToMainMenu = false;
					restart = false;
					setupExitPrompt();
					runExitPrompt();
					initGL();
				}

				// If none of those things occur (The race is running normally)
				else {
					// Add a little delay to let the other threads catch up
					Thread.sleep(1);

					// Update the timer, handle the inputs, draw and update the
					// screen
					updateTimer();
					handleInputs();
					update();
					draw();
					Display.update();

					// If the race is done, end the race
					if (currentLap > 3) {
						endRace();
					}
				}
			}
		} catch (Exception exception) {
			System.out.println("RacingApp.run() error: " + exception);
		}
	}

	/**
	 * Runs the exit prompt (in-game).
	 */
	public static void runExitPrompt() {
		try {
			while (!exit && !resume && !goToMainMenu && !restart) {

				// If the display is not visible (minimised), add much more
				// delay
				if (!Display.isVisible()) {
					Thread.sleep(200);
				}

				// If the display is requested to close, exit the program
				else if (Display.isCloseRequested())
					exit = true;

				// Otherwise, make the thread delay for a bit to let other
				// threads catch up
				else
					Thread.sleep(10);

				// Update the timer, handle the inputs, draw and update the
				// screen
				updateTimer();
				handleExitPromptInputs();
				drawExitPrompt();
				Display.update();
			}
		} catch (Exception exception) {
			System.out.println("exitPrompt().run() error: " + exception);
		}
	}

	/**
	 * Runs the ask for high score screen, which lets the player input their
	 * name
	 * 
	 * @return the player's name that was entered
	 */
	public static String askForHighScore() {
		String playerName = "";
		goToPostRaceScreen = false;
		try {
			while (!exit && !goToPostRaceScreen) {

				// If the display is not visible (minimised), add much more
				// delay
				if (!Display.isVisible()) {
					Thread.sleep(200);
				}

				// If the display is requested to close, exit the program
				else if (Display.isCloseRequested())
					exit = true;

				// Otherwise, make the thread delay for a bit to let other
				// threads catch up
				else
					Thread.sleep(10);

				// Update the timer, handle the inputs (and reset the player
				// name), draw and update the screen
				updateTimer();
				playerName = handleAskHighScoreInputs(playerName);
				drawAskHighScore(playerName);
				Display.update();
			}
		} catch (Exception exception) {
			System.out.println("exitPrompt().run() error: " + exception);
		}

		return playerName;
	}

	/**
	 * Runs the after-race screen containing the results and high scores
	 */
	public static void runPostRaceScreen() {
		try {
			while (!exit && !goToMainMenu) {
				// If the display is not visible (minimised), add much more
				// delay
				if (!Display.isVisible()) {
					Thread.sleep(200);
				}

				// If the display is requested to close, exit the program
				else if (Display.isCloseRequested())
					exit = true;

				// Otherwise, make the thread delay for a bit to let other
				// threads catch up
				else
					Thread.sleep(10);

				// Update the timer, handle the inputs, draw and update the
				// screen
				updateTimer();
				handlePostRaceInputs();
				drawPostRaceScreen();
				Display.update();

			}
		} catch (Exception exception) {
			System.out.println("RacingApp.run() error: " + exception);
		}

		try {
			// Create a new file writer to export the high scores
			FileWriter outFile = new FileWriter("HighScores/highScores.txt");
			PrintWriter out = new PrintWriter(outFile);

			// For each high score, print it to the new text file
			for (int highScore = 0; highScore < highScores.length; highScore++) {
				out.println(highScoreNames[highScore] + " "
						+ highScores[highScore]);
			}

			// Close the text file
			out.close();
		} catch (Exception exception) {
			System.out.println("KouchKarting.runPostRaceScreen() error: "
					+ exception);
		}
	}

	/**
	 * This method ends the race after 3 seconds passed and goes to the post
	 * race screen
	 */
	public static void endRace() {
		timeAfterEnding = (Sys.getTime() - lapStartTime) / ticksPerSecond;
		if (timeAfterEnding > 3) {
			goToPostRaceScreen = true;
		}
	}

	/**
	 * Returns the frames per second the program is currently running at
	 * 
	 * @return the frames per second
	 */
	public static double getFPS() {
		return 1 / avgSecsPerFrame;
	}

	/**
	 * Returns the average seconds it take to draw a frame
	 * 
	 * @return the average seconds per frame
	 */
	public static float getSecondsPerFrame() {
		return (float) avgSecsPerFrame;
	}

	/**
	 * Returns the number of ticks per second of the system
	 * 
	 * @return the number of ticks per second
	 */
	public static long getTicksPerSecond() {
		return ticksPerSecond;
	}

	/**
	 * Updates the lap time and controls the lap that the player is in
	 */
	public static void updateLap() {

		// Keep track if the player changed laps this frame and the player's
		// position
		boolean changedLap = false;
		Vector position = playerCouch.getPosition();

		// The track is divided up into four sections equally (like squares)
		// Based on the players position, find what section the player is in
		if (position.x < 1500) {
			if (position.z > 0) {

				// If the player moved from section 4 to section 1, they changed
				// a lap
				if (currentLapSection == 4) {
					currentLap += 1;
					changedLap = true;
				}

				currentLapSection = 1;
			} else {

				// If the player moved from section 1 to section 4, they went
				// back a lap
				if (currentLapSection == 1) {
					currentLap -= 1;
					changedLap = true;
				}

				currentLapSection = 4;
			}
		} else {
			if (position.z > 0) {
				currentLapSection = 2;
			} else {
				currentLapSection = 3;
			}
		}

		// If the player changed laps this turn
		if (changedLap) {

			// And they are in section one, it means that they went forward one
			// lap
			if (currentLapSection == 1) {

				// Reset the lap start time and coins
				lapStartTime = Sys.getTime();
				coins.collectedNone();
			}

			// Otherwise, it means that the player went back one lap
			else if (currentLap > 0) {

				// Set the lap start time to the previous lap's start time
				lapStartTime = (Sys.getTime() / ticksPerSecond - lapTimes[currentLap - 1])
						* ticksPerSecond;

				// And set the coins so that all of them appear collected
				coins.collectedAll();
			}
		}

		if (currentLap > 0 && currentLap <= lapTimes.length) {
			lapTimes[currentLap - 1] = (Sys.getTime() - lapStartTime)
					/ ticksPerSecond;
		}
	}

	/**
	 * Prints the lap times (and money for each lap) during the game
	 */
	public static void printTime() {

		// Store the minutes, seconds and milliseconds of each lap
		int[] minutes = new int[lapTimes.length];
		int[] seconds = new int[lapTimes.length];
		int[] milliseconds = new int[lapTimes.length];

		// For each lap, calculate the minutes, seconds and milliseconds
		for (int lap = 0; lap < lapTimes.length && lap < currentLap; lap++) {
			minutes[lap] = (int) Math.floor(lapTimes[lap] / 60);
			seconds[lap] = (int) Math.floor(lapTimes[lap] % 60);
			milliseconds[lap] = Math.round((lapTimes[lap] % 1f) * 100);
		}

		// Based on what lap the player is currently in, print the current lap
		// time and money, and the lap times and money for the last 3 laps
		// (empty if the player didn't drive that lap yet)
		if (currentLap > 0 && currentLap < 4) {
			print(20, viewportH - 50, String.format(
					"Current Lap: %d:%02d:%02d  Money: $%d",
					minutes[currentLap - 1], seconds[currentLap - 1],
					milliseconds[currentLap - 1], lapMoney[currentLap - 1]),
					28, "Font/timeFont.png");
		} else {
			print(20, viewportH - 50, "Current Lap: -:--:--  Money: $0", 28,
					"Font/timeFont.png");
		}
		if (currentLap > 1) {
			print(40, viewportH - 80, String.format(
					"Lap 1: %d:%02d:%02d  Money: $%d", minutes[0], seconds[0],
					milliseconds[0], lapMoney[0]), 20, "Font/timeFont.png");
		} else {
			print(40, viewportH - 80, "Lap 1: -:--:--  Money: $0", 20,
					"Font/timeFont.png");
		}
		if (currentLap > 2) {
			print(40, viewportH - 105, String.format(
					"Lap 2: %d:%02d:%02d  Money: $%d", minutes[1], seconds[1],
					milliseconds[1], lapMoney[1]), 20, "Font/timeFont.png");
		} else {
			print(40, viewportH - 105, "Lap 2: -:--:--  Money: $0", 20,
					"Font/timeFont.png");
		}
		if (currentLap > 3) {
			print(40, viewportH - 130, String.format(
					"Lap 3: %d:%02d:%02d  Money: $%d", minutes[2], seconds[2],
					milliseconds[2], lapMoney[2]), 20, "Font/timeFont.png");
		} else {
			print(40, viewportH - 130, "Lap 3: -:--:--  Money: $0", 20,
					"Font/timeFont.png");
		}
	}

	/**
	 * Build a character set from the given texture image.
	 * 
	 * @param fontName
	 *            texture image containing 256 characters in a 16x16 grid
	 *            (bitmap font)
	 * @param fontWidth
	 *            how many pixels to allow per character on screen
	 * 
	 * @return if the font was created successfully
	 */
	public static boolean buildFont(String fontName, int fontWidth) {

		// Save the current font name
		currentFontName = fontName;

		// Make a CustomImage from the font bitmap
		CustomImage textureImg = new CustomImage(fontName);
		if (textureImg == null) {
			currentFontName = "none";
			return false;
		}

		// Create a new texture from the font
		fontTextureHandle = MaterialLibrary.makeTexture(textureImg);

		// Build a character set as call list of 256 textured squares
		buildFont(fontTextureHandle, fontWidth);

		return true;
	}

	/**
	 * Render a text string in 2D over the scene, using the characters created
	 * by buildFont().
	 * 
	 * @param xPos
	 *            x position of the message
	 * @param yPos
	 *            y position of the message
	 * @param message
	 *            message to print
	 * @param fontWidth
	 *            the width of the font (size) to print with
	 * @param fontName
	 *            the name of the font you are using
	 */
	public static void print(int xPos, int yPos, String message, int fontWidth,
			String fontName) {

		// If font was not loaded, load the new font
		if (fontListBase == -1 || fontTextureHandle == -1
				|| currentFontName != fontName) {
			if (!buildFont(fontName, 16)) {
				System.out
						.println("KouchKarting.print(): character set has not been created -- see buildFont()");
				return;
			}
		}

		// Calculate the offset created by the list
		int offset = fontListBase;

		// Preserve current GL settings
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT
				| GL11.GL_LIGHTING_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// Turn off lighting
		GL11.glDisable(GL11.GL_LIGHTING);

		// Enable alpha testing, so character background is transparent
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// Select the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fontTextureHandle);

		// Turn ortho mode (2D) on
		turnOrthoOn();

		// Move the position of drawing to the given coordinates
		GL11.glTranslatef(xPos, yPos, 0);

		// Calculate the scale which ensures the proper character size
		float scale = fontWidth / 16f;

		// Scale the openGL billboard to print the text in the right size
		GL11.glScalef(scale, scale, 0);

		// For each character in the message, draw it
		for (int chr = 0; chr < message.length(); chr++) {
			GL11.glCallList(offset + message.charAt(chr));
			GL11.glTranslatef(fontWidth / scale * 0.8f, 0, 0);
		}

		// Turn ortho mode (2D) back off
		turnOrthoOff();

		// Restore previous settings
		GL11.glPopAttrib();
	}

	/********************************************************************************/
	/**
	 * The rest of the methods were coded by napier @ potatoland.org, and I give
	 * him/her many thanks as I would never have been able to code these methods
	 * myself. I did not comment these methods, and left them as napier made
	 * them as I did not want to take credit for or edit other people's work.
	 */
	/********************************************************************************/

	/**
	 * Build the character set display list from the given texture. Creates one
	 * quad for each character, with one letter textured onto each quad. Assumes
	 * the texture is a 256x256 image containing every character of the charset
	 * arranged in a 16x16 grid. Each character is 16x16 pixels.
	 * 
	 * Should be in ORTHO (2D) mode to render text.
	 * 
	 * Special thanks to NeHe and Giuseppe D'Agata for the "2D Texture Font"
	 * tutorial (http://nehe.gamedev.net).
	 * 
	 * @param charSetImage
	 *            texture image containing 256 characters in a 16x16 grid
	 * @param fontWidth
	 *            how many pixels to allow per character on screen
	 */
	public static void buildFont(int fontTxtrHandle, int fontWidth) {
		float factor = 1f / 16f;
		float cx, cy;
		fontListBase = GL11.glGenLists(256); // Creating 256 Display Lists
		for (int i = 0; i < 256; i++) {
			cx = (float) (i % 16) / 16f; // X Texture Coord Of Character (0 -
											// 1.0)
			cy = (float) (i / 16) / 16f; // Y Texture Coord Of Character (0 -
											// 1.0)
			GL11.glNewList(fontListBase + i, GL11.GL_COMPILE); // Start Building
																// A List
			GL11.glBegin(GL11.GL_QUADS); // Use A 16x16 pixel Quad For Each
											// Character
			GL11.glTexCoord2f(cx, 1 - cy - factor); // Texture Coord (Bottom
													// Left)
			GL11.glVertex2i(0, 0);
			GL11.glTexCoord2f(cx + factor, 1 - cy - factor); // Texture Coord
																// (Bottom
																// Right)
			GL11.glVertex2i(fontWidth, 0);
			GL11.glTexCoord2f(cx + factor, 1 - cy); // Texture Coord (Top Right)
			GL11.glVertex2i(fontWidth, fontWidth);
			GL11.glTexCoord2f(cx, 1 - cy); // Texture Coord (Top Left)
			GL11.glVertex2i(0, fontWidth);
			GL11.glEnd(); // Done Building Our Quad (Character)
			GL11.glEndList(); // Done Building The Display List
		} // Loop Until All 256 Are Built
	}

	/**
	 * Find a power of two equal to or greater than the given value. I.e.
	 * getPowerOfTwoBiggerThan(800) will return 1024.
	 * 
	 * @param n
	 *            the number to get a power of two greater than
	 * @return the power of two equal to or bigger than the given dimension
	 */
	public static int getPowerOfTwoBiggerThan(int n) {
		if (n < 0)
			return 0;
		--n;
		n |= n >> 1;
		n |= n >> 2;
		n |= n >> 4;
		n |= n >> 8;
		n |= n >> 16;
		return n + 1;
	}

	/**
	 * Return true if the OpenGL context supports the given OpenGL extension.
	 */
	public static boolean extensionExists(String extensionName) {
		if (OpenGLextensions == null) {
			String[] GLExtensions = GL11.glGetString(GL11.GL_EXTENSIONS).split(
					" ");
			OpenGLextensions = new Hashtable();
			for (int i = 0; i < GLExtensions.length; i++) {
				OpenGLextensions.put(GLExtensions[i].toUpperCase(), "");
			}
		}
		return (OpenGLextensions.get(extensionName.toUpperCase()) != null);
	}

	/**
	 * Create an IntBuffer with the number of integers given
	 * <P>
	 * Note: method made by napier @ potatoland.org
	 * 
	 * @param howmany
	 *            how many integers needed to be included
	 * @return the created IntBuffer
	 */
	public static IntBuffer allocInts(int howmany) {
		return ByteBuffer.allocateDirect(howmany * SIZE_INT)
				.order(ByteOrder.nativeOrder()).asIntBuffer();
	}

	/**
	 * Create an IntBuffer with the number of floats given
	 * <P>
	 * Note: method made by napier @ potatoland.org
	 * 
	 * @param howmany
	 *            how many floats needed to be included
	 * @return the created FloatBuffer
	 */
	public static FloatBuffer allocFloats(int howmany) {
		return ByteBuffer.allocateDirect(howmany * SIZE_FLOAT)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
	}

	/**
	 * Create a FloatBuffer with the same float values as in the given array
	 * 
	 * @param floatarray
	 *            the given float array containing the values
	 * @return the FloatBuffer containing the same values given
	 */
	public static FloatBuffer allocFloats(float[] floatarray) {
		FloatBuffer fb = ByteBuffer
				.allocateDirect(floatarray.length * SIZE_FLOAT)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		fb.put(floatarray).flip();
		return fb;
	}

	/**
	 * Return a String array containing the path portion of a filename
	 * (result[0]), and the filename (result[1]). If there is no path, then
	 * result[0] will be "" and result[1] will be the full filename.
	 * <p>
	 * Note: method by napier @ potatoland.org
	 * 
	 * @param filename
	 *            the name of the file to be split up
	 * @return a two element array of strings with the split up filename
	 */
	public static String[] getPathAndFile(String filename) {
		String[] pathAndFile = new String[2];
		Matcher matcher = Pattern.compile("^.*/").matcher(filename);
		if (matcher.find()) {
			pathAndFile[0] = matcher.group();
			pathAndFile[1] = filename.substring(matcher.end());
		} else {
			pathAndFile[0] = "";
			pathAndFile[1] = filename;
		}
		return pathAndFile;
	}

	/**
	 * Puts a value into a byte buffer
	 * <P>
	 * Note: method by napier @ potatoland.org
	 * 
	 * @param b
	 *            the byte buffer
	 * @param values
	 *            the values to put
	 */
	public static void put(ByteBuffer b, byte[] values) {
		b.clear();
		b.put(values).flip();
	}

	/**
	 * Puts a value into an int buffer
	 * <P>
	 * Note: method by napier @ potatoland.org
	 * 
	 * @param b
	 *            the int buffer
	 * @param values
	 *            the values to put
	 */
	public static void put(IntBuffer b, int[] values) {
		b.clear();
		b.put(values).flip();
	}

	/**
	 * Puts a value into a float buffer
	 * <P>
	 * Note: method by napier @ potatoland.org
	 * 
	 * @param b
	 *            the float buffer
	 * @param values
	 *            the values to put
	 */
	public static void put(FloatBuffer b, float[] values) {
		b.clear();
		b.put(values).flip();
	}

	/**
	 * Puts a value into a double buffer
	 * <P>
	 * Note: method by napier @ potatoland.org
	 * 
	 * @param b
	 *            the double buffer
	 * @param values
	 *            the values to put
	 */
	public static void put(DoubleBuffer b, double[] values) {
		b.clear();
		b.put(values).flip();
	}
}
