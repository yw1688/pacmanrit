import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.awt.MouseInfo;
import java.awt.Point;

import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.util.Pair;
import javafx.geometry.*;
import javafx.animation.*;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.util.*;

/**
 * PackmanGEOStarter with JavaFX and Thread
 * ASSESS ME INTENSITY: LOW
 */

public class Game extends Application {
   // Window attributes
   private Stage stage;
   private Scene scene;
   private VBox root;

   private static String[] args;

   private final static String BACKGROUND = "background.png";
   private final static String ICON_IMAGE = "packman.png"; // file with icon for a racer
   private final static String GHOST = "Ghost.gif";
   Color colorMap = Color.web("#004EFF");

   private int iconWidth; // width (in pixels) of the icon
   private int iconHeight; // height (in pixels) or the icon
   private PacmanRacer racer = null;

   private Ghost ghost = null;
   private TextArea ta = new TextArea();

   // array of racers
   private Image backgroundImage = null;
   private Image carImage = null;
   private Image ghostImage = null;

   // array of ghosts
   private int ghostPosX = 0;
   private int ghostPosY = 0;
   private int ghostROT = 0;

   // ArrayList for ghosts
   ArrayList<Ghost> gList = new ArrayList<Ghost>();

   //// locations of the pacman
   protected int racePosX = 0; // x position of the racer
   private int racePosY = 0; // x position of the racer
   private int raceROT = 0; // x position of the racer
   //// locations of the ghosts
   private PixelReader pxCollision;

   private Canvas canvas = new Canvas(1024, 688);

   private AnimationTimer timer; // timer to control animation

   private double distance = Math
         .sqrt((Math.pow(racePosX - ghostPosX, 2) + Math.sqrt(Math.pow(racePosY - ghostPosY, 2))));

   private ArrayList<Vertex> dotArray = new ArrayList<Vertex>();

   private MediaPlayer musicPlayer;

   // main program
   public static void main(String[] _args) {
      args = _args;
      launch(args);
   }

   // start() method, called via launch
   public void start(Stage _stage) {
      // stage seteup
      stage = _stage;
      stage.setTitle("Game2D Starter");
      stage.setOnCloseRequest(
            new EventHandler<WindowEvent>() {
               public void handle(WindowEvent evt) {
                  System.exit(0);
               }
            });

      // root pane
      root = new VBox();

      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setContentText(
            "The player controls Pac-Man, who must eat all the dots inside an enclosed maze while avoiding three colored ghosts. Eating large flashing dots called Power Pellets causes the ghosts to temporarily turn blue, allowing Pac-Man to eat them for bonus points.");
      alert.setTitle("Pacman Game Instructions");
      alert.showAndWait();

      GraphicsContext gContext = canvas.getGraphicsContext2D();

      dotArray.add(new Vertex(726, 320));
      dotArray.add(new Vertex(300, 320));
      dotArray.add(new Vertex(200, 320));
      dotArray.add(new Vertex(300, 130));
      for (Vertex vertex : dotArray) {
         gContext.setFill(Color.DARKMAGENTA);
         gContext.fillOval(vertex.x, vertex.y, 10, 10);

      }
       music();
      // create an array of Racers (Panes) and start
      initializeScene();

   }
   //method for adding music
   public void music(){
    String s="music.wav";
    Media media=new Media(Paths.get(s).toUri().toString());
    musicPlayer=new MediaPlayer(media);
    musicPlayer.play();
   }

   // method for private chat
   public void sendMessage() {

   }

   // method to check collision with dots
   public boolean dotCollide(Vertex vertex) {
      int racePosXend = racePosX + (int) carImage.getWidth();
      int racePosYend = racePosY + (int) carImage.getHeight();

      boolean isInXRange = vertex.x >= racePosX && vertex.x <= racePosXend;
      boolean isInYRange = vertex.y >= racePosY && vertex.y <= racePosYend;
      System.out.println(isInXRange && isInYRange);
      System.out.println(racePosX);
      System.out.println(racePosXend);
      System.out.println(racePosY);
      System.out.println(racePosYend);
      System.out.println(vertex.x);
      System.out.println(vertex.y);
      return isInXRange && isInYRange;

   }

