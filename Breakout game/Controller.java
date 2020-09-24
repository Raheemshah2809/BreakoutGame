
import javafx.scene.input.KeyEvent;

// The breakout controller receives KeyPress events from the GUI (via
// the KeyEventHandler). It maps the keys onto methods in the model and
// calls them appropriately
public class Controller
{
  public Model model;
  public View  view;
  
  // we don't really need a constructor method, but include one to print a 
  // debugging message if required
  public Controller()
  {
    Debug.trace("Controller::<constructor>");
  }
  
  // This is how the View talks to the Controller
  // AND how the Controller talks to the Model
  // This method is called by the View to respond to key presses in the GUI
  // The controller's job is to decide what to do. In this case it converts
  // the keypresses into commands which are run in the model
  public void userKeyInteraction(KeyEvent event )
  {
    Debug.trace("Controller::userKeyInteraction: keyCode = " + event.getCode() );
      
    switch ( event.getCode() )             
    {
      case LEFT:                     // Left Arrow
        model.moveBat( -1);          // move bat left
        break;
      case RIGHT:                    // Right arrow
        model.moveBat( +1 );         // Move bat right
        break;
      case F :
        // Very fast ball movement
        model.setFast(true);
        break;
      case N :
        // Normal speed ball movement
        model.setFast(false);
        break;
      case S :
        // stop the game
        model.setGameRunning(false);
        break;
    }
  }
}
