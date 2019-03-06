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

public class LecturersPanel extends JPanel {
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
		JTextField JTF_Add_Name, JTF_Add_Email;
		JButton JB_Add;
		// ���������:
		JTextField JTF_Delete;
		JButton JB_Delete;
		// ����������:
		JTextField JTF_Update_Number, JTF_Update_Name, JTF_Update_Email;
		JButton JB_Update;
		// �������:
		JTextField JTF_Search;
		JButton JB_Search;
		
		// �������
		JTable JT_Table = new JTable();
		JScrollPane JSP_Scroller;
		
		// ----- ����������� [ ������ ]
		public LecturersPanel() throws Exception {
			this.setLayout(new BorderLayout());
			
			// �������:
			JSP_Scroller = new JScrollPane(JT_Table);
			loadAllLecturersAsc();
			this.add(BorderLayout.CENTER, JSP_Scroller);
			JT_Table.addMouseListener(new java.awt.event.MouseAdapter() { // ������ �� ������ �� ��� �� ��������� �� ������� � ������ ���������� ����� � ��� ������ �� �� �������
			    @Override
			    public void mouseClicked(java.awt.event.MouseEvent evt) {
			        int row = JT_Table.rowAtPoint(evt.getPoint());
			        int col = JT_Table.columnAtPoint(evt.getPoint());
			        if (row >= 0 && col >= 0) {
			        	JB_Menu_Update.doClick(); // �������� ����-�� �� �������
			        	Object[] obj =  (Object[]) model.data.get(row);
			        	JTF_Update_Number.setText(obj[0].toString()); // ����� �� �������������
			        	JTF_Update_Name.setText(obj[1].toString()); // ��� �� �������������
			        	JTF_Update_Email.setText(obj[2].toString()); // ���� �� �������������
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
					loadAllLecturersAsc();
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
				JTF_Add_Name.setUI(new HintTextFieldUI("Lecturer name", true));
				JTF_Add_Email = new JTextField();
				JTF_Add_Email.setUI(new HintTextFieldUI("Lecturer em@il", true));
				JB_Add = new JButton("������");
				JB_goToMenu = new JButton("\u25B2");
				
				wrapper.add(JTF_Add_Name);
				wrapper.add(JTF_Add_Email);
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
						final String email = JTF_Add_Email.getText().toString().trim();
						if(name.isEmpty() || email.isEmpty()) {
							showNotification(1, "������ ������ ������ �� ����� ���������.");
							return;
						}
						if(name.length() > 100) {
							showNotification(1, "����� �� ���� �� ������� ������ �� 100 �������.");
							return;
						}
						if(email.length() > 100) {
							showNotification(1, "������ �� ���� �� ������� ������ �� 100 �������.");
							return;
						}
						if (!email.matches("^[a-zA-Z0-9]+@[a-zA-Z0-9]+(.[a-zA-Z]{2,})$")) {
							showNotification(1, "�� ��� ������ ������� em@il �����.");
							return;
						}
						
						// �������� ���� ������������ � ���� email ����� ���������� ����� �� ��������� ������.
						conn = MyDB.getConnected();
						String sql = "SELECT L_EMAIL FROM LECTURERS WHERE L_EMAIL=?;";
						try {
							state = conn.prepareStatement(sql);
							state.setString(1, email);
							result = state.executeQuery();
							if(result.isBeforeFirst()) {
								showNotification(1, "���� ���������� ������������ � ����� ema@il �����.");
								return;
							}
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// ���� �� ����� �� ������������ ���� ���� ������ � �����
						sql = "INSERT INTO LECTURERS VALUES(NULL, NULL, ?, ?);";
						try {
							state = conn.prepareStatement(sql);
							state.setString(1, name);
							state.setString(2, email);
							state.execute();
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// ��� ��� �� ��� ��������� �� ����� �� �������� ����� ������ � �����:
						showNotification(2, "������� ��������� ��� ������������.");
						 // ����� �� ������� ��������� ��� ������ ����� ������ �� �� ����� ������ ����� ���-������
						try {
							loadAllLecturersDesc();
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
				JTF_Delete.setUI(new HintTextFieldUI("Lecturer number", true));
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
						final String lecturer_number_string = JTF_Delete.getText().toString().trim();
						if(lecturer_number_string.isEmpty()) {
							showNotification(1, "�� ��� ������ ����� �� ������������ �� ���������.");
							return;
						}
						int lecturer_number = 0;
						try {
							lecturer_number = Integer.parseInt(lecturer_number_string);
						} catch (Exception exc) {
							showNotification(1, "�� ��� ������ ����� �� ������������.");
							return;
						}
						if(lecturer_number<2000) {
							showNotification(1, "�������� �� ��������������� �������� �� 2000.");
							return;
						}
						
						// �������� ���� ������������ � ���� ����� ���������� ����� �� ��������� ������.
						conn = MyDB.getConnected();
						String sql = "SELECT L_NUMBER FROM LECTURERS WHERE L_NUMBER=?;";
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
						
						// ���� �� ��������� �� ������ ���������� ���� ���� �����, �� ����������
						sql = "DELETE FROM LECTURERS WHERE L_NUMBER=?;";
						try {
							state = conn.prepareStatement(sql);
							state.setInt(1, lecturer_number);
							state.execute();
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// ��� ��� �� ��� ��������� �� ����� �� �������� ����� ������ � �����:
						showNotification(2, "������� �������� ������������ � ����� " + lecturer_number);
						 // ����� �� ������� ��������� ��� ������
						try {
							loadAllLecturersAsc();
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
				JTF_Update_Number.setUI(new HintTextFieldUI("Lecturer number", true));
				JTF_Update_Name = new JTextField();
				JTF_Update_Name.setUI(new HintTextFieldUI("Lecturer name", true));
				JTF_Update_Email = new JTextField();
				JTF_Update_Email.setUI(new HintTextFieldUI("Lecturer em@il", true));
				JB_Update = new JButton("������");
				JB_goToMenu = new JButton("\u25B2");
				
				wrapper.add(JTF_Update_Number);
				wrapper.add(JTF_Update_Name);
				wrapper.add(JTF_Update_Email);
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
						final String email = JTF_Update_Email.getText().toString().trim();
						
						if(numb.isEmpty() || name.isEmpty() || email.isEmpty()) {
							showNotification(1, "������ ������ ������ �� ����� ���������.");
							return;
						}
						if(name.length() > 100) {
							showNotification(1, "����� �� ���� �� ������� ������ �� 100 �������.");
							return;
						}
						if(email.length() > 100) {
							showNotification(1, "������ �� ���� �� ������� ������ �� 100 �������.");
							return;
						}
						int lecturer_number = 0;
						try {
							lecturer_number = Integer.parseInt(numb);
						} catch(Exception e10) {
							showNotification(1, "�� ��� ������ ����� �� ������������.");
							return;
						}
						if (!email.matches("^[a-zA-Z0-9]+@[a-zA-Z0-9]+(.[a-zA-Z]{2,})$")) {
							showNotification(1, "�� ��� ������ ������� em@il �����.");
							return;
						}
						
						// �������� ���� ������������ � ���� ����� ���������� ����� �� ��������� ������.
						conn = MyDB.getConnected();
						String sql = "SELECT L_NUMBER FROM LECTURERS WHERE L_NUMBER=?;";
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
						
						// �������� ���� ������������ � ���� ema@il ���������� ����� �� ��������� ������.
						sql = "SELECT L_NUMBER FROM LECTURERS WHERE L_EMAIL=?;";
						try {
							state = conn.prepareStatement(sql);
							state.setString(1, email);
							result = state.executeQuery();
							if(result.isBeforeFirst()) {
								result.next(); // ������� �� ������ �������
								if(Integer.parseInt(result.getObject(1).toString()) != lecturer_number) {
									showNotification(1, "���� em@il ���� � ������� �� ���� ������������.");
									return;
								}
							}
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// ���� �� ���������� �� ������������� ���� ���� ������ � �����
						sql = "UPDATE LECTURERS SET L_NAME=?, L_EMAIL=? WHERE L_NUMBER=?;";
						try {
							state = conn.prepareStatement(sql);
							state.setString(1, name);
							state.setString(2, email);
							state.setInt(3, lecturer_number);
							state.execute();
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// ��� ��� �� ��� ��������� �� ����� �� �������� ����� ������ � �����:
						showNotification(2, "������� ��������� ������������ � ����� " + lecturer_number + " (" + name + ", " + email + ")");
						 // ����� �� ������� ��������� ��� ������
						try {
							loadAllLecturersAsc();
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
				JTF_Search.setUI(new HintTextFieldUI("Lecturer name", true));
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
						final String lecturer_name = JTF_Search.getText().toString().trim();
						if(lecturer_name.isEmpty()) {
							showNotification(1, "�� ��� ������ ��� �� ������������ �� �������.");
							return;
						}
						
						conn = MyDB.getConnected();
						//�� ������� ��������� query ���� �� ���������� �� https://stackoverflow.com/questions/49920450/perform-case-insensitive-string-search-in-h2
						String sql = "SELECT L_NUMBER, L_NAME, L_EMAIL FROM LECTURERS WHERE locate (lower(?), lower(L_NAME)) AND L_ID > 1;";
						try {
							state = conn.prepareStatement(sql);
							state.setString(1, lecturer_name);
							result = state.executeQuery();
							if(!result.isBeforeFirst()) {
								showNotification(1, "�� ���������� ������������ � ������� ���.");
								return;
							} else {
								model = new MyModel(result);
								JT_Table.setModel(model);
								updateTableColumnNames();
								showNotification(2, "��� �������� �� ��������� �� \"" + lecturer_name + "\"");
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
		void loadAllLecturersAsc() throws Exception {
			conn = MyDB.getConnected();
			String sql = "SELECT L_NUMBER, L_NAME, L_EMAIL FROM LECTURERS WHERE L_NUMBER > 1 ORDER BY L_NUMBER;";
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
		
		void loadAllLecturersDesc() throws Exception {
			conn = MyDB.getConnected();
			String sql = "SELECT L_NUMBER, L_NAME, L_EMAIL FROM LECTURERS WHERE L_NUMBER > 1 ORDER BY L_NUMBER DESC;";
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
			JT_Table.getColumnModel().getColumn(2).setHeaderValue("em@il");
			JT_Table.getTableHeader().repaint();
		}

}//----- ���� LECTURERSPANEL [  ����  ]