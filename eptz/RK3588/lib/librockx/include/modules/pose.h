/****************************************************************************
*
*    Copyright (c) 2018 - 2021 by Rockchip Corp.  All rights reserved.
*
*    The material in this file is confidential and contains trade secrets
*    of Rockchip Corporation. This is proprietary information owned by
*    Rockchip Corporation. No part of this work may be disclosed,
*    reproduced, copied, transmitted, or used in any way for any purpose,
*    without the express written permission of Rockchip Corporation.
*
*****************************************************************************/

#ifndef _ROCKX_POSE_H
#define _ROCKX_POSE_H

#include <vector>
#include "../rockx_type.h"
#include "./face.h"

#ifdef __cplusplus
extern "C" {
#endif

extern rockx_module_t ROCKX_MODULE_POSE_BODY;                   ///< Body Pose(14 KeyPoints)
extern rockx_module_t ROCKX_MODULE_POSE_BODY_V2;                ///< Body Pose(17 KeyPoints)
extern rockx_module_t ROCKX_MODULE_POSE_FINGER_3;               ///< Finger Landmark(3 KeyPoint)
extern rockx_module_t ROCKX_MODULE_POSE_FINGER_21;              ///< Finger Landmark(21 KeyPoint)

/**
 * @brief Pose of Body KeyPoints Name
 * 
 * 	"Nose", "Neck",
 *	"R-Sho", "R-Elb", "R-Wr",
 *	"L-Sho", "L-Elb", "L-Wr",
 *	"R-Hip", "R-Knee", "R-Ank",
 *	"L-Hip", "L-Knee", "L-Ank",
 *	"R-Eye", "L-Eye", "R-Ear", "L-Ear"
 */
extern const char * const ROCKX_POSE_BODY_KEYPOINTS_NAME[];

/**
 * @brief Hand Landmark Result (get from @ref rockx_hand_landmark)
 */
typedef struct rockx_hand_landmark_t {
    rockx_rectf_center_t hand_box;  ///< Hand region
    int landmarks_count;            ///< Landmark points count
    rockx_pointf_t landmarks[128];   ///< Landmark points
    float score;                    ///< Hand score
	float handedness;
} rockx_hand_landmark_t;

typedef struct rockx_keypointsf_t {
    int count;
	rockx_rect_t box;
    rockx_pointf_t points[32];
} rockx_keypointsf_t;

/**
 * @brief KeyPoints for One Body or Finger
 */
typedef struct rockx_keypoints_t {
    int id;			///< key points track id
    int count;                  ///< key points count
    rockx_point_t points[32];   ///< key points
    float score[32];		///< Key points score
	rockx_rect_t box;
	float box_score;
} rockx_keypoints_t;

/**
 * @brief KeyPoints Array
 */
typedef struct rockx_keypoints_array_t {
	int count;							///< Array size
	rockx_keypoints_t keypoints[32];	///< Array of rockx_keypoints_t
} rockx_keypoints_array_t;

/**
 * Get KeyPoint of Human Body (Multi Person)
 * @param handle [in] Handle of a created ROCKX_MODULE_POSE_BODY module(created by @ref rockx_create)
 * @param in_img [in] Input image
 * @param keypoints_array [out] Array of pose key points
 * @param callback [in] Async callback function pointer
 * @return @ref rockx_ret_t
 */
rockx_ret_t rockx_pose_body(rockx_handle_t handle, rockx_image_t *in_img, rockx_keypoints_array_t *keypoints_array,
		rockx_async_callback *callback);

/**
 * Get KeyPoint of A Human Hand
 *
 * Finger 21 KeyPoint As Show in Figure 1.
 * @image html res/finger_landmark21.jpg Figure 1 Finger 21 KeyPoints Detection
 *
 * @param handle [in] Handle of a created ROCKX_MODULE_POSE_FINGER_3 or ROCKX_MODULE_POSE_FINGER_21 module(created by @ref rockx_create)
 * @param in_img [in] Input image
 * @param keypoints [out] KeyPoints
 * @return @ref rockx_ret_t
 */
rockx_ret_t rockx_pose_finger(rockx_handle_t handle, rockx_image_t *in_img, rockx_keypoints_t *keypoints);

#ifdef __cplusplus
} //extern "C"
#endif

#endif // _ROCKX_POSE_H
