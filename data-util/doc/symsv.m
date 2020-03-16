
%   Detailed explanation goes here

%                          SYMSV
%
% 07/06 - Ported by Stephanie Taylor from Fortran code written
% by Marty Straume.
% ************************************************************
%
% function [v,s,t,idx] = symsv(a,f,n)
%
% ************************************************************
%       SUBROUTINE SYMSV(A,V,F,N,idx,S,T)
%  
%  SOLVES THE MATRIX EQUATION A*V=F WHERE A IS SYMMETRIC
%  VIA THE SQUARE ROOT METHOD
function [v,s,t,idx] = symsv(a,f,n)

maxfits=76;

v = zeros(1,maxfits);
s = zeros(maxfits,maxfits);
t = zeros(1,maxfits);
idx = 1;

if (a(1,1) == 0)
    idx = 0;
    return;
end;
if n == 1
    v(1)=f(1)/a(1,1);
    return;
end;
s(1,1) = sqrt(abs(a(1,1)));
for j = 2 : n,
    s(1,j) =  a(1,j)/s(1,1);
end;
for i = 2 : n,
    im1 = i-1;
    for j = i : n
        sum2 = 0;
        for iii = 1 : im1
            sum2 = sum2 + s(iii,i)*s(iii,j);
        end;
        sum = a(i,j) - sum2;
        if j == i
            if sum == 0
                return;
            else
                sum = abs(sum);
                s(i,i) = sqrt(sum);
            end;
        else
            s(i,j) = sum/s(i,i);
        end;
    end;
end;
t(1) = f(1)/s(1,1);
for i = 2 : n,
    sum2=0;
    for iii=1:(i-1)
        sum2=sum2+s(iii,i)*t(iii);
    end;
    t(i)=(f(i)-sum2)/s(i,i);
end;

j = n;
v(j) = t(j)/s(j,j);
while true
    l = j;
    j = j - 1;
    if j == 0
        return;
    end;
    % ...the following summation should be performed in double precision
    sum=0.00;
    for k = 1 : n,
        sum = sum+s(j,k)*v(k);
    end;
    v(j)=(t(j)-sum)/s(j,j);
end;

