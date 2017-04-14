#!/usr/bin/env python


import rospy
from geometry_msgs.msg import Twist
from geometry_msgs.msg import TwistStamped
from std_msgs.msg import Header
import time

def velocityBroadcast():
       
    rospy.init_node('position_broadcast',anonymous=True)       
    pub = rospy.Publisher('/drone1/navigator/velocity_setpoint',TwistStamped,queue_size=1)
    sequence = 0
    
    while(True):
        header = Header()
        header.seq = sequence
        sequence = sequence + 1
        header.stamp = rospy.Time.now()

        twist = Twist()
        twist.linear.x = 1
        twist.linear.y = 0
        twist.linear.z = 0
        twist.angular.x = 0
        twist.angular.y = 0
        twist.angular.z = 0

        pub.publish(header,twist)

        time.sleep(0.1)        



if __name__ == '__main__':
    velocityBroadcast()

