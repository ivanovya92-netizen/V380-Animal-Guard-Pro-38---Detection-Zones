# V380 Animal Guard Pro 38 - Final Readable Field Build Manual

## Какво е поправено

Тази версия поправя UI проблема от screenshot-а:
- няма бял текст върху бели карти
- всички navigation/metric cards са тъмни с бял текст
- всички information cards са тъмни с ясно светъл текст
- primary бутоните са с бял текст
- STOP бутонът е червен
- премахнат е дублираният TEST FULL FIELD ALARM бутон
- премахната е дублираната BoarVision карта

## Какво прави приложението автоматично

При засечено прасе, диво прасе или boar-like unknown night object:

1. Пуска V380 сирената на камерата чрез S01 direct protocol.
2. Пуска full-screen аларма на телефона.
3. Звъни с alarm ringtone.
4. Вибрира.
5. Пуска reminder-и, докато не натиснеш ACK или Snooze.

## Първи тест след install

1. Home -> TEST FULL FIELD ALARM  
   Очаквано: камерата пуска сирена + телефонът алармира.

2. Home -> TEST SIREN NOW  
   Очаквано: само камерата пуска сирена.

3. Home -> TEST PHONE ALARM  
   Очаквано: само телефонът алармира.

4. Backtest -> RUN AI BACKTEST  
   Очаквано: PASS.

5. Guard -> CLOUD TEST ONCE  
   Очаквано: Cloud OK или event ID.

## Реална работа

1. Натисни START GUARD.
2. Остави телефона с интернет.
3. Battery usage: Unrestricted.
4. Notifications: Allow.
5. Alarms and reminders: Allow.
6. Full-screen notifications/lock screen: Allow.

След това при реален V380 event приложението анализира snapshot. Ако CameraBoarVision реши, че е прасе/диво прасе, камерата автоматично плаши животното със сирената.
