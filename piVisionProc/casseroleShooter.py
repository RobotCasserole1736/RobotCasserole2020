import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D



class Shooter():
    def __init__(self):
        self.Gravity = 32.2
        self.BallRadius = 3.5/12
        self.BallArea = np.pi*(self.BallRadius**2)
        self.AirDensity = 1.21
        self.Mass = 0.141748
        self.k = (self.AirDensity*self.BallArea)/(2*self.Mass)
        # self.TARGET1=[-1*(2+(5.25/12)),,(8+(2.25/12))]
        # self.TARGET2=[0,,(8+(2.25/12))]
        self.userParameters()

    def userParameters(self):
        self.Cd = 0
        self.Cl = 0
        self.robotX = 40
        self.robotY = 10
        self.robotZ = 3.5
        self.shotV = 150
        self.pose = 0
        self.robotV = 0
        self.deltaT = 0.0001
        self.total = 2.1

    def shotCalculator(self):
        fig = plt.figure()
        ax = fig.add_subplot(111, projection='3d')
        initAngle = np.radians(22)
        vx = np.cos(initAngle)*self.shotV
        vz = np.sin(initAngle)*self.shotV
        pose=np.arctan2(self.robotZ,self.robotX)
        output = itemthing.AngleChecker(vx, vz,self.robotX,self.robotY,self.robotZ, initAngle,pose)
        ax.plot(output['x'], output['y'], output['z'])
        ax.plot([self.robotX],[self.robotY],[self.robotZ],"r+")
        plt.show()
        #plt.plot(output['x'], output['y'])
        #plt.show()

    def AccelerationX(self, V, Vx, Vz):
        return -1*self.k*V*(self.Cd*Vx+self.Cl*Vz)

    def AccelerationZ(self, V, Vx, Vz):
        return self.k*V*(self.Cl*Vx-self.Cd*Vz)-self.Gravity

    def AngleChecker(self, Vx, Vz, Xoffset, Yoffset, Zoffset, A, Pose):
        xList = []
        yList = []
        zList = []
        X=0
        Y=0
        Z=0
        for time in np.arange(0, self.total, self.deltaT):
            xList.append(X*np.cos(Pose)-Y*np.sin(Pose)+Xoffset)
            yList.append(X*np.sin(Pose)+Y*np.cos(Pose)+Yoffset)
            zList.append(Z+Zoffset)
            V = np.sqrt((Vz**2)+(Vx**2))
            A = np.arctan2(Vz, Vx)
            Vx = np.cos(A)*V
            Vz = np.sin(A)*V
            Ax = self.AccelerationX(V, Vx, Vz)
            Az = self.AccelerationZ(V, Vx, Vz)
            X += Vx*self.deltaT
            Z += Vz*self.deltaT
            Vx += Ax*self.deltaT
            Vz += Az*self.deltaT
        return {'x': xList, 'y': yList, 'z':zList}


if __name__ == "__main__":
    itemthing = Shooter()
    itemthing.shotCalculator()
    

#
# DeltaHeight=-3.5

# deltaA=0.001
# distanceThresh=0.05
# timecutoff=5

# Constants


# mass in kg not sure if this works

# calculated constants


# def AngleRange(X,Y,V):
#     for atest in np.arange(0,np.radians(45),deltaA):
#         if(AngleFinder(X,Y,V,atest)):
#             for atest2 in np.arange(atest,np.radians(45),deltaA):
#                 if(not AngleFinder(X,Y,V,atest2)):
#                     return np.degrees(atest+((atest2-atest)/2))
#     return False

# def AngleFinder(X,Y,V,A):
#     angle=A
#     check=True
#     Time=0
#     while(check):
#         Vx=np.cos(angle)*V
#         Vy=np.sin(angle)*V
#         Ax=AccelerationX(V,Vx,Vy)
#         Ay=AccelerationY(V,Vx,Vy)
#         X+=Vx*deltaT
#         Y+=Vy*deltaT
#         Vx+=Ax*deltaT
#         Vy+=Ay*deltaT
#         angle=np.arctan2(Vy,Vx)
#         V=np.sqrt((Vy**2)+(Vx**2))
#         Time+=deltaT
#         if(X>Dis-distanceThresh and X<Dis+distanceThresh):
#             if(Y>DeltaHeight-distanceThresh and Y<DeltaHeight+distanceThresh):
#                 return True
#             else:
#                 return False
#         if(Time>timecutoff):
#             return False

# for x in np.arange(0,54,1/12):
#     for y in np.arange(0,27,1/12):
#         AngleRange(-x,-y,InitVelocity)
