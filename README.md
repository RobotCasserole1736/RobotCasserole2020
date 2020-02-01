![logo](http://robotcasserole.org/wp-content/uploads/2014/12/2014-Team-Logo_RedC40000_smallest.png)

# RobotCasserole2020
Software for Robot Casserole's 2020 FIRST Infinite Recharge competition season

## Contents
1. Driver view
2. Main Source Code
3. Log File Snagger & Log Viewer

## 1. Driver View Website
The Driver View web site is used to help test and tweak our code, and used to help drive team during match. The driver view is a javascript/HTML based viewer of data logs captured from the robot during operation. These data logs are then used to tweak code before, during, and after competition. 

## 2. Main Source Code
The source code this year includes code for: running the intake, controlling hopper to get balls from intake to the conveyor, directing the conveyor to move the power cells where we want them, ramping up shooter wheel speed to score powercells, code to make a west coast drivetrain function as it should, code to auto-align the robot towards the outer goal, as well as code to make our robot look pretty (LEDS). If you care to look at it, look [here](https://github.com/RobotCasserole1736/RobotCasserole2020/tree/master/Robotcode/RobotCode2020/src/main/java/frc/robot).

## 3. Log File Snagger & Log Viewer
The log file snagger is a python script used to  communticate with the roborio and grab all csv logs in a certain directory and put them in a log viewer where we can view them when the robot does somthing wacky.
