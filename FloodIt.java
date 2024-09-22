import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// represents the world for the FloodIt Game
class FloodItWorld extends World {
  // All the cells of the game
  ArrayList<ArrayList<Cell>> board;
  ArrayList<Color> colors = new ArrayList<Color>(
      Arrays.asList(Color.red, Color.blue, Color.green, Color.pink, Color.yellow,
          Color.magenta, Color.cyan));
  int size; 
  int numColors;
  Color floodColor;
  int moves;
  double maxMoves;

  FloodItWorld(int size, int numColors) {
    this.size = size; 
    this.numColors = numColors;
    this.board = new ArrayList<ArrayList<Cell>>();
    makeCells();
    linkNeighbors();
    board.get(0).get(0).updateFlooded(true);
    moves = 0;
    maxMoves = Math.floor(size * numColors * 0.45);
  }

  // constructor that takes in a board for testing
  FloodItWorld(int size, int numColors, ArrayList<ArrayList<Cell>> board) {
    this.size = size; 
    this.numColors = numColors;
    this.board = board;
    moves = 0;
    maxMoves = Math.floor(size * numColors * 0.45);
  }

  // draws a world scene
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(400, 400);
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.size(); j++) {
        board.get(i).get(j).draw(scene, size);
      }
    }
    scene.placeImageXY(new TextImage(Integer.toString(this.moves) 
        + "/" + Double.toString(this.maxMoves), Color.black), 350, 60);
    scene.placeImageXY(new TextImage("Flood-It", Color.black), 350, 40);
    return scene;
  }

  // on tick function
  public void onTick() {
    checkGameOver();
  }

  // resets the game when "r" key is pressed
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.board = new ArrayList<ArrayList<Cell>>();
      this.moves = 0;
      this.makeCells();
      this.linkNeighbors();
      board.get(0).get(0).updateFlooded(true);
    }
  }
  
  // creates the final scene
  public WorldScene lastScene(String msg) {
    WorldScene scene = new WorldScene(400, 400);
    scene.placeImageXY(new TextImage(msg, Color.black), 200, 200);
    return scene;
  }

  // handles the player clicking a square
  public void onMouseClicked(Posn pos) {
    Cell clickedCell = this.board.get(0).get(0);
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.size(); j++) {
        if (board.get(i).get(j).isClicked(pos)) {
          clickedCell = this.board.get(i).get(j);
        }
      }
    }
    Color newColor = clickedCell.color;
    Color oldColor = this.board.get(0).get(0).color;
    if (!clickedCell.flooded && newColor != oldColor) {

      flood(0, 0, oldColor, newColor);
      this.moves++;
      checkGameOver();
    }
  }


  // checks if the game is over, and ends the game
  void checkGameOver() {
    if (this.moves > this.maxMoves) {
      this.endOfWorld("You lost :(");
    }
    boolean fullFlood = false;
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.size(); j++) {
        fullFlood = fullFlood && board.get(i).get(j).flooded;
      }
    }
    if (fullFlood) {
      this.endOfWorld("You won!");
    }
  }

  // floods the cell if color matches, and floods adjacent cells accordingly
  void flood(int x, int y, Color oldColor, Color newColor) {
    Cell cell = this.board.get(x).get(y);
    if (cell.color == oldColor) {
      cell.color = newColor;
      cell.updateFlooded(true);
    }
    if (cell.top != null && cell.top.color == oldColor) {
      flood(x - 1, y, oldColor, newColor);
    }
    if (cell.bottom != null && cell.bottom.color == oldColor) {
      flood(x + 1, y, oldColor, newColor);
    }
    if (cell.left != null && cell.left.color == oldColor) {
      flood(x, y - 1, oldColor, newColor);
    }
    if (cell.right != null && cell.right.color == oldColor) {
      flood(x, y + 1, oldColor, newColor);
    }
  }

  // fills the board with cells of a random color
  void makeCells() {
    for (int i = 0; i < this.size; i++) {
      ArrayList<Cell> row = new ArrayList<Cell>();
      for (int j = 0; j < this.size; j++) {
        row.add(new Cell(i * 20, j * 20,  chooseColor(), false));
      }
      this.board.add(row);
    }

  }

  // links neighbors to each other
  void linkNeighbors() {
    for (int i = 0; i < this.size; i++) {
      for (int j = 0; j < this.size; j++) {
        if (i > 0) {
          this.board.get(i).get(j).updateTop(this.board.get(i - 1).get(j));
        }
        if (i < this.size - 1) {
          this.board.get(i).get(j).updateBot(this.board.get(i + 1).get(j));
        }
        if (j > 0) {
          this.board.get(i).get(j).updateLeft(this.board.get(i).get(j - 1));
        }
        if (j < this.size - 1) {
          this.board.get(i).get(j).updateRight(this.board.get(i).get(j + 1));
        }
      }
    }
  }

  // produces a random color from the list of colors
  Color chooseColor() {
    Random rand = new Random();
    return colors.get(rand.nextInt(numColors));
  }
}


//Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
  }

  // returns if this cell has been clicked
  public boolean isClicked(Posn pos) {
    return this.x < pos.x 
        && this.y < pos.y 
        && this.x + 20 > pos.x 
        && this.y + 20 > pos.y;
  }

  // updates the top neighbor of this cell with the given one
  void updateTop(Cell top) {
    this.top = top;
  }

  // updates the bottom neighbor of this cell with the given one
  void updateBot(Cell bot) {
    this.bottom = bot;
  }

  // updates the left neighbor of this cell with the given one
  void updateLeft(Cell left) {
    this.left = left;
  }

  //updates the right neighbor of this cell with the given one
  void updateRight(Cell right) {
    this.right = right;
  }

  // method for testing that updates all neighbors
  void updateAll(Cell top, Cell bot, Cell left, Cell right) {
    this.top = top;
    this.bottom = bot;
    this.left = left;
    this.right = right;
  }

  // draws this cell at the proper x/y cords
  public void draw(WorldScene scene, int worldSize) {
    scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, this.color), 
        this.x + 10, this.y + 10);
  }

  // updates flooded value 
  public void updateFlooded(boolean val) {
    this.flooded = val;
  }
}

class ExamplesFlood { 
  Cell c1;
  Cell c2;
  Cell c3; 
  Cell c4;
  ArrayList<Cell> row1 = new ArrayList<Cell>();
  ArrayList<Cell> row2 = new ArrayList<Cell>();
  ArrayList<ArrayList<Cell>> board1 = new ArrayList<ArrayList<Cell>>();
  FloodItWorld linkWorld;

  Cell c5;
  Cell c6;
  Cell c7; 
  Cell c8;
  ArrayList<Cell> row3 = new ArrayList<Cell>();
  ArrayList<Cell> row4 = new ArrayList<Cell>();
  ArrayList<ArrayList<Cell>> board2 = new ArrayList<ArrayList<Cell>>();
  FloodItWorld world2;


  // inits to test linking
  void initsForLink() {
    this.c1 = new Cell(0, 0, Color.BLUE, false);
    this.c2 = new Cell(20, 0, Color.GREEN, false);
    this.c3 = new Cell(0, 20, Color.PINK, false);
    this.c4 = new Cell(20, 20, Color.RED, false);

    this.c1.updateAll(null, null, null, null);
    this.c2.updateAll(null, null, null, null);
    this.c3.updateAll(null, null, null, null);
    this.c4.updateAll(null, null, null, null);

    this.row1 = new ArrayList<Cell>();
    this.row2 = new ArrayList<Cell>();
    this.board1 = new ArrayList<ArrayList<Cell>>();

    this.row1.add(c1);
    this.row1.add(c2);
    this.row2.add(c3);
    this.row2.add(c4);

    this.board1.add(row1);
    this.board1.add(row2);

    this.linkWorld = new FloodItWorld(2, 4, this.board1);
  }

