
import java.util.ArrayList;
import javafx.scene.paint.*;
import javafx.application.Platform;
import java.util.Arrays;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;  
import javafx.application.Application;  
import javafx.stage.Stage;  
import java.io.File;  
import javafx.util.Duration;
import java.io.File;
// The model represents all the actual content and functionality of the app
// For Breakout, it manages all the game objects that the View needs
// (the bat, ball, bricks, and the score), provides methods to allow the Controller
// to move the bat (and a couple of other fucntions - change the speed or stop 
// the game), and runs a background process (a 'thread') that moves the ball 
// every 20 milliseconds and checks for collisions 
public class Model 
{
    // First,a collection of useful values for calculating sizes and layouts etc.

    public int B              = 6;          // Border round the edge of the panel
    public int M              = 40;         // Height of menu bar space at the top

    public int BALL_SIZE      = 30;         // Ball side
    public int BRICK_WIDTH    = 50;         // Brick size
    public int BRICK_HEIGHT   = 30;         // Brick Height 
    public int WALL_TOP       = 60;         // Wall Top
    public int BAT_MOVE       = 10;         // Distance to move bat on each keypress
    public int BALL_MOVE      = 3;          // Units to move the ball on each step
    public int BRICKS_ROWS    = 10;         // how many bricks are in the row.
    public int HIT_BRICK      = 50;         // Score for hitting a brick
    public int HIT_BOTTOM     = -200;        // Score (penalty) for hitting the bottom of the screen
    public int lives          =  3;         // Number of Lives in a game 
    public int diffinc        = 2;         // increase in diffucluty steps
    
    
    // The other parts of the model-view-controller setup
    View view;
    Controller controller;

    // The game 'model' - these represent the state of the game
    // and are used by the View to display it
    public GameObj ball;                // The ball
    public ArrayList<GameObj> bricks;   // The bricks
    public GameObj bat;                 // The bat
    public int score = 0;               // The score
    
    // variables that control the game 
    public boolean gameRunning = true;  // Set false to stop the game
    public boolean fast = false;        // Set true to make the ball go faster

    // initialisation parameters for the model
    public int width;                   // Width of game
    public int window_Height;                  // Height of game
    
    public Media startupMusic,
          brickHitSound,
          winningFanfare;
    public MediaPlayer background_music,
                brickhitting,
                winningsound;
    // CONSTRUCTOR - needs to know how big the window will be
    public Model( int w, int h )
    {
        Debug.trace("Model::<constructor>");  
        width = w; 
        window_Height = h;
    }