   // start the race
   public void initializeScene() {

      // Make an icon image to find its size
      try {
         backgroundImage = new Image(new FileInputStream(BACKGROUND));
         carImage = new Image(new FileInputStream(ICON_IMAGE));

         ghostImage = new Image(new FileInputStream(GHOST));

         racer = new PacmanRacer();

         root.getChildren().addAll(racer);

         for (int i = 0; i < 3; i++) {
            Ghost ghost = new Ghost(ghostImage);
            gList.add(ghost);
            root.getChildren().addAll(ghost);

         }

      } catch (Exception e) {
         System.out.println("Exception: " + e);
         System.exit(1);
      }

      // Get image size
      iconWidth = (int) carImage.getWidth();
      iconHeight = (int) carImage.getHeight();

      // display the window
      scene = new Scene(new StackPane(new ImageView(backgroundImage), root, canvas), 1024, 688);

      stage.setScene(scene);

      stage.show();

      System.out.println("Starting race...");

      scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
         @Override
         public void handle(KeyEvent key) {

            if (key.getCode() == KeyCode.LEFT) {

               racer.goLeft();

            } else if (key.getCode() == KeyCode.RIGHT) {
               if (checkBlue()) {
                  System.out.println("Wall ahead");
               } else {
                  racer.goRight();

               }
               
               int colisionPointX = (int)carImage.getWidth();
               int colisionPointY = (int)(carImage.getHeight() / 2);
               PixelReader pxCollision=backgroundImage.getPixelReader();
               Color colorColision=pxCollision.getColor(colisionPointX,colisionPointY);
               System.out.println(colorColision);


            } else if (key.getCode() == KeyCode.UP) {
               if (checkBlue()) {
                  System.out.println("Wall ahead");
               } else {
                  racer.goUp();

               }

            } else if (key.getCode() == KeyCode.DOWN) {
               if (checkBlue()) {

                  System.out.println("Wall biach");
               } else {
                  racer.goDown();

               }

            }
            for (Vertex vertex : dotArray) {
               if (dotCollide(vertex)) {
                  System.out.println("they should be eaten!!");
                  gContext.setFill(Color.TRANSPARENT);
                  // gContext.clearRect(vertex.x,vertex.y,10,10);
                  // TODO: collision with dots
               }
            }
            // private chat, do this part later
            if (key.getCode() == KeyCode.ENTER) {
               sendMessage();
            }

         }
      });

      // Use an animation to update the screen
      timer = new AnimationTimer() {
         public void handle(long now) {
            racer.update();

            for (int i = 0; i < gList.size(); i++) {
               gList.get(i).update();
            }

         }
      };

      // TimerTask to delay start of race for 2 seconds
      TimerTask task = new TimerTask() {
         public void run() {
            timer.start();
         }
      };
      Timer startTimer = new Timer();
      long delay = 1000L;
      startTimer.schedule(task, delay);
   }

   protected class Ghost extends Pane {

      private ImageView ghostView;

      public Ghost(Image image) {
         ghostView = new ImageView(image);
         this.getChildren().addAll(ghostView);
      }

      public void update() {
         iconWidth = (int) ghostImage.getWidth();
         iconHeight = (int) ghostImage.getHeight();

         ghostView.setTranslateX(ghostPosX);
         ghostView.setTranslateY(ghostPosY);
         ghostView.setRotate(ghostROT);
         ghostPosX += (int) (Math.random() * iconWidth / 15);
         ghostPosY += (int) (Math.random() * iconHeight / 15);
         if (ghostPosX > 800)
            ghostPosX = 0;
         if (ghostPosY > 500)
            ghostPosY = 0;

      } // end update()

   }

   /**
    * Racer creates the race lane (Pane) and the ability to
    * keep itself going (Runnable)
    */
   public class PacmanRacer extends Pane implements Runnable {

      private ImageView aPicView; // a view of the icon ... used to display and move the image

      public PacmanRacer() {
         // Draw the icon for the racer
         aPicView = new ImageView(carImage);
         this.getChildren().add(aPicView);
         aPicView.setFitHeight(32);
         aPicView.setFitWidth(32);
      }

      /**
       * update() method keeps the thread (racer) alive and moving.
       */
      public void update() {

         aPicView.setTranslateX(racePosX);
         aPicView.setTranslateY(racePosY);
         aPicView.setRotate(raceROT);

         if (racePosX > 800)
            racePosX = 0;
         if (racePosY > 500)
            racePosY = 0;

      } // end update()

      // method for going right
      public void goRight() {
         racePosX = racePosX + 3;
         aPicView.setTranslateX(racePosX);
         raceROT = 0;
         aPicView.setRotate(raceROT);

      }

      // method for going left
      public void goLeft() {
         racePosX = racePosX - 3;
         aPicView.setTranslateX(racePosX);
         raceROT = 180;
         aPicView.setRotate(raceROT);

      }

      // method for going up
      public void goUp() {
         racePosY = racePosY - 3;
         aPicView.setTranslateY(racePosY);
         raceROT = 270;
         aPicView.setRotate(raceROT);
      }

      // method for going down
      public void goDown() {
         racePosY = racePosY + 3;
         aPicView.setTranslateY(racePosY);
         raceROT = 90;
         aPicView.setRotate(raceROT);

      }

      @Override
      public void run() {
         // TODO Auto-generated method stub

      }

   } // end inner class Racer

   public boolean checkCollision(int x, int y) {
      PixelReader pxPac = carImage.getPixelReader();
      int widthPac = (int) carImage.getWidth();
      int heightPac = (int) carImage.getHeight();
      x += widthPac / 2;
      y += heightPac / 2;
      System.out.println("X: " + x + " Y: " + y);

      pxCollision = backgroundImage.getPixelReader();
      int width = (int) backgroundImage.getWidth();
      int height = (int) backgroundImage.getHeight();

      Color colorPac = pxCollision.getColor(x, y);
      if (colorPac.equals(Color.rgb(0, 78, 255))) {
         System.out.println("Collision");
      }

      return true;

   }

   public boolean checkBlue() {
      try {
         Color colorPac = pxCollision.getColor(racePosX, racePosY);
         return colorPac.equals(colorMap);
      } catch (Exception e) {
         return false;
      }
   }

} // end class Races