  void inits() {
    this.c5 = new Cell(0, 0, Color.BLUE, false);
    this.c6 = new Cell(20, 0, Color.GREEN, false);
    this.c7 = new Cell(0, 20, Color.PINK, false);
    this.c8 = new Cell(20, 20, Color.RED, false);

    this.c5.updateAll(null, null, null, null);
    this.c6.updateAll(null, null, null, null);
    this.c7.updateAll(null, null, null, null);
    this.c8.updateAll(null, null, null, null);

    this.row3 = new ArrayList<Cell>();
    this.row4 = new ArrayList<Cell>();
    this.board2 = new ArrayList<ArrayList<Cell>>();

    this.row3.add(c5);
    this.row3.add(c6);
    this.row4.add(c7);
    this.row4.add(c8);

    this.board2.add(row3);
    this.board2.add(row4);

    this.world2 = new FloodItWorld(2, 4, this.board2);
    world2.linkNeighbors();
  }

  void testLinkNeighbors(Tester t) {
    initsForLink();
    linkWorld.linkNeighbors();

    t.checkExpect(this.c1.bottom, this.c3);
    t.checkExpect(this.c1.top, null);
    t.checkExpect(this.c1.left, null);
    t.checkExpect(this.c1.right, this.c2);

    t.checkExpect(this.c2.bottom, this.c4);
    t.checkExpect(this.c2.top, null);
    t.checkExpect(this.c2.left, this.c1);
    t.checkExpect(this.c2.right, null);

    t.checkExpect(this.c3.bottom, null);
    t.checkExpect(this.c3.top, this.c1);
    t.checkExpect(this.c3.left, null);
    t.checkExpect(this.c3.right, this.c4);

    t.checkExpect(this.c4.bottom, null);
    t.checkExpect(this.c4.top, this.c2);
    t.checkExpect(this.c4.left, this.c3);
    t.checkExpect(this.c4.right, null);
  }

