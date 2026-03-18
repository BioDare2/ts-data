%function [] = mfourfit(input_data, input_times, lower_tau, upper_tau, dtra, dtrb, intp, sp, sh)
function [] = mfourfit(filename)
%function [ts, amp, per, aic, ph, real_data] = mfourfit(input_data, input_times, lower_tau, upper_tau, dtra, dtrb, intp, sp, sh)

%dll version designed to act on one data set only

%dtra - remove amp trend first
%dtrb - remove baseline trend first
%intp - how to interpolate between points
%sp - show plot
%sh - count shoulders as peaks

dtra=1;
dtrb=1;
intp=0;
sp=0;
sh=0;
lower_tau=15;
upper_tau=35;
input = xlsread(filename,1)
input_times = input(:,1)
input_data = input(:,2)


%[input_times input_data] = textread(filename, '%f\t%f');
%input_times = input_times-11.75;
%input_data = [0.926 0.555 0.783 1 1.892 4.445 1.347 1.12 2.404 1.178 0.88 0.811 0.943]';
%input_data = [0.40548694 0.34801704 0.3984128 0.81816113 3.0114932 4.1569586 4.2040706 2.9818833 2.2338088 1.6651598 1 0.46234557 0.6253312]';

%input_data = [0.926 0.555 0.783 1 1.892 4.445 1.347 1.12 -1.00 -1 0.88 0.811 -1]';
%input_times = [24:2:48]';

%fill in any missing data values by linear interpolation
id = input_data;
it = input_times;
m = find(id == -1);%remove placeholders fo rmissig data
id(m) = [];
it(m) = [];
input_data = interp1(it, id, input_times, 'linear', 'extrap');
input_data(find(input_data < 0)) = 0;

if dtra > 0
    input_data = detrend_amp(input_data, input_times);
end
if dtrb > 0
    input_data = detrend_base(input_data, input_times);
end
real_data = cell(1, size(input_data,1));
for i = 1:size(input_data, 1)
    real_data{i} = input_data(i);
end
for i = 1:size(m, 1)
    real_data{m(i)} = {' '};
end

periods = [lower_tau:0.01:upper_tau];  % RANGE OF FUNDAMENTAL PERIODS OVER
%WHICH TO EVALUATE MODEL
num_components = 11;     % TOTAL NUMBER OF FOURIER SERIES PARAMETERS INCLUDING CONSTANT  
times = input_times;
expt_data = input_data;
num_times = size(times,1);   %NUMBER OF DATA POINTS IN EACH SERIES

[best_components, best_period, best_time_series] = sfourfit1(expt_data,times,num_times,periods,num_components); %IDENTIFIES BEST MODEL FOR EACH SERIES
%re-calculate these params for the best fit
ss = sum((expt_data-best_time_series).^2);%sum of squares for this fit
%add trend if it gives a better fit
temp_time_series = add_trend(times, expt_data, best_time_series);
temp_ss = sum((expt_data-temp_time_series).^2);%sum of squares for this fit
if temp_ss < ss
    ss = temp_ss
    best_time_series = temp_time_series;
end
%Write data in a format Anne can read!
xlswrite(filename, best_time_series, 2, 'A1')

sig = ss/num_times;%error for this fit weighted for num timepoints
lik = -0.5*num_times*log(sig);%likelyhood of this fit being correct
np = sum(abs(best_components)>0);%number of elements that are non-zero
aic = lik;%-np;%don't penalise this likelyhood according to number of significant components
                %so tat Brass won't take this into account when calculating
                %weighted mean  
              
new_times = [times(1):1/6:times(end)];   %point every 10 min
if intp == 0%spline fit best for shoulders
    best_time_series = spline(times, best_time_series, new_times);
elseif intp == 1%polynomial, 
    best_time_series = pchip(times, best_time_series, new_times);
else%linear 
    best_time_series = interp1(times, best_time_series, new_times);
end

if sp > 0
    rd = [];td = [];
    for i = 1:size(real_data,2)
        if strcmp(real_data{i}, ' ') == 0
            rd = [rd; real_data{i}];
            td = [td; times(i)];
        end
    end
%    plot(td, rd,'--ro');hold on;
%    plot(new_times', [best_time_series']);hold off;
end

%find first peak(s)
last = times(1) + best_period;
one_cycle = find(new_times < (last*2));
if one_cycle(end) < size(best_time_series,2)
    one_cycle = best_time_series(1:one_cycle(end)+1);
else
    one_cycle = best_time_series(1:end);
end

peaktimes = [];
%find shoulders from rate of change
dydt = zeros(1,size(one_cycle,2)-1);
for i = 1:size(dydt,2)
   dydt(i) = one_cycle(i+1) - one_cycle(i); 
end

