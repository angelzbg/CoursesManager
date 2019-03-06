package cm.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import cm.db.MyDB;
import cm.db.MyModel;

public class CoursesPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	// DataBase
	Connection conn = null;
	PreparedStatement state = null;
	ResultSet result = null;
	MyModel model = null;
	
	// ----- ����������
	// ��������� ��� ������ ����
	JLabel JL_Notification;
	// ����� ������:
	JPanel JP_Menu;
	JButton JB_Menu_Add, JB_Menu_Remove, JB_Menu_Update, JB_Menu_Search, JB_Menu_Refresh;
	// ���������:
	JButton JB_goToMenu;
	// ��������:
	JTextField JTF_Add_Name, JTF_Add_LectNum;
	JButton JB_Add;
	// ���������:
	JTextField JTF_Delete;
	JButton JB_Delete;
	// ����������:
	JTextField JTF_Update_Number, JTF_Update_Name, JTF_Update_LectNum;
	JButton JB_Update;
	// �������:
	JTextField JTF_Search;
	JButton JB_Search;
	
	// �������
	JTable JT_Table = new JTable();
	JScrollPane JSP_Scroller;
	
	// ----- ����������� [ ������ ]
	public CoursesPanel() throws Exception {
		this.setLayout(new BorderLayout());
		
		// �������:
		JSP_Scroller = new JScrollPane(JT_Table);
		loadAllCoursesAsc();
		this.add(BorderLayout.CENTER, JSP_Scroller);
		JT_Table.addMouseListener(new java.awt.event.MouseAdapter() { // ������ �� ������ �� ��� �� ��������� �� ������� � ������ ���������� ����� � ��� ������ �� �� �������
		    @Override
		    public void mouseClicked(java.awt.event.MouseEvent evt) {
		        int row = JT_Table.rowAtPoint(evt.getPoint());
		        int col = JT_Table.columnAtPoint(evt.getPoint());
		        if (row >= 0 && col >= 0) {
		        	JB_Menu_Update.doClick(); // �������� ����-�� �� �������
		        	Object[] obj =  (Object[]) model.data.get(row);
		        	JTF_Update_Number.setText(obj[0].toString()); // ����� �� ������������
		        	JTF_Update_Name.setText(obj[1].toString()); // ��� �� ������������
		        	
		        	// � ������ ��� �� ������ ����� �� ������������ � ���� �������, �� ������ �� �� �� ������� �� ����� ������ ��� ���� � ��������
		        	String lecturer_email = obj[3].toString();
		        	conn = MyDB.getConnected();
		    		String sql = "SELECT L_NUMBER FROM LECTURERS WHERE L_EMAIL='" + lecturer_email + "';";
		    		try {
		    			state = conn.prepareStatement(sql);
		    			result = state.executeQuery();
		    			if(result.next()) {
		    				JTF_Update_LectNum.setText(result.getObject(1).toString()); // ����� �� �������������
		    			}
		    		} catch (SQLException e) {
		    			e.printStackTrace();
		    		}
		        }
		    }
		});
		
		// ��������� �� ������
		JP_Menu = new JPanel();
		JP_Menu.setLayout(new BorderLayout());
		this.add(BorderLayout.PAGE_START, JP_Menu); // ����
		loadMenu();
		
	}
	// ----- ����������� [  ����  ]
	
	// ----- ActionListeners
	void loadMenu() {
		JP_Menu.removeAll();
		
		JPanel buttonsWrapper = new JPanel();
		buttonsWrapper.setLayout(new GridLayout(1,5)); //1 ���, 5 ������
		
		JB_Menu_Add = new JButton("������");
		JB_Menu_Remove = new JButton("������");
		JB_Menu_Update = new JButton("�������");
		JB_Menu_Search = new JButton("�����");
		JB_Menu_Refresh = new JButton("������");
		
		buttonsWrapper.add(JB_Menu_Add);
		buttonsWrapper.add(JB_Menu_Remove);
		buttonsWrapper.add(JB_Menu_Update);
		buttonsWrapper.add(JB_Menu_Search);
		buttonsWrapper.add(JB_Menu_Refresh);
		
		JB_Menu_Add.addActionListener(new ActionSwitchToAdd());
		JB_Menu_Remove.addActionListener(new ActionSwitchToRemove());
		JB_Menu_Update.addActionListener(new ActionSwitchToUpdate());
		JB_Menu_Search.addActionListener(new ActionSwitchToSearch());
		JB_Menu_Refresh.addActionListener(new ActionRefresh());
		
		JP_Menu.add(BorderLayout.CENTER, buttonsWrapper);
		
		JP_Menu.revalidate();
		JP_Menu.repaint();
	}
	
	class ActionRefresh implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				loadAllCoursesAsc();
			} catch (Exception e1) {
				e1.printStackTrace();
			}	
		}
	}//end ActionRefresh
	
	class ActionSwitchToAdd implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			JP_Menu.removeAll(); // ������ ������ � ����-��
			
			JPanel wrapper = new JPanel();
			wrapper.setLayout(new GridLayout(1,4)); //1 ���, 4 ������
			
			//JTF_Add_Name = new HintTextField("Course name");
			//JTF_Add_LectNum = new HintTextField("Lecturer number");
			JTF_Add_Name = new JTextField();
			JTF_Add_Name.setUI(new HintTextFieldUI("Course name", true));
			JTF_Add_LectNum = new JTextField();
			JTF_Add_LectNum.setUI(new HintTextFieldUI("Lecturer number", true));
			JB_Add = new JButton("������");
			JB_goToMenu = new JButton("\u25B2");
			
			wrapper.add(JTF_Add_Name);
			wrapper.add(JTF_Add_LectNum);
			wrapper.add(JB_Add);
			wrapper.add(JB_goToMenu);
			
			JP_Menu.add(BorderLayout.CENTER, wrapper); // �������� ������ ���� � ������
			// ���������� �� ����� ������
			JP_Menu.revalidate();
			JP_Menu.repaint();
			
			JB_goToMenu.addActionListener(new ActionListener() { // ����� �� ������� ������� � ����-��
				@Override
				public void actionPerformed(ActionEvent e) {
					loadMenu();
				}
			});
			
			JB_Add.addActionListener(new ActionListener() { // ���� �/� ������ "������" -> �������� -> �������� ��� ����������
				@Override
				public void actionPerformed(ActionEvent e) {
					final String name = JTF_Add_Name.getText().toString().trim();
					final String lect = JTF_Add_LectNum.getText().toString().trim();
					if(name.isEmpty() || lect.isEmpty()) {
						showNotification(1, "������ ������ ������ �� ����� ���������.");
						return;
					}
					if(name.length() > 100) {
						showNotification(1, "����� �� ���� �� ������� ������ �� 100 �������.");
						return;
					}
					int lect_number = 0;
					try {
						lect_number = Integer.parseInt(lect);
					} catch (Exception exc) {
						showNotification(1, "�� ��� ������ ����� �� ������������.");
						return;
					}
					
					// �������� ���� ������������ � ���� ����� ���������� ����� �� ��������� ������.
					conn = MyDB.getConnected();
					String sql = "SELECT L_NUMBER FROM LECTURERS WHERE L_NUMBER=?;";
					try {
						state = conn.prepareStatement(sql);
						state.setInt(1, lect_number);
						result = state.executeQuery();
						if(!result.isBeforeFirst()) {
							showNotification(1, "�� ���������� ������������ � ����� �����.");
							return;
						}
					} catch (SQLException exc) {
						exc.printStackTrace();
						return;
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}
					
					// ���� �� ����� �� ������ ���������� ���� ���� ������ � �����
					sql = "INSERT INTO COURSES VALUES(NULL, NULL, ?, ?);";
					try {
						state = conn.prepareStatement(sql);
						state.setString(1, name);
						state.setInt(2, lect_number);
						state.execute();
					} catch (SQLException exc) {
						exc.printStackTrace();
						return;
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}
					
					// ��� ��� �� ��� ��������� �� ����� �� �������� ����� ������ � �����:
					showNotification(2, "������� ���������� ���� ����������.");
					 // ����� �� ������� ��������� ��� ������ ����� ������ �� �� ����� ������ ����� ���-������
					try {
						loadAllCoursesDesc();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
		}
	}//end ActionSwitchToAdd
	
	class ActionSwitchToRemove implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			JP_Menu.removeAll(); // ������ ������ � ����-��
			
			JPanel wrapper = new JPanel();
			wrapper.setLayout(new GridLayout(1,3)); //1 ���, 3 ������
			
			JTF_Delete = new JTextField();
			JTF_Delete.setUI(new HintTextFieldUI("Course number", true));
			JB_Delete = new JButton("������");
			JB_goToMenu = new JButton("\u25B2");
			
			wrapper.add(JTF_Delete);
			wrapper.add(JB_Delete);
			wrapper.add(JB_goToMenu);
			
			JP_Menu.add(BorderLayout.CENTER, wrapper); // �������� ������ ���� � ������
			// ���������� �� ����� ������
			JP_Menu.revalidate();
			JP_Menu.repaint();
			
			JB_goToMenu.addActionListener(new ActionListener() { // ����� �� ������� ������� � ����-��
				@Override
				public void actionPerformed(ActionEvent e) {
					loadMenu();
				}
			});
			
			JB_Delete.addActionListener(new ActionListener() { // ���� �/� ������ "������" -> �������� -> ���� �� ���������
				@Override
				public void actionPerformed(ActionEvent e) {
					final String course_number_string = JTF_Delete.getText().toString().trim();
					if(course_number_string.isEmpty()) {
						showNotification(1, "�� ��� ������ ����� �� ���������� �� ���������.");
						return;
					}
					int course_number = 0;
					try {
						course_number = Integer.parseInt(course_number_string);
					} catch (Exception exc) {
						showNotification(1, "�� ��� ������ ����� �� ����������.");
						return;
					}
					if(course_number<1000) {
						showNotification(1, "�������� �� ������������ �������� �� 1000.");
						return;
					}
					
					// �������� ���� ���������� � ���� ����� ���������� ����� �� ��������� ������.
					conn = MyDB.getConnected();
					String sql = "SELECT C_NUMBER FROM COURSES WHERE C_NUMBER=?;";
					try {
						state = conn.prepareStatement(sql);
						state.setInt(1, course_number);
						result = state.executeQuery();
						if(!result.isBeforeFirst()) {
							showNotification(1, "�� ���������� ���������� � ����� �����.");
							return;
						}
					} catch (SQLException exc) {
						exc.printStackTrace();
						return;
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}
					
					// ���� �� ��������� �� ������ ���������� ���� ���� �����, �� ����������
					sql = "DELETE FROM COURSES WHERE C_NUMBER=?;";
					try {
						state = conn.prepareStatement(sql);
						state.setInt(1, course_number);
						state.execute();
					} catch (SQLException exc) {
						exc.printStackTrace();
						return;
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}
					
					// ��� ��� �� ��� ��������� �� ����� �� �������� ����� ������ � �����:
					showNotification(2, "������� �������� ���������� � ����� " + course_number);
					 // ����� �� ������� ��������� ��� ������
					try {
						loadAllCoursesAsc();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
		}
	}//end ActionSwitchToRemove
	
	class ActionSwitchToUpdate implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			JP_Menu.removeAll(); // ������ ������ � ����-��
			
			JPanel wrapper = new JPanel();
			wrapper.setLayout(new GridLayout(1,4)); //1 ���, 5 ������

			JTF_Update_Number = new JTextField();
			JTF_Update_Number.setUI(new HintTextFieldUI("Course number", true));
			JTF_Update_Name = new JTextField();
			JTF_Update_Name.setUI(new HintTextFieldUI("Course name", true));
			JTF_Update_LectNum = new JTextField();
			JTF_Update_LectNum.setUI(new HintTextFieldUI("Lecturer number", true));
			JB_Update = new JButton("������");
			JB_goToMenu = new JButton("\u25B2");
			
			wrapper.add(JTF_Update_Number);
			wrapper.add(JTF_Update_Name);
			wrapper.add(JTF_Update_LectNum);
			wrapper.add(JB_Update);
			wrapper.add(JB_goToMenu);
			
			JP_Menu.add(BorderLayout.CENTER, wrapper); // �������� ������ ���� � ������
			// ���������� �� ����� ������
			JP_Menu.revalidate();
			JP_Menu.repaint();
			
			JB_goToMenu.addActionListener(new ActionListener() { // ����� �� ������� ������� � ����-��
				@Override
				public void actionPerformed(ActionEvent e) {
					loadMenu();
				}
			});
			
			JB_Update.addActionListener(new ActionListener() { // ���� �/� ������ "������" -> �������� -> ������ ����������
				@Override
				public void actionPerformed(ActionEvent e) {
					final String numb = JTF_Update_Number.getText();
					final String name = JTF_Update_Name.getText().toString().trim();
					final String lect = JTF_Update_LectNum.getText();
					
					if(numb.isEmpty() || name.isEmpty() || lect.isEmpty()) {
						showNotification(1, "������ ������ ������ �� ����� ���������.");
						return;
					}
					if(name.length() > 100) {
						showNotification(1, "����� �� ���� �� ������� ������ �� 100 �������.");
						return;
					}
					int course_number = 0;
					try {
						course_number = Integer.parseInt(numb);
					} catch (Exception exc) {
						showNotification(1, "�� ��� ������ ����� �� ����������.");
						return;
					}
					int lecturer_number = 0;
					try {
						lecturer_number = Integer.parseInt(lect);
					} catch (Exception exc) {
						showNotification(1, "�� ��� ������ ����� �� ������������.");
						return;
					}
					
					// �������� ���� ���������� � ���� ����� ���������� ����� �� ��������� ������.
					conn = MyDB.getConnected();
					String sql = "SELECT C_NUMBER FROM COURSES WHERE C_NUMBER=?;";
					try {
						state = conn.prepareStatement(sql);
						state.setInt(1, course_number);
						result = state.executeQuery();
						if(!result.isBeforeFirst()) {
							showNotification(1, "�� ���������� ���������� � ����� �����.");
							return;
						}
					} catch (SQLException exc) {
						exc.printStackTrace();
						return;
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}
					
					// �������� ���� ������������ � ���� ����� ���������� ����� �� ��������� ������.
					sql = "SELECT L_NUMBER FROM LECTURERS WHERE L_NUMBER=?;";
					try {
						state = conn.prepareStatement(sql);
						state.setInt(1, lecturer_number);
						result = state.executeQuery();
						if(!result.isBeforeFirst()) {
							showNotification(1, "�� ���������� ������������ � ����� �����.");
							return;
						}
					} catch (SQLException exc) {
						exc.printStackTrace();
						return;
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}
					
					// ���� �� ���������� �� ������������ ���� ���� ������ � �����
					sql = "UPDATE COURSES SET C_NAME=?, L_NUMBER=? WHERE C_NUMBER=?;";
					try {
						state = conn.prepareStatement(sql);
						state.setString(1, name);
						state.setInt(2, lecturer_number);
						state.setInt(3, course_number);
						state.execute();
					} catch (SQLException exc) {
						exc.printStackTrace();
						return;
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}
					
					// ��� ��� �� ��� ��������� �� ����� �� �������� ����� ������ � �����:
					showNotification(2, "������� ��������� ���������� � ����� " + course_number + " (" + name + ")");
					 // ����� �� ������� ��������� ��� ������
					try {
						loadAllCoursesAsc();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
		}
	}//end ActionSwitchToUpdate
	
	class ActionSwitchToSearch implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			JP_Menu.removeAll(); // ������ ������ � ����-��
			
			JPanel wrapper = new JPanel();
			wrapper.setLayout(new GridLayout(1,3)); //1 ���, 3 ������
			
			JTF_Search = new JTextField();
			JTF_Search.setUI(new HintTextFieldUI("Course name", true));
			JB_Search = new JButton("�����");
			JB_goToMenu = new JButton("\u25B2");
			
			wrapper.add(JTF_Search);
			wrapper.add(JB_Search);
			wrapper.add(JB_goToMenu);
			
			JP_Menu.add(BorderLayout.CENTER, wrapper); // �������� ������ ���� � ������
			// ���������� �� ����� ������
			JP_Menu.revalidate();
			JP_Menu.repaint();
			
			JB_goToMenu.addActionListener(new ActionListener() { // ����� �� ������� ������� � ����-��
				@Override
				public void actionPerformed(ActionEvent e) {
					loadMenu();
				}
			});
			
			JB_Search.addActionListener(new ActionListener() { // ���� �/� ������ "�����" -> ���� �� �������
				@Override
				public void actionPerformed(ActionEvent e) {
					final String course_name = JTF_Search.getText().toString().trim();
					if(course_name.isEmpty()) {
						showNotification(1, "�� ��� ������ ��� �� ���������� �� �������.");
						return;
					}
					
					conn = MyDB.getConnected();
					//SELECT C_NUMBER, C_NAME, L_NAME, L_EMAIL FROM COURSES C JOIN LECTURERS L ON C.L_NUMBER = L.L_NUMBER WHERE CHARINDEX(?, C_NAME) > 0 AND C_ID > 1;
					//�� ������� ��������� query ���� �� ���������� �� https://stackoverflow.com/questions/49920450/perform-case-insensitive-string-search-in-h2
					String sql = "SELECT C_NUMBER, C_NAME, L_NAME, L_EMAIL FROM COURSES C JOIN LECTURERS L ON C.L_NUMBER = L.L_NUMBER WHERE locate (lower(?), lower(C.C_NAME)) AND C_ID > 1;";
					try {
						state = conn.prepareStatement(sql);
						state.setString(1, course_name);
						result = state.executeQuery();
						if(!result.isBeforeFirst()) {
							showNotification(1, "�� ���������� ���������� � ������� ���.");
							return;
						} else {
							model = new MyModel(result);
							JT_Table.setModel(model);
							updateTableColumnNames();
							showNotification(2, "��� �������� �� ��������� �� \"" + course_name + "\"");
						}
					} catch (SQLException exc) {
						exc.printStackTrace();
						return;
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}
				}
			});
		}
	}//end ActionSwitchToSearch
	
	// ----- ----- ----- -----
	
	// ----- �������� [ ������ ]
	void loadAllCoursesAsc() throws Exception {
		conn = MyDB.getConnected();
		String sql = "SELECT C_NUMBER, C_NAME, L_NAME, L_EMAIL FROM COURSES C JOIN LECTURERS L ON C.L_NUMBER = L.L_NUMBER WHERE C_ID > 1 ORDER BY C_NUMBER;";
		try {
			state = conn.prepareStatement(sql);
			result = state.executeQuery();
			model = new MyModel(result);
			JT_Table.setModel(model);
			updateTableColumnNames();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void loadAllCoursesDesc() throws Exception {
		conn = MyDB.getConnected();
		String sql = "SELECT C_NUMBER, C_NAME, L_NAME, L_EMAIL FROM COURSES C JOIN LECTURERS L ON C.L_NUMBER = L.L_NUMBER WHERE C_ID > 1 ORDER BY C_NUMBER DESC;";
		try {
			state = conn.prepareStatement(sql);
			result = state.executeQuery();
			model = new MyModel(result);
			JT_Table.setModel(model);
			updateTableColumnNames();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	// ----- �������� [  ����  ]
	
	// ----- Notificaion
	boolean isNotificationShown = false;
	void showNotification(int type, String message) { // 1 = ������, 2 = �����
		isNotificationShown = true;
		JL_Notification = new JLabel(message, SwingConstants.CENTER);
		JL_Notification.setForeground(Color.decode("#f2f5f7")); // ����� ���� �� ������
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(BorderLayout.CENTER, JL_Notification);
		switch(type) {
			case 1:
				panel.setBackground(Color.decode("#c63d3d")); // ������ ���������
				break;
			case 2:
				panel.setBackground(Color.decode("#4899db")); // ��� ���������
				break;
		}
		JP_Menu.add(BorderLayout.PAGE_START, panel);
		JP_Menu.revalidate();
		JP_Menu.repaint();
		new java.util.Timer().schedule( 
		        new java.util.TimerTask() {
		            @Override
		            public void run() {
		            	if(isNotificationShown) {
		            		isNotificationShown = false;
		            		JP_Menu.remove(panel);
			            	JP_Menu.revalidate();
			        		JP_Menu.repaint();
		            	}
		            }
		        }, 3000 ); // �������� �� ����������� ���� 3 ������� (3000 ms)
	}
	// ----- ----- -----
	
	// ����� ���� �� ���������
	void updateTableColumnNames() {
		JT_Table.getColumnModel().getColumn(0).setHeaderValue("�����");
		JT_Table.getColumnModel().getColumn(1).setHeaderValue("���");
		JT_Table.getColumnModel().getColumn(2).setHeaderValue("������������");
		JT_Table.getColumnModel().getColumn(3).setHeaderValue("������@�����");
		JT_Table.getTableHeader().repaint();
	}
	

}//----- ���� COURSESPANEL [  ����  ]