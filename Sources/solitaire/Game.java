package solitaire;

public class Game {

	Engine engine;
	GUI gui;
	
	public Game() {
		engine = new Engine();
		gui = new GUI(engine);

	}
	
	public static void main(String[] args) {
		Game Solitaire = new Game();
	}
}
