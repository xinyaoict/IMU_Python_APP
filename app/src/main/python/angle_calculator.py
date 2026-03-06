import math

def compute_flexion_extension(euler_hand, euler_wrist):
    # euler_hand 和 euler_wrist 都是 list，不是 ArrayList
    angle_hand = euler_hand[1]  # pitch
    angle_wrist = euler_wrist[1]  # pitch

    angle = angle_wrist - angle_hand

    # 规范化到 [-180, 180]
    angle = (angle + 180) % 360 - 180

    return angle

