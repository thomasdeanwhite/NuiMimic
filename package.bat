cd C:\work\instrumentation
call mvn clean install package
cd C:\work\leapmotion
call mvn clean package -DskipTests=true
move leap\target\leap-0.0.1-SNAPSHOT.jar C:\Apps
cd C:\Apps
del nuimimic.jar
rename leap-0.0.1-SNAPSHOT.jar nuimimic.jar