using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class OpenRestaurantInfo : MonoBehaviour
{
    [SerializeField]
    private GameObject _panel;
    [SerializeField]
    private Text _text;

    public void OnClickOpenButton()
    {
        Animator animator = _panel.GetComponent<Animator>();

        if(animator != null)
        {
            _text.text = TodayMenu.RunParsing();
            animator.SetBool("open", true);
        }

    }
}
