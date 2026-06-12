# V380 Animal Guard Pro 38 - Safe Confirmed Alarm Manual

## Защо има нова версия

Фалшива аларма без прасе е неприемлива. Pro37 променя политиката:

- camera-only boar signature вече НЕ пуска камера сирена
- camera-only boar signature отива в Review / notification
- камера сирената се пуска само при потвърдено прасе

## Кога Pro37 пуска камера сирената

Само при едно от тези:

1. ML/label evidence съдържа wild boar / boar / pig / hog / swine.
2. Има силен camera silhouette + отделно independent animal evidence, например animal/mammal/wildlife label.

## Кога НЕ пуска камера сирена

- празен кадър
- plant/soil/tree
- само camera blob без independent animal evidence
- куче
- котка
- лисица
- птица
- човек

## Какво остава включено

- TEST FULL FIELD ALARM пак тества камера сирена + телефон аларма
- TEST SIREN NOW пак тества само камера сирена
- TEST PHONE ALARM пак тества само телефона
- START GUARD пази имота, но в safe mode

## Какво да очакваш

Фалшивите аларми трябва да паднат рязко. Възможно е първоначално да пропусне прасе, ако ML Kit не даде никакво animal/mammal evidence. Това е умишлено, защото приоритетът вече е: първо да спрем фалшивото пищене.

Следващата стъпка за висока точност без пропуски е custom TFLite модел с реални кадри от тази камера.
