clear;
clc;
clf;

global Ts
global Ts_ctrl

% The following are again total guesses, this data is not available anywhere I can find.
Kf = 0.25; % Kinetic rotational coefficent of friction in Nm-s for motor + flywheel
speed_mag_zero_limit = 10; % in rad/sec This and below define pratical deadzones for speed 

SystemMomentOfInertia = 0.26* 0.0762^2; % kg-m^2 - assume flywheel is 5lbs, 3 inch radius
Ts = 0.001; %1 ms simulation rate
Ts_ctrl = 0.02; % controller updates at 20ms rate

CTRL_DECIM_FACTOR = Ts_ctrl/Ts;


end_time = 10.0;
num_samples = int16(end_time / Ts);
time = (0:1:num_samples-1)*Ts;

% Controller Tuning
V_min = 0;
V_max = 13;
setpointRPM = 1500;
kF = 0.005;
kJ = 0.02;
kLoadRatio = 1.0;


% Create a load torque profile from multiple balls injected
BALL_LOAD_NM = 2;
loadTorque = zeros(1, num_samples);
loadTorque(3.0/Ts:3.1/Ts) = BALL_LOAD_NM; 
loadTorque(3.4/Ts:3.5/Ts) = BALL_LOAD_NM; 
loadTorque(3.8/Ts:3.9/Ts) = BALL_LOAD_NM; 
loadTorque(4.2/Ts:4.3/Ts) = BALL_LOAD_NM; 
loadTorque(4.8/Ts:4.9/Ts) = BALL_LOAD_NM; 


% Prealocate other variables
torque = zeros(1,num_samples);
speed = zeros(1,num_samples);
current = zeros(1,num_samples);
voltage = zeros(1,num_samples);

for i = 2:1:num_samples-1
  
    if(mod(i,CTRL_DECIM_FACTOR) == 0)

      % Run control logic update loop. 
      setpoint = setpointRPM/60*2*pi;
      Bang-bang 
     if(setpoint > speed(i-1))
       voltage(i) = V_max;
     else
       voltage(i) = V_min;
     end

      % Cheesy poofs 2017 control law...ummmmm... nope.
      % voltage(i) = kF * setpoint + voltage(i-1) + kJ * Ts_ctrl * (kLoadRatio * setpoint - speed(i-1));

      if(voltage(i) > V_max)
        voltage(i) = V_max;
      end

      if(voltage < V_min)
        voltage(i) = V_min;
      end

    else
      % Hold previous controller value
      voltage(i) = voltage(i-1);
    end
    
  
    # Motor + flywheel Physics
    [current(i),torque(i)] = motor_doubleNeo(voltage(i), torque(i-1), speed(i-1));
    speed(i) = speed(i-1) + Ts*(1/SystemMomentOfInertia * (torque(i)-loadTorque(i)) - Kf*speed(i-1) );

end

subplot(4,1,1);
plot(time, voltage);
title('Voltage (V) vs. Time (s)');
subplot(4,1,2);
plot(time, torque);
title('Torque (Nm) vs. Time (s)');
subplot(4,1,3);
plot(time, speed*60/2/pi);
title('Speed (RPM) vs. Time (s)');
subplot(4,1,4);
plot(time, current);
title('Current (I) vs. Time (s)');


