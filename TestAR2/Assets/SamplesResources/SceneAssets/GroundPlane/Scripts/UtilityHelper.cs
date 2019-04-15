/*==============================================================================
Copyright (c) 2018 PTC Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other
countries.
==============================================================================*/

using UnityEngine;

public static class UtilityHelper
{

    public static void RotateTowardCamera(GameObject augmentation)
    {
        if (Vuforia.VuforiaManager.Instance.ARCameraTransform != null)
        {
            var lookAtPosition = Vuforia.VuforiaManager.Instance.ARCameraTransform.position - augmentation.transform.position;
            lookAtPosition.y = 0;
            var rotation = Quaternion.LookRotation(lookAtPosition);
            augmentation.transform.rotation = rotation;
        }
    }

    public static void EnableRendererColliderCanvas(GameObject gameObject, bool enable)
    {
        var rendererComponents = gameObject.GetComponentsInChildren<Renderer>(true);
        var colliderComponents = gameObject.GetComponentsInChildren<Collider>(true);
        var canvasComponents = gameObject.GetComponentsInChildren<Canvas>(true);

        // Enable rendering:
        foreach (var component in rendererComponents)
            component.enabled = enable;

        // Enable colliders:
        foreach (var component in colliderComponents)
            component.enabled = enable;

        // Enable canvas':
        foreach (var component in canvasComponents)
            component.enabled = enable;
    }

}