    // Initialise the game - reset the score and create the game objects 
    public void initialiseGame()
    {       
        score = 0;
        ball   = new GameObj(width/2, window_Height/2, BALL_SIZE, BALL_SIZE, Color.RED );
        bat    = new GameObj(width/2, window_Height - BRICK_HEIGHT*3/2, BRICK_WIDTH*3, 
                 BRICK_HEIGHT/4, Color.GRAY);
                 
        bricks = new ArrayList<>();
        ArrayList <Color> BRICK_COLORS = new ArrayList<Color> 
        (Arrays.asList(
               Color.web("RED"),
               Color.web("WHITE"),
               Color.web("BLUE"),
               Color.web("GREEN"),
               Color.web("YELLOW"),
               Color.web("Purple")
               ));
               
        int maxRows = BRICK_COLORS.size();
        int maxColumns = 20;
        
               BRICK_WIDTH = width/maxColumns;
               
        startupMusic = new Media(new File("media/Music.wav").toURI().toString());
        brickHitSound = new Media(new File("media/Bleep.wav").toURI().toString());
        winningFanfare = new Media(new File("media/Winning.wav").toURI().toString());
        
        MediaPlayer backgroundMusic = new MediaPlayer(startupMusic);        
        backgroundMusic.setVolume(0.2); // 20 %
        backgroundMusic.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                backgroundMusic.seek(Duration.ZERO);
            }
        });
        backgroundMusic.play();
        
        for (int i = 0; i < maxRows; i++) { 
            Color brickColor = BRICK_COLORS.get(i);
            for (int j = 0; j < maxColumns; j++) {
            GameObj brick = new GameObj(BRICK_WIDTH*j, 
           (BRICK_HEIGHT * i) + WALL_TOP + BRICKS_ROWS, BRICK_WIDTH, BRICK_HEIGHT, brickColor);
                bricks.add(brick);
            }
        }
    }
   
   

    // Animating the game
    // The game is animated by using a 'thread'. Threads allow the program to do 
    // two (or more) things at the same time. In this case the main program is
    // doing the usual thing (View waits for input, sends it to Controller,
    // Controller sends to Model, Model updates), but a second thread runs in 
    // a loop, updating the position of the ball, checking if it hits anything
    // (and changing direction if it does) and then telling the View the Model 
    // changed.
    
    // When we use more than one thread, we have to take care that they don't
    // interfere with each other (for example, one thread changing the value of 
    // a variable at the same time the other is reading it). We do this by 
    // SYNCHRONIZING methods. For any object, only one synchronized method can
    // be running at a time - if another thread tries to run the same or another
    // synchronized method on the same object, it will stop and wait for the
    // first one to finish.
    
    // Start the animation thread
    public void startGame()
    {
        
        Thread t = new Thread( this::runGame );     // create a thread runnng the runGame method
        t.setDaemon(true);                          // Tell system this thread can die when it finishes
        t.start();                                  // Start the thread running
    }   
    
    // The main animation loop
    
    public void runGame()
    {
        try
        {
            // set gameRunning true - game will stop if it is set false (eg from main thread)
            setGameRunning(true);
            while (getGameRunning())
            {
                updateGame();                        // update the game state
                modelChanged();                      // Model changed - refresh screen
                Thread.sleep( getFast() ? 10 : 20 ); // wait a few milliseconds
            }
        } catch (Exception e) 
        { 
            Debug.error("Model::runAsSeparateThread error: " + e.getMessage() );
        }
    }
  
    // updating the game - this happens about 50 times a second to give the impression of movement
    public synchronized void updateGame()
    {
        // move the ball one step (the ball knows which direction it is moving in)
        ball.moveX(BALL_MOVE);                      
        ball.moveY(BALL_MOVE);
        // get the current ball possition (top left corner)
        int x = ball.topX;  
        int y = ball.topY;
        // Deal with possible edge of board hit
        if (x >= width - B - BALL_SIZE)  ball.changeDirectionX();
        if (x <= 0 + B)  ball.changeDirectionX();
        if (y >=window_Height- B - BALL_SIZE)  // Bottom
        { 
            lives-= 1;
            ball.changeDirectionY(); 
            addToScore( HIT_BOTTOM );     // score penalty for hitting the bottom of the screen
        }
        if (y <= 0 + M)  ball.changeDirectionY();

       // check whether ball has hit a (visible) brick
        boolean hit = false;
        
       for (GameObj brick: bricks) {
            if (brick.visible && brick.hitBy(ball)) {
                hit = true;
                brick.visible = false;      // set the brick invisible
                addToScore( HIT_BRICK ); 
  
                brickhitting = new MediaPlayer(brickHitSound);
                brickhitting.setVolume(0.2);
                brickhitting.play();

                if(score%750==0 && score !=0) {
                diffinc+=2;
                BAT_MOVE+=2;
                BALL_MOVE  = diffinc;
            }
        
            
                
            }
        }    
        
        if (hit)
            ball.changeDirectionY();

        // check whether ball has hit the bat
        if ( ball.hitBy(bat) )
            ball.changeDirectionY();
    }

    // This is how the Model talks to the View
    // Whenever the Model changes, this method calls the update method in
    // the View. It needs to run in the JavaFX event thread, and Platform.runLater 
    // is a utility that makes sure this happens even if called from the
    // runGame thread
    public synchronized void modelChanged()
    {
        Platform.runLater(view::update);
    }
    
    
    // Methods for accessing and updating values
    // these are all synchronized so that the can be called by the main thread 
    // or the animation thread safely
    
    // Change game running state - set to false to stop the game
    public synchronized void setGameRunning(Boolean value)
    {  
        gameRunning = value;
    }
    
    // Return game running state
    public synchronized Boolean getGameRunning()
    {  
        return gameRunning;
    }

    // Change game speed - false is normal speed, true is fast
    public synchronized void setFast(Boolean value)
    {  
        fast = value;
    }
    
    // Return game speed - false is normal speed, true is fast
    public synchronized Boolean getFast()
    {  
        return(fast);
    }

    // Return bat object
    public synchronized GameObj getBat()
    {
        return(bat);
    }
    
    // return ball object
    public synchronized GameObj getBall()
    {
        return(ball);
    }
    
    // return bricks
    public synchronized ArrayList<GameObj> getBricks()
    {
        return(bricks);
    }
    
    // return score
    public synchronized int getScore()
    {
        return(score);
    }
    
     // update the score
    public synchronized void addToScore(int n)    
    {
        score += n;        
    }
    
    // move the bat one step - -1 is left, +1 is right
    public synchronized void moveBat( int direction )
    {        
        int dist = direction * BAT_MOVE;    // Actual distance to move
        Debug.trace( "Model::moveBat: Move bat = " + dist );
        bat.moveX(dist);
    }
}  
    