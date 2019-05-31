using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class StartButton : MonoBehaviour
{
    [SerializeField]
    private GameObject _startScreen;

    public void OnClickStart()
    {
        Animator animator = _startScreen.GetComponent<Animator>();

        if(animator != null)
        {
            animator.SetBool("start", true);
        }
    }
}
