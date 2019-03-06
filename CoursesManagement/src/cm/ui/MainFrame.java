package cm.ui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	// ----- ����������� [ ������ ]
	public MainFrame( ) throws Exception {
		this.setTitle("Courses Manager");
		this.setMinimumSize(new Dimension(600,500));
		
		JTabbedPane tabbedPane = new JTabbedPane();

		this.add(tabbedPane); // �������� ��� ����� ��� ���������
		
		JPanel coursesPanel = new CoursesPanel();
		tabbedPane.addTab("Courses", coursesPanel); // �������� ������ �� ���������� � ��������
		
		JPanel lecturersPanel = new LecturersPanel();
		tabbedPane.addTab("Lecturers", lecturersPanel); // �������� ������ �� ������������� � ��������
		
		JPanel groupsPanel = new GroupsPanel();
		tabbedPane.addTab("Groups", groupsPanel); // �������� ������ �� ����� � ��������
		
		this.pack(); // �� �� �������� ��������� ��������� ������ ���������� � ����
		this.setLocationRelativeTo(null); // �� �� ����� ��������� �� ������
		this.setVisible(true); // ��������� ���������
	}
	// ----- ����������� [  ����  ]
}
