import javafx.application.Application;
import javafx.stage.Stage;

//  breakoutJavaFX project Main class
// The code here creates the breakout game, but without any bricks.
// The lab class exercise (with tutor support) is to add a row of bricks and make them
// disappear when the ball hits them.
// The assessment project is to add further functionality as discussed in lectures and 
// seminars. Tutors may not help directly with this but will talk you through examples and
// answer questions.
public class Main extends Application
{
    public static void main( String args[] )
    {
        // The main method only gets used when launching from the command line
        // launch initialises the system and then calls start
        // In BlueJ, BlueJ calls start itself
        launch(args);
    }

    public void start(Stage window) 
    {
        int H = 800;         // Height of window pixels 
        int W = 600;         // Width  of window pixels 

        // set up debugging and print initial debugging message
        Debug.set(true);             
        Debug.trace("breakoutJavaFX starting"); 
        Debug.trace("Main::start"); 

        // Create the Model, View and Controller objects
        Model model = new Model(W,H);
        View  view  = new View(W,H);
        Controller controller  = new Controller();

        // Link them together so they can talk to each other
        // Each one has instance variables for the other two
        model.view = view;
        model.controller = controller;
        controller.model = model;
        controller.view = view;
        view.model = model;
        view.controller = controller;

        // start up the GUI (view), and then tell the model to initialise itself
        // and start the game running
        view.start(window); 
        model.initialiseGame();
        model.startGame();

        // application is now running
        Debug.trace("breakoutJavaFX running"); 
    }
}
