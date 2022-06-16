package solitaire;

public class Game {

	// HJ: It's confusing why the engine is named game here - perhaps it'd be good to refactor this variable.
	Engine game;
	GUI gui;
	
	public Game() {
		game = new Engine();
		gui = new GUI(game);
		// HJ: Load initial setup
		game.load();
	}
	
	public static void main(String[] args) {
		Game Solitaire = new Game();
	}
}