for i = 3:size(dydt,2)-2
    if dydt(i) < 0 & dydt(i-1) >= 0 %- 
      %derrivitve falling through zero means a peak
       %save [time size sharpness]
       psize = one_cycle(i);
       pwidth = abs((dydt(i)-dydt(i-1))/(new_times(i) - new_times(i-1)));%second derrivitive at peak is an estimate       
       %high value = sharp peak
       peaktimes = [peaktimes; new_times(i) psize, pwidth];
       %or use variance as extimate of peak??
    end
    if new_times(i) >= last & ~isempty(peaktimes)
       break;%only go into second day if no peaks in first, possibly due to shortening tau
    end
end
%plot(new_times(1:size(dydt,2))', [dydt'*10 one_cycle(1:size(dydt,2))'], '.');hold on;

%look for shoulders using derrivitive
if sh ~= 0
    if intp ~= 0 %polynomial or linear
        %smooth derrivitive with mean for each period in real dat
        dydt = local_regression(new_times(1:size(dydt,2))', dydt', 1)';
    end

    for i = 2:size(dydt,2)-1
        if (dydt(i) >= 0) & (dydt(i) < dydt(i+1)) & (dydt(i) < dydt(i-1))
            peakfound=1;%upward shoulder
        elseif (dydt(i) <= 0) & (dydt(i) > dydt(i+1)) & (dydt(i) > dydt(i-1))
             peakfound=1;%downward shoulder
        else
             peakfound=0;
        end
        if peakfound > 0
        %    save [time size sharpness]
            psize = one_cycle(i);
            pwidth = abs((dydt(i)-dydt(i-1))/(new_times(i) - new_times(i-1)));%second derrivitive at peak is an estimate       
            %high value = sharp peak
            peaktimes = [peaktimes; new_times(i) psize, pwidth];
            %or use variance as extimate of peak??
        end
        if new_times(i) >= last & ~isempty(peaktimes)
            break;%only go into second day if no peaks in first, possibly due to shortening tau
        end
    end
end
%plot(new_times(1:size(dydt,2))', dydt'*10, 'r');hold off;

one_cycle = find(new_times < last);
if one_cycle(end) < size(best_time_series,2)
    one_cycle = best_time_series(1:one_cycle(end)+1);
else
    one_cycle = best_time_series(1:end);
end
if min(one_cycle) > 0
    amp = max(one_cycle) / min(one_cycle); %fold change
else
    amp = ' ';
end
ts = cell(1, 2 + 2 * size(new_times,2));
for i = 1:size(new_times,2)
   ts{1, i+1} = best_time_series(i); 
   ts{1, size(new_times,2) + 2 + i} = new_times(i);
end
ts{1,1} = 'Theoretical Data';
ts{1, size(new_times,2) + 2} = 'Times';

per = best_period;
if ~isempty(peaktimes)
    peaktimes = sortrows(peaktimes,2);%sort on size
    ph = peaktimes;
    ph = flipud(ph);%row vector
    ph = ph';
else
    ph=' ';
end
if sp > 0
    pause(1);
    delete(gcf);
end
file=fopen(strcat(filename,'.out'),'w');
fprintf(file,'AMPLITUDE,%f\n',amp);
fprintf(file,'PERIOD,%f\n',per);
fprintf(file,'AIC,%f\n',aic);
for i = 1:(size(ph,2))
	fprintf(file,'<<<PPS>>>\n');
	fprintf(file,'PHASE,%f\n',ph(1,i));
	fprintf(file,'PEAKSIZE,%f\n',ph(2,i));
	fprintf(file,'SHARPNESS,%f\n',ph(3,i));
	fprintf(file,'<<<ENDPPS>>>\n');
end
fprintf(file,'<<<THEE>>>\n');
for i = 1:size(new_times,2)
   fprintf(file,'THEE,%f,%f\n',best_time_series(i),new_times(i)); 
end
fprintf(file,'<<<ENDTHEE>>>\n');
fclose(file);
%===============================================================
function [best_components, best_period, best_time_series] = sfourfit1(expt_data,t,num_times,periods,num_components)

nct = ceil(num_components/2);
num_periods = size(periods,2);
sig = zeros(num_periods,1);
sig1 = zeros(num_periods,1);
lik1 = zeros(num_periods,1);
aic1 = zeros(num_periods,1);
sig_components = zeros(num_components,num_periods);
theoretical_fit = zeros(num_times,num_periods);
theoretical_fit1 = zeros(num_times,num_periods);

for per = 1:num_periods
    waves = zeros(num_times,num_components);%collection of sin and cos waves with a particular period
    for co = 1:nct
        for tm = 1:num_times
            waves(tm,co) = cos(2*pi*t(tm)*(co-1)/periods(per));
        end
    end
    for co=nct+1:num_components
        for tm = 1:num_times
            waves(tm,co) = sin(2*pi*t(tm)*(co-nct)/periods(per));
        end
    end 
    
  % waves(:,1) = baseTrend;

   % for co = 2:num_components
   %     waves(:,co) = waves(:,co).*ampTrend;
   % end
    ols_components = zeros(num_components,1);
    ols_var = zeros(num_components,num_components);
  %%add trends here??? or calculate SS after adding them  
    [ols_components stdx mse S] = lscov(waves, expt_data);%stdx not needed
    %V = ones(num_times, num_times);
    %[ols_components stdx] = lscov(waves, expt_data, V);%stdx not needed
    %mse = expt_data'*expt_data./(num_times-num_components);
    %S = inv(waves'*waves)*mse;
    ols_var = S./mse;

    theoretical_fit(:,per) = waves*ols_components;%best fit with that period

   % theoretical_fit(:,per)= AddTrends(t, expt_data, theoretical_fit(:,per));
%plot(theoretical_fit(:,per));hold on;
    ss = sum((expt_data-theoretical_fit(:,per)).^2);%sum of squares for this fit
    sig(per) = ss/num_times;%error for this fit weighted for num timepoints
    bv = diag(sig(per)*ols_var).^0.5;%diagonal of ols variance matrix. each value multiplied by sig and square rooted 

    nb = ols_components./bv; %weight coefficients according to goodness of fit
    isig = (abs(nb) > 2);   %note any component with value <= 2
    sig_components(:,per) = isig.*ols_components;%set to zero any of these
    theoretical_fit1(:,per) = waves*sig_components(:,per);%create improved theoretical fit
 
    %theoretical_fit1(:,per)= AddTrends(t, expt_data, theoretical_fit1(:,per));
% plot(theoretical_fit1(:,per), 'r');
    ss1 = sum((expt_data-theoretical_fit1(:,per)).^2);%sum of squares for this fit
    sig1(per) = ss1/num_times;%error for this fit weighted for num timepoints
    lik1(per) = -0.5*num_times*log(sig1(per));%likelyhood of this fit being correct
    aic1(per) = lik1(per) - sum(isig);%penlise this likelyhood according to number of significant components
end

[mv, best] = max(aic1);%row idx of largst val 
best_period = periods(best);%period with lowest aic for theoretical fit 23##################
best_time_series = theoretical_fit1(:,best);%corresponding time series values
best_components = sig_components(:,best);%cos and sin components of best fit

%=============================================================
function fv = local_regression(times, data_col, bw)

if nargin < 3
    bandwidth=10; % empirical bandwidth
else
    bandwidth = bw;
end
n=length(data_col);     % length of each data series
b=zeros(n,2);
te=zeros(2,1);
ov=ones(n,1);
ones_times=[ov times]; % column of ones added to times column
wcoeff=(1/(2*pi*bandwidth^2))^0.5; % coeff in front of kernel
hv=2*bandwidth^2;
for i=1:n
    w(:,i)=wcoeff*exp(-(times-times(i)).^2/hv); % w(j,:) is the jth set of weights
end
%w a square vector
for i=1:n
    W=diag(w(i,:));
    te=ones_times'*W;        % te*ones_times gives column [S,S_x S_x,S_xx] which is 2 x n : see below
    % te2 = ones_times.*w(i,:);
    b(i,:)=(inv(te*ones_times)*(te*data_col))';
end
fv=b(:,1)+b(:,2).*times;


% the regression uses the fact that the value of a and b that minimises
% chisq=sum_i w_i*(xi - a - b*ti)^2 satisfies aS + bSx = Sy and aSx + bSxx =
% Sxy where S = sum_i w_i, Sx = sum_i w_i*xi, Sy = sum_i w_i*y_i, 
% Sxy = sum_i w_i*x_i*y_i. Here x is times and y is the data.

%=====================================================================
function r = add_trend(times, expt_data, theor_data)

%adds trends in amplitude and base line from real data to theoretical data
baseTrend = local_regression(times, expt_data);%trend in real data
ampTrend=2*local_regression(times, abs(expt_data-baseTrend));%trend in amp

r = theor_data - mean(theor_data);% baseline = 0
r = r./max(r); %amp = 1
r = r.*ampTrend;%add trend in amp
r = r + baseTrend;%add trend to theoretical data
   
%======================================================================
function z = detrend_amp(data_col, times)
%removes trend in amplitude
fv1 = local_regression(times, data_col);%^baseline trend
x=data_col-fv1;%basline trend removed
y=abs(x);
i=sign(x);
R=local_regression(times, y);%trend in amp
mR=mean(R);
z=y./R;%remove trand in amp
z=(z.*i);
z=mR*z+fv1;


%======================================================================
function z = detrend_base(data_col, times)

fv1 = local_regression(times, data_col);%baseline trend
x = data_col-fv1;%basline trend removed  
z = x + mean(fv1);

