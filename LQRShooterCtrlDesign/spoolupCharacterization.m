% Extracts shooter spoolup and spooldown events from match data to help inform the plant model of the shooter system

pkg load signal
close all

% Hardcode CSV parse constants based on the log files in raw_data - note this assumes log order did not change between matches. This should have been true for miami valley.
TIME_COL      = 1;
DES_SPD_COL   = 5;
ACT_SPD_COL   = 79;
CURRENT_1_COL = 46;
CURRENT_2_COL = 101;
DC_PCT_COL    = 57;
V_SYS_COL     = 64;
CTRL_MODE_COL = 29;


csvFiles = dir('rawData/*.csv');
numFiles = length(csvFiles);

% Matricies for system parameter over-defined system solution
A = [0,0];
B = [0];

%Need to sample-and-hold robot voltage
vSysPrev = 12.5;


for k=1:numFiles
    filename = strcat(csvFiles(k).folder,'\\',csvFiles(k).name);
    disp(strcat("Parsing ",filename));
    data=csvread(filename);
    timeData   = data(:, TIME_COL     );
    desSpdData = data(:, DES_SPD_COL  );
    actSpdData = filter([0.5,0.5], 1, data(:, ACT_SPD_COL  ));
    m1CurData  = data(:, CURRENT_1_COL);
    m2CurData  = data(:, CURRENT_2_COL);
    dcPctData  = filter([0.5,0.5], 1,data(:, DC_PCT_COL   ));
    vsysData   = data(:, V_SYS_COL    );
    ctlMdData  = data(:, CTRL_MODE_COL);
    actAccelData = filter([0.5,0.5], 1, diff([0;actSpdData])./diff([0;timeData]));

    numTimesteps = length(timeData);
    
    %figure;
    %plot( timeData, actSpdData);
    %return

    segmentStartIdx = 1;
    segmentEndIdx = 1;
    inSegment = false;
    %Look for step-on events
    for cur_samp=2:numTimesteps
        
        if(inSegment)
            segmentLen = cur_samp - segmentStartIdx;
        else
            segmentLen = -1;
        end

        % Do sample & hold logic on battery voltage
        if(vsysData(cur_samp) == 0.0 || vsysData(cur_samp) == NaN)
            vsysData(cur_samp) = vSysPrev;
        else 
            vSysPrev = vsysData(cur_samp);
        end

        if(~inSegment && desSpdData(cur_samp) ~= 0 && desSpdData(cur_samp-1) == 0  && actSpdData(cur_samp) < 100.0)
            segmentStartIdx = cur_samp;
            inSegment = true;
        elseif(inSegment && ctlMdData(cur_samp) ~= 1.0 && ctlMdData(cur_samp-1) == 1.0 && segmentLen > 50 || segmentLen > 175)
            segmentEndIdx = cur_samp;
            inSegment = false;

            filtLen = 10;
            appliedVoltage = dcPctData(segmentStartIdx:segmentEndIdx).*filter(ones(filtLen,1)/filtLen, 1, vsysData(segmentStartIdx:segmentEndIdx));

            if(desSpdData(cur_samp) > 100)
                disp("Found spool-up segment. Length: ");
                disp(segmentLen);
                %we've successfully identified a reasonable spool-up segment of data.
                % figure;
                % plot(
                %     timeData(segmentStartIdx:segmentEndIdx), actSpdData(segmentStartIdx:segmentEndIdx),
                %     timeData(segmentStartIdx:segmentEndIdx), dcPctData(segmentStartIdx:segmentEndIdx),
                %     timeData(segmentStartIdx:segmentEndIdx), appliedVoltage(:)
                %     %timeData(segmentStartIdx:segmentEndIdx), actAccelData(segmentStartIdx:segmentEndIdx)
                % );
                %motor plus mass will follow the form \omega_dot = K1 \omega + K2 \Vin. We solve here for constants K1 and K2, treating each sample as an equation in an over-determined system of linear equations.
                % In the standard Ax = B linear algebra equation, \omega & \Vin form the columns of the A matrix, \omega_dot forms the colum of the B matrix, and K1/K2 are the rows of x.

                
                for seg_samp=segmentStartIdx:segmentEndIdx
                    omega = actSpdData(seg_samp);
                    omega_dot = actAccelData(seg_samp);
                    voltage = appliedVoltage(seg_samp-segmentStartIdx+1);
                    
                    % Add this loop's samples into the system of equations
                    if(~isnan(omega_dot) && voltage > 0.0 && omega > 0.0)
                        A = [A;omega, voltage];
                        B = [B;omega_dot];
                    end
                end
            end
        end
    end

end

% Solve the over-determined system of equations
x = ((transpose(A)*A) .^ -1) * transpose(A) * B;
K1 = -1.0*x(1);
K2 = x(2);
disp(K1)
disp(K2)

