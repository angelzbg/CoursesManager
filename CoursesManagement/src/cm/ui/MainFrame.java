package cm.ui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	// ----- КОНСТРУКТОР [ НАЧАЛО ]
	public MainFrame( ) throws Exception {
		this.setTitle("Courses Manager");
		this.setMinimumSize(new Dimension(600,500));
		
		JTabbedPane tabbedPane = new JTabbedPane();

		this.add(tabbedPane); // добавяме таб пейна към прозореца
		
		JPanel coursesPanel = new CoursesPanel();
		tabbedPane.addTab("Courses", coursesPanel); // добавяме панела за дисциплини в табпейна
		
		JPanel lecturersPanel = new LecturersPanel();
		tabbedPane.addTab("Lecturers", lecturersPanel); // добавяме панела за преподаватели в табпейна
		
		JPanel groupsPanel = new GroupsPanel();
		tabbedPane.addTab("Groups", groupsPanel); // добавяме панела за групи в табпейна
		
		this.pack(); // за да оразмери прозореца подходящо спрямо елементите в него
		this.setLocationRelativeTo(null); // за да отиде централна на екрана
		this.setVisible(true); // показваме прозореца
	}
	// ----- КОНСТРУКТОР [  КРАЙ  ]
}