  void testMakeScene(Tester t) {
    inits();
    WorldScene scene =  new WorldScene(400, 400);
    scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, c5.color), 
        c5.x + 10 , c5.y + 10); 
    scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, c6.color), 
        c6.x + 10 , c6.y + 10);
    scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, c7.color), 
        c7.x + 10 , c7.y + 10);
    scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, c8.color), 
        c8.x + 10, c8.y + 10);
    scene.placeImageXY(new TextImage("0/3.0", Color.black), 350, 60);
    scene.placeImageXY(new TextImage("Flood-It", Color.black), 350, 40);
    t.checkExpect(world2.makeScene(), scene);
  }

  void testChooseColor(Tester t) {
    inits();
    t.checkOneOf(world2.chooseColor(), Color.red, Color.blue, Color.green, Color.pink);
    t.checkOneOf(world2.chooseColor(), Color.red, Color.blue, Color.green, Color.pink);
    t.checkOneOf(world2.chooseColor(), Color.red, Color.blue, Color.green, Color.pink);
    t.checkOneOf(world2.chooseColor(), Color.red, Color.blue, Color.green, Color.pink);
  }

  void testUpdateTop(Tester t) {
    initsForLink();
    this.c1.updateTop(this.c2);
    t.checkExpect(c1.top, this.c2);
    this.c2.updateTop(this.c4);
    t.checkExpect(c2.top, this.c4);
    this.c3.updateTop(null);
    t.checkExpect(c3.top, null);
  }

  void testUpdateBot(Tester t) {
    initsForLink();
    this.c1.updateBot(this.c2);
    t.checkExpect(c1.bottom, this.c2);
    this.c2.updateBot(this.c4);
    t.checkExpect(c2.bottom, this.c4);
    this.c3.updateBot(null);
    t.checkExpect(c3.bottom, null);
  }

  void testUpdateRight(Tester t) {
    initsForLink();
    this.c1.updateRight(this.c2);
    t.checkExpect(c1.right, this.c2);
    this.c2.updateRight(this.c4);
    t.checkExpect(c2.right, this.c4);
    this.c3.updateRight(null);
    t.checkExpect(c3.right, null);
  }

  void testUpdateLeft(Tester t) {
    initsForLink();
    this.c1.updateLeft(this.c2);
    t.checkExpect(c1.left, this.c2);
    this.c2.updateLeft(this.c4);
    t.checkExpect(c2.left, this.c4);
    this.c3.updateLeft(null);
    t.checkExpect(c3.left, null);
  }

  void testUpdateAll(Tester t) {
    initsForLink();
    this.c1.updateAll(null, null, null, null);
    t.checkExpect(this.c1.top, null);
    t.checkExpect(this.c1.bottom, null);
    t.checkExpect(this.c1.left, null);
    t.checkExpect(this.c1.right, null);

    this.c1.updateAll(this.c4, this.c3, this.c2, null);
    t.checkExpect(this.c1.top, this.c4);
    t.checkExpect(this.c1.bottom, this.c3);
    t.checkExpect(this.c1.left, this.c2);
    t.checkExpect(this.c1.right, null);
  }

  void testDraw(Tester t) {
    inits();
    WorldScene sceneTest =  new WorldScene(400, 400);
    sceneTest.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, c5.color), 
        c5.x + 10, c5.y + 10); 
    WorldScene sceneResult = (new WorldScene(400, 400));
    c5.draw(sceneResult, 2);
    t.checkExpect(sceneResult, sceneTest);

    sceneTest.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, c6.color), 
        c6.x + 10, c6.y + 10); 
    c6.draw(sceneResult, 2);
    t.checkExpect(sceneResult, sceneTest);
  }

  void testIsClicked(Tester t) {
    inits();
    t.checkExpect(c5.isClicked(new Posn(0,0)), false);
    t.checkExpect(c5.isClicked(new Posn(c5.x + 10, c5.y + 1)), true);
    t.checkExpect(c5.isClicked(new Posn(c5.x + 20, c5.y + 1)), false);
    t.checkExpect(c5.isClicked(new Posn(c5.x + 10, c5.y - 1)), false);
    t.checkExpect(c5.isClicked(new Posn(c5.x - 10, c5.y + 1)), false);
  }

  void testOnMouseClicked(Tester t) {
    inits();
    world2.onMouseClicked(new Posn(c5.x + 10, c5.y + 1));
    t.checkExpect(c5.color, Color.blue);
    world2.onMouseClicked(new Posn(c6.x + 10, c6.y + 1));
    t.checkExpect(c6.color, Color.green);
  }

  void testCheckGameOver(Tester t) {
    inits();
    world2.checkGameOver();
    world2.moves = 8;
    world2.checkGameOver();
  }

  void testLastScene(Tester t) {
    inits();
    WorldScene scene =  new WorldScene(400, 400);
    scene.placeImageXY(new TextImage("You lost :(", Color.black), 200, 200);
    t.checkExpect(world2.lastScene("You lost :("), scene);
    scene =  new WorldScene(400, 400);
    scene.placeImageXY(new TextImage("You won XD", Color.black), 200, 200);
    t.checkExpect(world2.lastScene("You won XD"), scene);
  }
  
  void testUpdateFlooded(Tester t) {
    inits();
    t.checkExpect(this.c6.flooded, false);
    this.c6.updateFlooded(true);
    t.checkExpect(this.c6.flooded, true);
  }
  
  void testFlood(Tester t) {
    inits();
    c6.color = Color.blue;
    world2.flood(0,0, Color.blue, Color.green);
    t.checkExpect(c6.color, Color.green);
    t.checkExpect(c5.color, Color.green);
    t.checkExpect(c5.flooded, true);
    t.checkExpect(c6.flooded, true);
    t.checkExpect(c7.flooded, false);
    t.checkExpect(c8.flooded, false);
  }
  
  void testBigBang(Tester t) {
    FloodItWorld world = new FloodItWorld(14, 6);
    int worldWidth = 400;
    int worldHeight = 400;
    double tickRate = .1;
    world.bigBang(worldWidth, worldHeight, tickRate);
  }
}





