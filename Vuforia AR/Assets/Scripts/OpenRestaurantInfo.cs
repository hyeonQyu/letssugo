﻿using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class OpenRestaurantInfo : MonoBehaviour
{
    [SerializeField]
    private GameObject _panel;

    public void OnClickOpenButton()
    {
        Animator animator = _panel.GetComponent<Animator>();

        if(animator != null)
        {
            animator.SetBool("open", true);
        }

    }
}