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
		
		// ----- Компоненти
		// Съобщение над панела меню
		JLabel JL_Notification;
		// Панел менюта:
		JPanel JP_Menu;
		JButton JB_Menu_Add, JB_Menu_Remove, JB_Menu_Update, JB_Menu_Search, JB_Menu_Refresh;
		// Посредник:
		JButton JB_goToMenu;
		// добавяне:
		JTextField JTF_Add_Name, JTF_Add_Email;
		JButton JB_Add;
		// изтриване:
		JTextField JTF_Delete;
		JButton JB_Delete;
		// обновяване:
		JTextField JTF_Update_Number, JTF_Update_Name, JTF_Update_Email;
		JButton JB_Update;
		// търсене:
		JTextField JTF_Search;
		JButton JB_Search;
		
		// Таблица
		JTable JT_Table = new JTable();
		JScrollPane JSP_Scroller;
		
		// ----- КОНСТРУКТОР [ НАЧАЛО ]
		public LecturersPanel() throws Exception {
			this.setLayout(new BorderLayout());
			
			// Таблица:
			JSP_Scroller = new JScrollPane(JT_Table);
			loadAllLecturersAsc();
			this.add(BorderLayout.CENTER, JSP_Scroller);
			JT_Table.addMouseListener(new java.awt.event.MouseAdapter() { // когато се кликне на ред от таблицата да показва в менюто съответния запис и ако искаме да го обновим
			    @Override
			    public void mouseClicked(java.awt.event.MouseEvent evt) {
			        int row = JT_Table.rowAtPoint(evt.getPoint());
			        int col = JT_Table.columnAtPoint(evt.getPoint());
			        if (row >= 0 && col >= 0) {
			        	JB_Menu_Update.doClick(); // отвараме меню-то за промяна
			        	Object[] obj =  (Object[]) model.data.get(row);
			        	JTF_Update_Number.setText(obj[0].toString()); // Номер на преподавателя
			        	JTF_Update_Name.setText(obj[1].toString()); // Име на преподавателя
			        	JTF_Update_Email.setText(obj[2].toString()); // Поща на преподавателя
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
					loadAllLecturersAsc();
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
				JTF_Add_Name.setUI(new HintTextFieldUI("Lecturer name", true));
				JTF_Add_Email = new JTextField();
				JTF_Add_Email.setUI(new HintTextFieldUI("Lecturer em@il", true));
				JB_Add = new JButton("Добави");
				JB_goToMenu = new JButton("\u25B2");
				
				wrapper.add(JTF_Add_Name);
				wrapper.add(JTF_Add_Email);
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
						final String email = JTF_Add_Email.getText().toString().trim();
						if(name.isEmpty() || email.isEmpty()) {
							showNotification(1, "Всички полета трябва да бъдат попълнени.");
							return;
						}
						if(name.length() > 100) {
							showNotification(1, "Името не може да съдържа повече от 100 символа.");
							return;
						}
						if(email.length() > 100) {
							showNotification(1, "Пощата не може да съдържа повече от 100 символа.");
							return;
						}
						if (!email.matches("^[a-zA-Z0-9]+@[a-zA-Z0-9]+(.[a-zA-Z]{2,})$")) {
							showNotification(1, "Не сте въвели валиден em@il адрес.");
							return;
						}
						
						// Проверка дали преподавател с този email адрес съществува преди да продължим напред.
						conn = MyDB.getConnected();
						String sql = "SELECT L_EMAIL FROM LECTURERS WHERE L_EMAIL=?;";
						try {
							state = conn.prepareStatement(sql);
							state.setString(1, email);
							result = state.executeQuery();
							if(result.isBeforeFirst()) {
								showNotification(1, "Вече съществува преподавател с такъв ema@il адрес.");
								return;
							}
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// Опит за запис на преподавател след като всичко е наред
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
						
						// Ако още не сме ритърнали от някоя от грешките значи всичко е точно:
						showNotification(2, "Успешно добавихте нов преподавател.");
						 // можем да обновим таблицата със записи отзад напред за да видим новият запис най-отгоре
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
				JP_Menu.removeAll(); // чистим панела с меню-то
				
				JPanel wrapper = new JPanel();
				wrapper.setLayout(new GridLayout(1,3)); //1 ред, 3 стълба
				
				JTF_Delete = new JTextField();
				JTF_Delete.setUI(new HintTextFieldUI("Lecturer number", true));
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
						final String lecturer_number_string = JTF_Delete.getText().toString().trim();
						if(lecturer_number_string.isEmpty()) {
							showNotification(1, "Не сте въвели номер на преподавател за изтриване.");
							return;
						}
						int lecturer_number = 0;
						try {
							lecturer_number = Integer.parseInt(lecturer_number_string);
						} catch (Exception exc) {
							showNotification(1, "Не сте въвели номер на преподавател.");
							return;
						}
						if(lecturer_number<2000) {
							showNotification(1, "Номерата на преподавателите започват от 2000.");
							return;
						}
						
						// Проверка дали преподавател с този номер съществува преди да продължим напред.
						conn = MyDB.getConnected();
						String sql = "SELECT L_NUMBER FROM LECTURERS WHERE L_NUMBER=?;";
						try {
							state = conn.prepareStatement(sql);
							state.setInt(1, lecturer_number);
							result = state.executeQuery();
							if(!result.isBeforeFirst()) {
								showNotification(1, "Не съществува преподавател с такъв номер.");
								return;
							}
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// Опит за изтриване на новата дисциплина след като знаем, че съществува
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
						
						// Ако още не сме ритърнали от някоя от грешките значи всичко е точно:
						showNotification(2, "Успешно изтрихте преподавател с номер " + lecturer_number);
						 // можем да обновим таблицата със записи
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
				JP_Menu.removeAll(); // чистим панела с меню-то
				
				JPanel wrapper = new JPanel();
				wrapper.setLayout(new GridLayout(1,4)); //1 ред, 5 стълба

				JTF_Update_Number = new JTextField();
				JTF_Update_Number.setUI(new HintTextFieldUI("Lecturer number", true));
				JTF_Update_Name = new JTextField();
				JTF_Update_Name.setUI(new HintTextFieldUI("Lecturer name", true));
				JTF_Update_Email = new JTextField();
				JTF_Update_Email.setUI(new HintTextFieldUI("Lecturer em@il", true));
				JB_Update = new JButton("Запиши");
				JB_goToMenu = new JButton("\u25B2");
				
				wrapper.add(JTF_Update_Number);
				wrapper.add(JTF_Update_Name);
				wrapper.add(JTF_Update_Email);
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
						final String email = JTF_Update_Email.getText().toString().trim();
						
						if(numb.isEmpty() || name.isEmpty() || email.isEmpty()) {
							showNotification(1, "Всички полета трябва да бъдат попълнени.");
							return;
						}
						if(name.length() > 100) {
							showNotification(1, "Името не може да съдържа повече от 100 символа.");
							return;
						}
						if(email.length() > 100) {
							showNotification(1, "Пощата не може да съдържа повече от 100 символа.");
							return;
						}
						int lecturer_number = 0;
						try {
							lecturer_number = Integer.parseInt(numb);
						} catch(Exception e10) {
							showNotification(1, "Не сте въвели номер на преподавател.");
							return;
						}
						if (!email.matches("^[a-zA-Z0-9]+@[a-zA-Z0-9]+(.[a-zA-Z]{2,})$")) {
							showNotification(1, "Не сте въвели валиден em@il адрес.");
							return;
						}
						
						// Проверка дали преподавател с този номер съществува преди да продължим напред.
						conn = MyDB.getConnected();
						String sql = "SELECT L_NUMBER FROM LECTURERS WHERE L_NUMBER=?;";
						try {
							state = conn.prepareStatement(sql);
							state.setInt(1, lecturer_number);
							result = state.executeQuery();
							if(!result.isBeforeFirst()) {
								showNotification(1, "Не съществува преподавател с такъв номер.");
								return;
							}
						} catch (SQLException exc) {
							exc.printStackTrace();
							return;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						
						// Проверка дали преподавател с този ema@il съществува преди да продължим напред.
						sql = "SELECT L_NUMBER FROM LECTURERS WHERE L_EMAIL=?;";
						try {
							state = conn.prepareStatement(sql);
							state.setString(1, email);
							result = state.executeQuery();
							if(result.isBeforeFirst()) {
								result.next(); // отиваме на първия елемент
								if(Integer.parseInt(result.getObject(1).toString()) != lecturer_number) {
									showNotification(1, "Този em@il вече е зададен на друг преподавател.");
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
						
						// Опит за обновяване на преподавателя след като всичко е наред
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
						
						// Ако още не сме ритърнали от някоя от грешките значи всичко е точно:
						showNotification(2, "Успешно обновихте преподавател с номер " + lecturer_number + " (" + name + ", " + email + ")");
						 // можем да обновим таблицата със записи
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
				JP_Menu.removeAll(); // чистим панела с меню-то
				
				JPanel wrapper = new JPanel();
				wrapper.setLayout(new GridLayout(1,3)); //1 ред, 3 стълба
				
				JTF_Search = new JTextField();
				JTF_Search.setUI(new HintTextFieldUI("Lecturer name", true));
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
						final String lecturer_name = JTF_Search.getText().toString().trim();
						if(lecturer_name.isEmpty()) {
							showNotification(1, "Не сте въвели име на преподавател за търсене.");
							return;
						}
						
						conn = MyDB.getConnected();
						//За долното страхотно query може да благодарим на https://stackoverflow.com/questions/49920450/perform-case-insensitive-string-search-in-h2
						String sql = "SELECT L_NUMBER, L_NAME, L_EMAIL FROM LECTURERS WHERE locate (lower(?), lower(L_NAME)) AND L_ID > 1;";
						try {
							state = conn.prepareStatement(sql);
							state.setString(1, lecturer_name);
							result = state.executeQuery();
							if(!result.isBeforeFirst()) {
								showNotification(1, "Не съществува преподавател с подобно име.");
								return;
							} else {
								model = new MyModel(result);
								JT_Table.setModel(model);
								updateTableColumnNames();
								showNotification(2, "Има резултат от търсенето на \"" + lecturer_name + "\"");
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
			JT_Table.getColumnModel().getColumn(2).setHeaderValue("em@il");
			JT_Table.getTableHeader().repaint();
		}

}//----- КЛАС LECTURERSPANEL [  КРАЙ  ]