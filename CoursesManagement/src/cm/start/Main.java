package cm.start;

import javax.swing.WindowConstants;

import cm.ui.MainFrame;

public class Main {

	public static void main(String[] args) throws Exception {
		
		MainFrame mainFrame = new MainFrame();
		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // за да се затвори програмата когато се затвори mainFrame от бутона Х
	}

}
