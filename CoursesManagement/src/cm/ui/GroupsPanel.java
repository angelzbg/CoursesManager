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

public class GroupsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
		// DataBase
		Connection conn = null;
		PreparedStatement state = null;
		ResultSet result = null;
		MyModel model = null;
		
		// ----- Компоненти
		// Съобщение над панела меню
		JLabel JL_Notification;
		// Панел менюта:
		JPanel JP_Menu;
		JButton JB_Menu_Add, JB_Menu_Remove, JB_Menu_Update, JB_Menu_Search, JB_Menu_Refresh;
		// Посредник:
		JButton JB_goToMenu;
		// добавяне:
		JTextField JTF_Add_Name, JTF_Add_CourseNumber;
		JButton JB_Add;
		// изтриване:
		JTextField JTF_Delete;
		JButton JB_Delete;
		// обновяване:
		JTextField JTF_Update_Number, JTF_Update_Name, JTF_Update_CourseNumber;
		JButton JB_Update;
		// търсене:
		JTextField JTF_Search;
		JButton JB_Search;
		
		// Таблица
		JTable JT_Table = new JTable();
		JScrollPane JSP_Scroller;
		
		// ----- КОНСТРУКТОР [ НАЧАЛО ]
		public GroupsPanel() throws Exception {
			this.setLayout(new BorderLayout());
			
			// Таблица:
			JSP_Scroller = new JScrollPane(JT_Table);
			loadAllGroupsAsc();
			this.add(BorderLayout.CENTER, JSP_Scroller);
			JT_Table.addMouseListener(new java.awt.event.MouseAdapter() { // когато се кликне на ред от таблицата да показва в менюто съответния запис и ако искаме да го обновим
			    @Override
			    public void mouseClicked(java.awt.event.MouseEvent evt) {
			        int row = JT_Table.rowAtPoint(evt.getPoint());
			        int col = JT_Table.columnAtPoint(evt.getPoint());
			        if (row >= 0 && col >= 0) {
			        	JB_Menu_Update.doClick(); // отвараме меню-то за промяна
			        	Object[] obj =  (Object[]) model.data.get(row);
			        	JTF_Update_Number.setText(obj[0].toString()); // Номер на групата
			        	JTF_Update_Name.setText(obj[1].toString()); // Име на групата
			        	JTF_Update_CourseNumber.setText(obj[2].toString()); // Номер на дисципилината към която е групата
			        }
			    }
			});
			
			// Зареждане на менюто
			JP_Menu = new JPanel();
			JP_Menu.setLayout(new BorderLayout());
			this.add(BorderLayout.PAGE_START, JP_Menu); // горе
			loadMenu();
			
		}
		// ----- КОНСТРУКТОР [  КРАЙ  ]
		
		// ----- ActionListeners
		void loadMenu() {
			JP_Menu.removeAll();
			
			JPanel buttonsWrapper = new JPanel();
			buttonsWrapper.setLayout(new GridLayout(1,5)); //1 ред, 5 стълба
			
			JB_Menu_Add = new JButton("Добави");
			JB_Menu_Remove = new JButton("Изтрий");
			JB_Menu_Update = new JButton("Промени");
			JB_Menu_Search = new JButton("Търси");
			JB_Menu_Refresh = new JButton("Обнови");
			
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
					loadAllGroupsAsc();
				} catch (Exception e1) {
					e1.printStackTrace();
				}	
			}
		}//end ActionRefresh
		
		class ActionSwitchToAdd implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				JP_Menu.removeAll(); // чистим панела с меню-то
				
				JPanel wrapper = new JPanel();
				wrapper.setLayout(new GridLayout(1,4)); //1 ред, 4 стълба
				
				//JTF_Add_Name = new HintTextField("Course name");
				//JTF_Add_LectNum = new HintTextField("Lecturer number");
				JTF_Add_Name = new JTextField();
				JTF_Add_Name.setUI(new HintTextFieldUI("Group name", true));
				JTF_Add_CourseNumber = new JTextField();
				JTF_Add_CourseNumber.setUI(new HintTextFieldUI("Course number", true));
				JB_Add = new JButton("Добави");
				JB_goToMenu = new JButton("\u25B2");
				
				wrapper.add(JTF_Add_Name);
				wrapper.add(JTF_Add_CourseNumber);
				wrapper.add(JB_Add);
				wrapper.add(JB_goToMenu);
				
				JP_Menu.add(BorderLayout.CENTER, wrapper); // добавяме новото меню в панела
				// Обновяваме за всеки случай
				JP_Menu.revalidate();
				JP_Menu.repaint();
				
				JB_goToMenu.addActionListener(new ActionListener() { // бутон за връщане обратно в меню-то
					@Override
					public void actionPerformed(ActionEvent e) {
						loadMenu();
					}
				});
				
				JB_Add.addActionListener(new ActionListener() { // Клик в/у ботуна "Добави" -> Проверки -> добавяне към датабазата
					@Override
					public void actionPerformed(ActionEvent e) {
						final String name = JTF_Add_Name.getText().toString().trim();
						final String course = JTF_Add_CourseNumber.getText().toString().trim();
						if(name.isEmpty() || course.isEmpty()) {
							showNotification(1, "Всички полета трябва да бъдат попълнени.");
							return;
						}
						if(name.length() > 100) {
							showNotification(1, "Името не може да съдържа повече от 100 символа.");
							return;
						}
						int course_number = 0;
						try {
							course_number = Integer.parseInt(course);
						} catch (Exception exc) {
							showNotification(1, "Не сте въвели номер на дисциплина.");
							return;
						}
						
						// Проверка дали дисциплина с този номер съществува преди да продължим напред.
						conn = MyDB.getConnected();
						String sql = "SELECT C_NUMBER FROM COURSES WHERE C_NUMBER=?;";
						try {
							state = conn.prepareStatement(sql);
							state.setInt(1, course_number);
							result = state.executeQuery();
							if(!result.isBeforeFirst()) {
								showNotification(1, "Не съществува дисциплина с такъв номер.");
								return;
							}
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// Опит за запис на новата група след като всичко е наред
						sql = "INSERT INTO GROUPS VALUES(NULL, NULL, ?, ?);";
						try {
							state = conn.prepareStatement(sql);
							state.setString(1, name);
							state.setInt(2, course_number);
							state.execute();
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// Ако още не сме ритърнали от някоя от грешките значи всичко е точно:
						showNotification(2, "Успешно създадохте нова група.");
						 // можем да обновим таблицата със записи отзад напред за да видим новият запис най-отгоре
						try {
							loadAllGroupsDesc();
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
				JP_Menu.removeAll(); // чистим панела с меню-то
				
				JPanel wrapper = new JPanel();
				wrapper.setLayout(new GridLayout(1,3)); //1 ред, 3 стълба
				
				JTF_Delete = new JTextField();
				JTF_Delete.setUI(new HintTextFieldUI("Group number", true));
				JB_Delete = new JButton("Изтрий");
				JB_goToMenu = new JButton("\u25B2");
				
				wrapper.add(JTF_Delete);
				wrapper.add(JB_Delete);
				wrapper.add(JB_goToMenu);
				
				JP_Menu.add(BorderLayout.CENTER, wrapper); // добавяме новото меню в панела
				// Обновяваме за всеки случай
				JP_Menu.revalidate();
				JP_Menu.repaint();
				
				JB_goToMenu.addActionListener(new ActionListener() { // бутон за връщане обратно в меню-то
					@Override
					public void actionPerformed(ActionEvent e) {
						loadMenu();
					}
				});
				
				JB_Delete.addActionListener(new ActionListener() { // Клик в/у ботуна "Изтрий" -> Проверки -> опит за изтриване
					@Override
					public void actionPerformed(ActionEvent e) {
						final String group_number_string = JTF_Delete.getText().toString().trim();
						if(group_number_string.isEmpty()) {
							showNotification(1, "Не сте въвели номер на група за изтриване.");
							return;
						}
						int group_number = 0;
						try {
							group_number = Integer.parseInt(group_number_string);
						} catch (Exception exc) {
							showNotification(1, "Не сте въвели номер на група.");
							return;
						}
						if(group_number<32000) {
							showNotification(1, "Номерата на групите започват от 3000.");
							return;
						}
						
						// Проверка дали дисциплина с този номер съществува преди да продължим напред.
						conn = MyDB.getConnected();
						String sql = "SELECT G_NUMBER FROM GROUPS WHERE G_NUMBER=?;";
						try {
							state = conn.prepareStatement(sql);
							state.setInt(1, group_number);
							result = state.executeQuery();
							if(!result.isBeforeFirst()) {
								showNotification(1, "Не съществува група с такъв номер.");
								return;
							}
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// Опит за изтриване на групата след като знаем, че съществува
						sql = "DELETE FROM GROUPS WHERE G_NUMBER=?;";
						try {
							state = conn.prepareStatement(sql);
							state.setInt(1, group_number);
							state.execute();
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// Ако още не сме ритърнали от някоя от грешките значи всичко е точно:
						showNotification(2, "Успешно изтрихте група с номер " + group_number);
						 // можем да обновим таблицата със записи
						try {
							loadAllGroupsAsc();
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
				JP_Menu.removeAll(); // чистим панела с меню-то
				
				JPanel wrapper = new JPanel();
				wrapper.setLayout(new GridLayout(1,4)); //1 ред, 5 стълба

				JTF_Update_Number = new JTextField();
				JTF_Update_Number.setUI(new HintTextFieldUI("Group number", true));
				JTF_Update_Name = new JTextField();
				JTF_Update_Name.setUI(new HintTextFieldUI("Group name", true));
				JTF_Update_CourseNumber = new JTextField();
				JTF_Update_CourseNumber.setUI(new HintTextFieldUI("Course number", true));
				JB_Update = new JButton("Запиши");
				JB_goToMenu = new JButton("\u25B2");
				
				wrapper.add(JTF_Update_Number);
				wrapper.add(JTF_Update_Name);
				wrapper.add(JTF_Update_CourseNumber);
				wrapper.add(JB_Update);
				wrapper.add(JB_goToMenu);
				
				JP_Menu.add(BorderLayout.CENTER, wrapper); // добавяме новото меню в панела
				// Обновяваме за всеки случай
				JP_Menu.revalidate();
				JP_Menu.repaint();
				
				JB_goToMenu.addActionListener(new ActionListener() { // бутон за връщане обратно в меню-то
					@Override
					public void actionPerformed(ActionEvent e) {
						loadMenu();
					}
				});
				
				JB_Update.addActionListener(new ActionListener() { // Клик в/у ботуна "Запиши" -> Проверки -> ъпдейт датабазата
					@Override
					public void actionPerformed(ActionEvent e) {
						final String numb = JTF_Update_Number.getText();
						final String name = JTF_Update_Name.getText().toString().trim();
						final String course = JTF_Update_CourseNumber.getText();
						
						if(numb.isEmpty() || name.isEmpty() || course.isEmpty()) {
							showNotification(1, "Всички полета трябва да бъдат попълнени.");
							return;
						}
						if(name.length() > 100) {
							showNotification(1, "Името не може да съдържа повече от 100 символа.");
							return;
						}
						int group_number = 0;
						try {
							group_number = Integer.parseInt(numb);
						} catch (Exception exc) {
							showNotification(1, "Не сте въвели номер на група.");
							return;
						}
						int course_number = 0;
						try {
							course_number = Integer.parseInt(course);
						} catch (Exception exc) {
							showNotification(1, "Не сте въвели номер на дисциплина.");
							return;
						}
						
						// Проверка дали група с този номер съществува преди да продължим напред.
						String sql = "SELECT G_NUMBER FROM GROUPS WHERE G_NUMBER=?;";
						try {
							state = conn.prepareStatement(sql);
							state.setInt(1, group_number);
							result = state.executeQuery();
							if(!result.isBeforeFirst()) {
								showNotification(1, "Не съществува група с такъв номер.");
								return;
							}
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// Проверка дали дисциплина с този номер съществува преди да продължим напред.
						conn = MyDB.getConnected();
						sql = "SELECT C_NUMBER FROM COURSES WHERE C_NUMBER=?;";
						try {
							state = conn.prepareStatement(sql);
							state.setInt(1, course_number);
							result = state.executeQuery();
							if(!result.isBeforeFirst()) {
								showNotification(1, "Не съществува дисциплина с такъв номер.");
								return;
							}
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// Опит за обновяване на дисциплината след като всичко е наред
						sql = "UPDATE GROUPS SET G_NAME=?, C_NUMBER=? WHERE G_NUMBER=?;";
						try {
							state = conn.prepareStatement(sql);
							state.setString(1, name);
							state.setInt(2, course_number);
							state.setInt(3, group_number);
							state.execute();
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// Ако още не сме ритърнали от някоя от грешките значи всичко е точно:
						showNotification(2, "Успешно обновихте група с номер " + group_number + " (" + name + ")");
						 // можем да обновим таблицата със записи
						try {
							loadAllGroupsAsc();
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
				JP_Menu.removeAll(); // чистим панела с меню-то
				
				JPanel wrapper = new JPanel();
				wrapper.setLayout(new GridLayout(1,3)); //1 ред, 3 стълба
				
				JTF_Search = new JTextField();
				JTF_Search.setUI(new HintTextFieldUI("Group name", true));
				JB_Search = new JButton("Търси");
				JB_goToMenu = new JButton("\u25B2");
				
				wrapper.add(JTF_Search);
				wrapper.add(JB_Search);
				wrapper.add(JB_goToMenu);
				
				JP_Menu.add(BorderLayout.CENTER, wrapper); // добавяме новото меню в панела
				// Обновяваме за всеки случай
				JP_Menu.revalidate();
				JP_Menu.repaint();
				
				JB_goToMenu.addActionListener(new ActionListener() { // бутон за връщане обратно в меню-то
					@Override
					public void actionPerformed(ActionEvent e) {
						loadMenu();
					}
				});
				
				JB_Search.addActionListener(new ActionListener() { // Клик в/у ботуна "Търси" -> опит за търсене
					@Override
					public void actionPerformed(ActionEvent e) {
						final String group_name = JTF_Search.getText().toString().trim();
						if(group_name.isEmpty()) {
							showNotification(1, "Не сте въвели име на група за търсене.");
							return;
						}
						
						conn = MyDB.getConnected();
						//За долното страхотно query може да благодарим на https://stackoverflow.com/questions/49920450/perform-case-insensitive-string-search-in-h2
						String sql = "SELECT G_NUMBER, G_NAME, G.C_NUMBER, L.L_NAME, L.L_EMAIL FROM GROUPS G JOIN COURSES C ON G.C_NUMBER = C.C_NUMBER JOIN LECTURERS L ON L.L_NUMBER = C.L_NUMBER WHERE locate (lower(?), lower(G.G_NAME)) AND G_ID > 1;";
						try {
							state = conn.prepareStatement(sql);
							state.setString(1, group_name);
							result = state.executeQuery();
							if(!result.isBeforeFirst()) {
								showNotification(1, "Не съществува група с подобно име.");
								return;
							} else {
								model = new MyModel(result);
								JT_Table.setModel(model);
								updateTableColumnNames();
								showNotification(2, "Има резултат от търсенето на \"" + group_name + "\"");
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
		
		// ----- Датабаза [ НАЧАЛО ]
		void loadAllGroupsAsc() throws Exception {
			conn = MyDB.getConnected();
			String sql = "SELECT G_NUMBER, G_NAME, G.C_NUMBER, L.L_NAME, L.L_EMAIL FROM GROUPS G JOIN COURSES C ON G.C_NUMBER = C.C_NUMBER JOIN LECTURERS L ON L.L_NUMBER = C.L_NUMBER WHERE G_ID > 1 ORDER BY G_NUMBER;";
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
		
		void loadAllGroupsDesc() throws Exception {
			conn = MyDB.getConnected();
			String sql = "SELECT G_NUMBER, G_NAME, G.C_NUMBER, L.L_NAME, L.L_EMAIL FROM GROUPS G JOIN COURSES C ON G.C_NUMBER = C.C_NUMBER JOIN LECTURERS L ON L.L_NUMBER = C.L_NUMBER WHERE G_ID > 1 ORDER BY G_NUMBER DESC;";
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
		// ----- Датабаза [  КРАЙ  ]
		
		// ----- Notificaion
		boolean isNotificationShown = false;
		void showNotification(int type, String message) { // 1 = грешка, 2 = успех
			isNotificationShown = true;
			JL_Notification = new JLabel(message, SwingConstants.CENTER);
			JL_Notification.setForeground(Color.decode("#f2f5f7")); // бежав цвят на шрифта
			final JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.add(BorderLayout.CENTER, JL_Notification);
			switch(type) {
				case 1:
					panel.setBackground(Color.decode("#c63d3d")); // червен бекграунд
					break;
				case 2:
					panel.setBackground(Color.decode("#4899db")); // син бекграунд
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
			        }, 3000 ); // скриване на съобщението след 3 секунди (3000 ms)
		}
		// ----- ----- -----
		
		// Малък фикс за таблицата
		void updateTableColumnNames() {
			JT_Table.getColumnModel().getColumn(0).setHeaderValue("Номер");
			JT_Table.getColumnModel().getColumn(1).setHeaderValue("Име");
			JT_Table.getColumnModel().getColumn(2).setHeaderValue("Номер на дисциплина");
			JT_Table.getColumnModel().getColumn(3).setHeaderValue("Преподавател");
			JT_Table.getColumnModel().getColumn(4).setHeaderValue("Препод@вател");
			JT_Table.getTableHeader().repaint();
		}

}//----- КЛАС GROUPSPANEL [  КРАЙ  ]