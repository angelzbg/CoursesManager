# CoursesManager
Приложението представлява управление на дисциплини. Съдържа преподаватели, групи и дисциплини. Всяка дисциплина има псобствен преподавател и всяка група има собствена дициплина.<br>
Има три таблици - LECTURERS, COURSES и GROUPS.<br>
Преподавателите имат L_ID (id), L_NUMBER (уникален номер, започващ от 2000 -> за да не показваме ID-тата, по задание не трябвало), L_NAME (име до 100 символа, може да съдържа символи от кирилицата и разни UTF8), L_EMAIL (до 100 символа + валидация в програмата дали е валиден за това нямаме валидация в h2)<br>
Дисциплините имат C_ID (id), C_NUMBER (уникален номер, започващ от 1000), C_NAME (име до 100 символа...), L_NUMBER (външен ключ, сочещ към преподавателски номер от таблицата LECTURERS, при изтриването на преподавател, към който сочим от тази таблица, автоматично се задава ключ със стойност 1, а в таблицата LECTURERS на номер 1 стои преподавател с данни ИЗТРИТ ИЗТРИТ)<br>
Групите имат G_ID (id), G_NUMBER (уникален номер, започващ от 3000), G_NAME (име до 100 символа...), C_NUMBER (външен ключ, сочещ към номер на дисциплина от таблицата COURSES, при изтриването на дисциплина, към който сочим от тази таблица, автоматично се задава ключ със стойност 1, а в таблицата COURSES на номер 1 стои дисциплина с данни ИЗТРИТА ИЗТРИТА)<br>
За да се подсигурим, че при едни такива изтривания на данни няма да си прецакаме логиката, ще направим и проверка дали при изтриване сме задали номер на дисциплина >= 1000 и номер на преподавател >= 2000, за групите няма смисъл, тъй като от тях не зависи нищо, тях можем да ги трием и без да губим информация, но все пак има проверка.<br><br>

За да осигурим тези номера в датабазата, ние пушваме изкуствено номерата до там със следните заявки:(+номерата 1 за изтритите)<br>
INSERT INTO LECTURERS VALUES(NULL, NULL, 'ИЗТРИТ', 'ИЗТРИТ');<br>
INSERT INTO LECTURERS VALUES(NULL, 1999, 'PUSH', 'PUSH');<br>
DELETE FROM LECTURERS WHERE L_NUMBER=1999; -> тук даже трием, понеже тези данни стават излишни<br>
-> Следващият добавен елемент ще бъде с автоматично генериран номер 2000, а айдитата се генерират автоамтично както си следва, най-вероятно ще бъде с айди 3 и т.н.<br><br>

INSERT INTO COURSES VALUES(NULL, NULL, 'ИЗТРИТА', 1);<br>
INSERT INTO COURSES VALUES(NULL, 999, 'PUSH', 1);<br>
DELETE FROM COURSES WHERE C_NUMBER=999;<br>
Следващият добавен елемент ще бъде с автоматично генериран номер 1000, 1001, 1002 и т.н.<br><br>

INSERT INTO GROUPS VALUES(NULL, NULL, 'ИЗТРИТА', 1);<br>
INSERT INTO GROUPS VALUES(NULL, 2999, 'PUSH', 1);<br>
DELETE FROM GROUPS WHERE G_NUMBER=2999;<br>
Следващият добавен елемент ще бъде с автоматично генериран номер 3000 -//-<br><br>
