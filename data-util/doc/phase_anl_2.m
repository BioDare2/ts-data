% ************************************************************
%                      phase__anl_1
%
% This code extracts phase information from input data
% It assumes the data can be modelled as just a single cosine wave
% and determines the best fit phase, amplitude and offset for a known
% period. 
%
% It uses non-linear least squares with a Gauss Newton algorithm for 
% estimating the phase, amplitude and offset.
% 


% INPUTS:
% times - vector of time values
% data - vector of corresponding data values 
% period - already estimated

input_data = xlsread('testdata.xls',1);
times = input_data(:,1);
data = input_data(:,2);
period = input_data(1,3);

% Constants
% Assume we want to fit phase, amplitude and offset for a single cosine
% So numfit = 3
maxloops = 20;
numparms = 3;

ccvar = 1.d-3; %Limit at which estimate variance has converged


% Initial estimates of phase, amplitude and offset derived from input data

%%%%% Need to think about when sampling interval isn't hourly %%%%%%%
%%%%% Especially for phi %%%%%%%%%%
amp = (max(data) - min(data))/2;
offset = max(data) - amp;
[dummy,phi]=max(data(1:period+1)); 
phi = phi-1;

% First initial estimate of fitted data

answers = zeros(3,1);

answers(1) = offset;
answers(2) = amp;
answers(3) = phi;

fxs = zeros(length(data),1);
res = zeros(length(data),1);

oldssr=0;
idx=0;


for i = 1 : length(data)
            
    ret = answers(1);

    arg=(2*pi*(times(i)+answers(3)))/period;
    if (abs(arg) < 1e18) 
        ret = ret+answers(2)*cos(arg);
    end;
        
    fxs(i)=ret;

end;

z = (fxs-data);
oldssr = dot(z,z);

% c store initial best set of answers
bstssr=oldssr;
bstans(1:numfit) = answers(1:numfit);



for cur1loop = 1 : maxloops + 1, % main convergence loop
 
    % now initialize the matrix equation to all zeros
    JTJ = zeros(numfit,numfit);
    JTR = zeros(numfit,1);
    partials = zeros(3,1);
    
    % now calculate ata and atd
    idx=0;
    for t = 1 : length(data), % loop over each data point
        
        % calculate the difference between data and calculated curve
        dif=(data(t)-fxs(t));
        
        % calculate partial derivatives - 
        % needed for non-linear Least Squares
        
        partials(1,1) = 1;
        partials(2,1) = cos((2*pi/period)*(times(t) + answers(3)));
        partials(3,1) = (-2*pi*answers(2)/period)*sin((2*pi/period)*(times(t) + answers(3)));
            

               
        % Build up matrix equation:
        % We are trying to solve JTJ*e = JTR to find e
        % Where J is Jacobian and R is residual
        
        for i = 1 : numfit,
            JTR(i)=JTR(i)+dif*partials(i,1);
            for j = i : numfit,
                JTJ(i,j)=JTJ(i,j)+partials(i,1)*partials(j,1);
            end;
        end;
    
        % fill in remainder of JTJ matrix 
        % Using the fact that it is symmetric
        for i = 1 : numfit,
            for j = i : numfit,
                JTJ(j,i) = JTJ(i,j);
            end;
        end;
    end;
   
    % Now solve matrix equation by sqrt root method
    % Or in Java use your favourite lscov equivalent
    
    [e,s,t,idx] = symsv(JTJ,JTR,numfit);
    
    
    % update answer array to give improve parameter estimates
    for i = 1 : numfit,
        answers(i) = answers(i) + e(i);
    end;
    
    

    %Calculate ssr using new answers and compare with previous ssr
    
    newssr=0;
       
    for i = 1 : length(data)
            
        ret = answers(1);
 
        arg=(2*pi*(times(i)+answers(3)))/period;
        if (abs(arg) < 1e18) 
            ret = ret+answers(2)*cos(arg);
        end;
        
        fxs(i)=ret;
        z=(ret-data(i));
        newssr=newssr+z*z;
    end;
    
    % if new answers are better then store them 
    
    if(newssr < bstssr)
        bstssr=newssr;
        for i = 1 : numfit,
            bstans(i) = answers(i);
        end;
    end;
    
    zz=abs(newssr/oldssr-1);
    
    % if old and new ssr differ by less than 1 part in 1000 we are done
    % and want to break out of convergence loop
    
    if (zz < ccvar)
        break;
    end
    
    % otherwise, if ssr decreased then go do it all over again
    if(newssr < oldssr)
        oldssr=newssr;
    end;

    ssr(cur1loop) = newssr;
    
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 

% Now update answers to be best obtained from convergence loop

for i = 1 : numfit,
    answers(i) = bstans(i);
end;

% now recalculate the function at those answers

for i = 1 : length(data)

    ret = answers(1);

    arg=(2*pi*(times(i)+answers(3)))/period;
    if (abs(arg) < 1e18) 
        ret = ret+answers(2)*cos(arg);
    end;

    fxs(i)=ret;

end
   

% calculate the residuals

for i = 1 : length(data)
    res(i)=(data(i)-fxs(i));
end;



% Now we need to plot original data and modelled data

figure;
plot(times, data, times, fxs);
hleg1 = legend('input data', 'modelled data');

phase = 2*pi*answers(3)/period

% And output original data and modelled data

xlswrite('testdata.xls', times, 2, 'A1')
xlswrite('testdata.xls', data, 2,'B1')
xlswrite('testdata.xls', fxs, 2, 'C1')


