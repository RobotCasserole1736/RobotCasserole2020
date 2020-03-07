% Run a sim of the system behavior
% Assume K1/K2 have been set from system characterization

Ts = 0.02;
sim_end_time = 10.0;
onVoltage = 12.0;

timeVec = 0:Ts:sim_end_time;
speedVec = zeros(1,length(timeVec));

for idx = 2:length(timeVec)

    omega_dot = (K1 * speedVec(idx-1) + onVoltage * K2);
    speedVec(idx) = speedVec(idx - 1) + omega_dot*Ts;
end

figure;
plot(timeVec, speedVec);